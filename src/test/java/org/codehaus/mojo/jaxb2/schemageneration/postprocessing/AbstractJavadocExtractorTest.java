package org.codehaus.mojo.jaxb2.schemageneration.postprocessing;

import org.codehaus.mojo.jaxb2.BufferingLog;
import org.codehaus.mojo.jaxb2.schemageneration.XsdGeneratorHelper;
import org.codehaus.mojo.jaxb2.schemageneration.postprocessing.javadoc.JavaDocExtractor;
import org.codehaus.mojo.jaxb2.schemageneration.postprocessing.javadoc.SearchableDocumentation;
import org.codehaus.mojo.jaxb2.shared.FileSystemUtilities;
import org.codehaus.mojo.jaxb2.shared.Validate;
import org.codehaus.mojo.jaxb2.shared.filters.Filter;
import org.codehaus.mojo.jaxb2.shared.filters.pattern.PatternFileFilter;
import org.junit.Assert;
import org.junit.Before;
import org.w3c.dom.Document;
import se.jguru.shared.algorithms.api.resources.PropertyResources;

import java.io.File;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public abstract class AbstractJavadocExtractorTest {

    // Shared state
    protected List<File> sourceRootDirectories;
    protected BufferingLog log;
    protected JavaDocExtractor extractor;
    protected List<Filter<File>> javaSourceExcludeFilter;

    @Before
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
        Assert.assertNotNull("Effective resourcePath [" + resourcePath + "] could not be found.", resource);

        final File toAdd = new File(resource.getPath());
        final boolean exists = toAdd.exists();
        final boolean isDirectory = toAdd.isDirectory();

        Assert.assertTrue("Resource [" + toAdd.getAbsolutePath() + "] was nonexistent.", exists);
        Assert.assertTrue("Resource [" + toAdd.getAbsolutePath() + "] was not a directory.", isDirectory);

        sourceRootDirectories.add(toAdd);
    }

    protected SearchableDocumentation processSources() {

        // First, add all sources to the extractor
        for (File current : sourceRootDirectories) {

            final List<File> currentFiles = FileSystemUtilities.filterFiles(current,
                    null,
                    "",
                    log,
                    "JavaSources",
                    javaSourceExcludeFilter);

            // Add All source files found.
            extractor.addSourceFiles(currentFiles);
        }

        // All Done.
        return extractor.process();
    }

    protected Document createDocumentFrom(final String resource) {

        // Check sanity
        Validate.notEmpty(resource, "resource");

        // Read data, and convert into a Document.
        final String xmlData = PropertyResources.readFully(resource);
        return XsdGeneratorHelper.parseXmlStream(new StringReader(xmlData));
    }
}
