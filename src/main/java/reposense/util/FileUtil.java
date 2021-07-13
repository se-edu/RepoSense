package reposense.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import reposense.model.CommitHash;
import reposense.model.FileType;
import reposense.model.RepoConfiguration;
import reposense.system.LogsManager;

/**
 * Contains file processing related functionalities.
 */
public class FileUtil {
    public static final String REPOS_ADDRESS = "repos";

    // zip file which contains all the specified file types
    public static final String ZIP_FILE = "archive.zip";

    private static final Logger logger = LogsManager.getLogger(FileUtil.class);
    private static final String GITHUB_API_DATE_FORMAT = "yyyy-MM-dd";
    private static final ByteBuffer buffer = ByteBuffer.allocate(1 << 11); // 2KB

    private static final String BARE_REPO_SUFFIX = "_bare";
    private static final String PARTIAL_REPO_SUFFIX = "_partial";
    private static final String SHALLOW_PARTIAL_REPO_SUFFIX = "_shallow_partial";

    private static final String MESSAGE_INVALID_FILE_PATH = "\"%s\" is an invalid file path. Skipping this directory.";
    private static final String MESSAGE_FAIL_TO_ZIP_FILES =
            "Exception occurred while attempting to zip the report files.";
    private static final String MESSAGE_FAIL_TO_COPY_ASSETS =
            "Exception occurred while attempting to copy custom assets.";

    /**
     * Zips all files of type {@code fileTypes} that are in the directory {@code pathsToZip} into a single file and
     * output it to {@code sourceAndOutputPath}.
     */
    public static void zipFoldersAndFiles(List<Path> pathsToZip, Path sourceAndOutputPath, String... fileTypes) {
        zipFoldersAndFiles(pathsToZip, sourceAndOutputPath, sourceAndOutputPath, fileTypes);
    }

    /**
     * Zips all files listed in {@code pathsToZip} of type {@code fileTypes} located in the directory
     * {@code sourcePath} into {@code outputPath}.
     */
    public static void zipFoldersAndFiles(List<Path> pathsToZip,
            Path sourcePath, Path outputPath, String... fileTypes) {
        try (
                FileOutputStream fos = new FileOutputStream(outputPath + File.separator + ZIP_FILE);
                ZipOutputStream zos = new ZipOutputStream(fos)
        ) {
            for (Path pathToZip : pathsToZip) {
                List<Path> allPaths = getFilePaths(pathToZip, fileTypes);
                for (Path path : allPaths) {
                    String filePath = sourcePath.relativize(path.toAbsolutePath()).toString();
                    String zipEntry = Files.isDirectory(path) ? filePath + File.separator : filePath;
                    zos.putNextEntry(new ZipEntry(zipEntry.replace("\\", "/")));
                    if (Files.isRegularFile(path)) {
                        Files.copy(path, zos);
                    }
                    zos.closeEntry();
                }
            }
        } catch (IOException ioe) {
            logger.severe(MESSAGE_FAIL_TO_ZIP_FILES);
        }
    }

    /**
     * Writes the JSON file representing the {@code object} at the given {@code path}.
     * @return An Optional containing the Path to the JSON file, or an empty Optional
     *         if there was an error while writing the JSON file.
     */
    public static Optional<Path> writeJsonFile(Object object, String path) {
        Gson gson = new GsonBuilder()
                .setDateFormat(GITHUB_API_DATE_FORMAT)
                .registerTypeAdapter(FileType.class, new FileType.FileTypeSerializer())
                .create();
        String result = gson.toJson(object);

        try (PrintWriter out = new PrintWriter(path)) {
            out.print(result);
            out.print("\n");
            return Optional.of(path).map(Paths::get);
        } catch (FileNotFoundException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * Writes the ignore revs file containing the {@code ignoreCommitList} at the given {@code path}.
     * @return An Optional containing the Path to the ignore revs file, or an empty Optional
     *         if there was an error while writing the ignore revs file.
     */
    public static Optional<Path> writeIgnoreRevsFile(String path, List<CommitHash> ignoreCommitList) {
        String contentOfIgnoreRevsFile = ignoreCommitList
                                            .stream()
                                            .map(CommitHash::toString)
                                            .reduce("", (hashes, newHash) -> hashes + newHash + "\n");

        try (PrintWriter out = new PrintWriter(path)) {
            out.print(contentOfIgnoreRevsFile);
            return Optional.of(path).map(Paths::get);
        } catch (FileNotFoundException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * Deletes the {@code root} directory.
     * @throws IOException if the root path does not exist.
     */
    public static void deleteDirectory(String root) throws IOException {
        File rootDirectory = Paths.get(root).toFile();
        if (rootDirectory.exists()) {
            for (File file : rootDirectory.listFiles()) {
                if (file.isDirectory()) {
                    deleteDirectory(file.toString());
                } else {
                    file.delete();
                }
            }
            rootDirectory.delete();
            if (rootDirectory.exists()) {
                throw new IOException(String.format("Fail to delete directory %s", rootDirectory));
            }
        }
    }

    /**
     * Unzips the contents of the {@code zipSourcePath} into {@code outputPath}.
     * @throws IOException if {@code zipSourcePath} is an invalid path.
     */
    public static void unzip(Path zipSourcePath, Path outputPath) throws IOException {
        try (InputStream is = Files.newInputStream(zipSourcePath)) {
            unzip(is, outputPath);
        }
    }

    /**
     * Unzips the contents of the {@code is} into {@code outputPath}.
     * @throws IOException if {@code is} refers to an invalid path.
     */
    public static void unzip(InputStream is, Path outputPath) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(is)) {
            ZipEntry entry;
            Files.createDirectories(outputPath);
            while ((entry = zis.getNextEntry()) != null) {
                Path path = Paths.get(outputPath.toString(), entry.getName());
                // create the directories of the zip directory
                if (entry.isDirectory()) {
                    Files.createDirectories(path.toAbsolutePath());
                    zis.closeEntry();
                    continue;
                }
                if (!Files.exists(path.getParent())) {
                    Files.createDirectories(path.getParent());
                }
                try (OutputStream output = Files.newOutputStream(path)) {
                    int length;
                    while ((length = zis.read(buffer.array())) > 0) {
                        output.write(buffer.array(), 0, length);
                    }
                }
                zis.closeEntry();
            }
        }
    }

    /**
     * Copies the template files from {@code sourcePath} to the {@code outputPath}.
     * @throws IOException if {@code is} refers to an invalid path.
     */
    public static void copyTemplate(InputStream is, String outputPath) throws IOException {
        FileUtil.unzip(is, Paths.get(outputPath));
    }

    /**
     * Copies files from {@code sourcePath} to the {@code outputPath}.
     * @throws IOException if {@code is} refers to an invalid path.
     */
    public static void copyDirectoryContents(String sourcePath, String outputPath) throws IOException {
        copyDirectoryContents(sourcePath, outputPath, null);
    }

    /**
     * Copies files from {@code sourcePath} to the {@code outputPath}.
     * If {@code whiteList} is provided, only filenames specified by the whitelist will be copied.
     * @throws IOException if {@code is} refers to an invalid path.
     */
    public static void copyDirectoryContents(String sourcePath, String outputPath, List<String> whiteList)
            throws IOException {
        Path source = Paths.get(sourcePath);
        Path out = Paths.get(outputPath);

        Files.walk(source, 1).skip(1).forEach(file -> {
            if (whiteList == null || whiteList != null && whiteList.contains(file.getFileName().toString())) {
                try {
                    Files.copy(file, out.resolve(source.relativize(file)), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException ioe) {
                    logger.severe(MESSAGE_FAIL_TO_COPY_ASSETS);
                }
            }
        });
    }
    /**
     * Creates the {@code dest} directory if it does not exist.
     */
    public static void createDirectory(Path dest) throws IOException {
        Files.createDirectories(dest);
    }

    /**
     * Returns the relative path to the bare repo version of {@code config}.
     */
    public static Path getRepoParentFolder(RepoConfiguration config) {
        return Paths.get(FileUtil.REPOS_ADDRESS, config.getRepoFolderName());
    }

    /**
     * Returns the relative path to the bare repo version of {@code config}.
     */
    public static Path getBareRepoPath(RepoConfiguration config) {
        return Paths.get(FileUtil.REPOS_ADDRESS,
                config.getRepoFolderName(), config.getRepoName() + BARE_REPO_SUFFIX);
    }

    /**
     * Returns the relative path to the partial bare repo version of {@code config}.
     */
    public static Path getPartialBareRepoPath(RepoConfiguration config) {
        return Paths.get(FileUtil.REPOS_ADDRESS,
                config.getRepoFolderName(), config.getRepoName() + PARTIAL_REPO_SUFFIX);
    }

    /**
     * Returns the relative path to the shallow partial bare repo version of {@code config}.
     */
    public static Path getShallowPartialBareRepoPath(RepoConfiguration config) {
        return Paths.get(FileUtil.REPOS_ADDRESS,
                config.getRepoFolderName(), config.getRepoName() + SHALLOW_PARTIAL_REPO_SUFFIX);
    }

    /**
     * Returns the folder name of the bare repo version of {@code config}.
     */
    public static String getBareRepoFolderName(RepoConfiguration config) {
        return config.getRepoName() + BARE_REPO_SUFFIX;
    }

    /**
     * Returns the folder name of the partial bare repo version of {@code config}.
     */
    public static String getPartialBareRepoFolderName(RepoConfiguration config) {
        return config.getRepoName() + PARTIAL_REPO_SUFFIX;
    }

    /**
     * Returns the folder name of the shallow partial bare repo version of {@code config}.
     */
    public static String getShallowPartialBareRepoFolderName(RepoConfiguration config) {
        return config.getRepoName() + SHALLOW_PARTIAL_REPO_SUFFIX;
    }

    /**
     * Returns true if {@code path} is a valid path.
     * Produces log messages when the invalid file path is skipped.
     */
    public static boolean isValidPath(String path) {
        try {
            Paths.get(path);
        } catch (InvalidPathException ipe) {
            logger.log(Level.WARNING, String.format(MESSAGE_INVALID_FILE_PATH, path));
            return false;
        }
        return true;
    }

    public static boolean isEmptyFile(String directoryPath, String relativeFilePath) {
        return (new File(directoryPath, relativeFilePath).length() == 0);
    }

    /**
     * Returns a list of {@code Path} of {@code fileTypes} contained in the given {@code directoryPath} directory.
     */
    private static List<Path> getFilePaths(Path directoryPath, String... fileTypes) throws IOException {
        return Files.walk(directoryPath)
                .filter(p -> FileUtil.isFileTypeInPath(p, fileTypes) || Files.isDirectory(p))
                .collect(Collectors.toList());
    }

    /**
     * Returns true if the {@code path} contains one of the {@code fileTypes} extension.
     */
    private static boolean isFileTypeInPath(Path path, String... fileTypes) {
        return Arrays.stream(fileTypes).anyMatch(path.toString()::endsWith);
    }

}
