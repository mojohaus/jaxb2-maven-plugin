package org.codehaus.mojo.jaxb2.shared.filters.pattern;

import org.codehaus.mojo.jaxb2.BufferingLog;
import org.codehaus.mojo.jaxb2.shared.FileSystemUtilities;
import org.junit.Assert;
import org.junit.Before;

import java.io.File;
import java.net.URL;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public abstract class AbstractPatternFilterTest {

    // Shared state
    protected File exclusionDirectory;
    protected File[] fileList;
    protected BufferingLog log;
    private String defaultExclusionDirectory;

    public AbstractPatternFilterTest(final String defaultExclusionDirectory) {
        this.defaultExclusionDirectory = defaultExclusionDirectory;
    }

    public AbstractPatternFilterTest() {
        this("testdata/shared/filefilter/exclusion");
    }

    @Before
    @SuppressWarnings("all")
    public void setupSharedState() {

        log = new BufferingLog(BufferingLog.LogLevel.DEBUG);

        final URL exclusionDirUrl = getClass().getClassLoader().getResource(defaultExclusionDirectory);
        exclusionDirectory = new File(exclusionDirUrl.getPath());
        Assert.assertTrue(FileSystemUtilities.EXISTING_DIRECTORY.accept(exclusionDirectory));

        fileList = exclusionDirectory.listFiles();
        for (File current : fileList) {
            Assert.assertTrue(FileSystemUtilities.EXISTING_FILE.accept(current));
        }

        // Delegate
        onSetup();
    }

    protected void onSetup() {
        // Do nothing
    }

    protected <T> Map<String, Boolean> applyFilterAndRetrieveResults(final AbstractPatternFilter<T> unitUnderTest,
                                                                     final T[] candidates,
                                                                     final StringConverter<T> converter) {

        Map<String, Boolean> toReturn = new TreeMap<String, Boolean>();

        for (T current : candidates) {
            toReturn.put(converter.convert(current), unitUnderTest.accept(current));
        }

        return toReturn;
    }
}
