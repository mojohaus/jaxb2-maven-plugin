package org.codehaus.mojo.jaxb2.schemageneration.postprocessing.javadoc.enums;

import org.codehaus.mojo.jaxb2.schemageneration.postprocessing.javadoc.AbstractSourceCodeAwareNodeProcessingTest;
import org.junit.Test;
import se.jguru.nazgul.test.xmlbinding.XmlTestUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class XsdAnnotationProcessorAndEnumsTest extends AbstractSourceCodeAwareNodeProcessingTest {


    @Test
    public void validateGeneratedXmlForEnums() throws Exception {

        // Assemble
        final String expected = XmlTestUtils
                .readFully("testdata/schemageneration/javadoc/expectedRawExampleEnumHolder.xml");
        final ExampleEnumHolder exampleEnumHolder = new ExampleEnumHolder();
        exampleEnumHolder.getCoins().addAll(Arrays.asList(AmericanCoin.values()));
        exampleEnumHolder.getFoodPreferences().addAll(Arrays.asList(FoodPreference.values()));

        final StringWriter out = new StringWriter();

        // Act
        final JAXBContext context = JAXBContext.newInstance(FoodPreference.class, ExampleEnumHolder.class);
        final Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        marshaller.marshal(exampleEnumHolder, out);
        out.close();

        // Assert
        XmlTestUtils.compareXmlIgnoringWhitespace(expected, out.toString());
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    protected List<Class<?>> getJaxbAnnotatedClassesForJaxbContext() {

        final List<Class<?>> toReturn = new ArrayList<Class<?>>();
        for (Class<?> current : Arrays.asList(FoodPreference.class, ExampleEnumHolder.class, AmericanCoin.class)) {
            toReturn.add(current);
        }

        return toReturn;
    }
}
