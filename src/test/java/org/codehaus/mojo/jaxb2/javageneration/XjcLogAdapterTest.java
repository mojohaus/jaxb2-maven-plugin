package org.codehaus.mojo.jaxb2.javageneration;

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

import java.util.Map;

import org.codehaus.mojo.jaxb2.BufferingLog;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXParseException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class XjcLogAdapterTest {

    @Test
    void validateInfoDoesNotPassThrowableWhenDebugDisabled() {
        // Assemble
        final BufferingLog log = new BufferingLog(BufferingLog.LogLevel.INFO);
        final XjcLogAdapter unitUnderTest = new XjcLogAdapter(log);
        final SAXParseException exception = new SAXParseException("generating code", null);

        // Act
        unitUnderTest.info(exception);

        // Assert
        final Map<String, Throwable> entries = log.getLogBuffer();
        assertEquals(1, entries.size());

        final Map.Entry<String, Throwable> entry = entries.entrySet().iterator().next();
        assertNull(entry.getValue(), "Info log must not pass Throwable when debug is disabled");
        assertTrue(entry.getKey().contains("generating code"));
        assertFalse(entry.getKey().contains("null [-1,-1]"), "Must not output 'null [-1,-1]' prefix");
    }

    @Test
    void validateInfoPassesThrowableWhenDebugEnabled() {
        // Assemble
        final BufferingLog log = new BufferingLog(BufferingLog.LogLevel.DEBUG);
        final XjcLogAdapter unitUnderTest = new XjcLogAdapter(log);
        final SAXParseException exception = new SAXParseException("generating code", null);

        // Act
        unitUnderTest.info(exception);

        // Assert
        final Map<String, Throwable> entries = log.getLogBuffer();
        assertEquals(1, entries.size());

        final Map.Entry<String, Throwable> entry = entries.entrySet().iterator().next();
        assertNotNull(entry.getValue(), "Info log should pass Throwable when debug is enabled");
        assertTrue(entry.getKey().contains("generating code"));
    }

    @Test
    void validateWarningFormatsLocationAndOmitsThrowableWhenDebugDisabled() {
        // Assemble
        final BufferingLog log = new BufferingLog(BufferingLog.LogLevel.WARN);
        final XjcLogAdapter unitUnderTest = new XjcLogAdapter(log);
        final SAXParseException exception = new SAXParseException("Unused type", "publicId", "systemId.xsd", 10, 5);

        // Act
        unitUnderTest.warning(exception);

        // Assert
        final Map<String, Throwable> entries = log.getLogBuffer();
        assertEquals(1, entries.size());

        final Map.Entry<String, Throwable> entry = entries.entrySet().iterator().next();
        assertNull(entry.getValue(), "Warning log must not pass Throwable when debug is disabled");
        assertTrue(entry.getKey().contains("publicId [10,5] Unused type"));
    }
}
