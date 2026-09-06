package org.codehaus.mojo.jaxb2.schemageneration.postprocessing.schemaenhancement;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.StringReader;
import java.util.List;

import org.codehaus.mojo.jaxb2.schemageneration.XsdGeneratorHelper;
import org.codehaus.mojo.jaxb2.schemageneration.postprocessing.DebugNodeProcessor;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QualifyTargetNamespaceReferencesProcessorTest {

    @Test
    void validateUnprefixedElementReferenceIsPrefixedWhenNoDefaultNamespace() {
        // Assemble
        final String prefix = "tns";
        final String xmlStream = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
                + "<xs:schema version=\"1.0\"\n"
                + "           targetNamespace=\"http://schemas.acme.com/vehicles\"\n"
                + "           xmlns:tns=\"http://schemas.acme.com/vehicles\"\n"
                + "           xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">\n"
                + "  <xs:element name=\"CarType\" type=\"tns:CarType\"/>\n"
                + "  <xs:complexType name=\"Car\">\n"
                + "    <xs:sequence>\n"
                + "      <xs:element ref=\"CarType\"/>\n"
                + "    </xs:sequence>\n"
                + "  </xs:complexType>\n"
                + "</xs:schema>\n";

        final QualifyTargetNamespaceReferencesProcessor unitUnderTest =
                new QualifyTargetNamespaceReferencesProcessor(prefix, false);
        final DebugNodeProcessor debugNodeProcessor = new DebugNodeProcessor(unitUnderTest);

        // Act
        final Document document = XsdGeneratorHelper.parseXmlStream(new StringReader(xmlStream));
        XsdGeneratorHelper.process(document.getFirstChild(), true, debugNodeProcessor);

        // Assert
        final List<Node> acceptedNodes = debugNodeProcessor.getAcceptedNodes();
        assertEquals(1, acceptedNodes.size());
        assertEquals(1, unitUnderTest.getModifiedCount());
        Node elementReferenceAttribute = acceptedNodes.get(0);
        assertEquals("ref", elementReferenceAttribute.getNodeName());
        assertEquals("tns:CarType", elementReferenceAttribute.getNodeValue());
    }

    @Test
    void validateUnprefixedElementReferenceNotModifiedWhenDefaultNamespacePresent() {
        // Assemble: schema has xmlns="http://schemas.acme.com/vehicles"
        final String prefix = "tns";
        final String xmlStream = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
                + "<xs:schema version=\"1.0\"\n"
                + "           targetNamespace=\"http://schemas.acme.com/vehicles\"\n"
                + "           xmlns=\"http://schemas.acme.com/vehicles\"\n"
                + "           xmlns:tns=\"http://schemas.acme.com/vehicles\"\n"
                + "           xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">\n"
                + "  <xs:complexType name=\"Car\">\n"
                + "    <xs:sequence>\n"
                + "      <xs:element ref=\"CarType\"/>\n"
                + "    </xs:sequence>\n"
                + "  </xs:complexType>\n"
                + "</xs:schema>\n";

        final QualifyTargetNamespaceReferencesProcessor unitUnderTest =
                new QualifyTargetNamespaceReferencesProcessor(prefix, true);
        final DebugNodeProcessor debugNodeProcessor = new DebugNodeProcessor(unitUnderTest);

        // Act
        final Document document = XsdGeneratorHelper.parseXmlStream(new StringReader(xmlStream));
        XsdGeneratorHelper.process(document.getFirstChild(), true, debugNodeProcessor);

        // Assert
        assertTrue(debugNodeProcessor.getAcceptedNodes().isEmpty());
        assertEquals(0, unitUnderTest.getModifiedCount());
    }

    @Test
    void validateAlreadyPrefixedElementReferenceNotModified() {
        // Assemble
        final String prefix = "tns";
        final String xmlStream = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
                + "<xs:schema version=\"1.0\"\n"
                + "           targetNamespace=\"http://schemas.acme.com/vehicles\"\n"
                + "           xmlns:other=\"http://schemas.acme.com/other\"\n"
                + "           xmlns:tns=\"http://schemas.acme.com/vehicles\"\n"
                + "           xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">\n"
                + "  <xs:complexType name=\"Car\">\n"
                + "    <xs:sequence>\n"
                + "      <xs:element ref=\"other:CarType\"/>\n"
                + "    </xs:sequence>\n"
                + "  </xs:complexType>\n"
                + "</xs:schema>\n";

        final QualifyTargetNamespaceReferencesProcessor unitUnderTest =
                new QualifyTargetNamespaceReferencesProcessor(prefix, false);
        final DebugNodeProcessor debugNodeProcessor = new DebugNodeProcessor(unitUnderTest);

        // Act
        final Document document = XsdGeneratorHelper.parseXmlStream(new StringReader(xmlStream));
        XsdGeneratorHelper.process(document.getFirstChild(), true, debugNodeProcessor);

        // Assert
        assertTrue(debugNodeProcessor.getAcceptedNodes().isEmpty());
        assertEquals(0, unitUnderTest.getModifiedCount());
    }

    @Test
    void validateNullOrEmptyPrefixDoesNotModifyNodes() {
        // Assemble
        final String xmlStream = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
                + "<xs:schema version=\"1.0\"\n"
                + "           xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">\n"
                + "  <xs:complexType name=\"Car\">\n"
                + "    <xs:sequence>\n"
                + "      <xs:element ref=\"CarType\"/>\n"
                + "    </xs:sequence>\n"
                + "  </xs:complexType>\n"
                + "</xs:schema>\n";

        final QualifyTargetNamespaceReferencesProcessor unitUnderTest =
                new QualifyTargetNamespaceReferencesProcessor(null, false);
        final DebugNodeProcessor debugNodeProcessor = new DebugNodeProcessor(unitUnderTest);

        // Act
        final Document document = XsdGeneratorHelper.parseXmlStream(new StringReader(xmlStream));
        XsdGeneratorHelper.process(document.getFirstChild(), true, debugNodeProcessor);

        // Assert
        assertTrue(debugNodeProcessor.getAcceptedNodes().isEmpty());
        assertEquals(0, unitUnderTest.getModifiedCount());
    }
}
