package org.codehaus.mojo.jaxb2.schemageneration;

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

import org.apache.maven.artifact.DependencyResolutionRequiredException;
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

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * <p>Mojo that creates XML schema(s) from compile-scope Java sources or binaries
 * by invoking the JAXB SchemaGenerator. This implementation is tailored to use the
 * JAXB Reference Implementation from project Kenai.</p>
 * <p>Note that the SchemaGenerationMojo was completely re-implemented for the 2.x versions.
 * Its configuration semantics and parameter set is <strong>not necessarily
 * backwards compatible</strong> with the 1.x plugin versions. If you are
 * upgrading from version 1.x of the plugin, read the documentation carefully.</p>
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>
 * @see <a href="https://jaxb.java.net/">The JAXB Reference Implementation</a>
 */
@Mojo(name = "schemagen",
        defaultPhase = LifecyclePhase.GENERATE_RESOURCES,
        requiresDependencyResolution = ResolutionScope.COMPILE,
        threadSafe = true)
public class SchemaGenerationMojo extends AbstractXsdGeneratorMojo {

    /**
     * The last part of the stale fileName for this SchemaGenerationMojo.
     */
    public static final String STALE_FILENAME = "schemaGenerationStaleFlag";

    /**
     * Default exclude file name suffixes for sources, used unless overridden by an
     * explicit configuration in the {@code sourceExcludeSuffixes} parameter.
     */
    public static final List<Filter<File>> STANDARD_SOURCE_EXCLUDE_FILTERS;

    static {

        final List<Filter<File>> srcTemp = new ArrayList<Filter<File>>();
        srcTemp.addAll(AbstractJaxbMojo.STANDARD_EXCLUDE_FILTERS);
        srcTemp.add(new PatternFileFilter(Arrays.asList("\\.xjb", "\\.xsd", "\\.properties"), true));
        STANDARD_SOURCE_EXCLUDE_FILTERS = Collections.unmodifiableList(srcTemp);
    }

    /**
     * <p>Parameter holding List of paths to files and/or directories which should be recursively searched
     * for Java source files. Only files or directories that actually exist will be included (in the case of files)
     * or recursively searched for source files to include (in the case of directories or JARs).
     * Configure using standard Maven structure for Lists:</p>
     * <pre>
     * <code>
     *   &lt;configuration>
     *   ...
     *       &lt;sources>
     *          &lt;source>/a/full/absolute/path/to/a/SourceFile.java&lt;/source>
     *          &lt;source>target/some/sourceJar.jar&lt;/source>
     *          &lt;source>src/main/java&lt;/source>
     *      &lt;/sources>
     *   &lt;/configuration>
     * </code>
     * </pre>
     * <p><strong>Note</strong>: if configured, the sources parameters replace the default
     * value, which is the single directory {@code getProject().getCompileSourceRoots()}.</p>
     *
     * @since 2.0
     */
    @Parameter(required = false)
    private List<String> sources;

    /**
     * <p>Parameter holding a List of Filters, used to match all files under the {@code sources} directories
     * which should <strong>not</strong> be considered SchemaGenerator source files. (The filters identify files to
     * exclude, and hence this parameter is called {@code schemaSourceExcludeFilters}). If a file under any of the
     * source directories matches at least one of the Filters supplied in the {@code schemaSourceExcludeFilters},
     * it is not considered an XJC source file, and therefore excluded from processing.</p>
     * <p>If not explicitly provided, the Mojo uses the value within {@code STANDARD_SOURCE_EXCLUDE_FILTERS}.
     * The algorithm for finding XJC sources is as follows:</p>
     * <ol>
     * <li>Find all files given in the sources List. Any Directories provided are searched for files
     * recursively.</li>
     * <li>Exclude any found files matching any of the supplied {@code schemaSourceExcludeFilters} List.</li>
     * <li>The remaining Files are submitted for processing by the XJC tool.</li>
     * </ol>
     * <p><strong>Example:</strong> The following configuration would exclude any sources whose names end with
     * {@code .txt} or {@code .foo}:</p>
     * <pre>
     *     <code>
     *         &lt;configuration>
     *         ...
     *              &lt;schemaSourceExcludeFilters>
     *                  &lt;filter implementation="org.codehaus.mojo.jaxb2.shared.filters.pattern.PatternFileFilter">
     *                      &lt;patterns>
     *                          &lt;pattern>\.txt&lt;/pattern>
     *                          &lt;pattern>\.foo&lt;/pattern>
     *                      &lt;/patterns>
     *                  &lt;/filter>
     *              &lt;/schemaSourceExcludeFilters>
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
     * @since 2.0
     */
    @Parameter(required = false)
    private List<Filter<File>> schemaSourceExcludeFilters;

    /**
     * <p>The directory where the generated XML Schema file(s) will be
     * placed, after all transformations are done.</p>
     */
    @Parameter(defaultValue = "${project.build.directory}/generated-resources/schemagen", required = true)
    private File outputDirectory;

    /**
     * <p>The directory where the {@code schemagen} tool will output XSDs, episode files - and intermediary bytecode
     * files. From this directory the XSDs and the episode file (but not the bytecode files) will be copied to the
     * outputDirectory for further processing.</p>
     *
     * @see #outputDirectory
     */
    @Parameter(defaultValue = "${project.build.directory}/schemagen-work/compile_scope", required = true)
    private File workDirectory;

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<URL> getCompiledClassNames() {

        List<Filter<File>> excludeFilters = schemaSourceExcludeFilters == null
                ? STANDARD_BYTECODE_EXCLUDE_FILTERS
                : schemaSourceExcludeFilters;
        Filters.initialize(getLog(), excludeFilters);

        try {
            return FileSystemUtilities.filterFiles(
                    getProject().getBasedir(),
                    null,
                    getProject().getCompileClasspathElements(),
                    getLog(),
                    "compiled bytecode",
                    excludeFilters);
        } catch (DependencyResolutionRequiredException e) {
            throw new IllegalStateException("Could not resolve dependencies.", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<URL> getSources() {

        /*
        // TODO: Add source-classified Artifacts in classpath?
        for(Artifact current : (Set<Artifact>) getProject().getDependencyArtifacts()) {
            final ArtifactRepository repository = current.getRepository();
        }
        */

        final List<Filter<File>> sourceExcludes = schemaSourceExcludeFilters == null
                ? STANDARD_SOURCE_EXCLUDE_FILTERS
                : schemaSourceExcludeFilters;
        Filters.initialize(getLog(), sourceExcludes);

        final List<String> defaultSources = getProject().getCompileSourceRoots();

        // All Done.
        return FileSystemUtilities.filterFiles(
                getProject().getBasedir(),
                (sources == null) ? defaultSources : sources,
                defaultSources,
                getLog(),
                "sources",
                sourceExcludes);
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

        List<String> toReturn = null;
        try {
            toReturn = getProject().getCompileClasspathElements();
        } catch (DependencyResolutionRequiredException e) {
            throw new MojoExecutionException("Could not acquire compile classpath elements from MavenProject", e);
        }

        // All done.
        return toReturn;
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
    protected File getWorkDirectory() {
        return workDirectory;
    }
}
