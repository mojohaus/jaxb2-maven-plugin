package org.codehaus.mojo.jaxb2.schemageneration.postprocessing.javadoc;

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

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import static org.junit.jupiter.api.Assertions.assertTrue;

class XsdAnnotationProcessorWithUnderscoreTest extends AbstractSourceCodeAwareNodeProcessingTest {

    private JavaDocRenderer renderer = new DefaultJavaDocRenderer();

    @Test
    void validateProcessingClassNameWithUnderscore() throws Exception {
        final Document document = namespace2DocumentMap.get(DummyRequest_v1.NAMESPACE);
        final Node rootNode = document.getFirstChild();

        final XsdAnnotationProcessor unitUnderTest = new XsdAnnotationProcessor(docs, renderer);

        // Act
        process(rootNode, true, unitUnderTest);

        // Assert
        final String processed = printDocument(document);

        // Verify that both class-level and field-level documentation annotations are present
        assertTrue(processed.contains("Class documentation for DummyRequest_v1."));
        assertTrue(processed.contains("Field documentation for requestData."));
    }

    @Override
    protected List<Class<?>> getJaxbAnnotatedClassesForJaxbContext() {
        return Collections.<Class<?>>singletonList(DummyRequest_v1.class);
    }
}
