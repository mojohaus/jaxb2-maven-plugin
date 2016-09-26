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
@Mojo(name = "xjc",
        threadSafe = false,
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
     * Default exclude Filters for sources, used unless overridden by an
     * explicit configuration in the {@code xjcSourceExcludeFilters} parameter.
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
     * Default List of exclude Filters for XJB files, unless overridden by providing
     * an explicit configuration in the {@code xjbExcludeSuffixes} parameter.
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

        final List<Filter<File>> excludePatterns = xjcSourceExcludeFilters == null
                ? STANDARD_SOURCE_EXCLUDE_FILTERS
                : xjcSourceExcludeFilters;
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
    protected List<File> getSourceXJBs() {

        final List<Filter<File>> excludePatterns = xjbExcludeFilters == null
                ? STANDARD_XJB_EXCLUDE_FILTERS
                : xjbExcludeFilters;
        Filters.initialize(getLog(), excludePatterns);

        return FileSystemUtilities.filterFiles(
                getProject().getBasedir(),
                xjbSources,
                STANDARD_XJB_DIRECTORY,
                getLog(),
                "xjbSources",
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
            return (List<String>) getProject().getCompileClasspathElements();
        } catch (DependencyResolutionRequiredException e) {
            throw new MojoExecutionException("Could not retrieve Compile classpath.", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void addGeneratedSourcesToProjectSourceRoot() {
        getProject().addCompileSourceRoot(FileSystemUtilities.getCanonicalPath(getOutputDirectory()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void addResource(final Resource resource) {
        getProject().addResource(resource);
    }
}
