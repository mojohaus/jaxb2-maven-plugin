package org.codehaus.mojo.jaxb2.schemageneration.postprocessing.javadoc.enums;

import org.codehaus.mojo.jaxb2.schemageneration.postprocessing.javadoc.AbstractSourceCodeAwareNodeProcessingTest;
import org.codehaus.mojo.jaxb2.schemageneration.postprocessing.javadoc.JavaDocRenderer;
import org.codehaus.mojo.jaxb2.schemageneration.postprocessing.javadoc.NoAuthorJavaDocRenderer;
import org.codehaus.mojo.jaxb2.schemageneration.postprocessing.javadoc.SomewhatNamedPerson;
import org.codehaus.mojo.jaxb2.schemageneration.postprocessing.javadoc.XsdAnnotationProcessor;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.Diff;
import se.jguru.shared.algorithms.api.resources.PropertyResources;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class XsdAnnotationProcessorAndEnumsTest extends AbstractSourceCodeAwareNodeProcessingTest
{

    // Shared state
    private JavaDocRenderer renderer = new NoAuthorJavaDocRenderer();

    @Test
    public void validateGeneratedXmlForEnums() throws Exception
    {

        // Assemble
        final String expected = PropertyResources.readFully(
                "testdata/schemageneration/javadoc/expectedRawExampleEnumHolder.xml" );
        final ExampleEnumHolder exampleEnumHolder = new ExampleEnumHolder();
        exampleEnumHolder.getCoins().addAll( Arrays.asList( AmericanCoin.values() ) );
        exampleEnumHolder.getFoodPreferences().addAll( Arrays.asList( FoodPreference.values() ) );

        final StringWriter out = new StringWriter();

        // Act
        final JAXBContext context = JAXBContext.newInstance( FoodPreference.class, ExampleEnumHolder.class );
        final Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, true );
        marshaller.setProperty( Marshaller.JAXB_ENCODING, "UTF-8" );
        marshaller.marshal( exampleEnumHolder, out );
        out.close();

        // Assert
        final Diff diff = DiffBuilder.compare( expected ).withTest(
                out.toString() ).ignoreWhitespace().ignoreComments().build();
        Assert.assertFalse( diff.hasDifferences() );
        // XmlTestUtils.compareXmlIgnoringWhitespace( expected, out.toString() );
    }

    @Test
    public void validateHandlingXmlElementWrapperDocumentation() throws Exception
    {

        // Assmeble
        final Document document = namespace2DocumentMap.get( SomewhatNamedPerson.NAMESPACE );
        final Node rootNode = document.getFirstChild();

        final XsdAnnotationProcessor unitUnderTest = new XsdAnnotationProcessor( docs, renderer );

        // Act
        process( rootNode, true, unitUnderTest );

        // Assert
        final String processed = printDocument( document );
        System.out.println( "Got: " + processed );


        // Act

        // Assert
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings( "unchecked" )
    @Override
    protected List<Class<?>> getJaxbAnnotatedClassesForJaxbContext()
    {

        final List<Class<?>> toReturn = new ArrayList<Class<?>>();
        for ( Class<?> current : Arrays.asList( FoodPreference.class, ExampleEnumHolder.class, AmericanCoin.class ) )
        {
            toReturn.add( current );
        }

        return toReturn;
    }
}
