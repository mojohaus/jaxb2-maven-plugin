package org.codehaus.mojo.jaxb2.schemageneration.postprocessing.javadoc;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.codehaus.mojo.jaxb2.PropertyResources;
import org.codehaus.mojo.jaxb2.schemageneration.XsdGeneratorHelper;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
class DomHelperTest {

    @Test
    void validateDomHelperAccessors() throws Exception {

        // Assemble
        final String xsd = PropertyResources.readFully("testdata/schemageneration/javadoc/enums/rawEnumSchema.xsd");
        final Document document = XsdGeneratorHelper.parseXmlStream(new StringReader(xsd));

        final Element schemaElement = document.getDocumentElement();
        final Element simpleTypeElement = getChildOf(schemaElement, "simpleType", "foodPreference");
        final Element restrictionElement = getChildOf(simpleTypeElement, "restriction", null);

        // Act
        final List<Element> enumElements = new ArrayList<Element>();
        final NodeList childNodes = restrictionElement.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {

            final Node current = childNodes.item(i);
            if (current.getNodeType() == Node.ELEMENT_NODE
                    && current.getLocalName().equals("enumeration")) {
                enumElements.add((Element) current);
            }
        }

        final Map<String, String> xpath2ValueMap = new TreeMap<String, String>();
        for (Element current : enumElements) {
            final String currentXPath = DomHelper.getXPathFor(current);
            xpath2ValueMap.put(currentXPath, DomHelper.getValueAttribute(current));
        }

        // Assert
        assertNotNull(xpath2ValueMap);
        assertEquals(3, xpath2ValueMap.size());

        final String prefix =
                "#document/xs:schema/xs:simpleType[@name='foodPreference']/" + "xs:restriction/xs:enumeration[@value='";

        assertEquals("LACTO_VEGETARIAN", xpath2ValueMap.get(prefix + "LACTO_VEGETARIAN']"));
        assertEquals("NONE", xpath2ValueMap.get(prefix + "NONE']"));
        assertEquals("VEGAN", xpath2ValueMap.get(prefix + "VEGAN']"));
    }

    //
    // private helpers
    //

    private Element getChildOf(final Element parent, final String localName, final String name) {

        final NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {

            Node current = children.item(i);
            if (current.getNodeType() == Node.ELEMENT_NODE) {

                final String currentLocalName = current.getLocalName();
                final String nameAttribute = DomHelper.getNameAttribute(current);

                if (currentLocalName.equals(localName) && (name == null || name.equals(nameAttribute))) {
                    return (Element) current;
                }
            }
        }

        return null;
    }
}
