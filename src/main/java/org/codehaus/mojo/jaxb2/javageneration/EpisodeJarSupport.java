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
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.maven.plugin.MojoExecutionException;

/**
 * Locates JAXB episode resources within artifact JARs and materializes them as local files
 * suitable for XJC external binding inputs. The episode discovery order is:
 *
 * <ol>
 *     <li>An explicitly configured resource path, if provided.</li>
 *     <li>{@code META-INF/sun-jaxb.episode} (the JAXB RI convention).</li>
 *     <li>{@code META-INF/JAXB/sun-jaxb.episode.xjb}.</li>
 *     <li>The single {@code .xjb} file directly within {@code META-INF/JAXB/} (the location used
 *     by this plugin when packaging generated episodes), provided exactly one such file exists.</li>
 * </ol>
 *
 * @see EpisodeArtifact
 * @since 4.1.1
 */
public final class EpisodeJarSupport {

    /**
     * The standard JAXB RI episode resource location.
     */
    public static final String STANDARD_EPISODE_RESOURCE = "META-INF/sun-jaxb.episode";

    /**
     * The episode location used by this plugin's own {@code getEpisodeFile} mechanism.
     */
    public static final String MOJOHAUS_EPISODE_DIRECTORY = "META-INF/JAXB/";

    /**
     * Hide constructor for utility classes.
     */
    private EpisodeJarSupport() {}

    /**
     * <p>Selects the episode entry within the supplied artifact file, following the discovery
     * order defined by this class. Extracted episode files preserve the timestamp of the source
     * JAR entry, so that incremental builds re-generate only when the episode artifact changes.</p>
     *
     * @param artifactFile         The resolved artifact file (JAR) to search.
     * @param explicitResourcePath An explicit resource path within the artifact, or {@code null} for
     *                             auto-discovery.
     * @param coordinate           The Maven coordinate of the artifact, used in diagnostics.
     * @return The name of the selected JAR entry, or {@code null} if no episode was found.
     * @throws MojoExecutionException If the artifact is not a readable JAR, an explicitly configured
     *                                resource path does not exist, or multiple episode candidates exist.
     */
    public static String selectEpisodeEntry(
            final File artifactFile, final String explicitResourcePath, final String coordinate)
            throws MojoExecutionException {

        if (!artifactFile.isFile()) {
            throw new MojoExecutionException("JAXB episode artifact " + coordinate + " resolved to ["
                    + artifactFile.getAbsolutePath()
                    + "] which is not a readable file. For reactor modules, ensure the module is packaged "
                    + "(e.g. run 'mvn package' or 'mvn install') before this module's xjc goal executes.");
        }

        final List<String> entryNames = listEntryNames(artifactFile);

        if (explicitResourcePath != null && !explicitResourcePath.isEmpty()) {
            if (entryNames.contains(explicitResourcePath)) {
                return explicitResourcePath;
            }
            throw new MojoExecutionException("Configured episode resource path [" + explicitResourcePath
                    + "] does not exist within artifact " + coordinate + " [" + artifactFile.getAbsolutePath()
                    + "].");
        }

        if (entryNames.contains(STANDARD_EPISODE_RESOURCE)) {
            return STANDARD_EPISODE_RESOURCE;
        }

        if (entryNames.contains(MOJOHAUS_EPISODE_DIRECTORY + "sun-jaxb.episode.xjb")) {
            return MOJOHAUS_EPISODE_DIRECTORY + "sun-jaxb.episode.xjb";
        }

        final List<String> candidates = new ArrayList<>();
        for (String current : entryNames) {
            if (current.startsWith(MOJOHAUS_EPISODE_DIRECTORY)
                    && current.endsWith(".xjb")
                    && current.indexOf('/', MOJOHAUS_EPISODE_DIRECTORY.length()) < 0) {
                candidates.add(current);
            }
        }

        if (candidates.isEmpty()) {
            return null;
        }
        if (candidates.size() > 1) {
            Collections.sort(candidates);
            throw new MojoExecutionException("Multiple JAXB episode resources found in artifact " + coordinate
                    + ": " + candidates + ". Configure <resourcePath> within the <episode> element to select "
                    + "one explicitly.");
        }
        return candidates.get(0);
    }

    /**
     * <p>Extracts the supplied entry from the artifact JAR to a deterministic location beneath
     * {@code extractionRoot}: {@code groupId/artifactId/version/entryName}. Guarded against
     * path traversal (ZIP-slip). The extracted file is stamped with the JAR entry's timestamp
     * (or the artifact file's, if the entry has none), enabling stable incremental builds.</p>
     *
     * @param artifactFile  The resolved artifact file (JAR).
     * @param entryName     The JAR entry name to extract.
     * @param extractionRoot The root directory beneath which the entry is materialized.
     * @param groupId       The groupId of the artifact (used for the target path).
     * @param artifactId    The artifactId of the artifact (used for the target path).
     * @param version       The version of the artifact (used for the target path).
     * @return The extracted, existing episode file.
     * @throws MojoExecutionException If the JAR cannot be read or the entry cannot be extracted.
     */
    public static File extractEpisodeEntry(
            final File artifactFile,
            final String entryName,
            final Path extractionRoot,
            final String groupId,
            final String artifactId,
            final String version)
            throws MojoExecutionException {

        if (entryName == null || entryName.isEmpty() || entryName.startsWith("/")) {
            throw new MojoExecutionException("Invalid episode entry path within artifact: " + entryName);
        }
        for (String segment : entryName.split("/")) {
            if ("..".equals(segment)) {
                throw new MojoExecutionException("Invalid episode entry path within artifact: " + entryName);
            }
        }

        final Path targetPath = extractionRoot
                .resolve(groupId)
                .resolve(artifactId)
                .resolve(version)
                .resolve(entryName)
                .normalize();

        if (!targetPath.startsWith(extractionRoot.normalize() + File.separator)) {
            throw new MojoExecutionException("Invalid episode entry path within artifact: " + entryName);
        }

        try (JarFile jarFile = new JarFile(artifactFile)) {
            final JarEntry entry = jarFile.getJarEntry(entryName);
            if (entry == null) {
                throw new MojoExecutionException("Episode entry [" + entryName + "] not found within artifact ["
                        + artifactFile.getAbsolutePath() + "].");
            }
            Files.createDirectories(targetPath.getParent());
            try (InputStream is = jarFile.getInputStream(entry)) {
                Files.copy(is, targetPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
            final long entryTime = entry.getTime() > 0 ? entry.getTime() : artifactFile.lastModified();
            if (!targetPath.toFile().setLastModified(entryTime)) {
                // Non-fatal: the extraction result exists, but incremental staleness may
                // re-generate more often than strictly required.
            }
            return targetPath.toFile();
        } catch (IOException e) {
            throw new MojoExecutionException(
                    "Could not extract episode entry [" + entryName + "] from artifact ["
                            + artifactFile.getAbsolutePath() + "].",
                    e);
        }
    }

    private static List<String> listEntryNames(final File jarFile) throws MojoExecutionException {
        try (JarFile theJar = new JarFile(jarFile)) {
            final List<String> entryNames =
                    theJar.stream().map(JarEntry::getName).collect(java.util.stream.Collectors.toList());
            return entryNames;
        } catch (IOException e) {
            throw new MojoExecutionException("Could not read artifact JAR [" + jarFile.getAbsolutePath() + "].", e);
        }
    }
}
