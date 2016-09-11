package org.codehaus.mojo.jaxb2.shared;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.mojo.jaxb2.AbstractJaxbMojo;
import org.codehaus.mojo.jaxb2.shared.filters.Filter;
import org.codehaus.mojo.jaxb2.shared.filters.Filters;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.Os;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * The Jaxb2 Maven Plugin needs to fiddle with the filesystem a great deal, to create and optionally prune
 * directories or detect/create various files. This utility class contains all such algorithms, and serves as
 * an entry point to any Plexus Utils methods.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>
 * @since 2.0
 */
public final class FileSystemUtilities {

    /*
     * Hide the constructor for utility classes.
     */
    private FileSystemUtilities() {
        // Do nothing
    }

    /**
     * FileFilter which accepts Files that exist and for which {@code File.isFile() } is {@code true}.
     */
    public static final FileFilter EXISTING_FILE = new FileFilter() {
        @Override
        public boolean accept(final File candidate) {
            return candidate != null && candidate.exists() && candidate.isFile();
        }
    };

    /**
     * FileFilter which accepts Files that exist and for which {@code File.isDirectory() } is {@code true}.
     */
    public static final FileFilter EXISTING_DIRECTORY = new FileFilter() {
        @Override
        public boolean accept(final File candidate) {
            return candidate != null && candidate.exists() && candidate.isDirectory();
        }
    };

    /**
     * Acquires the canonical path for the supplied file.
     *
     * @param file A non-null File for which the canonical path should be retrieved.
     * @return The canonical path of the supplied file.
     */
    public static String getCanonicalPath(final File file) {
        return getCanonicalFile(file).getPath();
    }

    /**
     * Non-valid Characters for naming files, folders under Windows: <code>":", "*", "?", "\"", "<", ">", "|"</code>
     *
     * @see <a href="http://support.microsoft.com/?scid=kb%3Ben-us%3B177506&x=12&y=13">
     * http://support.microsoft.com/?scid=kb%3Ben-us%3B177506&x=12&y=13</a>
     * @see {@code org.codehaus.plexus.util.FileUtils}
     */
    private static final String[] INVALID_CHARACTERS_FOR_WINDOWS_FILE_NAME = {":", "*", "?", "\"", "<", ">", "|"};

    /**
     * Acquires the canonical File for the supplied file.
     *
     * @param file A non-null File for which the canonical File should be retrieved.
     * @return The canonical File of the supplied file.
     */
    public static File getCanonicalFile(final File file) {

        // Check sanity
        Validate.notNull(file, "file");

        // All done
        try {
            return file.getCanonicalFile();
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not acquire the canonical file for ["
                    + file.getAbsolutePath() + "]", e);
        }
    }

    /**
     * <p>Retrieves the canonical File matching the supplied path in the following order or priority:</p>
     * <ol>
     * <li><strong>Absolute path:</strong> The path is used by itself (i.e. {@code new File(path);}). If an
     * existing file or directory matches the provided path argument, its canonical path will be returned.</li>
     * <li><strong>Relative path:</strong> The path is appended to the baseDir (i.e.
     * {@code new File(baseDir, path);}). If an existing file or directory matches the provided path argument,
     * its canonical path will be returned. Only in this case will be baseDir argument be considered.</li>
     * </ol>
     * <p>If no file or directory could be derived from the supplied path and baseDir, {@code null} is returned.</p>
     *
     * @param path    A non-null path which will be used to find an existing file or directory.
     * @param baseDir A directory to which the path will be appended to search for the existing file or directory in
     *                case the file was nonexistent when interpreted as an absolute path.
     * @return either a canonical File for the path, or {@code null} if no file or directory matched
     * the supplied path and baseDir.
     */
    public static File getExistingFile(final String path, final File baseDir) {

        // Check sanity
        Validate.notEmpty(path, "path");
        final File theFile = new File(path);
        File toReturn = null;

        // Is 'path' absolute?
        if (theFile.isAbsolute() && (EXISTING_FILE.accept(theFile) || EXISTING_DIRECTORY.accept(theFile))) {
            toReturn = getCanonicalFile(theFile);
        }

        // Is 'path' relative?
        if (!theFile.isAbsolute()) {

            // In this case, baseDir cannot be null.
            Validate.notNull(baseDir, "baseDir");
            final File relativeFile = new File(baseDir, path);

            if (EXISTING_FILE.accept(relativeFile) || EXISTING_DIRECTORY.accept(relativeFile)) {
                toReturn = getCanonicalFile(relativeFile);
            }
        }

        // The path provided did not point to an existing File or Directory.
        return toReturn;
    }

    /**
     * Retrieves the URL for the supplied File. Convenience method which hides exception handling
     * for the operation in question.
     *
     * @param aFile A File for which the URL should be retrieved.
     * @return The URL for the supplied aFile.
     * @throws java.lang.IllegalArgumentException if getting the URL yielded a MalformedURLException.
     */
    public static URL getUrlFor(final File aFile) throws IllegalArgumentException {

        // Check sanity
        Validate.notNull(aFile, "aFile");

        try {
            return aFile.toURI().normalize().toURL();
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Could not retrieve the URL from file ["
                    + getCanonicalPath(aFile) + "]", e);
        }
    }

    /**
     * Acquires the file for a supplied URL, provided that its protocol is is either a file or a jar.
     *
     * @param anURL    a non-null URL.
     * @param encoding The encoding to be used by the URLDecoder to decode the path found.
     * @return The File pointing to the supplied URL, for file or jar protocol URLs and null otherwise.
     */
    public static File getFileFor(final URL anURL, final String encoding) {

        // Check sanity
        Validate.notNull(anURL, "anURL");
        Validate.notNull(encoding, "encoding");

        final String protocol = anURL.getProtocol();
        File toReturn = null;
        if ("file".equalsIgnoreCase(protocol)) {
            try {
                final String decodedPath = URLDecoder.decode(anURL.getPath(), encoding);
                toReturn = new File(decodedPath);
            } catch (Exception e) {
                throw new IllegalArgumentException("Could not get the File for [" + anURL + "]", e);
            }
        } else if ("jar".equalsIgnoreCase(protocol)) {

            try {

                // Decode the JAR
                final String tmp = URLDecoder.decode(anURL.getFile(), encoding);

                // JAR URLs generally contain layered protocols, such as:
                // jar:file:/some/path/to/nazgul-tools-validation-aspect-4.0.2.jar!/the/package/ValidationAspect.class
                final URL innerURL = new URL(tmp);

                // We can handle File protocol URLs here.
                if ("file".equalsIgnoreCase(innerURL.getProtocol())) {

                    // Peel off the inner protocol
                    final String innerUrlPath = innerURL.getPath();
                    final String filePath = innerUrlPath.contains("!")
                            ? innerUrlPath.substring(0, innerUrlPath.indexOf("!"))
                            : innerUrlPath;
                    toReturn = new File(filePath);
                }
            } catch (Exception e) {
                throw new IllegalArgumentException("Could not get the File for [" + anURL + "]", e);
            }
        }

        // All done.
        return toReturn;
    }


    /**
     * Filters files found either in the sources paths (or in the standardDirectory if no explicit sources are given),
     * and retrieves a List holding those files that do not match any of the supplied Java Regular Expression
     * excludePatterns.
     *
     * @param baseDir             The non-null basedir Directory.
     * @param sources             The sources which should be either absolute or relative (to the given baseDir)
     *                            paths to files or to directories that should be searched recursively for files.
     * @param standardDirectories If no sources are given, revert to searching all files under these standard
     *                            directories. Each path is {@code relativize()}-d to the supplied baseDir to
     *                            reach a directory path.
     * @param log                 A non-null Maven Log for logging any operations performed.
     * @param fileTypeDescription A human-readable short description of what kind of files are searched for, such as
     *                            "xsdSources" or "xjbSources".
     * @param excludePatterns     An optional List of patterns used to construct an ExclusionRegExpFileFilter used to
     *                            identify files which should be excluded from the result.
     * @return URLs to all Files under the supplied sources (or standardDirectories, if no explicit sources
     * are given) which do not match the supplied Java Regular excludePatterns.
     */
    public static List<URL> filterFiles(final File baseDir,
                                        final List<String> sources,
                                        final List<String> standardDirectories,
                                        final Log log,
                                        final String fileTypeDescription,
                                        final List<Filter<File>> excludePatterns) {

        final SortedMap<String, File> pathToResolvedSourceMap = new TreeMap<String, File>();

        for (String current : standardDirectories) {
            for (File currentResolvedSource : FileSystemUtilities.filterFiles(
                    baseDir,
                    sources,
                    FileSystemUtilities.relativize(current, baseDir),
                    log,
                    fileTypeDescription,
                    excludePatterns)) {

                // Add the source
                pathToResolvedSourceMap.put(
                        FileSystemUtilities.getCanonicalPath(currentResolvedSource),
                        currentResolvedSource);
            }
        }

        final List<URL> toReturn = new ArrayList<URL>();

        // Extract the URLs for all resolved Java sources.
        for (Map.Entry<String, File> current : pathToResolvedSourceMap.entrySet()) {
            toReturn.add(FileSystemUtilities.getUrlFor(current.getValue()));
        }

        if (log.isDebugEnabled()) {

            final StringBuilder builder = new StringBuilder();
            builder.append("\n+=================== [Filtered " + fileTypeDescription + "]\n");

            builder.append("|\n");
            builder.append("| " + excludePatterns.size() + " Exclude patterns:\n");
            for (int i = 0; i < excludePatterns.size(); i++) {
                builder.append("| [" + (i + 1) + "/" + excludePatterns.size() + "]: " + excludePatterns.get(i) + "\n");
            }

            builder.append("|\n");
            builder.append("| " + standardDirectories.size() + " Standard Directories:\n");
            for (int i = 0; i < standardDirectories.size(); i++) {
                builder.append("| [" + (i + 1) + "/" + standardDirectories.size() + "]: "
                        + standardDirectories.get(i) + "\n");
            }

            builder.append("|\n");
            builder.append("| " + toReturn.size() + " Results:\n");
            for (int i = 0; i < toReturn.size(); i++) {
                builder.append("| [" + (i + 1) + "/" + toReturn.size() + "]: " + toReturn.get(i) + "\n");
            }
            builder.append("|\n");
            builder.append("+=================== [End Filtered " + fileTypeDescription + "]\n\n");

            // Log all.
            log.debug(builder.toString().replace("\n", AbstractJaxbMojo.NEWLINE));
        }

        // All done.
        return toReturn;
    }

    /**
     * Filters files found either in the sources paths (or in the standardDirectory if no explicit sources are given),
     * and retrieves a List holding those files that do not match any of the supplied Java Regular Expression
     * excludePatterns.
     *
     * @param baseDir             The non-null basedir Directory.
     * @param sources             The sources which should be either absolute or relative (to the given baseDir)
     *                            paths to files or to directories that should be searched recursively for files.
     * @param standardDirectory   If no sources are given, revert to searching all files under this standard directory.
     *                            This is the path appended to the baseDir to reach a directory.
     * @param log                 A non-null Maven Log for logging any operations performed.
     * @param fileTypeDescription A human-readable short description of what kind of files are searched for, such as
     *                            "xsdSources" or "xjbSources".
     * @param excludeFilters      An optional List of Filters used to identify files which should be excluded from
     *                            the result.
     * @return All files under the supplied sources (or standardDirectory, if no explicit sources are given) which
     * do not match the supplied Java Regular excludePatterns.
     */
    @SuppressWarnings("CheckStyle")
    public static List<File> filterFiles(final File baseDir,
                                         final List<String> sources,
                                         final String standardDirectory,
                                         final Log log,
                                         final String fileTypeDescription,
                                         final List<Filter<File>> excludeFilters) {

        // Check sanity
        Validate.notNull(baseDir, "baseDir");
        Validate.notNull(log, "log");
        Validate.notEmpty(standardDirectory, "standardDirectory");
        Validate.notEmpty(fileTypeDescription, "fileTypeDescription");

        // No sources provided? Fallback to the standard (which should be a relative path).
        List<String> effectiveSources = sources;
        if (sources == null || sources.isEmpty()) {
            effectiveSources = new ArrayList<String>();

            final File tmp = new File(standardDirectory);
            final File rootDirectory = tmp.isAbsolute() ? tmp : new File(baseDir, standardDirectory);
            effectiveSources.add(FileSystemUtilities.getCanonicalPath(rootDirectory));
        }

        // First, remove the non-existent sources.
        List<File> existingSources = new ArrayList<File>();
        for (String current : effectiveSources) {

            final File existingFile = FileSystemUtilities.getExistingFile(current, baseDir);
            if (existingFile != null) {
                existingSources.add(existingFile);

                if (log.isDebugEnabled()) {
                    log.debug("Accepted configured " + fileTypeDescription + " ["
                            + FileSystemUtilities.getCanonicalFile(existingFile) + "]");
                }
            } else {
                if (log.isInfoEnabled()) {
                    log.info("Ignored given or default " + fileTypeDescription + " [" + current
                            + "], since it is not an existent file or directory.");
                }
            }
        }

        if (log.isDebugEnabled() && existingSources.size() > 0) {

            final int size = existingSources.size();

            log.debug(" [" + size + " existing " + fileTypeDescription + "] ...");
            for (int i = 0; i < size; i++) {
                log.debug("   " + (i + 1) + "/" + size + ": " + existingSources.get(i));
            }
            log.debug(" ... End [" + size + " existing " + fileTypeDescription + "]");
        }

        // All Done.
        return FileSystemUtilities.resolveRecursively(existingSources, excludeFilters, log);
    }

    /**
     * Filters all supplied files using the
     *
     * @param files        The list of files to resolve, filter and return. If the {@code files} List
     *                     contains directories, they are searched for Files recursively. Any found Files in such
     *                     a search are included in the resulting File List if they match the acceptFilter supplied.
     * @param acceptFilter A filter matched to all files in the given List. If the acceptFilter matches a file, it is
     *                     included in the result.
     * @param log          The active Maven Log.
     * @return All files in (or files in subdirectories of directories provided in) the files List, provided that each
     * file is accepted by an ExclusionRegExpFileFilter.
     */
    public static List<File> filterFiles(final List<File> files, final Filter<File> acceptFilter, final Log log) {

        // Check sanity
        Validate.notNull(files, "files");

        final List<File> toReturn = new ArrayList<File>();

        if (files.size() > 0) {
            for (File current : files) {

                final boolean isAcceptedFile = EXISTING_FILE.accept(current) && acceptFilter.accept(current);
                final boolean isAcceptedDirectory = EXISTING_DIRECTORY.accept(current) && acceptFilter.accept(current);

                if (isAcceptedFile) {
                    toReturn.add(current);
                } else if (isAcceptedDirectory) {
                    recurseAndPopulate(toReturn, Collections.singletonList(acceptFilter), current, false, log);
                }
            }
        }

        // All done
        return toReturn;
    }

    /**
     * Retrieves a List of Files containing all the existing files within the supplied files List, including all
     * files found in directories recursive to any directories provided in the files list. Each file included in the
     * result must pass an ExclusionRegExpFileFilter synthesized from the supplied exclusions pattern(s).
     *
     * @param files            The list of files to resolve, filter and return. If the {@code files} List
     *                         contains directories, they are searched for Files recursively. Any found Files in such
     *                         a search are included in the resulting File List if they do not match any of the
     *                         exclusionFilters supplied.
     * @param exclusionFilters A List of Filters which identify files to remove from the result - implying that any
     *                         File matched by any of these exclusionFilters will not be included in the result.
     * @param log              The active Maven Log.
     * @return All files in (or files in subdirectories of directories provided in) the files List, provided that each
     * file is accepted by an ExclusionRegExpFileFilter.
     */
    public static List<File> resolveRecursively(final List<File> files,
                                                final List<Filter<File>> exclusionFilters,
                                                final Log log) {

        // Check sanity
        Validate.notNull(files, "files");

        final List<Filter<File>> effectiveExclusions = exclusionFilters == null
                ? new ArrayList<Filter<File>>()
                : exclusionFilters;

        final List<File> toReturn = new ArrayList<File>();

        if (files.size() > 0) {
            for (File current : files) {

                final boolean isAcceptedFile = EXISTING_FILE.accept(current)
                        && Filters.noFilterMatches(current, effectiveExclusions);
                final boolean isAcceptedDirectory = EXISTING_DIRECTORY.accept(current)
                        && Filters.noFilterMatches(current, effectiveExclusions);

                if (isAcceptedFile) {
                    toReturn.add(current);
                } else if (isAcceptedDirectory) {
                    recurseAndPopulate(toReturn, effectiveExclusions, current, true, log);
                }
            }
        }

        // All done
        return toReturn;
    }

    /**
     * Convenience method to successfully create a directory - or throw an exception if failing to create it.
     *
     * @param aDirectory        The directory to create.
     * @param cleanBeforeCreate if {@code true}, the directory and all its content will be deleted before being
     *                          re-created. This will ensure that the created directory is really clean.
     * @throws MojoExecutionException if the aDirectory could not be created (and/or cleaned).
     */
    public static void createDirectory(final File aDirectory, final boolean cleanBeforeCreate)
            throws MojoExecutionException {

        // Check sanity
        Validate.notNull(aDirectory, "aDirectory");
        validateFileOrDirectoryName(aDirectory);

        // Clean an existing directory?
        if (cleanBeforeCreate) {
            try {
                FileUtils.deleteDirectory(aDirectory);
            } catch (IOException e) {
                throw new MojoExecutionException("Could not clean directory [" + getCanonicalPath(aDirectory) + "]", e);
            }
        }

        // Now, make the required directory, if it does not already exist as a directory.
        final boolean existsAsFile = aDirectory.exists() && aDirectory.isFile();
        if (existsAsFile) {
            throw new MojoExecutionException("[" + getCanonicalPath(aDirectory) + "] exists and is a file. "
                    + "Cannot make directory");
        } else if (!aDirectory.exists() && !aDirectory.mkdirs()) {
            throw new MojoExecutionException("Could not create directory [" + getCanonicalPath(aDirectory) + "]");
        }
    }

    /**
     * If the supplied path refers to a file or directory below the supplied basedir, the returned
     * path is identical to the part below the basedir.
     *
     * @param path      The path to strip off basedir path from, and return.
     * @param parentDir The maven project basedir.
     * @return The path relative to basedir, if it is situated below the basedir. Otherwise the supplied path.
     */
    public static String relativize(final String path, final File parentDir) {

        // Check sanity
        Validate.notNull(path, "path");
        Validate.notNull(parentDir, "parentDir");

        final String basedirPath = FileSystemUtilities.getCanonicalPath(parentDir);
        String toReturn = path;

        // Compare case insensitive
        if (path.toLowerCase().startsWith(basedirPath.toLowerCase())) {
            toReturn = path.substring(basedirPath.length() + 1);
        }

        // Handle whitespace in the argument.
        return toReturn;
    }

    /**
     * If the supplied fileOrDir is a File, it is added to the returned List if any of the filters Match.
     * If the supplied fileOrDir is a Directory, it is listed and any of the files immediately within the fileOrDir
     * directory are returned within the resulting List provided that they match any of the supplied filters.
     *
     * @param fileOrDir   A File or Directory.
     * @param fileFilters A List of filter of which at least one must match to add the File
     *                    (or child Files, in case of a directory) to the resulting List.
     * @param log         The active Maven Log
     * @return A List holding the supplied File (or child Files, in case fileOrDir is a Directory) given that at
     * least one Filter accepts them.
     */
    public static List<File> listFiles(final File fileOrDir,
                                       final List<Filter<File>> fileFilters,
                                       final Log log) {
        return listFiles(fileOrDir, fileFilters, false, log);
    }

    /**
     * If the supplied fileOrDir is a File, it is added to the returned List if any of the filters Match.
     * If the supplied fileOrDir is a Directory, it is listed and any of the files immediately within the fileOrDir
     * directory are returned within the resulting List provided that they match any of the supplied filters.
     *
     * @param fileOrDir              A File or Directory.
     * @param fileFilters            A List of filter of which at least one must match to add the File (or child Files, in case
     *                               of a directory) to the resulting List.
     * @param excludeFilterOperation if {@code true}, all fileFilters are considered exclude filter, i.e. if
     *                               any of the Filters match the fileOrDir, that fileOrDir will be excluded from the
     *                               result.
     * @param log                    The active Maven Log
     * @return A List holding the supplied File (or child Files, in case fileOrDir is a Directory) given that at
     * least one Filter accepts them.
     */
    public static List<File> listFiles(final File fileOrDir,
                                       final List<Filter<File>> fileFilters,
                                       final boolean excludeFilterOperation,
                                       final Log log) {

        // Check sanity
        Validate.notNull(log, "log");
        Validate.notNull(fileFilters, "fileFilters");
        final List<File> toReturn = new ArrayList<File>();

        if (EXISTING_FILE.accept(fileOrDir)) {
            checkAndAdd(toReturn, fileOrDir, fileFilters, excludeFilterOperation, log);
        } else if (EXISTING_DIRECTORY.accept(fileOrDir)) {

            final File[] listedFiles = fileOrDir.listFiles();
            if (listedFiles != null) {
                for (File current : listedFiles) {
                    checkAndAdd(toReturn, current, fileFilters, excludeFilterOperation, log);
                }
            }
        }

        // All done.
        return toReturn;
    }

    //
    // Private helpers
    //

    private static void checkAndAdd(final List<File> toPopulate,
                                    final File current,
                                    final List<Filter<File>> fileFilters,
                                    final boolean excludeFilterOperation,
                                    final Log log) {

        //
        // When no filters are supplied...
        // [Include Operation]: all files will be rejected
        // [Exclude Operation]: all files will be included
        //
        final boolean noFilters = fileFilters == null || fileFilters.isEmpty();
        final boolean addFile = excludeFilterOperation
                ? noFilters || Filters.rejectAtLeastOnce(current, fileFilters)
                : noFilters || Filters.matchAtLeastOnce(current, fileFilters);
        final String logPrefix = (addFile ? "Accepted " : "Rejected ")
                + (current.isDirectory() ? "directory" : "file") + " [";

        if (addFile) {
            toPopulate.add(current);
        }

        if (log.isDebugEnabled()) {
            log.debug(logPrefix + getCanonicalPath(current) + "]");
        }
    }

    private static void validateFileOrDirectoryName(final File fileOrDir) {

        if (Os.isFamily(Os.FAMILY_WINDOWS) && !FileUtils.isValidWindowsFileName(fileOrDir)) {
            throw new IllegalArgumentException(
                    "The file (" + fileOrDir + ") cannot contain any of the following characters: \n"
                            + StringUtils.join(INVALID_CHARACTERS_FOR_WINDOWS_FILE_NAME, " "));
        }
    }

    private static void recurseAndPopulate(final List<File> toPopulate,
                                           final List<Filter<File>> fileFilters,
                                           final File aDirectory,
                                           final boolean excludeOperation,
                                           final Log log) {

        final List<File> files = listFiles(aDirectory, fileFilters, excludeOperation, log);
        for (File current : files) {
            if (EXISTING_FILE.accept(current)) {
                toPopulate.add(current);
            }

            if (EXISTING_DIRECTORY.accept(current)) {
                recurseAndPopulate(toPopulate, fileFilters, current, excludeOperation, log);
            }
        }
    }
}
