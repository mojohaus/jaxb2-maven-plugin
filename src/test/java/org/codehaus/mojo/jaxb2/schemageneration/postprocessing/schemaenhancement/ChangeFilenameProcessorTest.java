package org.codehaus.mojo.jaxb2.schemageneration.postprocessing.schemaenhancement;

import org.codehaus.mojo.jaxb2.schemageneration.XsdGeneratorHelper;
import org.codehaus.mojo.jaxb2.schemageneration.postprocessing.DebugNodeProcessor;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a> */
public class ChangeFilenameProcessorTest
{
    @Test
    public void validateAcceptCriteria()
    {
        // Assemble
        final String oldFileName = "foo";
        final String newFileName = "bar";
        final String xmlStream = getXmlDocumentSample( oldFileName );
        final String namespaceToBeRelocated = "http://another/namespace";

        final Map<String, String> namespaceUriToNewFilenameMap = new TreeMap<String, String>();
        namespaceUriToNewFilenameMap.put( namespaceToBeRelocated, newFileName );

        final ChangeFilenameProcessor unitUnderTest = new ChangeFilenameProcessor( namespaceUriToNewFilenameMap );
        final DebugNodeProcessor debugNodeProcessor = new DebugNodeProcessor( unitUnderTest );

        // Act
        final Document document = XsdGeneratorHelper.parseXmlStream(new StringReader(xmlStream));
        XsdGeneratorHelper.process(document.getFirstChild(), true, debugNodeProcessor);

        // Assert
        final List<Node> acceptedNodes = debugNodeProcessor.getAcceptedNodes();
        Assert.assertEquals( 1, acceptedNodes.size() );
        Assert.assertEquals( "schemaLocation", acceptedNodes.get( 0 ).getNodeName() );
        Assert.assertEquals( newFileName, acceptedNodes.get( 0 ).getNodeValue() );
    }

    //
    // Private helpers
    //

    private String getXmlDocumentSample( String fileName )
    {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
                + "<xs:schema version=\"1.0\"\n"
                + "           targetNamespace=\"http://some/namespace\"\n"
                + "           xmlns:another=\"http://another/namespace\"\n"
                + "           xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">\n"
                + "\n"
                + "  <xs:import namespace=\"http://another/namespace\" schemaLocation=\"" + fileName + "\"/>\n"
                + "  <xs:element name=\"anOptionalElementInSomeNamespace\" type=\"xs:string\"/>\n"
                + "\n"
                + "  <xs:complexType name=\"fooBar\">\n"
                + "    <xs:sequence>\n"
                + "      <xs:element name=\"requiredElement\" type=\"xs:string\" default=\"requiredElementValue\"/>\n"
                + "      <xs:element ref=\"another:aRequiredElementInAnotherNamespace\" />\n"
                + "      <xs:element name=\"optionalElement\" type=\"xs:string\" minOccurs=\"0\"/>\n"
                + "    </xs:sequence>\n"
                + "    <xs:attribute name=\"requiredAttribute\" type=\"xs:string\" use=\"required\"/>\n"
                + "    <xs:attribute name=\"optionalAttribute\" type=\"xs:string\"/>\n"
                + "  </xs:complexType>\n"
                + "</xs:schema>\n";
    }
}