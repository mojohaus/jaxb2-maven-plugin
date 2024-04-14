package org.codehaus.mojo.jaxb2.shared.filters.pattern;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class PatternFileFilterTest extends AbstractPatternFilterTest {

    // Shared state
    public static final StringConverter<File> FILENAME_CONVERTER = new StringConverter<File>() {
        @Override
        public String convert(final File toConvert) {
            return toConvert.getName();
        }
    };

    /*
     * fileList contains 2 files:
     *
     * testdata/shared/filefilter/exclusion/ATextFile.txt
     * testdata/shared/filefilter/exclusion/AnXmlFile.xml
     */

    @Test
    void validateAcceptNothingOnNullPatterns() {

        // Assemble
        final PatternFileFilter unitUnderTest = new PatternFileFilter(null, true);

        // Act
        unitUnderTest.initialize(log);
        final Map<String, Boolean> result = applyFilterAndRetrieveResults(unitUnderTest, fileList, FILENAME_CONVERTER);

        // Assert
        for (Map.Entry<String, Boolean> current : result.entrySet()) {
            assertFalse(current.getValue());
        }
    }

    @Test
    void validateAcceptAllOnNullPatternsForExcludeOperation() {

        // Assemble
        final PatternFileFilter unitUnderTest = new PatternFileFilter(false, null, null, FILENAME_CONVERTER, false);

        // Act
        unitUnderTest.initialize(log);
        final Map<String, Boolean> result = applyFilterAndRetrieveResults(unitUnderTest, fileList, FILENAME_CONVERTER);

        // Assert
        for (Map.Entry<String, Boolean> current : result.entrySet()) {
            assertTrue(current.getValue());
        }
    }

    @Test
    void validateExcludingMatchingFiles() {

        // Assemble
        final List<String> txtFileSuffixExclusion = Arrays.asList("\\.txt");
        final PatternFileFilter unitUnderTest = new PatternFileFilter(txtFileSuffixExclusion, false);
        unitUnderTest.initialize(log);

        // Act
        final Map<String, Boolean> result = applyFilterAndRetrieveResults(unitUnderTest, fileList, FILENAME_CONVERTER);

        // Assert
        assertTrue(result.get("AnXmlFile.xml"));
        assertFalse(result.get("TextFile.txt"));
    }

    @Test
    void validateExcludingNoFilesOnNonMatchingPattern() {

        // Assemble
        final PatternFileFilter unitUnderTest = new PatternFileFilter(Arrays.asList("\\.nonexistent"), false);
        unitUnderTest.initialize(log);

        // Act
        final Map<String, Boolean> result = applyFilterAndRetrieveResults(unitUnderTest, fileList, FILENAME_CONVERTER);

        // Assert
        assertTrue(result.get("AnXmlFile.xml"));
        assertTrue(result.get("TextFile.txt"));
    }

    @Test
    void validatePatternMatchExclusion() {

        // Assemble
        final PatternFileFilter unitUnderTest = new PatternFileFilter(Arrays.asList("\\.*File\\..*"), false);
        unitUnderTest.initialize(log);

        // Act
        final Map<String, Boolean> result = applyFilterAndRetrieveResults(unitUnderTest, fileList, FILENAME_CONVERTER);

        // Assert
        assertFalse(result.get("AnXmlFile.xml"));
        assertFalse(result.get("TextFile.txt"));
    }
}
