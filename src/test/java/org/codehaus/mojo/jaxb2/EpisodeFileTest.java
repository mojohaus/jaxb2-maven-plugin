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
import java.net.URL;
import java.util.Collections;
import java.util.List;

import org.apache.maven.model.Resource;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.mojo.jaxb2.shared.FileSystemUtilities;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EpisodeFileTest {

    private static class ConcreteJaxbMojo extends AbstractJaxbMojo {
        private File outputDirectory;

        void setOutputDirectory(final File outputDirectory) {
            this.outputDirectory = outputDirectory;
        }

        @Override
        protected File getOutputDirectory() {
            return outputDirectory;
        }

        @Override
        protected boolean isReGenerationRequired() {
            return false;
        }

        @Override
        protected boolean performExecution() throws MojoExecutionException, MojoFailureException {
            return false;
        }

        @Override
        protected String getStaleFileName() {
            return "stale";
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
        protected List<URL> getSources() {
            return Collections.emptyList();
        }

        @Override
        protected void addResource(final Resource resource) {}
    }

    private ConcreteJaxbMojo mojo;

    @BeforeEach
    void setUp() {
        mojo = new ConcreteJaxbMojo();
        mojo.setLog(new BufferingLog());
    }

    @Test
    void validateEpisodeFileNameWhenDirectoryClearedBeforehand(@TempDir final File tempDir) throws Exception {
        final File outputDir = new File(tempDir, "output");
        assertTrue(outputDir.mkdirs());
        mojo.setOutputDirectory(outputDir);

        // First run generates base name
        final File episode1 = mojo.getEpisodeFile(null);
        assertEquals("sun-jaxb.episode.xjb", episode1.getName());
        assertTrue(episode1.createNewFile());

        // Without clearing output directory, subsequent run increments suffix
        final File episode2WithoutClear = mojo.getEpisodeFile(null);
        assertEquals("sun-jaxb.episode_1.xjb", episode2WithoutClear.getName());

        // With clearing before getEpisodeFile (fix for #300), base name is consistently maintained
        FileSystemUtilities.createDirectory(outputDir, true);
        final File episodeAfterClear = mojo.getEpisodeFile(null);
        assertEquals("sun-jaxb.episode.xjb", episodeAfterClear.getName());
    }
}
