package reposense.model;

import static org.apache.tools.ant.types.Commandline.translateCommandline;

import static reposense.util.TestUtil.loadResource;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import reposense.RepoSense;
import reposense.git.GitClone;
import reposense.parser.ArgsParser;
import reposense.parser.AuthorConfigCsvParser;
import reposense.parser.GroupConfigCsvParser;
import reposense.parser.RepoConfigCsvParser;
import reposense.report.ReportGenerator;
import reposense.util.InputBuilder;
import reposense.util.TestUtil;

public class RepoConfigurationTest {
    private static final Path IGNORE_STANDALONE_TEST_CONFIG_FILES =
            loadResource(RepoConfigurationTest.class, "RepoConfigurationTest/repoconfig_ignoreStandAlone_test");
    private static final Path IGNORE_STANDALONE_KEYWORD_TEST_CONFIG_FILES =
            loadResource(RepoConfigurationTest.class, "RepoConfigurationTest/repoconfig_ignoreStandAloneKeyword_test");
    private static final Path FORMATS_TEST_CONFIG_FILES = loadResource(RepoConfigurationTest.class,
            "RepoConfigurationTest/repoconfig_formats_test");
    private static final Path WITHOUT_FORMATS_TEST_CONFIG_FILES = loadResource(RepoConfigurationTest.class,
            "RepoConfigurationTest/repoconfig_withoutformats_test");
    private static final Path GROUPS_TEST_CONFIG_FILES = loadResource(RepoConfigurationTest.class,
            "RepoConfigurationTest/repoconfig_groups_test");
    private static final Path OVERRIDE_STANDALONE_TEST_CONFIG_FILE = loadResource(RepoConfigurationTest.class,
            "RepoConfigurationTest/repoconfig_overrideStandAlone_test");
    private static final Path IGNORE_AUTHORS_TEST_CONFIG_FILE = loadResource(RepoConfigurationTest.class,
            "RepoConfigurationTest/repoconfig_ignoreAuthors_test");
    private static final Path IGNORE_STANDALONE_FLAG_OVERRIDE_CSV_TEST = loadResource(RepoConfigurationTest.class,
            "RepoConfigurationTest/repoconfig_ignoreStandaloneOverrideCsv_test");
    private static final Path SHALLOW_CLONING_TEST_CONFIG_FILES =
            loadResource(RepoConfigurationTest.class, "RepoConfigurationTest/repoconfig_shallowCloning_test");
    private static final Path SHALLOW_CLONING_FLAG_OVERRIDE_TEST_CONFIG_FILES =
            loadResource(RepoConfigurationTest.class,
            "RepoConfigurationTest/repoconfig_shallowCloningOverrideCsv_test");

    private static final String TEST_REPO_BETA = "https://github.com/reposense/testrepo-Beta.git";
    private static final String TEST_REPO_DELTA = "https://github.com/reposense/testrepo-Delta.git";
    private static final String TEST_REPO_MINIMAL_STANDALONE_CONFIG =
            "https://github.com/reposense/testrepo-minimalstandaloneconfig.git";
    private static final String TEST_REPO_INVALID_LOCATION = "ftp://github.com/reposense/testrepo-Delta.git";

    private static final Author FIRST_AUTHOR = new Author("lithiumlkid");
    private static final Author SECOND_AUTHOR = new Author("codeeong");
    private static final Author THIRD_AUTHOR = new Author("jordancjq");
    private static final Author FOURTH_AUTHOR = new Author("lohtianwei");

    private static final List<String> FIRST_AUTHOR_ALIASES = Collections.singletonList("Ahmad Syafiq");
    private static final List<String> SECOND_AUTHOR_ALIASES = Collections.emptyList();
    private static final List<String> THIRD_AUTHOR_ALIASES = Collections.singletonList("Jordan Chong");
    private static final List<String> FOURTH_AUTHOR_ALIASES = Collections.singletonList("Tianwei");

    private static final List<String> REPO_LEVEL_GLOB_LIST = Collections.singletonList("collated**");
    private static final List<String> FIRST_AUTHOR_GLOB_LIST =
            Arrays.asList("*.aa1", "**.aa2", "**.java", "collated**");
    private static final List<String> SECOND_AUTHOR_GLOB_LIST = Arrays.asList("", "collated**");
    private static final List<String> THIRD_AUTHOR_GLOB_LIST = Arrays.asList("**[!(.md)]", "collated**");
    private static final List<String> FOURTH_AUTHOR_GLOB_LIST = Collections.singletonList("collated**");

    private static final List<FileType> CONFIG_FORMATS = FileType.convertFormatStringsToFileTypes(Arrays.asList(
            "java", "adoc", "md"));
    private static final List<FileType> FIRST_CONFIG_GROUPS = Arrays.asList(
            new FileType("test", Collections.singletonList("src/test**")),
            new FileType("code", Collections.singletonList("**.java")),
            new FileType("docs", Collections.singletonList("docs**")));
    private static final List<FileType> SECOND_CONFIG_GROUPS = Arrays.asList(
            new FileType("docs", Collections.singletonList("docs**")));
    private static final List<String> CLI_FORMATS = Arrays.asList("css", "html");

    private static RepoConfiguration repoDeltaStandaloneConfig;

    @BeforeClass
    public static void setUp() throws Exception {
        FIRST_AUTHOR.setAuthorAliases(FIRST_AUTHOR_ALIASES);
        SECOND_AUTHOR.setAuthorAliases(SECOND_AUTHOR_ALIASES);
        THIRD_AUTHOR.setAuthorAliases(THIRD_AUTHOR_ALIASES);
        FOURTH_AUTHOR.setAuthorAliases(FOURTH_AUTHOR_ALIASES);

        FIRST_AUTHOR.setIgnoreGlobList(FIRST_AUTHOR_GLOB_LIST);
        SECOND_AUTHOR.setIgnoreGlobList(SECOND_AUTHOR_GLOB_LIST);
        THIRD_AUTHOR.setIgnoreGlobList(THIRD_AUTHOR_GLOB_LIST);
        FOURTH_AUTHOR.setIgnoreGlobList(FOURTH_AUTHOR_GLOB_LIST);

        List<Author> expectedAuthors = new ArrayList<>();
        expectedAuthors.add(FIRST_AUTHOR);
        expectedAuthors.add(SECOND_AUTHOR);
        expectedAuthors.add(THIRD_AUTHOR);
        expectedAuthors.add(FOURTH_AUTHOR);

        repoDeltaStandaloneConfig = new RepoConfiguration(new RepoLocation(TEST_REPO_DELTA), "master");
        repoDeltaStandaloneConfig.setAuthorList(expectedAuthors);
        repoDeltaStandaloneConfig.addAuthorDetailsToAuthorMapEntry(FIRST_AUTHOR, FIRST_AUTHOR_ALIASES);
        repoDeltaStandaloneConfig.addAuthorDetailsToAuthorMapEntry(FOURTH_AUTHOR, FOURTH_AUTHOR_ALIASES);
        repoDeltaStandaloneConfig.setAuthorDisplayName(FIRST_AUTHOR, "Ahm");
        repoDeltaStandaloneConfig.setAuthorDisplayName(SECOND_AUTHOR, "Cod");
        repoDeltaStandaloneConfig.setAuthorDisplayName(THIRD_AUTHOR, "Jor");
        repoDeltaStandaloneConfig.setAuthorDisplayName(FOURTH_AUTHOR, "Loh");

        SECOND_AUTHOR.setEmails(Arrays.asList("codeeong@gmail.com", "33129797+codeeong@users.noreply.github.com"));
        for (Author author : expectedAuthors) {
            repoDeltaStandaloneConfig.addAuthorDetailsToAuthorMapEntry(author, author.getEmails());
        }

        repoDeltaStandaloneConfig.setIgnoreGlobList(REPO_LEVEL_GLOB_LIST);
        repoDeltaStandaloneConfig.setFormats(CONFIG_FORMATS);
    }

    @Test
    public void repoConfig_usesStandaloneConfig_success() throws Exception {
        RepoConfiguration actualConfig = new RepoConfiguration(new RepoLocation(TEST_REPO_DELTA), "master");
        GitClone.clone(actualConfig);
        ReportGenerator.updateRepoConfig(actualConfig);

        TestUtil.compareRepoConfig(repoDeltaStandaloneConfig, actualConfig);
    }

    @Test
    public void repoConfig_ignoresStandaloneConfig_success() throws Exception {
        List<Author> expectedAuthors = new ArrayList<>();
        Author author = new Author(FIRST_AUTHOR);
        author.setIgnoreGlobList(REPO_LEVEL_GLOB_LIST);
        expectedAuthors.add(author);

        RepoConfiguration expectedConfig = new RepoConfiguration(new RepoLocation(TEST_REPO_DELTA), "master");
        expectedConfig.setAuthorList(expectedAuthors);
        expectedConfig.addAuthorDetailsToAuthorMapEntry(author, FIRST_AUTHOR_ALIASES);
        expectedConfig.setAuthorDisplayName(author, "Ahm");

        expectedConfig.setIgnoreGlobList(REPO_LEVEL_GLOB_LIST);
        expectedConfig.setFormats(CONFIG_FORMATS);
        expectedConfig.setStandaloneConfigIgnored(true);

        String formats = String.join(" ", CLI_FORMATS);
        String input = new InputBuilder().addConfig(IGNORE_STANDALONE_TEST_CONFIG_FILES)
                .addFormats(formats)
                .build();
        CliArguments cliArguments = ArgsParser.parse(translateCommandline(input));

        List<RepoConfiguration> actualConfigs =
                new RepoConfigCsvParser(((ConfigCliArguments) cliArguments).getRepoConfigFilePath()).parse();
        List<AuthorConfiguration> authorConfigs =
                new AuthorConfigCsvParser(((ConfigCliArguments) cliArguments).getAuthorConfigFilePath()).parse();
        RepoConfiguration.merge(actualConfigs, authorConfigs);

        RepoConfiguration actualConfig = actualConfigs.get(0);
        GitClone.clone(actualConfig);
        ReportGenerator.updateRepoConfig(actualConfig);

        TestUtil.compareRepoConfig(expectedConfig, actualConfig);
    }

    @Test
    public void repoConfig_ignoresStandaloneConfigInCli_success() throws Exception {
        RepoConfiguration expectedConfig = new RepoConfiguration(new RepoLocation(TEST_REPO_DELTA), "master");
        expectedConfig.setFormats(FileType.convertFormatStringsToFileTypes(CLI_FORMATS));
        expectedConfig.setStandaloneConfigIgnored(true);

        String formats = String.join(" ", CLI_FORMATS);
        String input = new InputBuilder().addRepos(TEST_REPO_DELTA)
                .addFormats(formats)
                .addIgnoreStandaloneConfig()
                .build();
        CliArguments cliArguments = ArgsParser.parse(translateCommandline(input));
        List<RepoConfiguration> actualConfigs = RepoSense.getRepoConfigurations((LocationsCliArguments) cliArguments);
        RepoConfiguration.setFormatsToRepoConfigs(actualConfigs, cliArguments.getFormats());
        RepoConfiguration.setStandaloneConfigIgnoredToRepoConfigs(actualConfigs,
                cliArguments.isStandaloneConfigIgnored());
        RepoConfiguration actualConfig = actualConfigs.get(0);
        GitClone.clone(actualConfig);
        ReportGenerator.updateRepoConfig(actualConfig);

        TestUtil.compareRepoConfig(expectedConfig, actualConfig);
    }

    @Test
    public void repoConfig_ignoreStandaloneConfigInCli_overrideCsv() throws Exception {

        RepoConfiguration repoBetaExpectedConfig = new RepoConfiguration(
                new RepoLocation(TEST_REPO_BETA), "master");
        repoBetaExpectedConfig.setFormats(FileType.convertFormatStringsToFileTypes(CLI_FORMATS));
        repoBetaExpectedConfig.setStandaloneConfigIgnored(true);
        RepoConfiguration repoDeltaExpectedConfig = new RepoConfiguration(
                new RepoLocation(TEST_REPO_DELTA), "master");
        repoDeltaExpectedConfig.setStandaloneConfigIgnored(true);

        String input = new InputBuilder().addConfig(IGNORE_STANDALONE_FLAG_OVERRIDE_CSV_TEST)
                .addIgnoreStandaloneConfig()
                .build();
        CliArguments cliArguments = ArgsParser.parse(translateCommandline(input));
        List<RepoConfiguration> actualConfigs =
                new RepoConfigCsvParser(((ConfigCliArguments) cliArguments).getRepoConfigFilePath()).parse();
        RepoConfiguration.setStandaloneConfigIgnoredToRepoConfigs(actualConfigs,
                cliArguments.isStandaloneConfigIgnored());

        RepoConfiguration repoBetaActualConfig = actualConfigs.get(0);
        RepoConfiguration repoDeltaActualConfig = actualConfigs.get(1);
        GitClone.clone(repoBetaActualConfig);
        GitClone.clone(repoDeltaActualConfig);
        ReportGenerator.updateRepoConfig(repoBetaActualConfig);
        ReportGenerator.updateRepoConfig(repoDeltaActualConfig);
        TestUtil.compareRepoConfig(repoBetaExpectedConfig, repoBetaActualConfig);
        TestUtil.compareRepoConfig(repoDeltaExpectedConfig, repoDeltaActualConfig);
    }

    @Test
    public void repoConfig_withoutIgnoreStandaloneConfigInCli_useCsv() throws Exception {
        RepoConfiguration repoBetaExpectedConfig = new RepoConfiguration(
                new RepoLocation(TEST_REPO_BETA), "master");
        repoBetaExpectedConfig.setFormats(FileType.convertFormatStringsToFileTypes(CLI_FORMATS));
        repoBetaExpectedConfig.setStandaloneConfigIgnored(true);

        String input = new InputBuilder().addConfig(IGNORE_STANDALONE_FLAG_OVERRIDE_CSV_TEST).build();
        CliArguments cliArguments = ArgsParser.parse(translateCommandline(input));
        List<RepoConfiguration> actualConfigs =
                new RepoConfigCsvParser(((ConfigCliArguments) cliArguments).getRepoConfigFilePath()).parse();
        RepoConfiguration.setStandaloneConfigIgnoredToRepoConfigs(actualConfigs,
                cliArguments.isStandaloneConfigIgnored());

        RepoConfiguration repoBetaActualConfig = actualConfigs.get(0);
        RepoConfiguration repoDeltaActualConfig = actualConfigs.get(1);
        GitClone.clone(repoBetaActualConfig);
        GitClone.clone(repoDeltaActualConfig);
        ReportGenerator.updateRepoConfig(repoBetaActualConfig);
        ReportGenerator.updateRepoConfig(repoDeltaActualConfig);
        TestUtil.compareRepoConfig(repoBetaExpectedConfig, repoBetaActualConfig);
        TestUtil.compareRepoConfig(repoDeltaStandaloneConfig, repoDeltaActualConfig);
    }

    @Test
    public void repoConfig_wrongKeywordUseStandaloneConfig_success() throws Exception {
        String formats = String.join(" ", CLI_FORMATS);
        String input = new InputBuilder().addConfig(IGNORE_STANDALONE_KEYWORD_TEST_CONFIG_FILES)
                .addFormats(formats)
                .build();
        CliArguments cliArguments = ArgsParser.parse(translateCommandline(input));

        List<RepoConfiguration> actualConfigs =
                new RepoConfigCsvParser(((ConfigCliArguments) cliArguments).getRepoConfigFilePath()).parse();

        RepoConfiguration actualConfig = actualConfigs.get(0);
        GitClone.clone(actualConfig);
        ReportGenerator.updateRepoConfig(actualConfig);

        TestUtil.compareRepoConfig(repoDeltaStandaloneConfig, actualConfig);
    }

    @Test
    public void repoConfig_shallowCloning_success() throws Exception {
        RepoConfiguration expectedConfig = new RepoConfiguration(new RepoLocation(TEST_REPO_DELTA), "master");
        expectedConfig.setIgnoreGlobList(REPO_LEVEL_GLOB_LIST);
        expectedConfig.setFormats(CONFIG_FORMATS);
        expectedConfig.setStandaloneConfigIgnored(true);
        expectedConfig.setIsShallowCloningPerformed(true);

        String formats = String.join(" ", CLI_FORMATS);
        String input = new InputBuilder().addConfig(SHALLOW_CLONING_TEST_CONFIG_FILES)
                .addFormats(formats)
                .build();
        CliArguments cliArguments = ArgsParser.parse(translateCommandline(input));

        List<RepoConfiguration> actualConfigs =
                new RepoConfigCsvParser(((ConfigCliArguments) cliArguments).getRepoConfigFilePath()).parse();
        RepoConfiguration.setStandaloneConfigIgnoredToRepoConfigs(actualConfigs, true);

        RepoConfiguration actualConfig = actualConfigs.get(0);
        GitClone.clone(actualConfig);
        ReportGenerator.updateRepoConfig(actualConfig);

        TestUtil.compareRepoConfig(expectedConfig, actualConfig);
    }

    @Test
    public void repoConfig_shallowCloningInCli_success() throws Exception {
        RepoConfiguration expectedConfig = new RepoConfiguration(new RepoLocation(TEST_REPO_DELTA), "master");
        expectedConfig.setFormats(FileType.convertFormatStringsToFileTypes(CLI_FORMATS));
        expectedConfig.setStandaloneConfigIgnored(true);
        expectedConfig.setIsShallowCloningPerformed(true);

        String formats = String.join(" ", CLI_FORMATS);
        String input = new InputBuilder().addRepos(TEST_REPO_DELTA)
                .addFormats(formats)
                .addShallowCloning()
                .build();
        CliArguments cliArguments = ArgsParser.parse(translateCommandline(input));
        List<RepoConfiguration> actualConfigs = RepoSense.getRepoConfigurations((LocationsCliArguments) cliArguments);
        RepoConfiguration.setFormatsToRepoConfigs(actualConfigs, cliArguments.getFormats());
        RepoConfiguration.setStandaloneConfigIgnoredToRepoConfigs(actualConfigs, true);
        RepoConfiguration.setIsShallowCloningPerformedToRepoConfigs(actualConfigs,
                cliArguments.isShallowCloningPerformed());

        RepoConfiguration actualConfig = actualConfigs.get(0);
        GitClone.clone(actualConfig);
        ReportGenerator.updateRepoConfig(actualConfig);

        TestUtil.compareRepoConfig(expectedConfig, actualConfig);
    }

    @Test
    public void repoConfig_shallowCloningInCli_overrideCsv() throws Exception {
        RepoConfiguration repoBetaExpectedConfig = new RepoConfiguration(
                new RepoLocation(TEST_REPO_BETA), "master");
        repoBetaExpectedConfig.setFormats(FileType.convertFormatStringsToFileTypes(CLI_FORMATS));
        repoBetaExpectedConfig.setStandaloneConfigIgnored(true);
        repoBetaExpectedConfig.setIsShallowCloningPerformed(true);
        RepoConfiguration repoDeltaExpectedConfig = new RepoConfiguration(
                new RepoLocation(TEST_REPO_DELTA), "master");
        repoDeltaExpectedConfig.setStandaloneConfigIgnored(true);
        repoDeltaExpectedConfig.setIsShallowCloningPerformed(true);

        String input = new InputBuilder().addConfig(SHALLOW_CLONING_FLAG_OVERRIDE_TEST_CONFIG_FILES)
                .addShallowCloning()
                .build();
        CliArguments cliArguments = ArgsParser.parse(translateCommandline(input));
        List<RepoConfiguration> actualConfigs =
                new RepoConfigCsvParser(((ConfigCliArguments) cliArguments).getRepoConfigFilePath()).parse();
        RepoConfiguration.setStandaloneConfigIgnoredToRepoConfigs(actualConfigs, true);
        RepoConfiguration.setIsShallowCloningPerformedToRepoConfigs(actualConfigs,
                cliArguments.isShallowCloningPerformed());

        RepoConfiguration repoBetaActualConfig = actualConfigs.get(0);
        RepoConfiguration repoDeltaActualConfig = actualConfigs.get(1);
        GitClone.clone(repoBetaActualConfig);
        GitClone.clone(repoDeltaActualConfig);
        ReportGenerator.updateRepoConfig(repoBetaActualConfig);
        ReportGenerator.updateRepoConfig(repoDeltaActualConfig);
        TestUtil.compareRepoConfig(repoBetaExpectedConfig, repoBetaActualConfig);
        TestUtil.compareRepoConfig(repoDeltaExpectedConfig, repoDeltaActualConfig);
    }

    @Test
    public void repoConfig_withoutShallowCloningInInCli_useCsv() throws Exception {
        RepoConfiguration repoBetaExpectedConfig = new RepoConfiguration(
                new RepoLocation(TEST_REPO_BETA), "master");
        repoBetaExpectedConfig.setFormats(FileType.convertFormatStringsToFileTypes(CLI_FORMATS));
        repoBetaExpectedConfig.setStandaloneConfigIgnored(true);
        repoBetaExpectedConfig.setIsShallowCloningPerformed(true);
        RepoConfiguration repoDeltaExpectedConfig = new RepoConfiguration(
                new RepoLocation(TEST_REPO_DELTA), "master");
        repoDeltaExpectedConfig.setStandaloneConfigIgnored(true);

        String input = new InputBuilder().addConfig(SHALLOW_CLONING_FLAG_OVERRIDE_TEST_CONFIG_FILES).build();
        CliArguments cliArguments = ArgsParser.parse(translateCommandline(input));
        List<RepoConfiguration> actualConfigs =
                new RepoConfigCsvParser(((ConfigCliArguments) cliArguments).getRepoConfigFilePath()).parse();
        RepoConfiguration.setStandaloneConfigIgnoredToRepoConfigs(actualConfigs, true);
        RepoConfiguration.setIsShallowCloningPerformedToRepoConfigs(actualConfigs,
                cliArguments.isShallowCloningPerformed());

        RepoConfiguration repoBetaActualConfig = actualConfigs.get(0);
        RepoConfiguration repoDeltaActualConfig = actualConfigs.get(1);
        GitClone.clone(repoBetaActualConfig);
        GitClone.clone(repoDeltaActualConfig);
        ReportGenerator.updateRepoConfig(repoBetaActualConfig);
        ReportGenerator.updateRepoConfig(repoDeltaActualConfig);
        TestUtil.compareRepoConfig(repoBetaExpectedConfig, repoBetaActualConfig);
        TestUtil.compareRepoConfig(repoDeltaExpectedConfig, repoDeltaActualConfig);
    }

    @Test
    public void repoConfig_withFormats_ignoreCliFormats() throws Exception {
        String formats = String.join(" ", CLI_FORMATS);
        String input = new InputBuilder().addConfig(FORMATS_TEST_CONFIG_FILES)
                .addFormats(formats)
                .build();
        CliArguments cliArguments = ArgsParser.parse(translateCommandline(input));

        List<RepoConfiguration> actualConfigs =
                new RepoConfigCsvParser(((ConfigCliArguments) cliArguments).getRepoConfigFilePath()).parse();
        RepoConfiguration.setFormatsToRepoConfigs(actualConfigs, cliArguments.getFormats());

        Assert.assertEquals(1, actualConfigs.size());
        Assert.assertEquals(CONFIG_FORMATS, actualConfigs.get(0).getFileTypeManager().getFormats());
    }

    @Test
    public void repoConfig_withoutFormats_useCliFormats() throws Exception {
        String formats = String.join(" ", CLI_FORMATS);
        String input = new InputBuilder().addConfig(WITHOUT_FORMATS_TEST_CONFIG_FILES)
                .addFormats(formats)
                .build();
        CliArguments cliArguments = ArgsParser.parse(translateCommandline(input));

        List<RepoConfiguration> actualConfigs =
                new RepoConfigCsvParser(((ConfigCliArguments) cliArguments).getRepoConfigFilePath()).parse();
        RepoConfiguration.setFormatsToRepoConfigs(actualConfigs, cliArguments.getFormats());

        Assert.assertEquals(1, actualConfigs.size());

        List<FileType> actualFormats = actualConfigs.get(0).getFileTypeManager().getFormats();
        Assert.assertEquals(FileType.convertFormatStringsToFileTypes(CLI_FORMATS), actualFormats);
    }

    @Test
    public void repoConfig_withCustomGroups_useCustomGroups() throws Exception {
        String input = new InputBuilder().addConfig(GROUPS_TEST_CONFIG_FILES).build();
        CliArguments cliArguments = ArgsParser.parse(translateCommandline(input));

        List<RepoConfiguration> actualConfigs =
                new RepoConfigCsvParser(((ConfigCliArguments) cliArguments).getRepoConfigFilePath()).parse();
        List<GroupConfiguration> groupConfigs =
                new GroupConfigCsvParser(((ConfigCliArguments) cliArguments).getGroupConfigFilePath()).parse();

        RepoConfiguration.setGroupConfigsToRepos(actualConfigs, groupConfigs);

        Assert.assertEquals(2, actualConfigs.size());
        Assert.assertEquals(FIRST_CONFIG_GROUPS, actualConfigs.get(0).getFileTypeManager().getGroups());
        Assert.assertEquals(SECOND_CONFIG_GROUPS, actualConfigs.get(1).getFileTypeManager().getGroups());
    }

    @Test
    public void repoConfig_withoutFormatsAndCliFormats_useAllFormats() throws Exception {
        String input = new InputBuilder().addConfig(WITHOUT_FORMATS_TEST_CONFIG_FILES).build();
        CliArguments cliArguments = ArgsParser.parse(translateCommandline(input));

        List<RepoConfiguration> actualConfigs =
                new RepoConfigCsvParser(((ConfigCliArguments) cliArguments).getRepoConfigFilePath()).parse();
        RepoConfiguration.setFormatsToRepoConfigs(actualConfigs, cliArguments.getFormats());

        Assert.assertEquals(1, actualConfigs.size());
        Assert.assertEquals(FileTypeTest.NO_SPECIFIED_FORMATS, actualConfigs.get(0).getFileTypeManager().getFormats());
    }

    @Test
    public void repoConfig_emptyLocationDifferentBranch_equal() throws Exception {
        RepoConfiguration emptyLocationEmptyBranchRepoConfig = new RepoConfiguration(new RepoLocation(""), "");
        RepoConfiguration emptyLocationDefaultBranchRepoConfig = new RepoConfiguration(new RepoLocation(""));
        RepoConfiguration emptyLocationWithBranchRepoConfig = new RepoConfiguration(new RepoLocation(""), "master");

        Assert.assertEquals(emptyLocationDefaultBranchRepoConfig, emptyLocationEmptyBranchRepoConfig);
        Assert.assertEquals(emptyLocationWithBranchRepoConfig, emptyLocationEmptyBranchRepoConfig);
    }

    @Test
    public void repoConfig_sameLocationDifferentBranch_notEqual() throws Exception {
        RepoConfiguration validLocationValidBranchRepoConfig =
                new RepoConfiguration(new RepoLocation(TEST_REPO_DELTA), "master");
        RepoConfiguration validLocationDefaultBranchRepoConfig =
                new RepoConfiguration(new RepoLocation(TEST_REPO_DELTA));

        Assert.assertNotEquals(validLocationDefaultBranchRepoConfig, validLocationValidBranchRepoConfig);
    }

    @Test
    public void repoConfig_overrideStandaloneConfig_success() throws Exception {
        RepoConfiguration expectedConfig = new RepoConfiguration(new RepoLocation(TEST_REPO_DELTA), "master",
                Collections.emptyList(), Collections.emptyList(), false, Collections.emptyList(),
                true, true, true, false, false);
        expectedConfig.setIsIgnoredAuthorsListOverriding(true);
        expectedConfig.setIgnoredAuthorsList(Arrays.asList("lithiumlkid"));

        List<Author> expectedAuthorList = new ArrayList<>();
        Author[] authors = new Author[]{FIRST_AUTHOR, SECOND_AUTHOR, THIRD_AUTHOR, FOURTH_AUTHOR};
        for (Author author : authors) {
            Author expectedAuthor = new Author(author);
            List<String> expectedAuthorIgnoreGlobList = new ArrayList<>();
            expectedAuthorIgnoreGlobList.addAll(author.getIgnoreGlobList());

            // Authors' original ignoreGlobList contains values from StandaloneConfig repo level, thus need to remove
            expectedAuthorIgnoreGlobList.removeAll(REPO_LEVEL_GLOB_LIST);
            expectedAuthor.setIgnoreGlobList(expectedAuthorIgnoreGlobList);
            expectedAuthorList.add(expectedAuthor);
        }
        expectedConfig.setAuthorList(expectedAuthorList);
        expectedConfig.setAuthorDisplayNameMap(repoDeltaStandaloneConfig.getAuthorDisplayNameMap());
        expectedConfig.setAuthorDetailsToAuthorMap(repoDeltaStandaloneConfig.getAuthorDetailsToAuthorMap());

        String formats = String.join(" ", CLI_FORMATS);
        String input = new InputBuilder().addConfig(OVERRIDE_STANDALONE_TEST_CONFIG_FILE)
                .addFormats(formats)
                .build();
        CliArguments cliArguments = ArgsParser.parse(translateCommandline(input));

        List<RepoConfiguration> actualConfigs =
                new RepoConfigCsvParser(((ConfigCliArguments) cliArguments).getRepoConfigFilePath()).parse();

        RepoConfiguration actualConfig = actualConfigs.get(0);
        GitClone.clone(actualConfig);
        ReportGenerator.updateRepoConfig(actualConfig);

        TestUtil.compareRepoConfig(expectedConfig, actualConfig);
    }

    @Test
    public void repoConfig_minimalStandaloneConfig_fieldsAssignedDefaultValues() throws Exception {
        RepoConfiguration expectedConfig = new RepoConfiguration(new RepoLocation(TEST_REPO_MINIMAL_STANDALONE_CONFIG),
                "master");

        Author firstAuthor = new Author("bluein-green");
        Author secondAuthor = new Author("jylee-git");
        List<Author> expectedAuthors = Arrays.asList(firstAuthor, secondAuthor);
        expectedConfig.setAuthorList(expectedAuthors);

        expectedConfig.setIgnoreGlobList(Collections.emptyList());
        expectedConfig.setFormats(Collections.emptyList());
        expectedConfig.setIgnoreCommitList(Collections.emptyList());

        RepoConfiguration actualConfig = new RepoConfiguration(new RepoLocation(TEST_REPO_MINIMAL_STANDALONE_CONFIG),
                "master");
        GitClone.clone(actualConfig);
        ReportGenerator.updateRepoConfig(actualConfig);

        TestUtil.compareRepoConfig(expectedConfig, actualConfig);
    }

    @Test
    public void repoConfig_removeIgnoredAuthors_success() throws Exception {
        List<Author> expectedAuthors = new ArrayList<>();
        Author author = new Author(FIRST_AUTHOR);
        author.setIgnoreGlobList(REPO_LEVEL_GLOB_LIST);
        expectedAuthors.add(author);

        RepoConfiguration expectedConfig = new RepoConfiguration(new RepoLocation(TEST_REPO_DELTA), "master");
        expectedConfig.setAuthorList(expectedAuthors);
        expectedConfig.addAuthorDetailsToAuthorMapEntry(author, FIRST_AUTHOR_ALIASES);
        expectedConfig.setAuthorDisplayName(author, "Ahm");

        expectedConfig.setIgnoreGlobList(REPO_LEVEL_GLOB_LIST);
        expectedConfig.setFormats(CONFIG_FORMATS);
        expectedConfig.setStandaloneConfigIgnored(true);
        expectedConfig.setIgnoredAuthorsList(Arrays.asList("jordancjq", "Eugene Peh"));

        String formats = String.join(" ", CLI_FORMATS);
        String input = new InputBuilder().addConfig(IGNORE_AUTHORS_TEST_CONFIG_FILE)
                .addFormats(formats)
                .build();
        CliArguments cliArguments = ArgsParser.parse(translateCommandline(input));

        List<RepoConfiguration> actualConfigs =
                new RepoConfigCsvParser(((ConfigCliArguments) cliArguments).getRepoConfigFilePath()).parse();
        List<AuthorConfiguration> authorConfigs =
                new AuthorConfigCsvParser(((ConfigCliArguments) cliArguments).getAuthorConfigFilePath()).parse();
        RepoConfiguration.merge(actualConfigs, authorConfigs);

        RepoConfiguration actualConfig = actualConfigs.get(0);
        GitClone.clone(actualConfig);
        ReportGenerator.updateRepoConfig(actualConfig);
        Method updateAuthorList = ReportGenerator.class.getDeclaredMethod("updateAuthorList", RepoConfiguration.class);
        updateAuthorList.setAccessible(true);
        updateAuthorList.invoke(null, actualConfig);

        TestUtil.compareRepoConfig(expectedConfig, actualConfig);
    }

    @Test
    public void repoConfig_withInvalidLocation_success() throws Exception {
        String formats = String.join(" ", CLI_FORMATS);
        String input = new InputBuilder().addRepos(TEST_REPO_BETA, TEST_REPO_DELTA, TEST_REPO_INVALID_LOCATION)
                .addFormats(formats)
                .addIgnoreStandaloneConfig()
                .build();
        CliArguments cliArguments = ArgsParser.parse(translateCommandline(input));
        List<RepoConfiguration> actualConfigs = RepoSense.getRepoConfigurations((LocationsCliArguments) cliArguments);

        Assert.assertEquals(2, actualConfigs.size());
    }
}
