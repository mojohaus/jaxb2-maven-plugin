package org.codehaus.mojo.jaxb2.schemageneration.postprocessing.javadoc.enums;

import org.codehaus.mojo.jaxb2.schemageneration.postprocessing.javadoc.AbstractSourceCodeAwareNodeProcessingTest;
import org.codehaus.mojo.jaxb2.schemageneration.postprocessing.javadoc.DefaultJavaDocRenderer;
import org.codehaus.mojo.jaxb2.schemageneration.postprocessing.javadoc.JavaDocRenderer;
import org.codehaus.mojo.jaxb2.schemageneration.postprocessing.javadoc.XsdAnnotationProcessor;
import org.junit.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class XsdAnnotationProcessorAndEnumsTest extends AbstractSourceCodeAwareNodeProcessingTest {

    // Shared state
    private JavaDocRenderer renderer = new DefaultJavaDocRenderer();

    @Test
    public void createXsdForEnums() throws Exception {

        // Assemble
        final ExampleEnumHolder exampleEnumHolder = new ExampleEnumHolder();
        exampleEnumHolder.getCoins().addAll(Arrays.asList(AmericanCoin.values()));
        exampleEnumHolder.getFoodPreferences().addAll(Arrays.asList(FoodPreference.values()));

        final StringWriter out = new StringWriter();

        final JAXBContext context = JAXBContext.newInstance(FoodPreference.class, ExampleEnumHolder.class);
        final Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        marshaller.marshal(exampleEnumHolder, out);
        out.close();

        System.out.println("Got: " + out.toString());

        // Act

        // Assert
    }

    @Test
    public void validateProcessingNodesInVanillaXSD() throws Exception {

        // Assemble
        /*
        final String path = "testdata/schemageneration/javadoc/expectedSemiDocumentedClass.xml";
        final String expected = readFully(path);
        final Document document = namespace2DocumentMap.get(SomewhatNamedPerson.NAMESPACE);
        final Node rootNode = document.getFirstChild();
        */

        final XsdAnnotationProcessor unitUnderTest = new XsdAnnotationProcessor(docs, renderer);

        // Act
        // process(rootNode, true, unitUnderTest);

        // Assert
        // final String processed = printDocument(document);
        // System.out.println("Got: " + processed);

        // Assert.assertTrue(compareXmlIgnoringWhitespace(expected, processed).identical());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<Class<?>> getJaxbAnnotatedClassesForJaxbContext() {
        return Collections.<Class<?>>singletonList(FoodPreference.class);
    }
}
