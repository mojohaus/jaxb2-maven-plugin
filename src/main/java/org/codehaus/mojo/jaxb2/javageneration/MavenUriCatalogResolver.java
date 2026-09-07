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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;

/**
 * Pre-processes an OASIS XML Catalog file by resolving {@code maven:} URIs into
 * concrete {@code jar:file://} URLs backed by artifacts in the local Maven repository.
 *
 * <h2>The {@code maven:} URI scheme</h2>
 * <p>The scheme follows the format used by the
 * <a href="https://github.com/highsource/jaxb-tools">jaxb-tools</a> project:</p>
 * <pre>
 *   maven:groupId:artifactId:type:classifier!/path/in/jar
 * </pre>
 * <p>Examples:</p>
 * <ul>
 *   <li>{@code maven:com.example:shared-schemas:jar:!/schemas/common.xsd}</li>
 *   <li>{@code maven:com.example:shared-schemas:jar::!/schemas/common.xsd} (empty classifier)</li>
 *   <li>{@code maven:com.example:shared-schemas:jar!/schemas/common.xsd} (omit classifier segment)</li>
 * </ul>
 *
 * <h2>Resolution</h2>
 * <p>Each {@code maven:} URI is resolved via the Aether {@link RepositorySystem} to obtain the
 * artifact's local file. The resolved URI becomes a {@code jar:file:///…!/path/in/jar} URL that
 * XJC's built-in catalog support can follow without any further custom resolver.</p>
 *
 * <h2>Catalog pre-processing</h2>
 * <p>The original catalog file is read, all {@code maven:…} occurrences are replaced, and the
 * result is written to a new file in the build directory. The new file path is returned and used
 * as the catalog argument to XJC in place of the original.</p>
 *
 * @since 4.1.1
 */
public final class MavenUriCatalogResolver {

    /**
     * Regex that matches a {@code maven:} URI anywhere in a catalog file line.
     *
     * <p>The captured groups are:</p>
     * <ol>
     *   <li>groupId</li>
     *   <li>artifactId</li>
     *   <li>type (may be empty → defaults to {@code jar})</li>
     *   <li>classifier (optional segment after the third colon; may be absent or empty)</li>
     *   <li>path-in-jar (after {@code !/})</li>
     * </ol>
     *
     * <p>Supported formats:</p>
     * <ul>
     *   <li>{@code maven:g:a:jar:cls!/path} — four-segment, explicit classifier</li>
     *   <li>{@code maven:g:a:jar:!/path} — four-segment, empty classifier</li>
     *   <li>{@code maven:g:a:jar!/path} — three-segment, no classifier</li>
     *   <li>{@code maven:g:a!/path} — two-segment, defaults type=jar, no classifier</li>
     * </ul>
     */
    static final Pattern MAVEN_URI_PATTERN = Pattern.compile(
            // scheme + groupId:artifactId
            "maven:([^:!/]+):([^:!/]+)"
                    // optional :type
                    + "(?::([^:!/]*))?"
                    // optional :classifier
                    + "(?::([^:!/]*))?"
                    // !/ separator and path-in-jar
                    + "!(/[^\"' \\t>]*)");

    private final RepositorySystem repositorySystem;
    private final RepositorySystemSession session;
    private final List<RemoteRepository> remoteRepositories;
    private final Log log;

    /**
     * Creates a new resolver.
     *
     * @param repositorySystem   Aether repository system, injected by the mojo
     * @param session            the current repository system session
     * @param remoteRepositories the project's remote repositories
     * @param log                the Maven build log
     */
    public MavenUriCatalogResolver(
            final RepositorySystem repositorySystem,
            final RepositorySystemSession session,
            final List<RemoteRepository> remoteRepositories,
            final Log log) {
        this.repositorySystem = repositorySystem;
        this.session = session;
        this.remoteRepositories = remoteRepositories;
        this.log = log;
    }

    /**
     * Returns {@code true} if the given catalog file text contains at least one {@code maven:} URI.
     *
     * <p>Used as a fast-path guard: catalogs without {@code maven:} references are passed straight
     * to XJC without any pre-processing.</p>
     *
     * @param catalogText the full text of the catalog file
     * @return {@code true} when a {@code maven:…!/…} pattern is present
     */
    public static boolean containsMavenUris(final String catalogText) {
        return MAVEN_URI_PATTERN.matcher(catalogText).find();
    }

    /**
     * Reads {@code originalCatalog}, resolves all {@code maven:} URIs within it to
     * {@code jar:file://} URLs, and writes the result to {@code resolvedCatalog}.
     *
     * <p>If the catalog does not contain any {@code maven:} URIs this method simply returns
     * {@code originalCatalog} without touching the filesystem.</p>
     *
     * @param originalCatalog the user-supplied catalog file
     * @param resolvedCatalog the target file for the pre-processed catalog in the build directory
     * @return {@code resolvedCatalog} when any {@code maven:} URIs were found and replaced,
     *         or {@code originalCatalog} if the file contained no {@code maven:} URIs
     * @throws MojoExecutionException if an artifact cannot be resolved or the file cannot be written
     */
    public File resolve(final File originalCatalog, final File resolvedCatalog) throws MojoExecutionException {

        final String originalText;
        try {
            originalText = new String(Files.readAllBytes(originalCatalog.toPath()), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new MojoExecutionException("Cannot read catalog file [" + originalCatalog.getAbsolutePath() + "]", e);
        }

        // Fast path: nothing to do when no maven: URIs are present.
        if (!containsMavenUris(originalText)) {
            if (log.isDebugEnabled()) {
                log.debug("Catalog [" + originalCatalog.getName() + "] contains no maven: URIs; "
                        + "passing it to XJC unchanged.");
            }
            return originalCatalog;
        }

        log.info("Resolving maven: URIs in catalog [" + originalCatalog.getAbsolutePath() + "]");

        // Resolve each unique maven: coordinate to a local jar: URL.
        // We cache per-coordinate to avoid duplicate Aether round-trips for the same artifact.
        final Map<String, String> mavenToJarUrl = new LinkedHashMap<>();

        final Matcher m = MAVEN_URI_PATTERN.matcher(originalText);
        while (m.find()) {
            final String fullMatch = m.group(0); // the full maven:…!/path token
            if (mavenToJarUrl.containsKey(fullMatch)) {
                continue; // already resolved
            }

            final String groupId = m.group(1);
            final String artifactId = m.group(2);
            // group(3) = type segment (may be null or empty → default to "jar")
            final String type = (m.group(3) == null || m.group(3).isEmpty()) ? "jar" : m.group(3);
            // group(4) = classifier segment (may be null or empty → no classifier)
            final String classifier = (m.group(4) == null || m.group(4).isEmpty()) ? "" : m.group(4);
            // group(5) = path-in-jar (always starts with /)
            final String pathInJar = m.group(5);

            final String coordinate =
                    groupId + ":" + artifactId + ":" + type + (classifier.isEmpty() ? "" : ":" + classifier);

            if (log.isDebugEnabled()) {
                log.debug("  Resolving maven: artifact [" + coordinate + "] for path [" + pathInJar + "]");
            }

            final File artifactFile = resolveArtifact(groupId, artifactId, type, classifier, coordinate);

            // Produce a jar: URL pointing into the resolved artifact.
            // We use toURI().toASCIIString() to encode any special characters in the path.
            final String jarUrl = "jar:" + artifactFile.toURI().toASCIIString() + "!" + pathInJar;

            if (log.isDebugEnabled()) {
                log.debug("  Resolved [" + fullMatch + "] -> [" + jarUrl + "]");
            }

            mavenToJarUrl.put(fullMatch, jarUrl);
        }

        // Replace all maven: tokens with their resolved jar: URLs.
        String resolvedText = originalText;
        for (Map.Entry<String, String> entry : mavenToJarUrl.entrySet()) {
            // Escape the key for regex replacement (it contains : and / which are regex metacharacters).
            resolvedText = resolvedText.replace(entry.getKey(), entry.getValue());
        }

        // Write the resolved catalog to the build directory.
        try {
            final File parentDir = resolvedCatalog.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                Files.createDirectories(parentDir.toPath());
            }
            Files.write(resolvedCatalog.toPath(), resolvedText.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new MojoExecutionException(
                    "Cannot write resolved catalog to [" + resolvedCatalog.getAbsolutePath() + "]", e);
        }

        log.info("Resolved catalog written to [" + resolvedCatalog.getAbsolutePath() + "]");
        return resolvedCatalog;
    }

    /**
     * Resolves a single Maven artifact via Aether and returns its local file.
     *
     * @param groupId    the artifact group ID
     * @param artifactId the artifact artifact ID
     * @param type       the artifact type (e.g. {@code jar})
     * @param classifier the artifact classifier, or empty string for none
     * @param coordinate human-readable coordinate for error messages
     * @return the resolved local artifact file
     * @throws MojoExecutionException if the artifact cannot be resolved
     */
    private File resolveArtifact(
            final String groupId,
            final String artifactId,
            final String type,
            final String classifier,
            final String coordinate)
            throws MojoExecutionException {

        final org.apache.maven.artifact.Artifact projectArtifact =
                findArtifactFromProject(groupId, artifactId, type, classifier, coordinate);

        // Fast path: if Maven already resolved the artifact file (e.g. from the local repository
        // or a reactor build module) and it is a regular file on disk, return it directly.
        if (projectArtifact.getFile() != null && projectArtifact.getFile().isFile()) {
            return projectArtifact.getFile();
        }

        // Use null classifier when empty so Aether resolves the artifact without one.
        final String aetherClassifier = classifier.isEmpty() ? null : classifier;

        final org.eclipse.aether.artifact.Artifact artifact;
        try {
            artifact = new DefaultArtifact(groupId, artifactId, aetherClassifier, type, projectArtifact.getVersion());
        } catch (final IllegalArgumentException e) {
            throw new MojoExecutionException(
                    "Invalid artifact coordinate in catalog maven: URI [" + coordinate + "]: " + e.getMessage(), e);
        }

        if (repositorySystem == null) {
            throw new MojoExecutionException("Cannot resolve maven: catalog artifact [" + coordinate
                    + "] because RepositorySystem is null and artifact file has not been materialized.");
        }

        final ArtifactRequest request = new ArtifactRequest();
        request.setArtifact(artifact);
        request.setRepositories(remoteRepositories);

        try {
            final ArtifactResult result = repositorySystem.resolveArtifact(session, request);
            final File artifactFile = result.getArtifact().getFile();
            if (artifactFile == null || !artifactFile.isFile()) {
                throw new MojoExecutionException("Resolved artifact [" + coordinate + "] has no readable local file.");
            }
            return artifactFile;
        } catch (final ArtifactResolutionException e) {
            throw new MojoExecutionException(
                    "Cannot resolve maven: catalog artifact [" + coordinate
                            + "]. Ensure it is declared as a project dependency.",
                    e);
        }
    }

    /**
     * Finds an artifact already present in the project's resolved dependency set.
     *
     * <p>The {@code maven:} URI scheme does not include a version — the version is inferred from
     * the project's declared dependencies. This method locates the artifact among the project's
     * resolved artifacts and returns it so that its file or version can be used.</p>
     *
     * @param groupId    the artifact group ID to look up
     * @param artifactId the artifact artifact ID to look up
     * @param type       the artifact type
     * @param classifier the artifact classifier, empty string if none
     * @param coordinate human-readable coordinate for error messages
     * @return the matching resolved project artifact
     * @throws MojoExecutionException if no matching dependency is found
     */
    private org.apache.maven.artifact.Artifact findArtifactFromProject(
            final String groupId,
            final String artifactId,
            final String type,
            final String classifier,
            final String coordinate)
            throws MojoExecutionException {

        if (projectArtifacts == null) {
            throw new MojoExecutionException("Project artifacts have not been provided to MavenUriCatalogResolver. "
                    + "This is a plugin bug — please report it.");
        }

        for (final org.apache.maven.artifact.Artifact a : projectArtifacts) {
            if (!groupId.equals(a.getGroupId())) continue;
            if (!artifactId.equals(a.getArtifactId())) continue;
            if (!type.equals(a.getType())) continue;
            final String aClassifier = a.getClassifier() == null ? "" : a.getClassifier();
            if (!classifier.equals(aClassifier)) continue;

            return a;
        }

        throw new MojoExecutionException("No dependency matching the maven: catalog URI [" + coordinate
                + "] was found among the project's resolved artifacts. "
                + "Declare the artifact as a <dependency> in your pom.xml so its version "
                + "is known and its JAR is available in the local repository.");
    }

    /**
     * The set of resolved project artifacts, used by {@link #findVersionFromProject} to look up
     * the version for artifact coordinates that appear in {@code maven:} URIs.
     */
    private java.util.Set<org.apache.maven.artifact.Artifact> projectArtifacts;

    /**
     * Provides the set of resolved project artifacts.
     *
     * <p>Must be called before {@link #resolve} when the catalog may contain {@code maven:} URIs.
     * Typically populated from {@code MavenProject.getArtifacts()} in the calling mojo.</p>
     *
     * @param projectArtifacts the resolved artifacts of the current Maven project
     */
    public void setProjectArtifacts(final java.util.Set<org.apache.maven.artifact.Artifact> projectArtifacts) {
        this.projectArtifacts = projectArtifacts;
    }
}
