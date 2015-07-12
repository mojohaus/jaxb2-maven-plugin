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
 * <p>Mojo that creates XML schema(s) from test-scope Java testSources or binaries
 * by invoking the JAXB SchemaGenerator. This implementation is tailored to use the
 * JAXB Reference Implementation from project Kenai.</p>
 * <p>Note that the TestSchemaGenerationMojo was completely re-implemented for the 2.x versions.
 * Its configuration semantics and parameter set is <strong>not backwards compatible</strong>
 * with the 1.x plugin versions. If you are upgrading from version 1.x of the plugin, read
 * the documentation carefully.</p>
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>
 * @see <a href="https://jaxb.java.net/">The JAXB Reference Implementation</a>
 */
@Mojo(name = "testSchemagen",
        defaultPhase = LifecyclePhase.GENERATE_TEST_RESOURCES,
        requiresDependencyResolution = ResolutionScope.TEST,
        threadSafe = true)
public class TestSchemaGenerationMojo extends AbstractXsdGeneratorMojo {

    /**
     * The last part of the stale fileName for this TestSchemaGenerationMojo.
     */
    public static final String STALE_FILENAME = "testSchemaGenerationStaleFlag";

    /**
     * Default exclude file name suffixes for testSources, used unless overridden by an
     * explicit configuration in the {@code testSchemaSourceExcludeFilters} parameter.
     */
    public static final List<Filter<File>> STANDARD_TEST_SOURCE_EXCLUDE_FILTERS;

    static {

        final List<Filter<File>> testSrcTemp = new ArrayList<Filter<File>>();
        testSrcTemp.addAll(AbstractJaxbMojo.STANDARD_EXCLUDE_FILTERS);
        testSrcTemp.add(new PatternFileFilter(Arrays.asList("\\.xjb", "\\.xsd", "\\.properties"), true));
        STANDARD_TEST_SOURCE_EXCLUDE_FILTERS = Collections.unmodifiableList(testSrcTemp);
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
     *       &lt;testSources>
     *          &lt;testSource>/a/full/absolute/path/to/a/SourceFile.java&lt;/testSource>
     *          &lt;testSource>target/some/sourceJar.jar&lt;/test>
     *          &lt;testSource>src/test/java&lt;/test>
     *      &lt;/testSources>
     *   &lt;/configuration>
     * </code>
     * </pre>
     * <p><strong>Note</strong>: if configured, the sources parameters replace the default
     * value, which is a List containing the paths to the directories defined by
     * {@code getProject().getBuild().getTestSourceDirectory()}.</p>
     *
     * @since 2.0
     */
    @Parameter(required = false)
    private List<String> testSources;

    /**
     * <p>Parameter holding a List of Filters, used to match all files under the {@code testSources} directories
     * which should <strong>not</strong> be considered SchemaGenerator testSource files. (The filters identify files to
     * exclude, and hence this parameter is called {@code testSchemaSourceExcludeFilters}). If a file under any of the
     * testSource directories matches at least one of the Filters supplied in the
     * {@code testSchemaSourceExcludeFilters}, it is not considered an XJC test source file, and therefore excluded
     * from processing.</p>
     * <p>If not explicitly provided, the Mojo uses the value within {@code STANDARD_TEST_SOURCE_EXCLUDE_FILTERS}.
     * The algorithm for finding XJC sources is as follows:</p>
     * <ol>
     * <li>Find all files given in the testSources List. Any Directories provided are searched for files
     * recursively.</li>
     * <li>Exclude any found files matching any of the supplied {@code testSchemaSourceExcludeFilters} List.</li>
     * <li>The remaining Files are submitted for processing by the XJC tool.</li>
     * </ol>
     * <p><strong>Example:</strong> The following configuration would exclude any testSources whose names end with
     * {@code .txt} or {@code .foo}:</p>
     * <pre>
     *     <code>
     *         &lt;configuration>
     *         ...
     *              &lt;testSchemaSourceExcludeFilters>
     *                  &lt;filter implementation="org.codehaus.mojo.jaxb2.shared.filters.pattern.PatternFileFilter">
     *                      &lt;patterns>
     *                          &lt;pattern>\.txt&lt;/pattern>
     *                          &lt;pattern>\.foo&lt;/pattern>
     *                      &lt;/patterns>
     *                  &lt;/filter>
     *              &lt;/testSchemaSourceExcludeFilters>
     *         &lt;/configuration>
     *     </code>
     * </pre>
     * <p>Note that inner workings of the Dependency Injection mechanism used by Maven Plugins (i.e. the DI from
     * the Plexus container) requires that the full class name to the Filter implementation should be supplied for
     * each filter, as is illustrated in the sample above. This is true also if you implement custom Filters.</p>
     *
     * @see #STANDARD_TEST_SOURCE_EXCLUDE_FILTERS
     * @see org.codehaus.mojo.jaxb2.shared.filters.pattern.PatternFileFilter
     * @see org.codehaus.mojo.jaxb2.shared.filters.pattern.AbstractPatternFilter
     * @see org.codehaus.mojo.jaxb2.shared.filters.AbstractFilter
     */
    @Parameter(required = false)
    private List<Filter<File>> testSchemaSourceExcludeFilters;

    /**
     * <p>The directory where the generated XML Schema file(s) will be placed.</p>
     */
    @Parameter(defaultValue = "${project.build.directory}/generated-test-resources/schemagen", required = true)
    private File outputDirectory;

    /**
     * <p>The directory where the {@code schemagen} tool will output XSDs, episode files - and intermediary bytecode
     * files. From this directory the XSDs and the episode file (but not the bytecode files) will be copied to the
     * outputDirectory for further processing.</p>
     *
     * @see #outputDirectory
     */
    @Parameter(defaultValue = "${project.build.directory}/schemagen-work/test_scope", required = true)
    private File testWorkDirectory;

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<URL> getCompiledClassNames() {

        final List<Filter<File>> excludeFilters = testSchemaSourceExcludeFilters == null
                ? STANDARD_BYTECODE_EXCLUDE_FILTERS
                : testSchemaSourceExcludeFilters;
        Filters.initialize(getLog(), excludeFilters);

        final List<String> defaultTestSources = getProject().getTestCompileSourceRoots();
        try {
            return FileSystemUtilities.filterFiles(
                    getProject().getBasedir(),
                    (testSources == null) ? defaultTestSources : testSources,
                    defaultTestSources,
                    getLog(),
                    "test-compiled bytecode",
                    excludeFilters);
        } catch (Exception e) {
            throw new IllegalStateException("Could not resolve test classpath elements.", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<String> getClasspath() throws MojoExecutionException {

        List<String> toReturn = null;
        try {
            toReturn = getProject().getTestClasspathElements();
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
    protected List<URL> getSources() {

        final List<Filter<File>> excludeFilters = testSchemaSourceExcludeFilters == null
                ? STANDARD_TEST_SOURCE_EXCLUDE_FILTERS
                : testSchemaSourceExcludeFilters;
        Filters.initialize(getLog(), excludeFilters);

        return FileSystemUtilities.filterFiles(
                getProject().getBasedir(),
                testSources,
                getProject().getTestCompileSourceRoots(),
                getLog(),
                "test schema sources",
                excludeFilters);
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
    protected String getStaleFileName() {
        return STALE_FILENAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected File getWorkDirectory() {
        return testWorkDirectory;
    }
}
