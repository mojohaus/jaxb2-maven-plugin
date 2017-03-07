package org.codehaus.mojo.jaxb2.schemageneration.postprocessing.javadoc;

import org.codehaus.mojo.jaxb2.schemageneration.postprocessing.javadoc.wrappers.ExampleXmlWrapper;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class XsdAnnotationProcessorTest extends AbstractSourceCodeAwareNodeProcessingTest {

    // Shared state
    private JavaDocRenderer renderer = new DefaultJavaDocRenderer();

    @Test
    public void validateProcessingNodesInVanillaXSD() throws Exception {

        // Assemble
        final String path = "testdata/schemageneration/javadoc/expectedTransformedSomewhatNamedPerson.xml";
        final String expected = readFully(path);
        final Document document = namespace2DocumentMap.get(SomewhatNamedPerson.NAMESPACE);
        final Node rootNode = document.getFirstChild();

        final XsdAnnotationProcessor unitUnderTest = new XsdAnnotationProcessor(docs, renderer);

        // Act
        process(rootNode, true, unitUnderTest);

        // Assert
        final String processed = printDocument(document);
        // System.out.println("Got: " + processed);

        Assert.assertTrue(compareXmlIgnoringWhitespace(expected, processed).identical());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<Class<?>> getJaxbAnnotatedClassesForJaxbContext() {
        return Arrays.<Class<?>>asList(SomewhatNamedPerson.class, ExampleXmlWrapper.class);
    }
}
