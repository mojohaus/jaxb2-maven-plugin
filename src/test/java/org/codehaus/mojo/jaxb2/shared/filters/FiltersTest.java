package org.codehaus.mojo.jaxb2.shared.filters;

import org.codehaus.mojo.jaxb2.BufferingLog;
import org.codehaus.mojo.jaxb2.shared.filters.pattern.PatternFileFilter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class FiltersTest {

    // Shared data
    private File exclusionDir;
    private File anXmlFile;
    private File aTextFile;

    @Before
    public void setupSharedState() {

        final String dirPath = "testdata/shared/filefilter/exclusion";
        final URL resource = getClass().getClassLoader().getResource(dirPath);
        exclusionDir = new File(resource.getPath());

        // Create files
        this.anXmlFile = new File(exclusionDir, "AnXmlFile.xml");
        this.aTextFile = new File(exclusionDir, "TextFile.txt");

        Assert.assertTrue(anXmlFile.exists() && anXmlFile.isFile());
        Assert.assertTrue(aTextFile.exists() && aTextFile.isFile());
    }

    @Test
    public void validateMatchAtLeastOnce() {

        // Assemble
        final BufferingLog log = new BufferingLog(BufferingLog.LogLevel.DEBUG);
        final List<Filter<File>> excludeFilters = PatternFileFilter.createExcludeFilterList(log, "txt");
        final List<Filter<File>> includeFilters = PatternFileFilter.createIncludeFilterList(log, "txt");
        final List<Filter<File>> noFilters = new ArrayList<Filter<File>>();

        // Act & Assert
        Assert.assertFalse(Filters.matchAtLeastOnce(anXmlFile, noFilters));
        Assert.assertFalse(Filters.matchAtLeastOnce(aTextFile, noFilters));

        Assert.assertTrue(Filters.matchAtLeastOnce(anXmlFile, excludeFilters));
        Assert.assertFalse(Filters.matchAtLeastOnce(aTextFile, excludeFilters));

        Assert.assertFalse(Filters.matchAtLeastOnce(anXmlFile, includeFilters));
        Assert.assertTrue(Filters.matchAtLeastOnce(aTextFile, includeFilters));
    }

    @Test
    public void validateNoMatches() {

        // Assemble
        final BufferingLog log = new BufferingLog(BufferingLog.LogLevel.DEBUG);
        final List<Filter<File>> excludeFilters = PatternFileFilter.createExcludeFilterList(log, "txt");
        final List<Filter<File>> includeFilters = PatternFileFilter.createIncludeFilterList(log, "txt");
        final List<Filter<File>> noFilters = new ArrayList<Filter<File>>();

        // Act & Assert
        Assert.assertTrue(Filters.noFilterMatches(anXmlFile, noFilters));
        Assert.assertTrue(Filters.noFilterMatches(aTextFile, noFilters));

        Assert.assertFalse(Filters.noFilterMatches(anXmlFile, excludeFilters));
        Assert.assertTrue(Filters.noFilterMatches(aTextFile, excludeFilters));

        Assert.assertTrue(Filters.noFilterMatches(anXmlFile, includeFilters));
        Assert.assertFalse(Filters.noFilterMatches(aTextFile, includeFilters));
    }

    @Test
    public void validateRejectIfMatchedAtLeastOnce() {

        // Assemble
        final BufferingLog log = new BufferingLog(BufferingLog.LogLevel.DEBUG);
        final List<Filter<File>> excludeFilters = PatternFileFilter.createExcludeFilterList(log, "txt");
        final List<Filter<File>> includeFilters = PatternFileFilter.createIncludeFilterList(log, "txt");
        final List<Filter<File>> noFilters = new ArrayList<Filter<File>>();

        // Act & Assert
        Assert.assertFalse(Filters.rejectAtLeastOnce(anXmlFile, noFilters));
        Assert.assertFalse(Filters.rejectAtLeastOnce(aTextFile, noFilters));

        Assert.assertFalse(Filters.rejectAtLeastOnce(anXmlFile, excludeFilters));
        Assert.assertTrue(Filters.rejectAtLeastOnce(aTextFile, excludeFilters));

        Assert.assertTrue(Filters.rejectAtLeastOnce(anXmlFile, includeFilters));
        Assert.assertFalse(Filters.rejectAtLeastOnce(aTextFile, includeFilters));
    }
}
