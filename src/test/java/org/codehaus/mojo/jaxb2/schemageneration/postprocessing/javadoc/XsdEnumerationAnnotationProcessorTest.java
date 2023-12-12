package org.codehaus.mojo.jaxb2.schemageneration.postprocessing.javadoc;

import java.util.Arrays;
import java.util.List;

import org.codehaus.mojo.jaxb2.schemageneration.postprocessing.javadoc.enums.AmericanCoin;
import org.codehaus.mojo.jaxb2.schemageneration.postprocessing.javadoc.enums.ExampleEnumHolder;
import org.codehaus.mojo.jaxb2.schemageneration.postprocessing.javadoc.enums.FoodPreference;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class XsdEnumerationAnnotationProcessorTest extends AbstractSourceCodeAwareNodeProcessingTest {

    // Shared state
    private JavaDocRenderer renderer = new DefaultJavaDocRenderer();

    @Test
    public void validateProcessingNodesInVanillaXSD() throws Exception {

        // Assemble
        final String path = "testdata/schemageneration/javadoc/enums/expectedTransformedExampleEnumHolder.xsd";
        final String expected = readFully(path);

        final Document xsdGeneratedFromClassesInMethod = namespace2DocumentMap.get(SomewhatNamedPerson.NAMESPACE);
        final Node rootNode = xsdGeneratedFromClassesInMethod.getFirstChild();

        final XsdEnumerationAnnotationProcessor unitUnderTest = new XsdEnumerationAnnotationProcessor(docs, renderer);

        // Act
        process(rootNode, true, unitUnderTest);

        // Assert
        final String processed = printDocument(xsdGeneratedFromClassesInMethod);
        // System.out.println("Got: " + processed);

        Assert.assertTrue(compareXmlIgnoringWhitespace(expected, processed).identical());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<Class<?>> getJaxbAnnotatedClassesForJaxbContext() {
        return Arrays.<Class<?>>asList(ExampleEnumHolder.class, AmericanCoin.class, FoodPreference.class);
    }
}
