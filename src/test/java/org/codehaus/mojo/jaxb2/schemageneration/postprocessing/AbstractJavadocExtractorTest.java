package org.codehaus.mojo.jaxb2.schemageneration.postprocessing;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.codehaus.mojo.jaxb2.BufferingLog;
import org.codehaus.mojo.jaxb2.PropertyResources;
import org.codehaus.mojo.jaxb2.schemageneration.XsdGeneratorHelper;
import org.codehaus.mojo.jaxb2.schemageneration.postprocessing.javadoc.JavaDocExtractor;
import org.codehaus.mojo.jaxb2.schemageneration.postprocessing.javadoc.SearchableDocumentation;
import org.codehaus.mojo.jaxb2.shared.FileSystemUtilities;
import org.codehaus.mojo.jaxb2.shared.Validate;
import org.codehaus.mojo.jaxb2.shared.filters.Filter;
import org.codehaus.mojo.jaxb2.shared.filters.pattern.PatternFileFilter;
import org.junit.jupiter.api.BeforeEach;
import org.w3c.dom.Document;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public abstract class AbstractJavadocExtractorTest {

    // Shared state
    protected List<File> sourceRootDirectories;
    protected BufferingLog log;
    protected JavaDocExtractor extractor;
    protected List<Filter<File>> javaSourceExcludeFilter;

    @BeforeEach
    public void setupSharedState() {

        sourceRootDirectories = new ArrayList<File>();
        log = new BufferingLog();
        extractor = new JavaDocExtractor(log);

        javaSourceExcludeFilter = new ArrayList<Filter<File>>();
        javaSourceExcludeFilter.add(new PatternFileFilter(Collections.singletonList("\\.java"), false));
    }

    /**
     * Adds a source root directory.
     *
     * @param resourcePath A non-null resource path.
     */
    protected void addSourceRootDirectory(final String resourcePath) {

        // Check sanity
        Validate.notEmpty(resourcePath, "resourcePath");

        final String effectiveResourcePath = resourcePath.charAt(0) == '/' ? resourcePath : "/" + resourcePath;
        final URL resource = Thread.currentThread().getContextClassLoader().getResource(effectiveResourcePath);
        assertNotNull(resource, "Effective resourcePath [" + resourcePath + "] could not be found.");

        final File toAdd = new File(resource.getPath());
        final boolean exists = toAdd.exists();
        final boolean isDirectory = toAdd.isDirectory();

        assertTrue(exists, "Resource [" + toAdd.getAbsolutePath() + "] was nonexistent.");
        assertTrue(isDirectory, "Resource [" + toAdd.getAbsolutePath() + "] was not a directory.");

        sourceRootDirectories.add(toAdd);
    }

    protected SearchableDocumentation processSources() {

        // First, add all sources to the extractor
        for (File current : sourceRootDirectories) {

            final List<File> currentFiles =
                    FileSystemUtilities.filterFiles(current, null, "", log, "JavaSources", javaSourceExcludeFilter);

            // Add All source files found.
            extractor.addSourceFiles(currentFiles);
        }

        // All Done.
        return extractor.process();
    }

    protected Document createDocumentFrom(final String resource) throws IOException {

        // Check sanity
        Validate.notEmpty(resource, "resource");

        // Read data, and convert into a Document.
        final String xmlData = PropertyResources.readFully(resource);
        return XsdGeneratorHelper.parseXmlStream(new StringReader(xmlData));
    }
}
