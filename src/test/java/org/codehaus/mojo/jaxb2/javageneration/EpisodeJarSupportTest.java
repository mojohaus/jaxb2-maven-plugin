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
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import org.apache.maven.plugin.MojoExecutionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EpisodeJarSupportTest {

    @TempDir
    File tempDir;

    private File jarFile;

    @BeforeEach
    void createJar() throws IOException {
        jarFile = new File(tempDir, "fixture.jar");
    }

    private void writeJar(final String... entries) throws IOException {
        try (JarOutputStream jos = new JarOutputStream(new FileOutputStream(jarFile))) {
            for (String entry : entries) {
                final JarEntry jarEntry = new JarEntry(entry);
                jarEntry.setTime(1_000_000L);
                jos.putNextEntry(jarEntry);
                jos.write(("<" + entry + "/>").getBytes(java.nio.charset.StandardCharsets.UTF_8));
                jos.closeEntry();
            }
        }
    }

    @Test
    void validateStandardEpisodeResourceDiscoveredFirst() throws Exception {
        writeJar("META-INF/sun-jaxb.episode", "META-INF/JAXB/sun-jaxb.episode.xjb", "com/acme/A.class");

        assertEquals("META-INF/sun-jaxb.episode", EpisodeJarSupport.selectEpisodeEntry(jarFile, null, "g:a:jar:1"));
    }

    @Test
    void validateMojohausLocationDiscoveredSecond() throws Exception {
        writeJar("META-INF/JAXB/sun-jaxb.episode.xjb", "com/acme/A.class");

        assertEquals(
                "META-INF/JAXB/sun-jaxb.episode.xjb", EpisodeJarSupport.selectEpisodeEntry(jarFile, null, "g:a:jar:1"));
    }

    @Test
    void validateSingleMojohausEpisodeDiscovered() throws Exception {
        writeJar("META-INF/JAXB/episode_generate-a.xjb", "com/acme/A.class");

        assertEquals(
                "META-INF/JAXB/episode_generate-a.xjb",
                EpisodeJarSupport.selectEpisodeEntry(jarFile, null, "g:a:jar:1"));
    }

    @Test
    void validateAmbiguousMojohausEpisodesFail() throws Exception {
        writeJar("META-INF/JAXB/episode_core.xjb", "META-INF/JAXB/episode_ext.xjb");

        final MojoExecutionException e = assertThrows(
                MojoExecutionException.class, () -> EpisodeJarSupport.selectEpisodeEntry(jarFile, null, "g:a:jar:1"));

        assertTrue(e.getMessage().contains("Multiple JAXB episode resources"));
        assertTrue(e.getMessage().contains("episode_core.xjb"));
        assertTrue(e.getMessage().contains("episode_ext.xjb"));
    }

    @Test
    void validateMissingEpisodeYieldsNull() throws Exception {
        writeJar("com/acme/A.class", "META-INF/MANIFEST.MF");

        assertNull(EpisodeJarSupport.selectEpisodeEntry(jarFile, null, "g:a:jar:1"));
    }

    @Test
    void validateExplicitResourcePathSelected() throws Exception {
        writeJar("META-INF/sun-jaxb.episode", "custom/episode.xjb");

        assertEquals(
                "custom/episode.xjb", EpisodeJarSupport.selectEpisodeEntry(jarFile, "custom/episode.xjb", "g:a:jar:1"));
    }

    @Test
    void validateExplicitResourcePathMustExist() throws Exception {
        writeJar("META-INF/sun-jaxb.episode");

        final MojoExecutionException e = assertThrows(
                MojoExecutionException.class,
                () -> EpisodeJarSupport.selectEpisodeEntry(jarFile, "does/not/exist.xjb", "g:a:jar:1"));

        assertTrue(e.getMessage().contains("does not exist within artifact"));
    }

    @Test
    void validateNonJarArtifactFails() {
        final MojoExecutionException e = assertThrows(
                MojoExecutionException.class,
                () -> EpisodeJarSupport.selectEpisodeEntry(new File(tempDir, "missing.jar"), null, "g:a:jar:1"));

        assertTrue(e.getMessage().contains("is not a readable file"));
    }

    @Test
    void validateExtractionProducesTimestampedFile() throws Exception {
        writeJar("META-INF/JAXB/episode_generate-a.xjb");
        final Path extractionRoot = Files.createTempDirectory(tempDir.toPath(), "extract");

        final File extracted = EpisodeJarSupport.extractEpisodeEntry(
                jarFile, "META-INF/JAXB/episode_generate-a.xjb", extractionRoot, "com.acme", "schema-a", "1.4.0");

        assertTrue(extracted.exists());
        assertEquals(
                new File(extractionRoot.toFile(), "com.acme/schema-a/1.4.0/META-INF/JAXB/episode_generate-a.xjb"),
                extracted);
        assertEquals(1_000_000L, extracted.lastModified());
    }

    @Test
    void validateExtractionRejectsPathTraversal() throws Exception {
        // Build a JAR whose entry name attempts to escape the extraction root.
        try (JarOutputStream jos = new JarOutputStream(new FileOutputStream(jarFile))) {
            final JarEntry evil = new JarEntry("../../evil.xjb");
            evil.setTime(1_000L);
            jos.putNextEntry(evil);
            jos.write("evil".getBytes(java.nio.charset.StandardCharsets.UTF_8));
            jos.closeEntry();
        }

        final MojoExecutionException e = assertThrows(
                MojoExecutionException.class,
                () -> EpisodeJarSupport.extractEpisodeEntry(
                        jarFile,
                        "../../evil.xjb",
                        Files.createTempDirectory(tempDir.toPath(), "extract2"),
                        "g",
                        "a",
                        "1"));
        assertNotNull(e);
    }

    @Test
    void validateExtractedContentMatchesEntry() throws Exception {
        writeJar("META-INF/JAXB/episode_generate-a.xjb");
        final File extracted = EpisodeJarSupport.extractEpisodeEntry(
                jarFile,
                "META-INF/JAXB/episode_generate-a.xjb",
                Files.createTempDirectory(tempDir.toPath(), "extract3"),
                "com.acme",
                "schema-a",
                "1.4.0");

        assertEquals(
                "<META-INF/JAXB/episode_generate-a.xjb/>",
                new String(Files.readAllBytes(extracted.toPath()), java.nio.charset.StandardCharsets.UTF_8));
    }

    @Test
    void validateEpisodeArtifactCoordinate() {
        final EpisodeArtifact episode = new EpisodeArtifact();
        episode.setGroupId("com.acme.schemas");
        episode.setArtifactId("schema-a");
        episode.setVersion("1.4.0");

        assertEquals("com.acme.schemas:schema-a:jar:1.4.0", episode.getCoordinate());

        episode.setClassifier("schemas");
        assertEquals("com.acme.schemas:schema-a:jar:schemas:1.4.0", episode.getCoordinate());
    }

    @Test
    void validateEpisodeArtifactValidation() {
        final EpisodeArtifact episode = new EpisodeArtifact();
        assertThrows(NullPointerException.class, episode::validate);

        episode.setGroupId("g");
        assertThrows(NullPointerException.class, episode::validate);

        episode.setArtifactId("a");
        assertThrows(NullPointerException.class, episode::validate);

        episode.setVersion("1");
        episode.validate();
    }
}
