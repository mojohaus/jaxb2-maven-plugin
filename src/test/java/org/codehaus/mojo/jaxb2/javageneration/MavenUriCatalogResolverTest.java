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

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.regex.Matcher;

import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link MavenUriCatalogResolver}.
 *
 * <p>These tests cover the {@code maven:} URI pattern matching and the
 * {@link MavenUriCatalogResolver#containsMavenUris(String)} fast-path check.
 * Full end-to-end resolution (Aether artifact lookup) is covered by the integration test
 * {@code xjc-catalog-maven-uri} in {@code src/it/}.</p>
 */
class MavenUriCatalogResolverTest {

    // ==================================================================================
    // containsMavenUris() fast-path tests
    // ==================================================================================

    /**
     * Verifies that a catalog text containing a {@code maven:} URI is detected correctly.
     */
    @Test
    void containsMavenUris_returnsTrueWhenMavenUriPresent() {
        final String catalog = "REWRITE_SYSTEM \"http://example.com/schema.xsd\"\n"
                + "    \"maven:com.example:shared:jar:!/schemas/schema.xsd\"";

        assertTrue(
                MavenUriCatalogResolver.containsMavenUris(catalog), "Expected true when catalog contains maven: URI");
    }

    /**
     * Verifies that a catalog with only standard URIs returns {@code false}.
     *
     * <p>The fast-path check must not trigger for files that already use {@code file://},
     * {@code http://}, or relative paths — those should be forwarded to XJC without any
     * pre-processing.</p>
     */
    @Test
    void containsMavenUris_returnsFalseWhenNoMavenUri() {
        final String catalog =
                "REWRITE_SYSTEM \"http://example.com/schema.xsd\"\n" + "    \"file:///local/schemas/schema.xsd\"";

        assertFalse(
                MavenUriCatalogResolver.containsMavenUris(catalog),
                "Expected false when catalog contains no maven: URI");
    }

    /**
     * Verifies that an empty catalog text returns {@code false}.
     */
    @Test
    void containsMavenUris_returnsFalseForEmptyCatalog() {
        assertFalse(MavenUriCatalogResolver.containsMavenUris(""), "Expected false for empty catalog text");
    }

    // ==================================================================================
    // MAVEN_URI_PATTERN matching tests
    // ==================================================================================

    /**
     * Verifies that the four-segment {@code maven:g:a:type:classifier!/path} form is parsed.
     *
     * <p>This is the most common form used by jaxb-tools-compatible catalog files.</p>
     */
    @Test
    void pattern_matchesFourSegmentWithClassifier() {
        // Assemble: full four-segment coordinate
        final String uri = "maven:com.example:shared-schemas:jar:sources!/schemas/common.xsd";

        // Act
        final Matcher m = MavenUriCatalogResolver.MAVEN_URI_PATTERN.matcher(uri);
        assertTrue(m.find(), "Expected pattern to match");

        // Assert
        assertEquals("com.example", m.group(1), "groupId");
        assertEquals("shared-schemas", m.group(2), "artifactId");
        assertEquals("jar", m.group(3), "type");
        assertEquals("sources", m.group(4), "classifier");
        assertEquals("/schemas/common.xsd", m.group(5), "path-in-jar");
    }

    /**
     * Verifies that the form with an empty classifier segment is matched.
     *
     * <p>Empty classifier: {@code maven:g:a:jar:!/path} — the segment exists but is blank.</p>
     */
    @Test
    void pattern_matchesFourSegmentWithEmptyClassifier() {
        final String uri = "maven:com.example:shared:jar:!/schemas/data.xsd";

        final Matcher m = MavenUriCatalogResolver.MAVEN_URI_PATTERN.matcher(uri);
        assertTrue(m.find(), "Expected pattern to match");

        assertEquals("com.example", m.group(1));
        assertEquals("shared", m.group(2));
        assertEquals("jar", m.group(3));
        // group(4) is empty string (classifier segment present but empty)
        assertTrue(m.group(4) == null || m.group(4).isEmpty(), "classifier should be empty");
        assertEquals("/schemas/data.xsd", m.group(5));
    }

    /**
     * Verifies that the three-segment form without a classifier is matched.
     *
     * <p>Three-segment: {@code maven:g:a:type!/path}</p>
     */
    @Test
    void pattern_matchesThreeSegmentNoClassifier() {
        final String uri = "maven:org.example:api-schemas:jar!/xsd/api.xsd";

        final Matcher m = MavenUriCatalogResolver.MAVEN_URI_PATTERN.matcher(uri);
        assertTrue(m.find(), "Expected pattern to match");

        assertEquals("org.example", m.group(1));
        assertEquals("api-schemas", m.group(2));
        assertEquals("jar", m.group(3));
        // group(4) absent (classifier segment omitted entirely)
        assertTrue(m.group(4) == null || m.group(4).isEmpty(), "classifier should be absent");
        assertEquals("/xsd/api.xsd", m.group(5));
    }

    /**
     * Verifies that a plain string with no {@code maven:} URI does NOT match.
     */
    @Test
    void pattern_doesNotMatchNonMavenUri() {
        final String text = "REWRITE_SYSTEM \"http://schemas.example.com/v1/types.xsd\" \"file:///local/types.xsd\"";

        final Matcher m = MavenUriCatalogResolver.MAVEN_URI_PATTERN.matcher(text);
        assertFalse(m.find(), "Expected no match for non-maven URI text");
    }

    /**
     * Verifies that multiple {@code maven:} URIs in the same catalog text are all matched.
     */
    @Test
    void pattern_matchesMultipleMavenUrisInSameText() {
        final String catalog = "REWRITE_SYSTEM \"http://a.example.com/a.xsd\"\n"
                + "    \"maven:com.example:module-a:jar:!/xsd/a.xsd\"\n"
                + "REWRITE_SYSTEM \"http://b.example.com/b.xsd\"\n"
                + "    \"maven:com.example:module-b:jar:!/xsd/b.xsd\"";

        final Matcher m = MavenUriCatalogResolver.MAVEN_URI_PATTERN.matcher(catalog);

        // First match
        assertTrue(m.find(), "Expected first match");
        assertEquals("module-a", m.group(2), "First artifactId");
        assertEquals("/xsd/a.xsd", m.group(5), "First path");

        // Second match
        assertTrue(m.find(), "Expected second match");
        assertEquals("module-b", m.group(2), "Second artifactId");
        assertEquals("/xsd/b.xsd", m.group(5), "Second path");

        // No more matches
        assertFalse(m.find(), "Expected no further matches");
    }

    // ==================================================================================
    // resolve() tests
    // ==================================================================================

    @Test
    void resolve_returnsOriginalCatalogWhenNoMavenUrisPresent(@TempDir final Path tempDir)
            throws IOException, MojoExecutionException {
        final File originalCatalog = tempDir.resolve("catalog.xml").toFile();
        final File resolvedCatalog = tempDir.resolve("catalog-resolved.cat").toFile();

        final String content = "<catalog xmlns=\"urn:oasis:names:tc:entity:xmlns:xml:catalog\">\n"
                + "  <system systemId=\"http://example.com/schema.xsd\" uri=\"schema.xsd\"/>\n"
                + "</catalog>";
        Files.write(originalCatalog.toPath(), content.getBytes(StandardCharsets.UTF_8));

        final MavenUriCatalogResolver resolver =
                new MavenUriCatalogResolver(null, null, Collections.emptyList(), new SystemStreamLog());

        final File result = resolver.resolve(originalCatalog, resolvedCatalog);

        assertSame(originalCatalog, result, "Should return original file when no maven: URIs exist");
        assertFalse(resolvedCatalog.exists(), "Resolved catalog should not be created");
    }

    @Test
    void resolve_replacesMavenUrisWithJarUrls(@TempDir final Path tempDir) throws IOException, MojoExecutionException {
        final File dummyJar = tempDir.resolve("my-lib-1.0.jar").toFile();
        Files.write(dummyJar.toPath(), "PK dummy zip".getBytes(StandardCharsets.UTF_8));

        final DefaultArtifact artifact = new DefaultArtifact(
                "com.example", "my-lib", "1.0", "compile", "jar", "", new DefaultArtifactHandler("jar"));
        artifact.setFile(dummyJar);

        final File originalCatalog = tempDir.resolve("catalog.cat").toFile();
        final File resolvedCatalog =
                tempDir.resolve("sub/dir/catalog-resolved.cat").toFile();

        final String content = "REWRITE_SYSTEM \"http://example.com/address.xsd\" "
                + "\"maven:com.example:my-lib:jar:!/schemas/address.xsd\"\n";
        Files.write(originalCatalog.toPath(), content.getBytes(StandardCharsets.UTF_8));

        final MavenUriCatalogResolver resolver =
                new MavenUriCatalogResolver(null, null, Collections.emptyList(), new SystemStreamLog());
        resolver.setProjectArtifacts(Collections.singleton(artifact));

        final File result = resolver.resolve(originalCatalog, resolvedCatalog);

        assertEquals(resolvedCatalog, result);
        assertTrue(resolvedCatalog.exists(), "Resolved catalog should be written");

        final String resultText = new String(Files.readAllBytes(resolvedCatalog.toPath()), StandardCharsets.UTF_8);
        assertFalse(resultText.contains("maven:"), "Should not contain maven: scheme");
        assertTrue(
                resultText.contains("jar:" + dummyJar.toURI().toASCIIString() + "!/schemas/address.xsd"),
                "Should contain resolved jar: URL");
    }

    @Test
    void resolve_throwsExceptionWhenDependencyNotFound(@TempDir final Path tempDir) throws IOException {
        final File originalCatalog = tempDir.resolve("catalog.cat").toFile();
        final File resolvedCatalog = tempDir.resolve("catalog-resolved.cat").toFile();

        final String content = "REWRITE_SYSTEM \"http://example.com/address.xsd\" "
                + "\"maven:com.unknown:missing-lib:jar:!/schemas/address.xsd\"\n";
        Files.write(originalCatalog.toPath(), content.getBytes(StandardCharsets.UTF_8));

        final MavenUriCatalogResolver resolver =
                new MavenUriCatalogResolver(null, null, Collections.emptyList(), new SystemStreamLog());
        resolver.setProjectArtifacts(Collections.emptySet());

        final MojoExecutionException ex =
                assertThrows(MojoExecutionException.class, () -> resolver.resolve(originalCatalog, resolvedCatalog));

        assertTrue(ex.getMessage().contains("missing-lib"), "Message should mention missing artifact");
    }
}
