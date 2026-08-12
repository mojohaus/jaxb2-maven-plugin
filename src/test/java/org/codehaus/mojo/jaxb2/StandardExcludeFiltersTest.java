package org.codehaus.mojo.jaxb2;

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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.codehaus.mojo.jaxb2.shared.filters.Filters;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Validates that the standard exclude filters reject README files without rejecting
 * everything that merely happens to sit below a directory whose name contains "readme".
 */
class StandardExcludeFiltersTest {

    @TempDir
    File projectDirectory;

    @BeforeEach
    void initializeFilters() {
        Filters.initialize(new BufferingLog(), AbstractJaxbMojo.STANDARD_EXCLUDE_FILTERS);
    }

    @Test
    void validateReadmeFilesAreExcluded() throws IOException {
        assertTrue(isExcluded("src/main/xsd/README.txt"));
        assertTrue(isExcluded("src/main/xsd/README"));
        assertTrue(isExcluded("src/main/xsd/readme.md"));
    }

    @Test
    void validateReadmeInDirectoryNameDoesNotExcludeSchemas() throws IOException {
        // A branch or workspace directory containing "readme" must not hide the schemas below it.
        assertFalse(isExcluded("my-readme-branch/src/main/xsd/schema.xsd"));
        assertFalse(isExcluded("readme/schema.xsd"));
        assertFalse(isExcluded("src/main/xsd/readme-of-doom/schema.xsd"));
    }

    @Test
    void validateReadmeSubstringInFileNameDoesNotExcludeSchemas() throws IOException {
        assertFalse(isExcluded("src/main/xsd/my-readme-schema.xsd"));
    }

    @Test
    void validateXmlAndTxtSuffixesAreStillExcluded() throws IOException {
        assertTrue(isExcluded("src/main/xsd/binding.xml"));
        assertTrue(isExcluded("src/main/xsd/notes.txt"));
        assertFalse(isExcluded("src/main/xsd/schema.xsd"));
    }

    //
    // Private helpers
    //

    private boolean isExcluded(final String relativePath) throws IOException {

        final File toCheck = new File(projectDirectory, relativePath);
        Files.createDirectories(toCheck.getParentFile().toPath());
        Files.write(toCheck.toPath(), new byte[0]);

        return Filters.matchAtLeastOnce(toCheck, AbstractJaxbMojo.STANDARD_EXCLUDE_FILTERS);
    }
}
