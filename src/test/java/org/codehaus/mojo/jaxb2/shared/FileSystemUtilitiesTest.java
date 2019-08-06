package org.codehaus.mojo.jaxb2.shared;

import java.io.File;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.codehaus.mojo.jaxb2.AbstractJaxbMojo;
import org.codehaus.mojo.jaxb2.BufferingLog;
import org.codehaus.mojo.jaxb2.shared.filters.Filter;
import org.codehaus.mojo.jaxb2.shared.filters.Filters;
import org.codehaus.mojo.jaxb2.shared.filters.pattern.PatternFileFilter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.jguru.shared.algorithms.api.resources.PropertyResources;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 * @since 2.0
 */
public class FileSystemUtilitiesTest {

    // Shared state
    private File fsUtilitiesDirectory;
    private File canonicalsDirectory;
    private File testFile1;
    private File testFile2;
    private File srcTestResources;
    private BufferingLog log;

    @Before
    public void setupSharedState() {

        this.log = new BufferingLog(BufferingLog.LogLevel.DEBUG);

        final URL fsUtilitiesDirUrl = getClass().getClassLoader().getResource("testdata/shared/filesystemutilities");
        fsUtilitiesDirectory = new File(fsUtilitiesDirUrl.getPath());
        canonicalsDirectory = new File(fsUtilitiesDirectory, "canonicals");

        Assert.assertTrue(FileSystemUtilities.EXISTING_DIRECTORY.accept(fsUtilitiesDirectory));
        Assert.assertTrue(FileSystemUtilities.EXISTING_DIRECTORY.accept(canonicalsDirectory));

        testFile2 = new File(fsUtilitiesDirectory, "TestFile2.txt");
        testFile1 = new File(canonicalsDirectory, "TestFile1.txt");

        Assert.assertTrue(FileSystemUtilities.EXISTING_FILE.accept(testFile1));
        Assert.assertTrue(FileSystemUtilities.EXISTING_FILE.accept(testFile2));

        final URL testdataDir = getClass().getClassLoader().getResource("testdata");
        srcTestResources = new File(testdataDir.getPath()).getParentFile();
        Assert.assertTrue(FileSystemUtilities.EXISTING_DIRECTORY.accept(srcTestResources));
    }

    @Test
    public void validateGettingUrlForFile() {

        // Assemble
        final String relativePath = "testdata/shared/filesystemutilities/canonicals/TestFile1.txt";
        final URL expected = getClass().getClassLoader().getResource(relativePath);

        // Act
        final URL result = FileSystemUtilities.getUrlFor(testFile1);

        // Assert
        Assert.assertEquals(expected, result);
    }

    @Test
    public void validateCanonicalFileAndPath() {

        // Assemble
        final File unitUnderTest = new File(canonicalsDirectory, "../TestFile2.txt");

        // Act
        final String cPath1 = FileSystemUtilities.getCanonicalPath(unitUnderTest);
        final String cPath2 = FileSystemUtilities.getCanonicalPath(testFile2);

        final File cFile1 = FileSystemUtilities.getCanonicalFile(unitUnderTest);
        final File cFile2 = FileSystemUtilities.getCanonicalFile(testFile2);

        // Assert
        Assert.assertEquals(cPath1, cPath2);
        Assert.assertEquals(cFile1, cFile2);
    }

    @Test(expected = NullPointerException.class)
    public void validateExceptionOnGettingExistingFileWithRelativePathAndNullBasedir() {

        // Assemble
        final String relativePath = "testdata/shared/filesystemutilities/TestFile2.txt";
        final File incorrectNullBaseDir = null;

        // Act & Assert
        FileSystemUtilities.getExistingFile(relativePath, incorrectNullBaseDir);
    }

    @Test
    public void validateSimplifiedFileExistenceRetrieval() {

        // Assemble
        final String absolutePath = FileSystemUtilities.getCanonicalPath(testFile2);
        final String aNonExistentAbsolutePath = new File(canonicalsDirectory, "nonExistent.txt").getAbsolutePath();
        final String relativePath = "canonicals/TestFile1.txt";
        final String aNonExistentRelativePath = "canonicals/doesNotExist.pdf";

        // Act
        final File absoluteFile = FileSystemUtilities.getExistingFile(absolutePath, null);
        final File nonExistentAbsoluteFile = FileSystemUtilities.getExistingFile(aNonExistentAbsolutePath, null);

        final File relativeFile = FileSystemUtilities.getExistingFile(relativePath, fsUtilitiesDirectory);
        final File nonExistentRelativeFile =
                FileSystemUtilities.getExistingFile(aNonExistentRelativePath, fsUtilitiesDirectory);

        // Assert
        Assert.assertNotNull(absoluteFile);
        Assert.assertNull(nonExistentAbsoluteFile);
        Assert.assertNotNull(relativeFile);
        Assert.assertNull(nonExistentRelativeFile);
    }

    @Test(expected = NullPointerException.class)
    public void validateExceptionOnNullFileListWhenResolvingFilesAndRemovingExclusions() {

        // Assemble
        final List<File> incorrectNullFileList = null;

        // Act & Assert
        FileSystemUtilities.resolveRecursively(incorrectNullFileList, null, log);
    }

    @Test
    public void validateFilteredDirectoryListingUsingIncludeFilters() {

        // Assemble
        final URL aDirURL = getClass().getClassLoader().getResource("testdata/shared/filefilter/exclusion");
        final File dir = new File(aDirURL.getPath());

        final List<Filter<File>> textFiles = PatternFileFilter.createIncludeFilterList(log, "\\.txt");
        final List<Filter<File>> xmlFiles = PatternFileFilter.createIncludeFilterList(log, "\\.xml");
        final List<Filter<File>> allFiles = PatternFileFilter.createIncludeFilterList(log, "\\.*");

        // Act & Assert
        Assert.assertEquals(2, FileSystemUtilities.listFiles(dir, allFiles, log).size());

        final List<File> textFilesList = FileSystemUtilities.listFiles(dir, textFiles, log);
        Assert.assertEquals(1, textFilesList.size());
        Assert.assertEquals("TextFile.txt", textFilesList.get(0).getName());

        final List<File> xmlFilesList = FileSystemUtilities.listFiles(dir, xmlFiles, log);
        Assert.assertEquals(1, xmlFilesList.size());
        Assert.assertEquals("AnXmlFile.xml", xmlFilesList.get(0).getName());
    }

    @Test
    @SuppressWarnings("all")
    public void validateFilteredDirectoryListingUsingExcludeFilters() {

        // Assemble
        final URL aDirURL = getClass().getClassLoader().getResource("testdata/shared/filefilter/exclusion");
        final File exclusionDir = new File(aDirURL.getPath());

        final List<Filter<File>> noTextFiles = PatternFileFilter.createExcludeFilterList(log, "\\.txt");
        final List<Filter<File>> noXmlFiles = PatternFileFilter.createExcludeFilterList(log, "\\.xml");
        final List<Filter<File>> noFiles = PatternFileFilter.createExcludeFilterList(log, "\\.*");

        // Act & Assert
        Assert.assertEquals(
                0, FileSystemUtilities.listFiles(exclusionDir, noFiles, log).size());

        final List<File> noXmlFilesList = FileSystemUtilities.listFiles(exclusionDir, noXmlFiles, log);
        Assert.assertEquals(1, noXmlFilesList.size());
        Assert.assertEquals("TextFile.txt", noXmlFilesList.get(0).getName());

        final List<File> noTextFilesList = FileSystemUtilities.listFiles(exclusionDir, noTextFiles, log);
        Assert.assertEquals(1, noTextFilesList.size());
        Assert.assertEquals("AnXmlFile.xml", noTextFilesList.get(0).getName());
    }

    @Test
    public void validateResolveRecursively() {

        // Assemble
        final URL fileFilterDirUrl = getClass().getClassLoader().getResource("testdata/shared/filefilter");
        final File fileFilterDir = new File(fileFilterDirUrl.getPath());

        final List<File> fileList = new ArrayList<File>();
        fileList.add(fsUtilitiesDirectory);
        fileList.add(fileFilterDir);

        final List<Filter<File>> noTextFilesPattern = PatternFileFilter.createIncludeFilterList(log, "\\.txt");

        // Act
        final List<File> result = FileSystemUtilities.resolveRecursively(fileList, noTextFilesPattern, log);

        // Assert
        Assert.assertEquals(1, result.size());
        Assert.assertEquals("AnXmlFile.xml", result.get(0).getName());
    }

    @Test
    public void validateDefaultExclusionsIncludeDotDirectories() {

        // Assemble
        final URL fileFilterDirUrl = getClass().getClassLoader().getResource("testdata/shared/standard/exclusions");
        final File fileFilterDir = new File(fileFilterDirUrl.getPath());

        final List<File> fileList = new ArrayList<File>();
        fileList.add(fileFilterDir);

        final List<Filter<File>> standardExcludeFilters = AbstractJaxbMojo.STANDARD_EXCLUDE_FILTERS;
        Filters.initialize(log, standardExcludeFilters);

        // Act
        final List<File> result = FileSystemUtilities.resolveRecursively(fileList, standardExcludeFilters, log);

        // Assert
        final StringBuilder builder = new StringBuilder();
        for (File current : result) {
            builder.append(current.getPath() + ", ");
        }
        Assert.assertEquals(1, result.size());
        Assert.assertEquals("someFile.log", result.get(0).getName());
    }

    @Test
    public void validateResolvingFilesAndRemovingExclusions() {

        // Assemble
        final URL fileFilterDirUrl = getClass().getClassLoader().getResource("testdata/shared/filefilter");
        final File fileFilterDir = new File(fileFilterDirUrl.getPath());
        final File depsPropertiesFile = new File(fileFilterDir.getParentFile(), "deps1.properties");

        final List<File> fileList = new ArrayList<File>();
        fileList.add(fsUtilitiesDirectory);
        fileList.add(fileFilterDir);
        fileList.add(depsPropertiesFile);

        final List<Filter<File>> matchTextFiles = PatternFileFilter.createIncludeFilterList(log, "\\.txt");
        final List<Filter<File>> matchXmlFiles = PatternFileFilter.createIncludeFilterList(log, "\\.xml");
        final List<Filter<File>> matchNonexistentFiles =
                PatternFileFilter.createIncludeFilterList(log, "\\.nonexistent");

        // Act
        final List<File> noTextFiles = FileSystemUtilities.resolveRecursively(fileList, matchTextFiles, log);
        final List<File> noXmlFiles = FileSystemUtilities.resolveRecursively(fileList, matchXmlFiles, log);
        final List<File> allFiles = FileSystemUtilities.resolveRecursively(fileList, matchNonexistentFiles, log);
        final List<File> noFilterFiles = FileSystemUtilities.resolveRecursively(fileList, null, log);

        // Assert
        Assert.assertEquals(2, noTextFiles.size());
        Assert.assertEquals(4, noXmlFiles.size());
        Assert.assertEquals(5, allFiles.size());
        Assert.assertEquals(5, noFilterFiles.size());

        final List<String> canonicalPathsForExplicitlyAddedFiles =
                getRelativeCanonicalPaths(noFilterFiles, depsPropertiesFile.getParentFile());
        Assert.assertTrue(canonicalPathsForExplicitlyAddedFiles.contains("/deps1.properties"));

        final List<String> canonicalPathsForNoTextFiles =
                getRelativeCanonicalPaths(noTextFiles, depsPropertiesFile.getParentFile());
        Assert.assertTrue(canonicalPathsForNoTextFiles.contains("/deps1.properties"));
        Assert.assertTrue(canonicalPathsForNoTextFiles.contains("/filefilter/exclusion/AnXmlFile.xml"));

        final List<String> canonicalPathsForNoXmlFiles =
                getRelativeCanonicalPaths(noXmlFiles, depsPropertiesFile.getParentFile());
        Assert.assertTrue(canonicalPathsForNoXmlFiles.contains("/deps1.properties"));
        Assert.assertTrue(canonicalPathsForNoXmlFiles.contains("/filefilter/exclusion/TextFile.txt"));
        Assert.assertTrue(canonicalPathsForNoXmlFiles.contains("/filesystemutilities/TestFile2.txt"));
        Assert.assertTrue(canonicalPathsForNoXmlFiles.contains("/filesystemutilities/canonicals/TestFile1.txt"));

        final List<String> canonicalPathsForAllFiles =
                getRelativeCanonicalPaths(allFiles, depsPropertiesFile.getParentFile());
        Assert.assertTrue(canonicalPathsForAllFiles.contains("/deps1.properties"));
        Assert.assertTrue(canonicalPathsForAllFiles.contains("/filefilter/exclusion/TextFile.txt"));
        Assert.assertTrue(canonicalPathsForAllFiles.contains("/filesystemutilities/TestFile2.txt"));
        Assert.assertTrue(canonicalPathsForAllFiles.contains("/filesystemutilities/canonicals/TestFile1.txt"));
        Assert.assertTrue(canonicalPathsForAllFiles.contains("/filefilter/exclusion/AnXmlFile.xml"));
    }

    @Test
    public void validateBufferingLog() {

        // Assemble
        final BufferingLog log = new BufferingLog(BufferingLog.LogLevel.INFO);

        // Act
        log.debug("debug");
        log.info("info", new NullPointerException("Blah!"));
        log.warn("warn");
        log.error(new IllegalArgumentException("Gnat"));

        // Assert
        final SortedMap<String, Throwable> logBuffer = log.getLogBuffer();
        final List<String> keys = new ArrayList<String>(logBuffer.keySet());

        Assert.assertEquals(3, keys.size());

        Assert.assertEquals("000: (INFO) info", keys.get(0));
        Assert.assertEquals("001: (WARN) warn", keys.get(1));
        Assert.assertEquals("002: (ERROR) ", keys.get(2));

        Assert.assertEquals("Blah!", logBuffer.get(keys.get(0)).getMessage());
        Assert.assertNull(logBuffer.get(keys.get(1)));
        Assert.assertTrue(logBuffer.get(keys.get(2)) instanceof IllegalArgumentException);
    }

    @Test
    public void validateFilteringFiles() {

        // Assemble
        final BufferingLog log = new BufferingLog(BufferingLog.LogLevel.DEBUG);
        final URL sharedDirUrl = getClass().getClassLoader().getResource("testdata/shared");
        final File basedir = new File(sharedDirUrl.getPath());
        final List<Filter<File>> excludeTextFiles = PatternFileFilter.createIncludeFilterList(log, "txt");

        // Act
        final List<File> result = FileSystemUtilities.filterFiles(
                basedir, null, "filefilter/exclusion", log, "testFiles", excludeTextFiles);

        // Assert
        Assert.assertEquals(1, result.size());

        final File theFile = result.get(0);
        Assert.assertEquals("AnXmlFile.xml", theFile.getName());
    }

    @Test
    public void validateFilterFilesWithSuppliedSourcesAndExcludePatterns() {

        // Assemble
        final BufferingLog log = new BufferingLog(BufferingLog.LogLevel.DEBUG);
        final List<String> sources =
                Arrays.asList("filefilter/exclusion/AnXmlFile.xml", "filefilter/exclusion/TextFile.txt");
        final URL sharedDirUrl = getClass().getClassLoader().getResource("testdata/shared");
        final File basedir = new File(sharedDirUrl.getPath());
        final List<Filter<File>> excludeTextFiles = PatternFileFilter.createIncludeFilterList(log, "\\.txt");

        // Act
        final List<File> result = FileSystemUtilities.filterFiles(
                basedir, sources, "filefilter/exclusion", log, "testFiles", excludeTextFiles);

        // Assert
        Assert.assertEquals(1, result.size());

        final File theFile = result.get(0);
        Assert.assertEquals("AnXmlFile.xml", theFile.getName());
    }

    @Test
    public void validateGettingFileForJar() {

        // Assemble
        final String jarPath = "testdata/shared/nazgul-tools-validation-aspect-4.0.1.jar";
        final URL resource = getClass().getClassLoader().getResource(jarPath);
        Assert.assertNotNull(resource);

        // Act
        final File jarFile = FileSystemUtilities.getFileFor(resource, "UTF-8");
        final String relativized = FileSystemUtilities.relativize(jarFile.getPath(), srcTestResources, true);

        // Assert
        Assert.assertTrue(jarFile.exists());
        Assert.assertTrue(jarFile.isFile());
        Assert.assertEquals(jarPath.replace("/", File.separator), relativized);
    }

    @Test
    public void validateGettingFileForClassURL() {

        // Assemble
        final URL streamingDhURL =
                PropertyResources.class.getProtectionDomain().getCodeSource().getLocation();
        Assert.assertNotNull(streamingDhURL);

        // Act
        final File jarFile = FileSystemUtilities.getFileFor(streamingDhURL, "UTF-8");

        // Assert
        Assert.assertTrue(jarFile.exists());
        Assert.assertTrue(jarFile.isFile());
    }

    @Test
    public void validateGettingFileForClassResourceURL() {

        // Assemble
        final String classResource = PropertyResources.class.getName().replace(".", "/") + ".class";
        final URL resource = getClass().getClassLoader().getResource(classResource);
        Assert.assertNotNull(resource);

        // Act
        final File jarFile = FileSystemUtilities.getFileFor(resource, "UTF-8");

        // Assert
        Assert.assertTrue(jarFile.exists());
        Assert.assertTrue(jarFile.isFile());
    }

    @Test
    public void validateUrlEncodingAndDecoding() throws Exception {

        // Assemble
        final String resourcePath = "testdata/shared/urlhandling/file with spaces.txt";
        final URL dirUrl = getClass().getClassLoader().getResource(resourcePath);
        Assert.assertNotNull(dirUrl);

        // Act
        final String normalizedPath = dirUrl.toURI().normalize().toURL().getPath();
        final String decoded = URLDecoder.decode(normalizedPath, "UTF-8");

        // Assert
        Assert.assertTrue(normalizedPath.endsWith("file%20with%20spaces.txt"));
        Assert.assertTrue(decoded.endsWith("file with spaces.txt"));
    }

    @Test
    public void validateRelativizingPaths() throws Exception {

        // Assemble
        final String path = "/project/backend/foobar/my-schema.xsd";
        final SortedMap<String, String> parentDir2Expected = new TreeMap<>();
        parentDir2Expected.put("/", "project/backend/foobar/my-schema.xsd");
        parentDir2Expected.put("", "/project/backend/foobar/my-schema.xsd");
        parentDir2Expected.put("/project", "backend/foobar/my-schema.xsd");
        parentDir2Expected.put("/not/a/path", "/project/backend/foobar/my-schema.xsd");
        parentDir2Expected.put("/project/", "backend/foobar/my-schema.xsd");

        // Act & Assert
        for (Map.Entry<String, String> current : parentDir2Expected.entrySet()) {

            final String expectedMessage = "Given parent dir [" + current.getKey() + "], expected ["
                    + current.getValue() + "] from [" + path + "] but found: ";

            final String result = FileSystemUtilities.relativize(path, new File(current.getKey()), true);

            Assert.assertEquals(expectedMessage + result, current.getValue(), result);
        }
    }

    //
    // Private helpers
    //

    private List<String> getRelativeCanonicalPaths(final List<File> fileList, final File cutoff) {

        final String cutoffPath = FileSystemUtilities.getCanonicalPath(cutoff).replace(File.separator, "/");
        final List<String> toReturn = new ArrayList<String>();

        for (File current : fileList) {

            final String canPath = FileSystemUtilities.getCanonicalPath(current).replace(File.separator, "/");
            if (!canPath.startsWith(cutoffPath)) {
                throw new IllegalArgumentException("Illegal cutoff provided. Cutoff: [" + cutoffPath
                        + "] must be a parent to CanonicalPath [" + canPath + "]");
            }

            toReturn.add(canPath.substring(cutoffPath.length()));
        }
        Collections.sort(toReturn);

        return toReturn;
    }
}
