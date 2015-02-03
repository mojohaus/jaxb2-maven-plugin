package org.codehaus.mojo.jaxb2.schemageneration.postprocessing.schemaenhancement;

import org.codehaus.mojo.jaxb2.schemageneration.XsdGeneratorHelper;
import org.codehaus.mojo.jaxb2.schemageneration.postprocessing.DebugNodeProcessor;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.io.StringReader;
import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>
 */
public class ChangeNamespacePrefixProcessorTest
{
    @Test
    public void validateAcceptCriteria()
    {
        // Assemble
        final String oldNamespacePrefix = "oldNamespacePrefix";
        final String newNamespacePrefix = "newNamespacePrefix";
        final String namespaceURI = "http://another/namespace";
        final String xmlStream = getXmlDocumentSample( oldNamespacePrefix, namespaceURI );

        final ChangeNamespacePrefixProcessor unitUnderTest =
            new ChangeNamespacePrefixProcessor( oldNamespacePrefix, newNamespacePrefix );
        final DebugNodeProcessor debugNodeProcessor = new DebugNodeProcessor( unitUnderTest );

        // Act
        final Document document = XsdGeneratorHelper.parseXmlStream(new StringReader(xmlStream));
        XsdGeneratorHelper.process(document.getFirstChild(), true, debugNodeProcessor);

        // Assert
        final List<Node> acceptedNodes = debugNodeProcessor.getAcceptedNodes();
        Assert.assertEquals( 3, acceptedNodes.size() );

        // Note that the DebugNodeProcessor acquires the node *before* it is actually
        // processed - implying that the nodeName is not yet changed.
        Node namespaceDefinitionAttribute = acceptedNodes.get( 0 );
        Assert.assertEquals( "xmlns:" + oldNamespacePrefix, namespaceDefinitionAttribute.getNodeName() );
        Assert.assertEquals( namespaceURI, namespaceDefinitionAttribute.getNodeValue() );

        Node elementReferenceAttribute = acceptedNodes.get( 1 );
        Assert.assertEquals( "ref", elementReferenceAttribute.getNodeName() );
        Assert.assertEquals( newNamespacePrefix + ":aRequiredElementInAnotherNamespace",
                             elementReferenceAttribute.getNodeValue() );

        Node extensionAttribute = acceptedNodes.get( 2 );
        Assert.assertEquals( "base", extensionAttribute.getNodeName() );
        Assert.assertEquals( newNamespacePrefix + ":aBaseType", extensionAttribute.getNodeValue() );
    }

    //
    // Private helpers
    //

    private String getXmlDocumentSample( String namespacePrefix, String namespaceURI )
    {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
                + "<xs:schema version=\"1.0\"\n"
                + "           targetNamespace=\"http://some/namespace\"\n"
                + "           xmlns:" + namespacePrefix + "=\"" + namespaceURI + "\"\n"
                + "           xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">\n"
                + "\n"
                + "  <xs:import namespace=\"" + namespaceURI + "\" schemaLocation=\"anotherSchema.xsd\"/>\n"
                + "\n"
                + "  <xs:element name=\"anOptionalElementInSomeNamespace\" type=\"xs:string\"/>\n"
                + "\n"
                + "  <xs:complexType name=\"fooBar\">\n"
                + "    <xs:sequence>\n"
                + "      <xs:element name=\"requiredElement\" type=\"xs:string\" default=\"requiredElementValue\"/>\n"
                + "      <xs:element ref=\"" + namespacePrefix + ":aRequiredElementInAnotherNamespace\" />\n"
                + "      <xs:element name=\"optionalElement\" type=\"xs:string\" minOccurs=\"0\"/>\n"
                + "    </xs:sequence>\n"
                + "    <xs:attribute name=\"requiredAttribute\" type=\"xs:string\" use=\"required\"/>\n"
                + "    <xs:attribute name=\"optionalAttribute\" type=\"xs:string\"/>\n"
                + "  </xs:complexType>\n"
                + "\n"
                + "       <xs:complexType name=\"anExtendedComplexType\">\n"
                + "            <xs:complexContent>\n"
                + "                <xs:extension base=\"" + namespacePrefix + ":aBaseType\"/>\n"
                + "            </xs:complexContent>\n"
                + "        </xs:complexType>\n"
                + "</xs:schema>\n";
    }
}