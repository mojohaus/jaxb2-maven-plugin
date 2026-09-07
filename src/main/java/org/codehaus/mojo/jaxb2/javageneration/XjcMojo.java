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

import javax.inject.Inject;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.codehaus.mojo.jaxb2.AbstractJaxbMojo;
import org.codehaus.mojo.jaxb2.shared.FileSystemUtilities;
import org.codehaus.mojo.jaxb2.shared.filters.Filter;
import org.codehaus.mojo.jaxb2.shared.filters.Filters;
import org.codehaus.mojo.jaxb2.shared.filters.pattern.PatternFileFilter;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;

/**
 * <p>Mojo that creates compile-scope Java source or binaries from XML schema(s)
 * by invoking the JAXB XJC binding compiler. This implementation is tailored
 * to use the JAXB Reference Implementation from project Kenai.</p>
 * <p>Note that the XjcMojo was completely re-implemented for the 2.x versions.
 * Its configuration semantics and parameter set is <strong>not necessarily
 * backwards compatible</strong> with the 1.x plugin versions. If you are
 * upgrading from version 1.x of the plugin, read the documentation carefully.</p>
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>
 * @see <a href="https://jaxb.java.net/">The JAXB Reference Implementation</a>
 */
@Mojo(
        name = "xjc",
        threadSafe = true,
        defaultPhase = LifecyclePhase.GENERATE_SOURCES,
        requiresDependencyResolution = ResolutionScope.COMPILE)
public class XjcMojo extends AbstractJavaGeneratorMojo {

    /**
     * The last part of the stale fileName for this XjcMojo.
     */
    public static final String STALE_FILENAME = "xjcStaleFlag";

    /**
     * <p>Standard directory path (relative to basedir) searched recursively for source
     * files (typically XSDs), unless overridden by an <code>sources</code> configuration element.</p>
     */
    public static final String STANDARD_SOURCE_DIRECTORY = "src/main/xsd";

    /**
     * <p>Default exclude Filters for sources, used unless overridden by an
     * explicit configuration in the {@code xjcSourceExcludeFilters} parameter.
     * The default values are found as follows:</p>
     * <pre>
     *     <code>
     *         final List&lt;Filter&lt;File&gt;&gt; xsdTemp = new ArrayList&lt;Filter&lt;File&gt;&gt;();
     *         xsdTemp.addAll(AbstractJaxbMojo.STANDARD_EXCLUDE_FILTERS);
     *         xsdTemp.add(new PatternFileFilter(Arrays.asList("\\.xjb"), true));
     *         STANDARD_SOURCE_EXCLUDE_FILTERS = Collections.unmodifiableList(xsdTemp);
     *     </code>
     * </pre>
     * @see #STANDARD_EXCLUDE_FILTERS
     */
    public static final List<Filter<File>> STANDARD_SOURCE_EXCLUDE_FILTERS;

    /**
     * <p>Standard directory path (relative to basedir) searched recursively for XJB
     * files, unless overridden by an <code>xjbSources</code> configuration element.
     * As explained in the JAXB specification, XJB files (JAXB Xml Binding files)
     * are used to configure parts of the Java source generation.</p>
     */
    public static final String STANDARD_XJB_DIRECTORY = "src/main/xjb";

    /**
     * <p>Default List of exclude Filters for XJB files, unless overridden by providing
     * an explicit configuration in the {@code xjbExcludeSuffixes} parameter.
     * The default values are found as follows:</p>
     * <pre>
     *     <code>
     *         final List&lt;Filter&lt;File&gt;&gt; xjbTemp = new ArrayList&lt;Filter&lt;File&gt;&gt;();
     *         xjbTemp.addAll(AbstractJaxbMojo.STANDARD_EXCLUDE_FILTERS);
     *         xjbTemp.add(new PatternFileFilter(Arrays.asList("\\.xsd"), true));
     *         STANDARD_XJB_EXCLUDE_FILTERS = Collections.unmodifiableList(xjbTemp);
     *     </code>
     * </pre>
     * @see #STANDARD_EXCLUDE_FILTERS
     */
    public static final List<Filter<File>> STANDARD_XJB_EXCLUDE_FILTERS;

    static {
        final List<Filter<File>> xjbTemp = new ArrayList<Filter<File>>();
        xjbTemp.addAll(AbstractJaxbMojo.STANDARD_EXCLUDE_FILTERS);
        xjbTemp.add(new PatternFileFilter(Arrays.asList("\\.xsd"), true));
        STANDARD_XJB_EXCLUDE_FILTERS = Collections.unmodifiableList(xjbTemp);

        final List<Filter<File>> xsdTemp = new ArrayList<Filter<File>>();
        xsdTemp.addAll(AbstractJaxbMojo.STANDARD_EXCLUDE_FILTERS);
        xsdTemp.add(new PatternFileFilter(Arrays.asList("\\.xjb"), true));
        STANDARD_SOURCE_EXCLUDE_FILTERS = Collections.unmodifiableList(xsdTemp);
    }

    /**
     * <p>Corresponding XJC parameter: {@code d}.</p>
     * <p>The working directory where the generated Java source files are created.</p>
     */
    @Parameter(defaultValue = "${project.build.directory}/generated-sources/jaxb", required = true)
    private File outputDirectory;

    /**
     * <p>Parameter holding List of XSD paths to files and/or directories which should be recursively searched
     * for XSD files. Only files or directories that actually exist will be included (in the case of files)
     * or recursively searched for XSD files to include (in the case of directories).
     * Configure using standard Maven structure for Lists:</p>
     * <pre>
     * <code>
     *   &lt;configuration>
     *   ...
     *       &lt;sources>
     *          &lt;source>some/explicit/relative/file.xsd&lt;/source>
     *          &lt;source>/another/absolute/path/to/a/specification.xsd&lt;/source>
     *          &lt;source>a/directory/holding/xsds&lt;/source>
     *      &lt;/sources>
     *   &lt;/configuration>
     * </code>
     * </pre>
     *
     * @see #STANDARD_SOURCE_DIRECTORY
     */
    @Parameter(required = false)
    private List<String> sources;

    /**
     * <p>Parameter holding List of XJB Files and/or directories which should be recursively searched
     * for XJB files. Only files or directories that actually exist will be included (in the case of files)
     * or recursively searched for XJB files to include (in the case of directories). JAXB binding files are
     * used to configure parts of the Java source generation.
     * Supply the configuration using the standard Maven structure for configuring plugin Lists:</p>
     * <pre>
     * <code>
     *   &lt;configuration>
     *   ...
     *       &lt;xjbSources>
     *          &lt;xjbSource>bindings/aBindingConfiguration.xjb&lt;/xjbSource>
     *          &lt;xjbSource>bindings/config/directory&lt;/xjbSource>
     *      &lt;/xjbSources>
     *   &lt;/configuration>
     * </code>
     * </pre>
     *
     * @see #STANDARD_XJB_DIRECTORY
     */
    @Parameter(required = false)
    private List<String> xjbSources;

    /**
     * <p>Parameter holding a List of Maven artifacts whose JAXB episode files should be used as
     * external bindings by XJC. This enables modular JAXB compilation: a schema module A can
     * generate its Java model and episode file, and a downstream module B can compile its own
     * schema against A's namespace while reusing A's generated types instead of generating
     * duplicates.</p>
     * <p>Each episode artifact is resolved using Maven Resolver; the episode resource is located
     * within the artifact (see {@link EpisodeJarSupport} for the discovery order) and materialized
     * beneath {@code episodeExtractionDirectory} before being handed to XJC as an external
     * binding file.</p>
     * <p><strong>Note:</strong> an episode only prevents duplicate Java class generation. The
     * imported XSD itself must still be resolvable by XJC, through a regular {@code schemaLocation},
     * an XML catalog, or an unpack step.</p>
     * <p><strong>Example:</strong></p>
     * <pre>
     *     <code>
     *         &lt;episodes&gt;
     *             &lt;episode&gt;
     *                 &lt;groupId&gt;com.acme.schemas&lt;/groupId&gt;
     *                 &lt;artifactId&gt;schema-a&lt;/artifactId&gt;
     *                 &lt;version&gt;1.4.0&lt;/version&gt;
     *             &lt;/episode&gt;
     *         &lt;/episodes&gt;
     *     </code>
     * </pre>
     *
     * @see EpisodeArtifact
     * @see EpisodeJarSupport
     * @since 4.1.1
     */
    @Parameter(required = false)
    private List<EpisodeArtifact> episodes;

    /**
     * <p>When {@code true}, all eligible project dependencies (by default within the {@code compile}
     * or {@code runtime} scopes, and non-transitive unless {@code includeTransitiveEpisodes} is set)
     * are scanned for JAXB episode resources, which are then used as external bindings by XJC.
     * Ordinary dependencies without episode resources are ignored. Defaults to {@code false} for
     * safety: explicit {@code episodes} declarations make the code-generation boundary visible.</p>
     *
     * @see #episodes
     * @see #includeTransitiveEpisodes
     * @since 4.1.1
     */
    @Parameter(defaultValue = "false")
    private boolean useDependenciesAsEpisodes;

    /**
     * <p>When {@code useDependenciesAsEpisodes} is {@code true}, this flag controls whether
     * transitive dependencies are also scanned for JAXB episode resources, or only direct project
     * dependencies. Defaults to {@code false}.</p>
     *
     * @see #useDependenciesAsEpisodes
     * @since 4.1.1
     */
    @Parameter(defaultValue = "false")
    private boolean includeTransitiveEpisodes;

    /**
     * <p>The directory beneath which episode resources from dependency artifacts are materialized,
     * in a deterministic {@code groupId/artifactId/version} layout. Defaults to
     * {@code ${project.build.directory}/jaxb2/episodes}.</p>
     *
     * @since 4.1.1
     */
    @Parameter(defaultValue = "${project.build.directory}/jaxb2/episodes")
    private File episodeExtractionDirectory;

    /**
     * The Maven Resolver service, injected via JSR-330, used to resolve explicit episode artifacts.
     */
    @Inject
    private RepositorySystem repositorySystem;

    /**
     * The session-scoped Maven Resolver session for the current build.
     */
    @Parameter(defaultValue = "${repositorySystemSession}", readonly = true)
    private RepositorySystemSession repositorySystemSession;

    /**
     * The remote repositories declared by the project, used when resolving explicit episode artifacts.
     */
    @Parameter(defaultValue = "${project.remoteProjectRepositories}", readonly = true)
    private List<RemoteRepository> remoteProjectRepositories;

    /**
     * Memoized episode binding files, resolved at most once per mojo execution.
     */
    private List<File> episodeBindings;

    /**
     * <p>Parameter holding a List of Filters, used to match all files under the {@code sources} directories
     * which should <strong>not</strong> be considered XJC source files. (The filters identify files to
     * exclude, and hence this parameter is called {@code xjcSourceExcludeFilters}). If a file under any of the
     * source directories matches at least one of the Filters supplied in the {@code xjcSourceExcludeFilters},
     * it is not considered an XJC source file, and therefore excluded from processing.</p>
     * <p>If not explicitly provided, the Mojo uses the value within {@code STANDARD_SOURCE_EXCLUDE_FILTERS}.
     * The algorithm for finding XJC sources is as follows:</p>
     * <ol>
     * <li>Find all files given in the sources List. Any Directories provided are searched for files
     * recursively.</li>
     * <li>Exclude any found files matching any of the supplied {@code xjcSourceExcludeFilters} List.</li>
     * <li>The remaining Files are submitted for processing by the XJC tool.</li>
     * </ol>
     * <p><strong>Example:</strong> The following configuration would exclude any sources whose names end with
     * {@code txt} or {@code foo}:</p>
     * <pre>
     *     <code>
     *         &lt;configuration>
     *         ...
     *              &lt;xjcSourceExcludeFilters>
     *                  &lt;filter implementation="org.codehaus.mojo.jaxb2.shared.filters.pattern.PatternFileFilter">
     *                      &lt;patterns>
     *                          &lt;pattern>\.txt&lt;/pattern>
     *                          &lt;pattern>\.foo&lt;/pattern>
     *                      &lt;/patterns>
     *                  &lt;/filter>
     *              &lt;/xjcSourceExcludeFilters>
     *         &lt;/configuration>
     *     </code>
     * </pre>
     * <p>Note that inner workings of the Dependency Injection mechanism used by Maven Plugins (i.e. the DI from
     * the Plexus container) requires that the full class name to the Filter implementation should be supplied for
     * each filter, as is illustrated in the sample above. This is true also if you implement custom Filters.</p>
     *
     * @see #STANDARD_SOURCE_EXCLUDE_FILTERS
     * @see org.codehaus.mojo.jaxb2.shared.filters.pattern.PatternFileFilter
     * @see org.codehaus.mojo.jaxb2.shared.filters.pattern.AbstractPatternFilter
     * @see org.codehaus.mojo.jaxb2.shared.filters.AbstractFilter
     */
    @Parameter(required = false)
    private List<Filter<File>> xjcSourceExcludeFilters;

    /**
     * <p>Parameter holding a List of Filters, used to match all files under the {@code xjbSources} directories
     * which should <strong>not</strong> be considered XJB files. (The filters identify files to exclude, and hence
     * this parameter is called {@code xjbExcludeFilters}). If a file matches at least one of the supplied Filters,
     * it is not considered an XJB file, and therefore excluded from processing.</p>
     * <p>If not explicitly provided, the Mojo uses the value within {@code STANDARD_XJB_EXCLUDE_FILTERS}.</p>
     * <p><strong>Example:</strong> The following configuration would exclude any XJB files whose names end with
     * {@code xml} or {@code foo}:</p>
     * <pre>
     *     <code>
     *         &lt;configuration>
     *         ...
     *              &lt;xjbExcludeFilters>
     *                  &lt;filter implementation="org.codehaus.mojo.jaxb2.shared.filters.pattern.PatternFileFilter">
     *                      &lt;patterns>
     *                          &lt;pattern>\.txt&lt;/pattern>
     *                          &lt;pattern>\.foo&lt;/pattern>
     *                      &lt;/patterns>
     *                  &lt;/filter>
     *              &lt;/xjbExcludeFilters>
     *         ...
     *         &lt;/configuration>
     *     </code>
     * </pre>
     * <p>Note that inner workings of the Dependency Injection mechanism used by Maven Plugins (i.e. the DI from
     * the Plexus container) requires that the full class name to the Filter implementation should be supplied for
     * each filter, as is illustrated in the sample above. This is true also if you implement custom Filters.</p>
     *
     * @see #STANDARD_XJB_EXCLUDE_FILTERS
     * @see org.codehaus.mojo.jaxb2.shared.filters.pattern.PatternFileFilter
     * @see org.codehaus.mojo.jaxb2.shared.filters.pattern.AbstractPatternFilter
     * @see org.codehaus.mojo.jaxb2.shared.filters.AbstractFilter
     */
    @Parameter(required = false)
    private List<Filter<File>> xjbExcludeFilters;

    /**
     * Indicate if the XjcMojo execution should be skipped.
     */
    @Parameter(property = "xjc.skip", defaultValue = "false")
    private boolean skipXjc;

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean shouldExecutionBeSkipped() {
        return skipXjc;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<URL> getSources() {

        final List<Filter<File>> excludePatterns =
                xjcSourceExcludeFilters == null ? STANDARD_SOURCE_EXCLUDE_FILTERS : xjcSourceExcludeFilters;
        Filters.initialize(getLog(), excludePatterns);

        return FileSystemUtilities.filterFiles(
                getProject().getBasedir(),
                sources,
                Arrays.asList(STANDARD_SOURCE_DIRECTORY),
                getLog(),
                "sources",
                excludePatterns);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<File> getSourceXJBs() throws MojoExecutionException {

        final List<Filter<File>> excludePatterns =
                xjbExcludeFilters == null ? STANDARD_XJB_EXCLUDE_FILTERS : xjbExcludeFilters;
        Filters.initialize(getLog(), excludePatterns);

        final List<File> bindings = new ArrayList<>(FileSystemUtilities.filterFiles(
                getProject().getBasedir(),
                xjbSources,
                STANDARD_XJB_DIRECTORY,
                getLog(),
                "xjbSources",
                excludePatterns));

        bindings.addAll(resolveEpisodeBindings());
        return bindings;
    }

    /**
     * Resolves all configured and/or discovered dependency episode bindings, in deterministic order:
     * <ol>
     *     <li>Explicit {@code episodes} declarations, in configuration order.</li>
     *     <li>Automatically discovered dependency episodes, in artifact coordinate order.</li>
     * </ol>
     * The result is memoized for the remainder of this mojo execution.
     *
     * @return A non-null List of local, existing episode binding files.
     */
    private List<File> resolveEpisodeBindings() throws MojoExecutionException {

        if (episodeBindings != null) {
            return episodeBindings;
        }

        if ((episodes == null || episodes.isEmpty()) && !useDependenciesAsEpisodes) {
            episodeBindings = Collections.emptyList();
            return episodeBindings;
        }

        final List<File> result = new ArrayList<>();
        final Set<String> handled = new LinkedHashSet<>();
        final File extractionRoot = FileSystemUtilities.getCanonicalFile(episodeExtractionDirectory);

        if (episodes != null) {
            for (EpisodeArtifact current : episodes) {
                current.validate();

                final org.eclipse.aether.artifact.Artifact resolved = resolveEpisodeArtifact(current);
                final String entry = EpisodeJarSupport.selectEpisodeEntry(
                        resolved.getFile(), current.getResourcePath(), current.getCoordinate());

                if (entry == null) {
                    if (current.isOptional()) {
                        getLog().warn("No JAXB episode resource found within optional episode artifact "
                                + current.getCoordinate() + "; skipping.");
                        continue;
                    }
                    throw new MojoExecutionException("No JAXB episode resource found within artifact "
                            + current.getCoordinate() + " ["
                            + resolved.getFile().getAbsolutePath()
                            + "]. Configure <resourcePath> if the episode resides at a non-standard location.");
                }

                final File extracted = EpisodeJarSupport.extractEpisodeEntry(
                        resolved.getFile(),
                        entry,
                        extractionRoot.toPath(),
                        current.getGroupId(),
                        current.getArtifactId(),
                        current.getVersion());

                getLog().info("Resolved JAXB episode " + current.getCoordinate() + " (" + entry + ") -> "
                        + extracted.getAbsolutePath());

                result.add(extracted);
                handled.add(current.getCoordinate() + ":" + entry);
            }
        }

        if (useDependenciesAsEpisodes) {
            for (final Map.Entry<Artifact, String> candidate :
                    findDependencyEpisodeCandidates().entrySet()) {
                final Artifact artifact = candidate.getKey();
                final String entry = candidate.getValue();
                final String dedupeKey = coordinateOf(artifact) + ":" + entry;

                if (handled.contains(dedupeKey)) {
                    continue;
                }

                final File extracted = EpisodeJarSupport.extractEpisodeEntry(
                        artifact.getFile(),
                        entry,
                        extractionRoot.toPath(),
                        artifact.getGroupId(),
                        artifact.getArtifactId(),
                        artifact.getVersion());

                getLog().debug("Discovered JAXB episode in dependency " + coordinateOf(artifact) + " (" + entry
                        + ") -> " + extracted.getAbsolutePath());

                result.add(extracted);
                handled.add(dedupeKey);
            }
        }

        episodeBindings = result;
        return episodeBindings;
    }

    /**
     * Scans the project's resolved dependency graph for episode resources. Ordinary dependencies
     * without episode resources are skipped at debug level.
     */
    private Map<Artifact, String> findDependencyEpisodeCandidates() throws MojoExecutionException {

        final Set<String> directDependencyKeys = new LinkedHashSet<>();
        if (!includeTransitiveEpisodes) {
            final List<Dependency> directDependencies = getProject().getDependencies();
            if (directDependencies != null) {
                for (Dependency current : directDependencies) {
                    directDependencyKeys.add(
                            current.getGroupId() + ":" + current.getArtifactId() + ":" + current.getVersion());
                }
            }
        }

        final List<Artifact> eligible = new ArrayList<>();
        for (Artifact current : getProject().getArtifacts()) {
            if (!"jar".equals(current.getType())
                    || current.getFile() == null
                    || !current.getFile().isFile()
                    || !isEligibleScope(current)) {
                continue;
            }
            if (!includeTransitiveEpisodes
                    && !directDependencyKeys.contains(
                            current.getGroupId() + ":" + current.getArtifactId() + ":" + current.getVersion())) {
                getLog().debug("Ignoring transitive dependency " + coordinateOf(current)
                        + " for episode scanning; set includeTransitiveEpisodes to include it.");
                continue;
            }
            eligible.add(current);
        }

        eligible.sort(Comparator.comparing(Artifact::getGroupId)
                .thenComparing(Artifact::getArtifactId)
                .thenComparing(Artifact::getVersion)
                .thenComparing(a -> a.getClassifier() == null ? "" : a.getClassifier()));

        final Map<Artifact, String> candidates = new LinkedHashMap<>();
        for (Artifact current : eligible) {
            final String entry = EpisodeJarSupport.selectEpisodeEntry(current.getFile(), null, coordinateOf(current));
            if (entry != null) {
                candidates.put(current, entry);
            } else {
                getLog().debug("No JAXB episode resource within dependency " + coordinateOf(current) + "; skipping.");
            }
        }
        return candidates;
    }

    private boolean isEligibleScope(final Artifact artifact) {
        final String scope = artifact.getScope();
        return Artifact.SCOPE_COMPILE.equals(scope) || Artifact.SCOPE_RUNTIME.equals(scope);
    }

    private String coordinateOf(final Artifact artifact) {
        final StringBuilder sb = new StringBuilder()
                .append(artifact.getGroupId())
                .append(':')
                .append(artifact.getArtifactId())
                .append(':')
                .append(artifact.getType());
        if (artifact.getClassifier() != null && !artifact.getClassifier().isEmpty()) {
            sb.append(':').append(artifact.getClassifier());
        }
        return sb.append(':').append(artifact.getVersion()).toString();
    }

    private org.eclipse.aether.artifact.Artifact resolveEpisodeArtifact(final EpisodeArtifact episode)
            throws MojoExecutionException {

        final String type = episode.getType() == null || episode.getType().isEmpty() ? "jar" : episode.getType();

        final org.eclipse.aether.artifact.Artifact artifact = new DefaultArtifact(
                episode.getGroupId(), episode.getArtifactId(), episode.getClassifier(), type, episode.getVersion());

        final ArtifactRequest request = new ArtifactRequest();
        request.setArtifact(artifact);
        request.setRepositories(remoteProjectRepositories);

        try {
            final ArtifactResult result = repositorySystem.resolveArtifact(repositorySystemSession, request);
            final File artifactFile = result.getArtifact().getFile();
            if (artifactFile == null || !artifactFile.isFile()) {
                throw new MojoExecutionException("Resolved JAXB episode artifact " + episode.getCoordinate()
                        + " has no readable local file: " + result.getArtifact());
            }
            return result.getArtifact();
        } catch (final ArtifactResolutionException e) {
            throw new MojoExecutionException(
                    "Could not resolve JAXB episode artifact " + episode.getCoordinate()
                            + ". Declare it as a regular <dependency> as well, so the generated code can compile "
                            + "against the reused JAXB model.",
                    e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getStaleFileName() {
        return STALE_FILENAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected File getOutputDirectory() {
        return outputDirectory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<String> getClasspath() throws MojoExecutionException {
        try {
            return (List<String>) getProject().getCompileClasspathElements();
        } catch (DependencyResolutionRequiredException e) {
            throw new MojoExecutionException("Could not retrieve Compile classpath.", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void addGeneratedSourcesToProjectSourceRoot(String canonicalPathToOutputDirectory) {
        getProject().addCompileSourceRoot(canonicalPathToOutputDirectory);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void addResource(final Resource resource) {
        getProject().addResource(resource);
    }
}
