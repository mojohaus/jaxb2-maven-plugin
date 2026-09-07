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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AbstractJaxbMojoTest {

    private static class ConcreteJaxbMojo extends AbstractJaxbMojo {
        @Override
        protected boolean isReGenerationRequired() {
            return false;
        }

        @Override
        protected boolean performExecution() throws MojoExecutionException, MojoFailureException {
            return false;
        }

        private File outputDirectory;

        void setOutputDirectory(final File outputDirectory) {
            this.outputDirectory = outputDirectory;
        }

        @Override
        protected File getOutputDirectory() {
            return outputDirectory;
        }

        @Override
        protected String getStaleFileName() {
            return "stale";
        }

        @Override
        protected void addGeneratedSourcesToProjectSourceRoot(String canonicalPathToOutputDirectory) {}

        @Override
        protected boolean shouldExecutionBeSkipped() {
            return false;
        }

        @Override
        protected java.util.List<String> getClasspath() {
            return java.util.Collections.emptyList();
        }

        @Override
        protected java.util.List<java.net.URL> getSources() {
            return java.util.Collections.emptyList();
        }

        @Override
        protected void addResource(org.apache.maven.model.Resource resource) {}
    }

    private ConcreteJaxbMojo mojo;
    private MavenProject project;

    @BeforeEach
    void setUp(@TempDir final File tempDir) {
        mojo = new ConcreteJaxbMojo();
        project = new MavenProject();
        project.setFile(new File(tempDir, "pom.xml"));
        mojo.setProject(project);
    }

    @Test
    void validateRefusesToClearBasedir() {
        final File basedir = project.getBasedir();
        final MojoExecutionException ex = assertThrows(
                MojoExecutionException.class, () -> mojo.validateOutputDirectory(basedir, true, "outputDirectory"));
        assertTrue(ex.getMessage().contains("Cowardly refusing to clear outputDirectory"));
    }

    @Test
    void validateRefusesToClearParentOfBasedir() {
        final File parentDir = project.getBasedir().getParentFile();
        final MojoExecutionException ex = assertThrows(
                MojoExecutionException.class, () -> mojo.validateOutputDirectory(parentDir, true, "outputDirectory"));
        assertTrue(ex.getMessage().contains("Cowardly refusing to clear outputDirectory"));
    }

    @Test
    void validateRefusesToClearGrandParentOfBasedir() {
        final File grandParentDir = project.getBasedir().getParentFile().getParentFile();
        if (grandParentDir != null) {
            final MojoExecutionException ex = assertThrows(
                    MojoExecutionException.class,
                    () -> mojo.validateOutputDirectory(grandParentDir, true, "outputDirectory"));
            assertTrue(ex.getMessage().contains("Cowardly refusing to clear outputDirectory"));
        }
    }

    @Test
    void validateAllowsClearingSubdirectoryOfBasedir() {
        final File targetDir = new File(project.getBasedir(), "target/generated-sources/jaxb");
        assertDoesNotThrow(() -> mojo.validateOutputDirectory(targetDir, true, "outputDirectory"));
    }

    @Test
    void validateAllowsBasedirWhenClearOutputDirIsFalse() {
        final File basedir = project.getBasedir();
        assertDoesNotThrow(() -> mojo.validateOutputDirectory(basedir, false, "outputDirectory"));
    }

    @Test
    void validateAllowsNullDirectoryOrNullProject() {
        assertDoesNotThrow(() -> mojo.validateOutputDirectory(null, true, "outputDirectory"));
        mojo.setProject(null);
        assertDoesNotThrow(() -> mojo.validateOutputDirectory(new File("/"), true, "outputDirectory"));
    }
}
