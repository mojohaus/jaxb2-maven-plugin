package org.codehaus.mojo.jaxb2.shared.filters;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.mojo.jaxb2.BufferingLog;
import org.codehaus.mojo.jaxb2.shared.filters.pattern.PatternFileFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
class FiltersTest {

    // Shared data
    private File exclusionDir;
    private File anXmlFile;
    private File aTextFile;

    @BeforeEach
    void setupSharedState() {

        final String dirPath = "testdata/shared/filefilter/exclusion";
        final URL resource = getClass().getClassLoader().getResource(dirPath);
        exclusionDir = new File(resource.getPath());

        // Create files
        this.anXmlFile = new File(exclusionDir, "AnXmlFile.xml");
        this.aTextFile = new File(exclusionDir, "TextFile.txt");

        assertTrue(anXmlFile.exists() && anXmlFile.isFile());
        assertTrue(aTextFile.exists() && aTextFile.isFile());
    }

    @Test
    void validateMatchAtLeastOnce() {

        // Assemble
        final BufferingLog log = new BufferingLog(BufferingLog.LogLevel.DEBUG);
        final List<Filter<File>> excludeFilters = PatternFileFilter.createExcludeFilterList(log, "txt");
        final List<Filter<File>> includeFilters = PatternFileFilter.createIncludeFilterList(log, "txt");
        final List<Filter<File>> noFilters = new ArrayList<Filter<File>>();

        // Act & Assert
        assertFalse(Filters.matchAtLeastOnce(anXmlFile, noFilters));
        assertFalse(Filters.matchAtLeastOnce(aTextFile, noFilters));

        assertTrue(Filters.matchAtLeastOnce(anXmlFile, excludeFilters));
        assertFalse(Filters.matchAtLeastOnce(aTextFile, excludeFilters));

        assertFalse(Filters.matchAtLeastOnce(anXmlFile, includeFilters));
        assertTrue(Filters.matchAtLeastOnce(aTextFile, includeFilters));
    }

    @Test
    void validateNoMatches() {

        // Assemble
        final BufferingLog log = new BufferingLog(BufferingLog.LogLevel.DEBUG);
        final List<Filter<File>> excludeFilters = PatternFileFilter.createExcludeFilterList(log, "txt");
        final List<Filter<File>> includeFilters = PatternFileFilter.createIncludeFilterList(log, "txt");
        final List<Filter<File>> noFilters = new ArrayList<Filter<File>>();

        // Act & Assert
        assertTrue(Filters.noFilterMatches(anXmlFile, noFilters));
        assertTrue(Filters.noFilterMatches(aTextFile, noFilters));

        assertFalse(Filters.noFilterMatches(anXmlFile, excludeFilters));
        assertTrue(Filters.noFilterMatches(aTextFile, excludeFilters));

        assertTrue(Filters.noFilterMatches(anXmlFile, includeFilters));
        assertFalse(Filters.noFilterMatches(aTextFile, includeFilters));
    }

    @Test
    void validateRejectIfMatchedAtLeastOnce() {

        // Assemble
        final BufferingLog log = new BufferingLog(BufferingLog.LogLevel.DEBUG);
        final List<Filter<File>> excludeFilters = PatternFileFilter.createExcludeFilterList(log, "txt");
        final List<Filter<File>> includeFilters = PatternFileFilter.createIncludeFilterList(log, "txt");
        final List<Filter<File>> noFilters = new ArrayList<Filter<File>>();

        // Act & Assert
        assertFalse(Filters.rejectAtLeastOnce(anXmlFile, noFilters));
        assertFalse(Filters.rejectAtLeastOnce(aTextFile, noFilters));

        assertFalse(Filters.rejectAtLeastOnce(anXmlFile, excludeFilters));
        assertTrue(Filters.rejectAtLeastOnce(aTextFile, excludeFilters));

        assertTrue(Filters.rejectAtLeastOnce(anXmlFile, includeFilters));
        assertFalse(Filters.rejectAtLeastOnce(aTextFile, includeFilters));
    }
}
