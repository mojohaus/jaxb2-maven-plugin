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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.maven.model.Resource;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.settings.Settings;
import org.codehaus.mojo.jaxb2.BufferingLog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AbstractJavaGeneratorMojoTest {

    private static class TestableJavaGeneratorMojo extends AbstractJavaGeneratorMojo {
        private File outputDirectory;
        private List<URL> sources = Collections.emptyList();
        private List<File> xjbFiles = Collections.emptyList();

        TestableJavaGeneratorMojo() {
            this.sourceType = SourceContentType.XmlSchema;
            this.settings = new Settings();
        }

        void setOutputDirectory(final File outputDirectory) {
            this.outputDirectory = outputDirectory;
        }

        void setStaleFileDirectory(final File staleFileDirectory) {
            this.staleFileDirectory = staleFileDirectory;
        }

        void setSources(final List<URL> sources) {
            this.sources = sources;
        }

        void setSourceXJBs(final List<File> xjbFiles) {
            this.xjbFiles = xjbFiles;
        }

        @Override
        protected List<URL> getSources() {
            return sources;
        }

        @Override
        protected List<File> getSourceXJBs() throws MojoExecutionException {
            return xjbFiles;
        }

        @Override
        protected String getStaleFileName() {
            return "staleFlag";
        }

        @Override
        protected File getOutputDirectory() {
            return outputDirectory;
        }

        @Override
        protected void addGeneratedSourcesToProjectSourceRoot(final String canonicalPathToOutputDirectory) {}

        @Override
        protected boolean shouldExecutionBeSkipped() {
            return false;
        }

        @Override
        protected List<String> getClasspath() {
            return Collections.emptyList();
        }

        @Override
        protected void addResource(final Resource resource) {}

        File getStaleFlagFile() {
            return getStaleFile();
        }
    }

    private TestableJavaGeneratorMojo mojo;

    @BeforeEach
    void setUp(@TempDir final File tempDir) throws Exception {
        mojo = new TestableJavaGeneratorMojo();
        mojo.setLog(new BufferingLog(BufferingLog.LogLevel.DEBUG));
        mojo.setOutputDirectory(new File(tempDir, "output"));
        final File fakeSchema = new File(tempDir, "schema.xsd");
        assertTrue(fakeSchema.createNewFile());
        mojo.setSources(Collections.singletonList(createFileUrl(fakeSchema)));
    }

    @Test
    void validateAutoNameResolutionFalseByDefault() {
        assertFalse(mojo.autoNameResolution);
    }

    @Test
    void validateAutoNameResolutionNotAddedWhenFalse() throws Exception {
        mojo.autoNameResolution = false;

        final List<String> arguments = Arrays.asList(mojo.getXjcArguments("target/classes", null));

        assertFalse(arguments.contains("-XautoNameResolution"));
    }

    @Test
    void validateAutoNameResolutionAddedWhenTrue() throws Exception {
        mojo.autoNameResolution = true;

        final List<String> arguments = Arrays.asList(mojo.getXjcArguments("target/classes", null));

        assertTrue(arguments.contains("-XautoNameResolution"));
    }

    @Test
    void validateReGenerationRequiredWhenOutputDirDoesNotExist(@TempDir final File tempDir) throws Exception {
        mojo.setOutputDirectory(new File(tempDir, "does-not-exist"));
        mojo.setStaleFileDirectory(tempDir);

        assertTrue(mojo.isReGenerationRequired());
    }

    @Test
    void validateReGenerationRequiredWhenOutputDirIsEmpty(@TempDir final File tempDir) throws Exception {
        final File emptyOutDir = new File(tempDir, "emptyOutput");
        assertTrue(emptyOutDir.mkdirs());
        mojo.setOutputDirectory(emptyOutDir);
        mojo.setStaleFileDirectory(tempDir);

        assertTrue(mojo.isReGenerationRequired());
    }

    @Test
    void validateReGenerationRequiredWhenOutputDirHasOnlyEmptySubdirectories(@TempDir final File tempDir)
            throws Exception {
        final File emptyOutDir = new File(tempDir, "nestedEmpty/sub/pkg");
        assertTrue(emptyOutDir.mkdirs());
        mojo.setOutputDirectory(new File(tempDir, "nestedEmpty"));
        mojo.setStaleFileDirectory(tempDir);

        assertTrue(mojo.isReGenerationRequired());
    }

    @Test
    void validateReGenerationNotRequiredWhenOutputDirHasFilesAndStaleFileFresh(@TempDir final File tempDir)
            throws Exception {
        final File outDir = new File(tempDir, "regen-output");
        assertTrue(outDir.mkdirs());
        final File generatedSource = new File(outDir, "Generated.java");
        assertTrue(generatedSource.createNewFile());

        final File staleFileDir = new File(tempDir, "staleDir");
        assertTrue(staleFileDir.mkdirs());
        mojo.setOutputDirectory(outDir);
        mojo.setStaleFileDirectory(staleFileDir);

        final File sourceFile = new File(tempDir, "regen-schema.xsd");
        assertTrue(sourceFile.createNewFile());
        sourceFile.setLastModified(1000L);
        mojo.setSources(Collections.singletonList(createFileUrl(sourceFile)));

        final File staleFile = mojo.getStaleFlagFile();
        assertTrue(staleFile.createNewFile());
        staleFile.setLastModified(2000L);

        // All sources older than staleFile and outputDir contains files -> generation NOT required
        assertFalse(mojo.isReGenerationRequired());

        // But if output directory is deleted, re-generation IS required even though staleFile is fresh (Issue #64)
        assertTrue(generatedSource.delete());
        assertTrue(outDir.delete());
        assertTrue(mojo.isReGenerationRequired());
    }

    private static URL createFileUrl(final File file) throws MalformedURLException {
        return file.toPath().toUri().toURL();
    }
}
