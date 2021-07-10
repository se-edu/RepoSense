package reposense.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import reposense.system.LogsManager;

/**
 * Represents author configuration information from CSV config file for a single repository.
 */
public class AuthorConfiguration {
    public static final String DEFAULT_BRANCH = "HEAD";
    private static final Logger logger = LogsManager.getLogger(AuthorConfiguration.class);
    private static final Pattern EMAIL_PLUS_OPERATOR_PATTERN =
            Pattern.compile("^(?<prefix>.+)\\+(?<suffix>.*)(?<domain>@.+)$");
    private static final boolean DEFAULT_HAS_AUTHOR_CONFIG_FILE = false;

    private static boolean hasAuthorConfigFile = DEFAULT_HAS_AUTHOR_CONFIG_FILE;

    private RepoLocation location;
    private String branch;

    private transient List<Author> authorList = new ArrayList<>();
    private transient Map<String, Author> authorDetailsToAuthorMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    private transient Map<Author, String> authorDisplayNameMap = new HashMap<>();

    public AuthorConfiguration(RepoLocation location) {
        this(location, DEFAULT_BRANCH);
    }

    public AuthorConfiguration(RepoLocation location, String branch) {
        this.location = location;
        this.branch = location.isEmpty() ? DEFAULT_BRANCH : branch;
    }

    /**
     * Clears authors information and use the information provided from {@code standaloneConfig}.
     */
    public void update(StandaloneConfig standaloneConfig, List<String> ignoreGlobList) {
        List<Author> newAuthorList = new ArrayList<>();
        Map<String, Author> newAuthorDetailsToAuthorMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        Map<Author, String> newAuthorDisplayNameMap = new HashMap<>();

        for (StandaloneAuthor sa : standaloneConfig.getAuthors()) {
            Author author = new Author(sa);
            author.importIgnoreGlobList(ignoreGlobList);

            newAuthorList.add(author);
            newAuthorDisplayNameMap.put(author, author.getDisplayName());
            List<String> aliases = new ArrayList<>(author.getAuthorAliases());
            List<String> emails = new ArrayList<>(author.getEmails());
            aliases.add(author.getGitId());
            aliases.forEach(alias -> {
                checkDuplicateAliases(newAuthorDetailsToAuthorMap, alias, author.getGitId());
                newAuthorDetailsToAuthorMap.put(alias, author);
            });
            emails.forEach(email -> newAuthorDetailsToAuthorMap.put(email, author));
        }

        setAuthorList(newAuthorList);
        setAuthorDetailsToAuthorMap(newAuthorDetailsToAuthorMap);
        setAuthorDisplayNameMap(newAuthorDisplayNameMap);
    }

    /**
     * Checks for duplicate aliases in {@code authorDetailsToAuthorMap} and generates warnings
     * @param authorDetailsToAuthorMap
     * @param alias
     */
    public void checkDuplicateAliases(Map<String, Author> authorDetailsToAuthorMap, String alias, String gitId) {
        if (authorDetailsToAuthorMap.containsKey(alias)) {
            logger.warning(String.format(
                    "Duplicate alias %s found. The alias will belong to the last author - %s", alias, gitId));
        }
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof AuthorConfiguration)) {
            return false;
        }

        AuthorConfiguration otherAuthorConfig = (AuthorConfiguration) other;

        return location.equals(otherAuthorConfig.location)
                && branch.equals(otherAuthorConfig.branch)
                && authorList.equals(otherAuthorConfig.authorList)
                && authorDetailsToAuthorMap.equals(otherAuthorConfig.authorDetailsToAuthorMap)
                && authorDisplayNameMap.equals(otherAuthorConfig.authorDisplayNameMap);
    }

    public Map<Author, String> getAuthorDisplayNameMap() {
        return authorDisplayNameMap;
    }

    public void setAuthorDisplayNameMap(Map<Author, String> authorDisplayNameMap) {
        this.authorDisplayNameMap = authorDisplayNameMap;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public List<Author> getAuthorList() {
        return authorList;
    }

    /**
     * Sets the details of {@code author} to {@code AuthorConfiguration} including the default alias, aliases
     * and display name.
     */
    private void setAuthorDetails(Author author) {
        // Set GitHub Id and its corresponding email as default
        addAuthorDetailsToAuthorMapEntry(author, Arrays.asList(author.getGitId()));

        addAuthorDetailsToAuthorMapEntry(author, author.getAuthorAliases());
        addAuthorDetailsToAuthorMapEntry(author, author.getEmails());

        setAuthorDisplayName(author, author.getDisplayName());
    }

    /**
     * Propagates {@code ignoreGlobList} to {@code author}.
     */
    public static void propagateIgnoreGlobList(Author author, List<String> ignoreGlobList) {
        author.importIgnoreGlobList(ignoreGlobList);
    }

    /**
     * Adds author to the {@code AuthorList}.
     */
    public void addAuthor(Author author) {
        authorList.add(author);
        setAuthorDetails(author);
    }

    /**
     * Adds {@code author} to the {@code AuthorList}, and propagates {@code ignoreGlobList} to the {@code author}.
     */
    public void addAuthor(Author author, List<String> ignoreGlobList) {
        addAuthor(author);
        propagateIgnoreGlobList(author, ignoreGlobList);
    }

    /**
     * Removes the authors provided in {@code ignoredAuthorsList} from the author list.
     */
    public void removeIgnoredAuthors(List<String> ignoredAuthorsList) {
        for (String author : ignoredAuthorsList) {
            if (authorDetailsToAuthorMap.containsKey(author)) {
                removeAuthorInformation(author);
            }
        }
    }

    /**
     * Removes all information of the {@code author} from the configs
     * Precondition: {@code author} must be present in {@code authorDetailsToAuthorMap}
     * @param author Can be an author's git ID, email, or alias
     */
    public void removeAuthorInformation(String author) {
        Author authorToRemove = authorDetailsToAuthorMap.get(author);
        authorList.remove(authorToRemove);
        authorDisplayNameMap.remove(authorToRemove);
        authorDetailsToAuthorMap.remove(authorToRemove.getGitId());

        List<String> aliases = authorToRemove.getAuthorAliases();
        aliases.forEach(alias -> authorDetailsToAuthorMap.remove(alias));

        List<String> emails = authorToRemove.getEmails();
        emails.forEach(email -> authorDetailsToAuthorMap.remove(email));
    }

    /**
     * Adds new authors from {@code authorList} and sets the default alias, aliases, emails and display name
     * of the new authors. Skips authors that have already been added previously.
     */
    public void addAuthors(List<Author> authorList, List<String> ignoreGlobList) {
        for (Author author : authorList) {
            if (containsAuthor(author)) {
                logger.warning(String.format(
                        "Skipping author as %s already in repository %s %s", author.getGitId(), location, branch));
                continue;
            }

            addAuthor(author, ignoreGlobList);
        }
    }

    public boolean containsAuthor(Author author) {
        return authorList.contains(author);
    }

    public void setAuthorList(List<Author> authorList) {
        this.authorList = authorList;
    }

    /**
     * Clears author mapping information.
     */
    public void clear() {
        authorDetailsToAuthorMap.clear();
        authorDisplayNameMap.clear();
    }

    /**
     * Resets author mapping information with the details of current author list.
     */
    public void buildFromAuthorList() {
        authorList.forEach(this::setAuthorDetails);
    }

    public Map<String, Author> getAuthorDetailsToAuthorMap() {
        return authorDetailsToAuthorMap;
    }

    public void setAuthorDetailsToAuthorMap(Map<String, Author> authorDetailsToAuthorMap) {
        this.authorDetailsToAuthorMap = authorDetailsToAuthorMap;
    }

    public void setAuthorDisplayName(Author author, String displayName) {
        authorDisplayNameMap.put(author, displayName);
    }

    /**
     * Adds {@code aliases} of {@code author} into the map
     * @param author
     * @param values
     */
    public void addAuthorDetailsToAuthorMapEntry(Author author, List<String> values) {
        values.forEach(value -> {
            checkDuplicateAliases(authorDetailsToAuthorMap, value, author.getGitId());
            authorDetailsToAuthorMap.put(value, author);
        });
    }

    public RepoLocation getLocation() {
        return location;
    }

    /**
     * Attempts to find matching {@code Author} given a name and an email.
     * If no matching {@code Author} is found, {@code Author#UNKNOWN_AUTHOR} is returned.
     */
    public Author getAuthor(String name, String email) {
        if (authorDetailsToAuthorMap.containsKey(name)) {
            return authorDetailsToAuthorMap.get(name);
        }
        if (authorDetailsToAuthorMap.containsKey(email)) {
            return authorDetailsToAuthorMap.get(email);
        }
        Matcher matcher = EMAIL_PLUS_OPERATOR_PATTERN.matcher(email);

        if (matcher.matches()) {
            return authorDetailsToAuthorMap.getOrDefault(matcher.group("suffix") + matcher.group("domain"),
                    Author.UNKNOWN_AUTHOR);
        }
        return Author.UNKNOWN_AUTHOR;
    }

    public boolean isDefaultBranch() {
        return this.branch.equals(DEFAULT_BRANCH);
    }

    public static void setHasAuthorConfigFile(boolean hasAuthorConfigFile) {
        AuthorConfiguration.hasAuthorConfigFile = hasAuthorConfigFile;
    }

    public static boolean hasAuthorConfigFile() {
        return AuthorConfiguration.hasAuthorConfigFile;
    }
}
