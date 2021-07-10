package reposense.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import reposense.git.GitBranch;
import reposense.git.exception.GitBranchException;
import reposense.system.LogsManager;
import reposense.util.FileUtil;

/**
 * Represents configuration information from CSV config file for a single repository.
 */
public class RepoConfiguration {
    public static final String DEFAULT_BRANCH = "HEAD";
    private static final Logger logger = LogsManager.getLogger(RepoConfiguration.class);

    private RepoLocation location;
    private String branch;
    private String displayName;
    private String outputFolderName;
    private transient String zoneId;
    private transient Date sinceDate;
    private transient Date untilDate;
    private transient String repoFolderName;

    private transient FileTypeManager fileTypeManager;
    private transient List<String> ignoreGlobList = new ArrayList<>();
    private transient List<String> ignoredAuthorsList = new ArrayList<>();
    private transient AuthorConfiguration authorConfig;
    private transient boolean isStandaloneConfigIgnored;
    private transient List<CommitHash> ignoreCommitList;
    private transient boolean isLastModifiedDateIncluded;
    private transient boolean isShallowCloningPerformed;
    private transient boolean isFormatsOverriding;
    private transient boolean isIgnoreGlobListOverriding;
    private transient boolean isIgnoreCommitListOverriding;
    private transient boolean isIgnoredAuthorsListOverriding = false;

    public RepoConfiguration(RepoLocation location) {
        this(location, DEFAULT_BRANCH);
    }

    public RepoConfiguration(RepoLocation location, String branch) {
        this(location, branch, Collections.emptyList(), Collections.emptyList(), false, Collections.emptyList(),
                false, false, false, false);
    }

    public RepoConfiguration(RepoLocation location, String branch, List<FileType> formats, List<String> ignoreGlobList,
            boolean isStandaloneConfigIgnored, List<CommitHash> ignoreCommitList, boolean isFormatsOverriding,
            boolean isIgnoreGlobListOverriding, boolean isIgnoreCommitListOverriding,
            boolean isShallowCloningPerformed) {
        this.authorConfig = new AuthorConfiguration(location, branch);
        this.location = location;
        this.branch = location.isEmpty() ? DEFAULT_BRANCH : branch;
        this.ignoreGlobList = ignoreGlobList;
        this.isStandaloneConfigIgnored = isStandaloneConfigIgnored;
        this.fileTypeManager = new FileTypeManager(formats);
        this.ignoreCommitList = ignoreCommitList;
        this.isFormatsOverriding = isFormatsOverriding;
        this.isIgnoreGlobListOverriding = isIgnoreGlobListOverriding;
        this.isIgnoreCommitListOverriding = isIgnoreCommitListOverriding;
        this.isShallowCloningPerformed = isShallowCloningPerformed;

        String organization = location.getOrganization();
        String repoName = location.getRepoName();
        displayName = repoName + "[" + branch + "]";
        outputFolderName = repoName + "_" + branch;
        repoFolderName = repoName;

        if (organization != null) {
            repoFolderName = organization + "_" + repoFolderName;
            displayName = organization + "/" + displayName;
            outputFolderName = organization + "_" + outputFolderName;
        }
    }

    public static void setDatesToRepoConfigs(List<RepoConfiguration> configs, Date sinceDate, Date untilDate) {
        for (RepoConfiguration config : configs) {
            config.setSinceDate(sinceDate);
            config.setUntilDate(untilDate);
        }
    }

    public static void setZoneIdToRepoConfigs(List<RepoConfiguration> configs, String zoneId) {
        for (RepoConfiguration config : configs) {
            config.setZoneId(zoneId);
        }
    }

    public static void setIsLastModifiedDateIncludedToRepoConfigs(List<RepoConfiguration> configs,
                                                                  boolean isLastModifiedDateIncluded) {
        for (RepoConfiguration config : configs) {
            config.setIsLastModifiedDateIncluded(isLastModifiedDateIncluded);
        }
    }

    public static void setIsShallowCloningPerformedToRepoConfigs(List<RepoConfiguration> configs,
                                                                 boolean isShallowCloningPerformed) {
        if (isShallowCloningPerformed) {
            configs.stream().forEach(config -> config.setIsShallowCloningPerformed(true));
        }
    }

    /**
     * Merges a {@code RepoConfiguration} from {@code repoConfigs} with an {@code AuthorConfiguration} from
     * {@code authorConfigs} if their {@code RepoLocation} and branch matches
     */
    public static void merge(List<RepoConfiguration> repoConfigs, List<AuthorConfiguration> authorConfigs) {
        for (AuthorConfiguration authorConfig : authorConfigs) {
            if (authorConfig.getLocation().isEmpty()) {
                for (RepoConfiguration repoConfig : repoConfigs) {
                    repoConfig.addAuthors(authorConfig.getAuthorList());
                }
                continue;
            }

            List<RepoConfiguration> locationMatchingRepoConfigs =
                    getMatchingRepoConfigsByLocation(repoConfigs, authorConfig.getLocation());

            if (locationMatchingRepoConfigs.isEmpty()) {
                logger.warning(String.format(
                        "Repository %s is not found in repo-config.csv.",
                        authorConfig.getLocation()));
                continue;
            }
            if (authorConfig.isDefaultBranch()) {
                locationMatchingRepoConfigs.forEach(matchingRepoConfig -> {
                    matchingRepoConfig.addAuthors(authorConfig.getAuthorList());
                });
                continue;
            }

            RepoConfiguration branchMatchingRepoConfig = getMatchingRepoConfig(repoConfigs, authorConfig);

            if (branchMatchingRepoConfig == null) {
                if (!authorConfig.isDefaultBranch()) {
                    logger.warning(String.format(
                            "Repository %s (branch %s) is not found in repo-config.csv.",
                            authorConfig.getLocation(), authorConfig.getBranch()));
                }
                continue;
            }

            branchMatchingRepoConfig.addAuthors(authorConfig.getAuthorList());
        }
    }

    /**
     * Sets the list of groups in {@code groupConfigs} to the respective {@code repoConfigs}.
     */
    public static void setGroupConfigsToRepos(List<RepoConfiguration> repoConfigs,
            List<GroupConfiguration> groupConfigs) {
        for (GroupConfiguration groupConfig : groupConfigs) {
            List<RepoConfiguration> matchingRepoConfigs;
            if (groupConfig.getLocation().isEmpty()) {
                matchingRepoConfigs = repoConfigs;
            } else {
                matchingRepoConfigs = getMatchingRepoConfigsByLocation(repoConfigs,
                        groupConfig.getLocation());
            }
            if (matchingRepoConfigs.isEmpty()) {
                logger.warning(String.format(
                        "Repository %s is not found in repo-config.csv.", groupConfig.getLocation()));
                continue;
            }
            matchingRepoConfigs.forEach(matchingRepoConfig -> {
                matchingRepoConfig.addGroups(groupConfig.getGroupsList());
            });
        }
    }

    /**
     * Iterates through {@code repoConfigs} to find a {@code RepoConfiguration} with {@code RepoLocation} and branch
     * that matches {@code authorConfig}. Returns {@code null} if no match is found.
     */
    private static RepoConfiguration getMatchingRepoConfig(
            List<RepoConfiguration> repoConfigs, AuthorConfiguration authorConfig) {
        if (authorConfig.isDefaultBranch()) {
            return null;
        }
        for (RepoConfiguration repoConfig : repoConfigs) {
            if (repoConfig.getLocation().equals(authorConfig.getLocation())
                    && repoConfig.getBranch().equals(authorConfig.getBranch())) {
                return repoConfig;
            }
        }
        return null;
    }

    /**
     * Returns a list of {@link RepoConfiguration} where the {@link RepoLocation} matches {@code targetRepoLocation}.
     */
    private static List<RepoConfiguration> getMatchingRepoConfigsByLocation(
            List<RepoConfiguration> configs, RepoLocation targetRepoLocation) {
        return configs.stream().filter(config -> config.getLocation().equals(targetRepoLocation))
                .collect(Collectors.toList());
    }

    /**
     * Sets {@code formats} to {@code RepoConfiguration} in {@code configs} if its format list is empty.
     */
    public static void setFormatsToRepoConfigs(List<RepoConfiguration> configs, List<FileType> formats) {
        for (RepoConfiguration config : configs) {
            if (!config.fileTypeManager.hasSpecifiedFormats()) {
                config.fileTypeManager.setFormats(formats);
            }
        }
    }

    /**
     * Sets each {@code RepoConfiguration} in {@code configs} to ignore its standalone config, if
     * {@code ignoreAllStandaloneConfigs} is true.
     */
    public static void setStandaloneConfigIgnoredToRepoConfigs(
            List<RepoConfiguration> configs, boolean ignoreAllStandaloneConfigs) {
        if (ignoreAllStandaloneConfigs) {
            configs.forEach(config -> config.setStandaloneConfigIgnored(true));
        }
    }

    /**
     * Clears existing information related to this repository and its authors, and replaces it with information from the
     * {@code standaloneConfig}.
     */
    public void update(StandaloneConfig standaloneConfig) {
        // only assign the new values when all the fields in {@code standaloneConfig} pass the validations.
        List<FileType> replacementFileTypes = FileType.convertFormatStringsToFileTypes(standaloneConfig.getFormats());
        CommitHash.validateCommits(standaloneConfig.getIgnoreCommitList());

        if (!isIgnoreGlobListOverriding) {
            ignoreGlobList = standaloneConfig.getIgnoreGlobList();
        }
        if (!isFormatsOverriding) {
            fileTypeManager.setFormats(replacementFileTypes);
        }
        if (!isIgnoreCommitListOverriding) {
            ignoreCommitList = CommitHash.convertStringsToCommits(standaloneConfig.getIgnoreCommitList());
        }
        if (!isIgnoredAuthorsListOverriding) {
            ignoredAuthorsList = standaloneConfig.getIgnoreAuthorList();
        }
        authorConfig.update(standaloneConfig, ignoreGlobList);
    }

    /**
     * Attempts to find matching {@code Author} given a name and an email.
     * If no matching {@code Author} is found, {@code Author#UNKNOWN_AUTHOR} is returned.
     */
    public Author getAuthor(String name, String email) {
        return authorConfig.getAuthor(name, email);
    }

    /**
     * Updates branch with {@code currentBranch} if default branch is specified.
     */
    public void updateBranch(String currentBranch) {
        if (branch.equals(DEFAULT_BRANCH)) {
            setBranch(currentBranch);
        }
    }

    /**
     * Gets the current branch and updates branch with current branch if default branch is specified.
     */
    public void updateBranch() throws GitBranchException {
        if (branch.equals(DEFAULT_BRANCH)) {
            String currentBranch = GitBranch.getCurrentBranch(getRepoRoot());
            setBranch(currentBranch);
        }
    }

    /**
     * Gets the path to the root folder of the repository.
     */
    public String getRepoRoot() {
        String path = FileUtil.REPOS_ADDRESS + File.separator + getRepoFolderName() + File.separator;

        if (!getRepoName().isEmpty()) {
            path += getRepoName() + File.separator;
        }

        return path;
    }

    /**
     * Gets the name of the folder containing the cloned repository; the parent directory of the repo's root folder.
     */
    public String getRepoFolderName() {
        return repoFolderName;
    }

    @Override
    public boolean equals(Object other) {

        if (other == this) {
            return true;
        }

        if (!(other instanceof RepoConfiguration)) {
            return false;
        }

        RepoConfiguration otherRepoConfig = (RepoConfiguration) other;

        return location.equals(otherRepoConfig.location)
                && branch.equals(otherRepoConfig.branch)
                && authorConfig.equals(otherRepoConfig.authorConfig)
                && ignoreGlobList.equals(otherRepoConfig.ignoreGlobList)
                && ignoredAuthorsList.equals(otherRepoConfig.ignoredAuthorsList)
                && isStandaloneConfigIgnored == otherRepoConfig.isStandaloneConfigIgnored
                && fileTypeManager.equals(otherRepoConfig.fileTypeManager)
                && isLastModifiedDateIncluded == otherRepoConfig.isLastModifiedDateIncluded
                && isFormatsOverriding == otherRepoConfig.isFormatsOverriding
                && isShallowCloningPerformed == otherRepoConfig.isShallowCloningPerformed
                && isIgnoreGlobListOverriding == otherRepoConfig.isIgnoreGlobListOverriding
                && isIgnoreCommitListOverriding == otherRepoConfig.isIgnoreCommitListOverriding
                && isIgnoredAuthorsListOverriding == otherRepoConfig.isIgnoredAuthorsListOverriding;
    }

    public Map<Author, String> getAuthorDisplayNameMap() {
        return authorConfig.getAuthorDisplayNameMap();
    }

    public void setAuthorDisplayNameMap(Map<Author, String> authorDisplayNameMap) {
        authorConfig.setAuthorDisplayNameMap(authorDisplayNameMap);
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        updateDisplayName(branch);
        updateOutputFolderName(branch);
        this.branch = branch;
        authorConfig.setBranch(branch);
    }

    /**
     * Updates the branch in the {@code displayName} to the
     * current {@code branch}.
     */
    public void updateDisplayName(String branch) {
        this.displayName = displayName.substring(0, displayName.lastIndexOf('[') + 1) + branch + "]";
    }

    /**
     * Updates the branch in the {@code outputFolderName} to the
     * current {@code branch}.
     */
    public void updateOutputFolderName(String branch) {
        this.outputFolderName = outputFolderName.substring(0, outputFolderName.lastIndexOf('_') + 1) + branch;
    }

    public List<String> getIgnoreGlobList() {
        return ignoreGlobList;
    }

    public void setIgnoreGlobList(List<String> ignoreGlobList) {
        this.ignoreGlobList = ignoreGlobList;
    }

    public List<CommitHash> getIgnoreCommitList() {
        return ignoreCommitList;
    }

    public void setIgnoreCommitList(List<CommitHash> ignoreCommitList) {
        this.ignoreCommitList = ignoreCommitList;
    }

    public List<String> getIgnoredAuthorsList() {
        return this.ignoredAuthorsList;
    }

    public void setIgnoredAuthorsList(List<String> ignoredAuthorsList) {
        this.ignoredAuthorsList = ignoredAuthorsList;
    }

    public boolean isIgnoredAuthorsListOverriding() {
        return this.isIgnoredAuthorsListOverriding;
    }

    public void setIsLastModifiedDateIncluded(boolean lastModifiedDateIncluded) {
        this.isLastModifiedDateIncluded = lastModifiedDateIncluded;
    }

    public void setIsShallowCloningPerformed(boolean isShallowCloningPerformed) {
        this.isShallowCloningPerformed = isShallowCloningPerformed;
    }

    public boolean isLastModifiedDateIncluded() {
        return this.isLastModifiedDateIncluded;
    }

    public boolean isShallowCloningPerformed() {
        return this.isShallowCloningPerformed;
    }

    public void setIsIgnoredAuthorsListOverriding(boolean isIgnoredAuthorsListOverriding) {
        this.isIgnoredAuthorsListOverriding = isIgnoredAuthorsListOverriding;
    }

    public void removeIgnoredAuthors() {
        authorConfig.removeIgnoredAuthors(ignoredAuthorsList);
    }

    public List<Author> getAuthorList() {
        return authorConfig.getAuthorList();
    }

    public void addAuthor(Author author) {
        authorConfig.addAuthor(author, this.getIgnoreGlobList());
    }

    public void addAuthors(List<Author> authorList) {
        authorConfig.addAuthors(authorList, this.getIgnoreGlobList());
    }

    public void setAuthorConfiguration(AuthorConfiguration authorConfig) {
        this.authorConfig = authorConfig;
        for (Author author : authorConfig.getAuthorList()) {
            AuthorConfiguration.propagateIgnoreGlobList(author, ignoreGlobList);
        }
    }

    public boolean containsAuthor(Author author) {
        return authorConfig.containsAuthor(author);
    }

    /**
     * Clears authors information and sets the {@code authorList} to {@code RepoConfiguration}.
     */
    public void setAuthorList(List<Author> authorList) {
        authorConfig.clear();
        authorConfig.setAuthorList(authorList);
        authorConfig.buildFromAuthorList();
        authorList.forEach(author -> AuthorConfiguration.propagateIgnoreGlobList(author, this.getIgnoreGlobList()));
    }

    public Map<String, Author> getAuthorDetailsToAuthorMap() {
        return authorConfig.getAuthorDetailsToAuthorMap();
    }

    public void setAuthorDetailsToAuthorMap(Map<String, Author> authorDetailsToAuthorMap) {
        authorConfig.setAuthorDetailsToAuthorMap(authorDetailsToAuthorMap);
    }

    public void setFormats(List<FileType> formats) {
        fileTypeManager.setFormats(formats);
    }

    private void setGroups(List<FileType> groups) {
        fileTypeManager.setGroups(groups);
    }

    public void addGroups(List<FileType> groups) {
        fileTypeManager.addGroups(groups);
    }

    /**
     * Returns all format or group types (depending on whether the user has specified a custom grouping).
     */
    public List<FileType> getAllFileTypes() {
        return fileTypeManager.getAllFileTypes();
    }

    public FileType getFileType(String fileName) {
        return fileTypeManager.getFileType(fileName);
    }

    public FileTypeManager getFileTypeManager() {
        return fileTypeManager;
    }

    public Date getSinceDate() {
        return sinceDate;
    }

    public void setSinceDate(Date sinceDate) {
        this.sinceDate = sinceDate;
    }

    public Date getUntilDate() {
        return untilDate;
    }

    public void setUntilDate(Date untilDate) {
        this.untilDate = untilDate;
    }

    public String getZoneId() {
        return zoneId;
    }

    public void setZoneId(String zoneId) {
        this.zoneId = zoneId;
    }

    public void setAuthorDisplayName(Author author, String displayName) {
        authorConfig.setAuthorDisplayName(author, displayName);
    }

    public void addAuthorDetailsToAuthorMapEntry(Author author, List<String> values) {
        authorConfig.addAuthorDetailsToAuthorMapEntry(author, values);
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getRepoName() {
        return location.getRepoName();
    }

    public String getOutputFolderName() {
        return outputFolderName;
    }

    public void setStandaloneConfigIgnored(boolean isStandaloneConfigIgnored) {
        this.isStandaloneConfigIgnored = isStandaloneConfigIgnored;
    }

    public RepoLocation getLocation() {
        return location;
    }

    public String getOrganization() {
        return location.getOrganization();
    }

    public boolean isStandaloneConfigIgnored() {
        return isStandaloneConfigIgnored;
    }

    public boolean isFormatsOverriding() {
        return isFormatsOverriding;
    }

    public boolean isIgnoreGlobListOverriding() {
        return isIgnoreGlobListOverriding;
    }

    public boolean isIgnoreCommitListOverriding() {
        return isIgnoreCommitListOverriding;
    }
}
