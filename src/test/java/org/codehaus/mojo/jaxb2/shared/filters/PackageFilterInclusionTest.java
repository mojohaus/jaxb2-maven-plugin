package org.codehaus.mojo.jaxb2.shared.filters;

import org.codehaus.mojo.jaxb2.BufferingLog;
import org.codehaus.mojo.jaxb2.shared.FileSystemUtilities;
import org.codehaus.mojo.jaxb2.shared.filters.pattern.FileFilterAdapter;
import org.codehaus.mojo.jaxb2.shared.filters.pattern.PatternFileFilter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import static java.io.File.separator;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class PackageFilterInclusionTest {

    // Shared state
    private File baseDirectory;
    private File srcMainJavaDir;
    private BufferingLog log;
    private String contextRoot;

    @Before
    public void setupSharedState() {

        log = new BufferingLog(BufferingLog.LogLevel.DEBUG);

        String basedir = System.getProperty("basedir");
        if (basedir == null) {
            baseDirectory = new File(getClass().getClassLoader().getResource("logback-test.xml").getPath())
                    .getParentFile()
                    .getParentFile()
                    .getParentFile();
        } else {
            baseDirectory = new File(basedir);
        }
        Assert.assertTrue(baseDirectory.exists() && baseDirectory.isDirectory());

        // Find all source files under the src/main/java directory.
        srcMainJavaDir = new File(basedir, "src/main/java");
        Assert.assertTrue(srcMainJavaDir.exists() && srcMainJavaDir.isDirectory());

        contextRoot = FileSystemUtilities.relativize(srcMainJavaDir.getPath(), baseDirectory, true);
    }

    @Test
    public void validateExcludingPackageInfoFiles() {

        // Assemble
        final String rootPackagePath = contextRoot + separator + "org" + separator + "codehaus" + separator + "mojo"
                + separator + "jaxb2";
        final String excludeFilenamePattern = "package-info\\.java";
        final List<Filter<File>> excludedFilesIdentifierFilter = PatternFileFilter
                .createIncludeFilterList(log, excludeFilenamePattern);

        // Act
        final List<File> sourceFiles = FileSystemUtilities.resolveRecursively(Collections.singletonList(srcMainJavaDir),
                excludedFilesIdentifierFilter,
                log);
        final SortedMap<String, File> path2FileMap = mapFiles(sourceFiles);

        // Assert
        for (String current : path2FileMap.keySet()) {

            final String relativePath = current.startsWith(baseDirectory.getPath())
                    ? current.substring(baseDirectory.getPath().length() + 1)
                    : current;

            Assert.assertTrue("Path " + relativePath + " did not start with the root package path " + rootPackagePath,
                    relativePath.startsWith(rootPackagePath));
            Assert.assertTrue("Path " + current + " was a package-info.java file.",
                    !current.contains("package-info"));
        }
    }

    @Test
    public void validateIncludingSubTrees() {

        // Assemble
        final String locationPackageDirName = separator + "location" + separator;
        final FileFilterAdapter includeFilter = new FileFilterAdapter(new FileFilter() {
            @Override
            public boolean accept(final File pathname) {
                return pathname.getPath().contains(locationPackageDirName);
            }
        });
        includeFilter.initialize(log);

        final List<File> allSourceFiles = FileSystemUtilities.resolveRecursively(
                Collections.singletonList(srcMainJavaDir),
                new ArrayList<Filter<File>>(),
                log);

        // Act
        final List<File> result = FileSystemUtilities.filterFiles(allSourceFiles, includeFilter, log);
        final SortedMap<String, File> path2FileMap = mapFiles(result);

        // Assert
        Assert.assertTrue(result.size() > 1);
        for (String current : path2FileMap.keySet()) {
            Assert.assertTrue("Path " + current + " contained disallowed pattern " + locationPackageDirName,
                    current.contains(locationPackageDirName));
        }
    }

    //
    // Private helpers
    //

    private SortedMap<String, File> mapFiles(final List<File> files) {

        final SortedMap<String, File> toReturn = new TreeMap<String, File>();
        for (File current : files) {
            toReturn.put(current.getPath(), current);
        }

        return toReturn;
    }
}
