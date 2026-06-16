package org.codehaus.mojo.jaxb2.schemageneration.postprocessing.schemaenhancement;

import java.io.StringReader;
import java.util.List;

import org.codehaus.mojo.jaxb2.schemageneration.XsdGeneratorHelper;
import org.codehaus.mojo.jaxb2.schemageneration.postprocessing.DebugNodeProcessor;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>
 */
class ChangeNamespacePrefixProcessorTest {
    @Test
    void validateAcceptCriteria() {
        // Assemble
        final String oldNamespacePrefix = "oldNamespacePrefix";
        final String newNamespacePrefix = "newNamespacePrefix";
        final String namespaceURI = "http://another/namespace";
        final String xmlStream = getXmlDocumentSample(oldNamespacePrefix, namespaceURI);

        final ChangeNamespacePrefixProcessor unitUnderTest =
                new ChangeNamespacePrefixProcessor(oldNamespacePrefix, newNamespacePrefix);
        final DebugNodeProcessor debugNodeProcessor = new DebugNodeProcessor(unitUnderTest);

        // Act
        final Document document = XsdGeneratorHelper.parseXmlStream(new StringReader(xmlStream));
        XsdGeneratorHelper.process(document.getFirstChild(), true, debugNodeProcessor);

        // Assert
        final List<Node> acceptedNodes = debugNodeProcessor.getAcceptedNodes();
        assertEquals(3, acceptedNodes.size());

        // Note that the DebugNodeProcessor acquires the node *before* it is actually
        // processed - implying that the nodeName is not yet changed.
        Node namespaceDefinitionAttribute = acceptedNodes.get(0);
        assertEquals("xmlns:" + oldNamespacePrefix, namespaceDefinitionAttribute.getNodeName());
        assertEquals(namespaceURI, namespaceDefinitionAttribute.getNodeValue());

        Node elementReferenceAttribute = acceptedNodes.get(1);
        assertEquals("ref", elementReferenceAttribute.getNodeName());
        assertEquals(
                newNamespacePrefix + ":aRequiredElementInAnotherNamespace", elementReferenceAttribute.getNodeValue());

        Node extensionAttribute = acceptedNodes.get(2);
        assertEquals("base", extensionAttribute.getNodeName());
        assertEquals(newNamespacePrefix + ":aBaseType", extensionAttribute.getNodeValue());
    }

    @Test
    void validateElementReferenceWithoutPrefixIsPrefixedForTns() {
        // Assemble
        final String oldNamespacePrefix = "tns";
        final String newNamespacePrefix = "newTns";
        final String xmlStream =
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + "<xs:schema version=\"1.0\"\n"
                        + "           targetNamespace=\"http://some/namespace\"\n"
                        + "           xmlns:tns=\"http://some/namespace\"\n"
                        + "           xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">\n"
                        + "  <xs:element name=\"enumElement\" type=\"xs:string\"/>\n"
                        + "  <xs:element ref=\"EnumType\" />\n"
                        + // ref without prefix
                        "</xs:schema>\n";

        final ChangeNamespacePrefixProcessor unitUnderTest =
                new ChangeNamespacePrefixProcessor(oldNamespacePrefix, newNamespacePrefix);
        final DebugNodeProcessor debugNodeProcessor = new DebugNodeProcessor(unitUnderTest);

        // Act
        final Document document = XsdGeneratorHelper.parseXmlStream(new StringReader(xmlStream));
        XsdGeneratorHelper.process(document.getFirstChild(), true, debugNodeProcessor);

        // Assert
        // Find the ref attribute and check its value
        boolean found = false;
        for (Node node : debugNodeProcessor.getAcceptedNodes()) {
            if ("ref".equals(node.getNodeName())) {
                found = true;
                assertEquals(newNamespacePrefix + ":EnumType", node.getNodeValue());
            }
        }
        assertTrue(found, "Should have found a ref attribute without prefix");
    }

    @Test
    void validateElementReferenceWithoutPrefixIsNotPrefixedForNonTns() {
        // Assemble
        final String oldNamespacePrefix = "foo";
        final String newNamespacePrefix = "bar";
        final String xmlStream =
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + "<xs:schema version=\"1.0\"\n"
                        + "           targetNamespace=\"http://some/namespace\"\n"
                        + "           xmlns:foo=\"http://some/namespace\"\n"
                        + "           xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">\n"
                        + "  <xs:element name=\"enumElement\" type=\"xs:string\"/>\n"
                        + "  <xs:element ref=\"EnumType\" />\n"
                        + // ref without prefix
                        "</xs:schema>\n";

        final ChangeNamespacePrefixProcessor unitUnderTest =
                new ChangeNamespacePrefixProcessor(oldNamespacePrefix, newNamespacePrefix);
        final DebugNodeProcessor debugNodeProcessor = new DebugNodeProcessor(unitUnderTest);

        // Act
        final Document document = XsdGeneratorHelper.parseXmlStream(new StringReader(xmlStream));
        XsdGeneratorHelper.process(document.getFirstChild(), true, debugNodeProcessor);

        // Assert
        // Find the ref attribute and check its value remains unchanged
        boolean found = false;
        for (Node node : debugNodeProcessor.getAcceptedNodes()) {
            if ("ref".equals(node.getNodeName())) {
                found = true;
                assertEquals("EnumType", node.getNodeValue());
            }
        }
        assertTrue(found, "Should have found a ref attribute without prefix");
    }

    //
    // Private helpers
    //

    private String getXmlDocumentSample(String namespacePrefix, String namespaceURI) {
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
