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

import org.apache.maven.artifact.DependencyResolutionRequiredException;
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

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * <p>Mojo that creates test-scope Java source or binaries from XML schema(s)
 * by invoking the JAXB XJC binding compiler. This implementation is tailored
 * to use the JAXB Reference Implementation from project Kenai.</p>
 * <p>Note that the TestXjcMojo was completely re-implemented for the 2.x versions.
 * Its configuration semantics and parameter set is <strong>not necessarily
 * backwards compatible</strong> with the 1.x plugin versions. If you are
 * upgrading from version 1.x of the plugin, read the documentation carefully.</p>
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>
 * @see <a href="https://jaxb.java.net/">The JAXB Reference Implementation</a>
 */
@Mojo(name = "testXjc",
        defaultPhase = LifecyclePhase.GENERATE_TEST_SOURCES,
        requiresDependencyResolution = ResolutionScope.TEST,
        threadSafe = false)
public class TestXjcMojo extends AbstractJavaGeneratorMojo {

    /**
     * The last part of the stale fileName for this TestXjcMojo.
     */
    public static final String STALE_FILENAME = "testXjcStaleFlag";

    /**
     * Standard directory path (relative to basedir) searched recursively for test
     * source files (typically XSDs), unless overridden by a
     * <code>testSources</code> configuration element.
     */
    public static final String STANDARD_TEST_SOURCE_DIRECTORY = "src/test/xsd";

    /**
     * <p>Default exclude Filters for test sources, used unless overridden by an
     * explicit configuration in the {@code testSourceExcludeFilters} parameter.
     * The default values are found as follows:</p>
     * <pre>
     *     <code>
     *         final List&lt;Filter&lt;File&gt;&gt; xjbTemp = new ArrayList&lt;Filter&lt;File&gt;&gt;();
     *         xjbTemp.addAll(AbstractJaxbMojo.STANDARD_EXCLUDE_FILTERS);
     *         xsdTemp.add(new PatternFileFilter(Arrays.asList("\\.xjb"), true));
     *         STANDARD_TEST_SOURCE_EXCLUDE_FILTERS = Collections.unmodifiableList(xsdTemp);
     *     </code>
     * </pre>
     *
     * @see #STANDARD_EXCLUDE_FILTERS
     */
    public static final List<Filter<File>> STANDARD_TEST_SOURCE_EXCLUDE_FILTERS;

    /**
     * <p>Standard directory path (relative to basedir) searched recursively for XJB
     * files, unless overridden by an <code>testXjbSources</code> configuration element.
     * As explained in the JAXB specification, XJB files (JAXB Xml Binding files)
     * are used to configure parts of the Java source generation.</p>
     */
    public static final String STANDARD_TEST_XJB_DIRECTORY = "src/test/xjb";

    /**
     * <p>Default List of exclude Filters for XJB files, unless overridden by providing
     * an explicit configuration in the {@code testXjbExcludeFilters} parameter.
     * The default values are found as follows:</p>
     * <pre>
     *     <code>
     *         final List&lt;Filter&lt;File&gt;&gt; xjbTemp = new ArrayList&lt;Filter&lt;File&gt;&gt;();
     *         xjbTemp.addAll(AbstractJaxbMojo.STANDARD_EXCLUDE_FILTERS);
     *         xjbTemp.add(new PatternFileFilter(Arrays.asList("\\.xsd"), true));
     *         STANDARD_TEST_XJB_EXCLUDE_FILTERS = Collections.unmodifiableList(xjbTemp);
     *     </code>
     * </pre>
     *
     * @see #STANDARD_EXCLUDE_FILTERS
     */
    public static final List<Filter<File>> STANDARD_TEST_XJB_EXCLUDE_FILTERS;

    static {

        final List<Filter<File>> xjbTemp = new ArrayList<Filter<File>>();
        xjbTemp.addAll(AbstractJaxbMojo.STANDARD_EXCLUDE_FILTERS);
        xjbTemp.add(new PatternFileFilter(Arrays.asList("\\.xsd"), true));
        STANDARD_TEST_XJB_EXCLUDE_FILTERS = Collections.unmodifiableList(xjbTemp);

        final List<Filter<File>> xsdTemp = new ArrayList<Filter<File>>();
        xsdTemp.addAll(AbstractJaxbMojo.STANDARD_EXCLUDE_FILTERS);
        xsdTemp.add(new PatternFileFilter(Arrays.asList("\\.xjb"), true));
        STANDARD_TEST_SOURCE_EXCLUDE_FILTERS = Collections.unmodifiableList(xsdTemp);
    }

    /**
     * <p>Corresponding XJC parameter: {@code d}.</p>
     * <p>The working directory where the generated Java test source files are created.</p>
     */
    @Parameter(defaultValue = "${project.build.directory}/generated-test-sources/jaxb", required = true)
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
     *       &lt;testSources>
     *          &lt;testSource>some/explicit/relative/file.xsd&lt;/testSource>
     *          &lt;testSource>/another/absolute/path/to/a/specification.xsd&lt;/testSource>
     *          &lt;testSource>a/directory/holding/xsds&lt;/testSource>
     *      &lt;/testSources>
     *   &lt;/configuration>
     * </code>
     * </pre>
     *
     * @see #STANDARD_TEST_SOURCE_DIRECTORY
     */
    @Parameter(required = false)
    private List<String> testSources;

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
     *       &lt;testXjbSources>
     *          &lt;testXjbSource>bindings/aBindingConfiguration.xjb&lt;/testXjbSource>
     *          &lt;testXjbSource>bindings/config/directory&lt;/testXjbSource>
     *      &lt;/testXjbSources>
     *   &lt;/configuration>
     * </code>
     * </pre>
     *
     * @see #STANDARD_TEST_XJB_DIRECTORY
     */
    @Parameter(required = false)
    private List<String> testXjbSources;

    /**
     * <p>Parameter holding a List of Filters, used to match all files under the {@code testSources} directories
     * which should <strong>not</strong> be considered XJC test source files. (The filters identify files to
     * exclude, and hence this parameter is called {@code testS§ourceExcludeFilters}). If a file under any of the
     * test source directories matches at least one of the Filters supplied in the {@code testSourceExcludeFilters},
     * it is not considered an XJC source file, and therefore excluded from processing.</p>
     * <p>If not explicitly provided, the Mojo uses the value within {@code STANDARD_TEST_SOURCE_EXCLUDE_FILTERS}.
     * The algorithm for finding XJC test sources is as follows:</p>
     * <ol>
     * <li>Find all files given in the testSources List. Any Directories provided are searched for files
     * recursively.</li>
     * <li>Exclude any found files matching any of the supplied {@code testSourceExcludeFilters} List.</li>
     * <li>The remaining Files are submitted for processing by the XJC tool.</li>
     * </ol>
     * <p><strong>Example:</strong> The following configuration would exclude any sources whose names end with
     * {@code txt} or {@code foo}:</p>
     * <pre>
     *     <code>
     *         &lt;configuration>
     *         ...
     *              &lt;testSourceExcludeFilters>
     *                  &lt;suffixFilter impl="org.codehaus.mojo.jaxb2.shared.filters.source.ExclusionRegularExpressionFileFilter">txt&lt;/suffixFilter>
     *                  &lt;suffixFilter impl="org.codehaus.mojo.jaxb2.shared.filters.source.ExclusionRegularExpressionFileFilter">foo&lt;/suffixFilter>
     *              &lt;/testSourceExcludeFilters>
     *         &lt;/configuration>
     *     </code>
     * </pre>
     *
     * @see #STANDARD_TEST_SOURCE_EXCLUDE_FILTERS
     * @see org.codehaus.mojo.jaxb2.shared.filters.pattern.PatternFileFilter
     * @see org.codehaus.mojo.jaxb2.shared.filters.pattern.AbstractPatternFilter
     * @see org.codehaus.mojo.jaxb2.shared.filters.AbstractFilter
     */
    @Parameter(required = false)
    private List<Filter<File>> testSourceExcludeFilters;

    /***
     * <p>Parameter holding a List of Filters, used to match all files under the {@code testXjbSources} directories
     * which should <strong>not</strong> be considered XJB files. (The filters identify files to exclude, and hence
     * this parameter is called {@code testXjbExcludeFilters}). If a file matches at least one of the supplied Filters,
     * it is not considered an XJB file, and therefore excluded from processing.</p>
     * <p>If not explicitly provided, the Mojo uses the value within {@code STANDARD_TEST_XJB_EXCLUDE_FILTERS}.</p>
     * <p><strong>Example:</strong> The following configuration would exclude any XJB files whose names end with
     * {@code xml} or {@code foo}:</p>
     * <pre>
     *     <code>
     *         &lt;configuration>
     *         ...
     *              &lt;testXjbExcludeFilters>
     *                  &lt;suffixFilter impl="org.codehaus.mojo.jaxb2.shared.filters.source.ExclusionRegularExpressionFileFilter">xml&lt;/suffixFilter>
     *                  &lt;suffixFilter impl="org.codehaus.mojo.jaxb2.shared.filters.source.ExclusionRegularExpressionFileFilter">foo&lt;/suffixFilter>
     *              &lt;/sourceExcludeFilters>
     *         &lt;/testXjbExcludeFilters>
     *     </code>
     * </pre>
     *
     * @see #STANDARD_TEST_XJB_EXCLUDE_FILTERS
     * @see org.codehaus.mojo.jaxb2.shared.filters.pattern.PatternFileFilter
     * @see org.codehaus.mojo.jaxb2.shared.filters.pattern.AbstractPatternFilter
     * @see org.codehaus.mojo.jaxb2.shared.filters.AbstractFilter
     */
    @Parameter(required = false)
    private List<Filter<File>> testXjbExcludeFilters;

    /**
     * Indicate if the XjcMojo execution should be skipped.
     */
    @Parameter(property = "xjc.test.skip", defaultValue = "false")
    private boolean skipTestXjc;

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean shouldExecutionBeSkipped() {
        return skipTestXjc;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<URL> getSources() {

        final List<Filter<File>> excludePatterns = testSourceExcludeFilters == null
                ? STANDARD_TEST_SOURCE_EXCLUDE_FILTERS
                : testSourceExcludeFilters;
        Filters.initialize(getLog(), excludePatterns);

        // All done.
        return FileSystemUtilities.filterFiles(
                getProject().getBasedir(),
                testSources,
                Arrays.asList(STANDARD_TEST_SOURCE_DIRECTORY),
                getLog(),
                "testSources",
                excludePatterns);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<File> getSourceXJBs() {

        final List<Filter<File>> excludePatterns = testXjbExcludeFilters == null
                ? STANDARD_TEST_XJB_EXCLUDE_FILTERS
                : testXjbExcludeFilters;
        Filters.initialize(getLog(), excludePatterns);

        return FileSystemUtilities.filterFiles(
                getProject().getBasedir(),
                testXjbSources,
                STANDARD_TEST_XJB_DIRECTORY,
                getLog(),
                "testXjbSources",
                excludePatterns);
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
            return (List<String>) getProject().getTestClasspathElements();
        } catch (DependencyResolutionRequiredException e) {
            throw new MojoExecutionException("Could not retrieve Compile classpath.", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void updateProject() {
        getProject().addTestCompileSourceRoot(getOutputDirectory().getAbsolutePath());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void addResource(final Resource resource) {
        getProject().addTestResource(resource);
    }
}
