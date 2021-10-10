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

import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.jaxb2.shared.FileSystemUtilities;
import org.codehaus.mojo.jaxb2.shared.Validate;
import org.codehaus.mojo.jaxb2.shared.environment.EnvironmentFacet;
import org.codehaus.mojo.jaxb2.shared.filters.Filter;
import org.codehaus.mojo.jaxb2.shared.filters.pattern.FileFilterAdapter;
import org.codehaus.mojo.jaxb2.shared.filters.pattern.PatternFileFilter;
import org.codehaus.mojo.jaxb2.shared.version.DependencyInfo;
import org.codehaus.mojo.jaxb2.shared.version.DependsFileParser;
import org.sonatype.plexus.build.incremental.BuildContext;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

/**
 * Abstract Mojo which collects common infrastructure, required and needed
 * by all subclass Mojos in the JAXB2 maven plugin codebase.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>
 */
public abstract class AbstractJaxbMojo extends AbstractMojo {

    /**
     * Standard name of the generated JAXB episode file.
     */
    public static final String STANDARD_EPISODE_FILENAME = "sun-jaxb.episode";

    /**
     * Standard name of the package-info.java file which may contain
     * JAXB annotations and Package JavaDoc.
     */
    public static final String PACKAGE_INFO_FILENAME = "package-info.java";

    /**
     * Platform-independent newline control string.
     */
    public static final String NEWLINE = System.getProperty("line.separator");

    /**
     * Pattern matching strings containing whitespace (or consisting only of whitespace).
     */
    public static final Pattern CONTAINS_WHITESPACE = Pattern.compile("(\\S*\\s+\\S*)+", Pattern.UNICODE_CASE);

    /**
     * <p>Standard excludes Filters for all Java generator Mojos.
     * The List is unmodifiable, and contains Filters on the following form:</p>
     * <pre>
     *     <code>
     *         // The standard exclude filters contain simple, exclude pattern filters.
     *         final List&lt;Filter&lt;File&gt;&gt; tmp = new ArrayList&lt;Filter&lt;File&gt;&gt;();
     *         tmp.add(new PatternFileFilter(Arrays.asList({"README.*", "\\.xml", "\\.txt"}), true));
     *         tmp.add(new FileFilterAdapter(new FileFilter() {
     *
     *             &#64;Override
     *             public boolean accept(final File aFileOrDir) {
     *
     *                 // Check sanity
     *                 if (aFileOrDir == null) {
     *                     return false;
     *                 }
     *
     *                 final String name = aFileOrDir.getName();
     *
     *                 // Ignore hidden files and CVS directories
     *                 return name.startsWith(".")
     *                         || (aFileOrDir.isDirectory() &amp;&amp; name.equals("CVS"));
     *
     *             }
     *         }));
     *     </code>
     * </pre>
     * <p><b>Note</b>! Since the plugin is currently developed in jdk 1.7-compliant code, we cannot
     * use lambdas within Filters just yet.</p>
     */
    public static final List<Filter<File>> STANDARD_EXCLUDE_FILTERS;

    private static final List<String> RELEVANT_GROUPIDS =
            Arrays.asList("com.sun.xml.bind", "jakarta.xml.bind");
    private static final String OWN_ARTIFACT_ID = "jaxb2-maven-plugin";
    private static final String SYSTEM_FILE_ENCODING_PROPERTY = "file.encoding";
    private static final String[] STANDARD_EXCLUDE_SUFFIXES = {"README.*", "\\.xml", "\\.txt"};
    private static final String[] STANDARD_PRELOADED_CLASSES = {
            "com.sun.tools.xjc.addon.episode.package-info",
            "com.sun.tools.xjc.reader.xmlschema.bindinfo.package-info",
            "org.glassfish.jaxb.core.v2.model.core.package-info",
            "org.glassfish.jaxb.runtime.v2.model.runtime.package-info",
            "org.glassfish.jaxb.core.v2.schemagen.episode.package-info",
            "org.glassfish.jaxb.runtime.v2.schemagen.xmlschema.package-info"
    };

    static {

        // The standard exclude filters contain simple, exclude pattern filters.
        final List<Filter<File>> tmp = new ArrayList<Filter<File>>();
        tmp.add(new PatternFileFilter(Arrays.asList(STANDARD_EXCLUDE_SUFFIXES), true));
        tmp.add(new FileFilterAdapter(new FileFilter() {
            @Override
            public boolean accept(final File aFileOrDir) {

                // Check sanity
                if (aFileOrDir == null) {
                    return false;
                }

                final String name = aFileOrDir.getName();

                // Ignore hidden files and CVS directories
                return name.startsWith(".")
                        || (aFileOrDir.isDirectory() && name.equals("CVS"));

            }
        }));

        // Make STANDARD_EXCLUDE_FILTERS be unmodifiable.
        STANDARD_EXCLUDE_FILTERS = Collections.unmodifiableList(tmp);

        // TODO: These are hardcoded. Move to overridable using a system property?
        // Preload relevant package-info classes to work around MNG-6506.
        try {

            final ClassLoader cl = AbstractJaxbMojo.class.getClassLoader();

            for(String current : STANDARD_PRELOADED_CLASSES) {
                cl.loadClass(current);
            }

        } catch (ClassNotFoundException ex) {
            throw new Error(ex);
        }
    }

    /**
     * The Plexus BuildContext is used to identify files or directories modified since last build,
     * implying functionality used to define if java generation must be performed again.
     */
    @Component
    private BuildContext buildContext;

    /**
     * The injected Maven project.
     */
    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;

    /**
     * Note that the execution parameter will be injected ONLY if this plugin is executed as part
     * of a maven standard lifecycle - as opposed to directly invoked with a direct invocation.
     * When firing this mojo directly (i.e. {@code mvn xjc:something} or {@code mvn schemagen:something}), the
     * {@code execution} object will not be injected.
     */
    @Parameter(defaultValue = "${mojoExecution}", readonly = true)
    private MojoExecution execution;

    /**
     * <p>The directory where the staleFile is found.
     * The staleFile assists in determining if re-generation of JAXB build products is required.</p>
     * <p>While it is permitted to re-define the staleFileDirectory, it is recommended to keep it
     * below the <code>${project.build.directory}</code>, to ensure that JAXB code or XSD re-generation
     * occurs after cleaning the project.</p>
     *
     * @since 2.0
     */
    @Parameter(defaultValue = "${project.build.directory}/jaxb2", readonly = true, required = true)
    protected File staleFileDirectory;

    /**
     * <p>Defines the encoding used by XJC (for generating Java Source files) and schemagen (for generating XSDs).
     * The corresponding argument parameter for XJC and SchemaGen is: {@code encoding}.</p>
     * <p>The algorithm for finding the encoding to use is as follows
     * (where the first non-null value found is used for encoding):
     * <ol>
     * <li>If the configuration property is explicitly given within the plugin's configuration, use that value.</li>
     * <li>If the Maven property <code>project.build.sourceEncoding</code> is defined, use its value.</li>
     * <li>Otherwise use the value from the system property <code>file.encoding</code>.</li>
     * </ol>
     * </p>
     *
     * @see #getEncoding(boolean)
     * @since 2.0
     */
    @Parameter(defaultValue = "${project.build.sourceEncoding}")
    private String encoding;

    /**
     * <p>A Locale definition to create and set the system (default) Locale when the XJB or SchemaGen tools executes.
     * The Locale will be reset to its default value after the execution of XJC or SchemaGen is complete.</p>
     * <p>The configuration parameter must be supplied on the form {@code language[,country[,variant]]},
     * such as {@code sv,SE} or {@code fr}. Refer to
     * {@code org.codehaus.mojo.jaxb2.shared.environment.locale.LocaleFacet.createFor(String, Log)} for further
     * information.</p>
     * <p><strong>Example</strong> (assigns french locale):</p>
     * <pre>
     *     <code>
     *         &lt;configuration&gt;
     *              &lt;locale&gt;fr&lt;/locale&gt;
     *         &lt;/configuration&gt;
     *     </code>
     * </pre>
     *
     * @see org.codehaus.mojo.jaxb2.shared.environment.locale.LocaleFacet#createFor(String, Log)
     * @see Locale#getAvailableLocales()
     * @since 2.2
     */
    @Parameter(required = false)
    protected String locale;

    /**
     * <p>Defines a set of extra EnvironmentFacet instances which are used to further configure the
     * ToolExecutionEnvironment used by this plugin to fire XJC or SchemaGen.</p>
     * <p><em>Example:</em> If you implement the EnvironmentFacet interface in the class
     * {@code org.acme.MyCoolEnvironmentFacetImplementation}, its {@code setup()} method is called before the
     * XJC or SchemaGen tools are executed to setup some facet of their Execution environment. Correspondingly, the
     * {@code restore()} method in your {@code org.acme.MyCoolEnvironmentFacetImplementation} class is invoked after
     * the XJC or SchemaGen execution terminates.</p>
     * <pre>
     *     <code>
     *         &lt;configuration&gt;
     *         ...
     *              &lt;extraFacets&gt;
     *                  &lt;extraFacet implementation="org.acme.MyCoolEnvironmentFacetImplementation" /&gt;
     *              &lt;/extraFacets&gt;
     *         ...
     *         &lt;/configuration&gt;
     *     </code>
     * </pre>
     *
     * @see EnvironmentFacet
     * @see org.codehaus.mojo.jaxb2.shared.environment.ToolExecutionEnvironment#add(EnvironmentFacet)
     * @since 2.2
     */
    @Parameter(required = false)
    protected List<EnvironmentFacet> extraFacets;

    /**
     * Adds the supplied Resource to the project using the appropriate scope (i.e. resource or testResource)
     * depending on the exact implementation of this AbstractJaxbMojo.
     *
     * @param resource The resource to add.
     */
    protected abstract void addResource(final Resource resource);

    /**
     * The Plexus BuildContext is used to identify files or directories modified since last build,
     * implying functionality used to define if java generation must be performed again.
     *
     * @return the active Plexus BuildContext.
     */
    protected final BuildContext getBuildContext() {
        return getInjectedObject(buildContext, "buildContext");
    }

    /**
     * @return The active MavenProject.
     */
    protected final MavenProject getProject() {
        return getInjectedObject(project, "project");
    }

    /**
     * @return The active MojoExecution.
     */
    public MojoExecution getExecution() {
        return getInjectedObject(execution, "execution");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void execute() throws MojoExecutionException, MojoFailureException {

        // 0) Get the log and its relevant level
        final Log log = getLog();
        final boolean isDebugEnabled = log.isDebugEnabled();
        final boolean isInfoEnabled = log.isInfoEnabled();

        // 1) Should we skip execution?
        if (shouldExecutionBeSkipped()) {

            if (isDebugEnabled) {
                log.debug("Skipping execution, as instructed.");
            }
            return;
        }

        // 2) Printout relevant version information.
        if (isDebugEnabled) {
            logPluginAndJaxbDependencyInfo();
        }

        // 3) Are generated files stale?
        if (isReGenerationRequired()) {

            if (performExecution()) {

                // As instructed by the performExecution() method, update
                // the timestamp of the stale File.
                updateStaleFileTimestamp();

                // Hack to support M2E
                buildContext.refresh(getOutputDirectory());

            } else if (isInfoEnabled) {
                log.info("Not updating staleFile timestamp as instructed.");
            }
        } else if (isInfoEnabled) {
            log.info("No changes detected in schema or binding files - skipping JAXB generation.");
        }

        // 4) If the output directories exist, add them to the MavenProject's source directories
        if (getOutputDirectory().exists() && getOutputDirectory().isDirectory()) {

            final String canonicalPathToOutputDirectory = FileSystemUtilities.getCanonicalPath(getOutputDirectory());

            if (log.isDebugEnabled()) {
                log.debug("Adding existing JAXB outputDirectory [" + canonicalPathToOutputDirectory
                        + "] to Maven's sources.");
            }

            // Add the output Directory.
            addGeneratedSourcesToProjectSourceRoot(canonicalPathToOutputDirectory);
        }
    }

    /**
     * Adds any directories containing the generated XJC classes to the appropriate Project compilation sources;
     * either {@code TestCompileSourceRoot} or {@code CompileSourceRoot} depending on the exact Mojo implementation
     * of this AbstractJavaGeneratorMojo.
     */
    protected abstract void addGeneratedSourcesToProjectSourceRoot(String canonicalPathToOutputDirectory);

    /**
     * Implement this method to check if this AbstractJaxbMojo should skip executing altogether.
     *
     * @return {@code true} to indicate that this AbstractJaxbMojo should bail out of its execute method.
     */
    protected abstract boolean shouldExecutionBeSkipped();

    /**
     * @return {@code true} to indicate that this AbstractJaxbMojo should be run since its generated files were
     * either stale or not present, and {@code false} otherwise.
     */
    protected abstract boolean isReGenerationRequired();

    /**
     * <p>Implement this method to perform this Mojo's execution.
     * This method will only be called if {@code !shouldExecutionBeSkipped() && isReGenerationRequired()}.</p>
     *
     * @return {@code true} if the timestamp of the stale file should be updated.
     * @throws MojoExecutionException if an unexpected problem occurs.
     *                                Throwing this exception causes a "BUILD ERROR" message to be displayed.
     * @throws MojoFailureException   if an expected problem (such as a compilation failure) occurs.
     *                                Throwing this exception causes a "BUILD FAILURE" message to be displayed.
     */
    protected abstract boolean performExecution() throws MojoExecutionException, MojoFailureException;

    /**
     * Override this method to acquire a List holding all URLs to the sources which this
     * AbstractJaxbMojo should use to produce its output (XSDs files for AbstractXsdGeneratorMojos and
     * Java Source Code for AbstractJavaGeneratorMojos).
     *
     * @return A non-null List holding URLs to sources used by this AbstractJaxbMojo to produce its output.
     */
    protected abstract List<URL> getSources();

    /**
     * Retrieves the directory where the generated files should be written to.
     *
     * @return the directory where the generated files should be written to.
     */
    protected abstract File getOutputDirectory();

    /**
     * Retrieves the configured List of paths from which this AbstractJaxbMojo and its internal toolset
     * (XJC or SchemaGen) should read bytecode classes.
     *
     * @return the configured List of paths from which this AbstractJaxbMojo and its internal toolset (XJC or
     * SchemaGen) should read classes.
     * @throws org.apache.maven.plugin.MojoExecutionException if the classpath could not be retrieved.
     */
    protected abstract List<String> getClasspath() throws MojoExecutionException;

    /**
     * Convenience method to invoke when some plugin configuration is incorrect.
     * Will output the problem as a warning with some degree of log formatting.
     *
     * @param propertyName The name of the problematic property.
     * @param description  The problem description.
     */
    @SuppressWarnings("all")
    protected void warnAboutIncorrectPluginConfiguration(final String propertyName, final String description) {

        final StringBuilder builder = new StringBuilder();
        builder.append("\n+=================== [Incorrect Plugin Configuration Detected]\n");
        builder.append("|\n");
        builder.append("| Property : " + propertyName + "\n");
        builder.append("| Problem  : " + description + "\n");
        builder.append("|\n");
        builder.append("+=================== [End Incorrect Plugin Configuration Detected]\n\n");
        getLog().warn(builder.toString().replace("\n", NEWLINE));
    }

    /**
     * @param arguments The final arguments to be passed to a JAXB tool (XJC or SchemaGen).
     * @param toolName  The name of the tool.
     * @return the arguments, untouched.
     */
    protected final String[] logAndReturnToolArguments(final String[] arguments, final String toolName) {

        // Check sanity
        Validate.notNull(arguments, "arguments");

        if (getLog().isDebugEnabled()) {

            final StringBuilder argBuilder = new StringBuilder();
            argBuilder.append("\n+=================== [" + arguments.length + " " + toolName + " Arguments]\n");
            argBuilder.append("|\n");
            for (int i = 0; i < arguments.length; i++) {
                argBuilder.append("| [").append(i).append("]: ").append(arguments[i]).append("\n");
            }
            argBuilder.append("|\n");
            argBuilder.append("+=================== [End " + arguments.length + " " + toolName + " Arguments]\n\n");
            getLog().debug(argBuilder.toString().replace("\n", NEWLINE));
        }

        // All done.
        return arguments;
    }

    /**
     * Retrieves the last name part of the stale file.
     * The full name of the stale file will be generated by pre-pending {@code "." + getExecution().getExecutionId()}
     * before this staleFileName.
     *
     * @return The name of the stale file used by this AbstractJavaGeneratorMojo to detect staleness amongst its
     * generated files.
     */
    protected abstract String getStaleFileName();

    /**
     * Acquires the staleFile for this execution
     *
     * @return the staleFile (used to define where) for this execution
     */
    protected final File getStaleFile() {
        final String staleFileName = "."
                + (getExecution() == null ? "nonExecutionJaxb" : getExecution().getExecutionId())
                + "-" + getStaleFileName();
        return new File(staleFileDirectory, staleFileName);
    }

    /**
     * <p>The algorithm for finding the encoding to use is as follows (where the first non-null value found
     * is used for encoding):</p>
     * <ol>
     * <li>If the configuration property is explicitly given within the plugin's configuration, use that value.</li>
     * <li>If the Maven property <code>project.build.sourceEncoding</code> is defined, use its value.</li>
     * <li>Otherwise use the value from the system property <code>file.encoding</code>.</li>
     * </ol>
     *
     * @param warnIfPlatformEncoding Defines if a warning should be logged if encoding is not configured but
     *                               the platform encoding (system property {@code file.encoding}) is used
     * @return The encoding to be used by this AbstractJaxbMojo and its tools.
     * @see #encoding
     */
    protected final String getEncoding(final boolean warnIfPlatformEncoding) {

        // Harvest information
        final boolean configuredEncoding = encoding != null;
        final String fileEncoding = System.getProperty(SYSTEM_FILE_ENCODING_PROPERTY);
        final String effectiveEncoding = configuredEncoding ? encoding : fileEncoding;

        // Should we warn if using platform encoding (i.e. platform dependent)?
        if (!configuredEncoding && warnIfPlatformEncoding) {
            getLog().warn("Using platform encoding [" + effectiveEncoding + "], i.e. build is platform dependent!");
        } else if (getLog().isDebugEnabled()) {
            getLog().debug("Using " + (configuredEncoding ? "explicitly configured" : "system property")
                    + " encoding [" + effectiveEncoding + "]");
        }

        // All Done.
        return effectiveEncoding;
    }

    /**
     * Retrieves a File to the JAXB Episode (which is normally written during the XJC process).
     * Moreover, ensures that the parent directory of that File is created, to enable writing the File.
     *
     * @param episodeFileName {@code null} to indicate that the standard episode file name ("sun-jaxb.episode")
     *                        should be used, and otherwise a non-empty name which should be used
     *                        as the episode file name.
     * @return A non-null File where the JAXB episode file should be written.
     * @throws MojoExecutionException if the parent directory of the episode file could not be created.
     */
    protected File getEpisodeFile(final String episodeFileName) throws MojoExecutionException {

        // Get the execution ID
        final String executionID = getExecution() != null && getExecution().getExecutionId() != null
                ? getExecution().getExecutionId()
                : null;

        final String effectiveEpisodeFileName = episodeFileName == null
                ? (executionID == null ? STANDARD_EPISODE_FILENAME : "episode_" + executionID)
                : episodeFileName;
        if (effectiveEpisodeFileName.isEmpty()) {
            throw new MojoExecutionException("Cannot handle null or empty JAXB Episode filename. "
                    + "Check 'episodeFileName' configuration property.");
        }

        // Find or create the episode directory.
        final Path episodePath;
        final File generatedJaxbEpisodeDirectory;
        try {
            final Path path = Paths.get(getOutputDirectory().getAbsolutePath(), "META-INF", "JAXB");
            episodePath = java.nio.file.Files.createDirectories(path);
            generatedJaxbEpisodeDirectory = episodePath.toFile();

            if (getLog().isInfoEnabled()) {
                getLog().info("Created EpisodePath [" + episodePath.toString() + "]: " +
                        (generatedJaxbEpisodeDirectory.exists() && generatedJaxbEpisodeDirectory.isDirectory()));
            }

        } catch (IOException e) {
            throw new MojoExecutionException("Could not create output directory.", e);
        }

        if (!generatedJaxbEpisodeDirectory.exists() || !generatedJaxbEpisodeDirectory.isDirectory()) {
            throw new MojoExecutionException("Could not create directory [" + episodePath.toString() + "]");
        }

        // Is there already an episode file here?
        File episodeFile = new File(generatedJaxbEpisodeDirectory, effectiveEpisodeFileName + ".xjb");
        final AtomicInteger index = new AtomicInteger(1);
        while (episodeFile.exists()) {
            episodeFile = new File(generatedJaxbEpisodeDirectory,
                    effectiveEpisodeFileName + "_" + index.getAndIncrement() + ".xjb");
        }

        // Add the (generated) outputDirectory to the Resources.
        final Resource outputDirectoryResource = new Resource();
        outputDirectoryResource.setDirectory(getOutputDirectory().getAbsolutePath());
        outputDirectoryResource.setIncludes(Collections.singletonList("**/" + episodeFile.getName()));
        this.addResource(outputDirectoryResource);

        // All Done.
        return episodeFile;
    }

    //
    // Private helpers
    //

    private void logPluginAndJaxbDependencyInfo() {

        if (getLog().isDebugEnabled()) {
            final StringBuilder builder = new StringBuilder();
            builder.append("\n+=================== [Brief Plugin Build Dependency Information]\n");
            builder.append("|\n");
            builder.append("| Note: These dependencies pertain to what was used to build *the plugin*.\n");
            builder.append("|       Check project dependencies to see the ones used in *your build*.\n");
            builder.append("|\n");

            // Find the dependency and version information within the dependencies.properties file.
            final SortedMap<String, String> versionMap = DependsFileParser.getVersionMap(OWN_ARTIFACT_ID);

            builder.append("|\n");
            builder.append("| Plugin's own information\n");
            builder.append("|     GroupId    : " + versionMap.get(DependsFileParser.OWN_GROUPID_KEY) + "\n");
            builder.append("|     ArtifactID : " + versionMap.get(DependsFileParser.OWN_ARTIFACTID_KEY) + "\n");
            builder.append("|     Version    : " + versionMap.get(DependsFileParser.OWN_VERSION_KEY) + "\n");
            builder.append("|     Buildtime  : " + versionMap.get(DependsFileParser.BUILDTIME_KEY) + "\n");
            builder.append("|\n");
            builder.append("| Plugin's JAXB-related dependencies\n");
            builder.append("|\n");

            final SortedMap<String, DependencyInfo> diMap = DependsFileParser.createDependencyInfoMap(versionMap);

            int dependencyIndex = 0;
            for (Map.Entry<String, DependencyInfo> current : diMap.entrySet()) {

                final String key = current.getKey().trim();
                for (String currentRelevantGroupId : RELEVANT_GROUPIDS) {
                    if (key.startsWith(currentRelevantGroupId)) {

                        final DependencyInfo di = current.getValue();
                        builder.append("|   " + (++dependencyIndex) + ") [" + di.getArtifactId() + "]\n");
                        builder.append("|     GroupId    : " + di.getGroupId() + "\n");
                        builder.append("|     ArtifactID : " + di.getArtifactId() + "\n");
                        builder.append("|     Version    : " + di.getVersion() + "\n");
                        builder.append("|     Scope      : " + di.getScope() + "\n");
                        builder.append("|     Type       : " + di.getType() + "\n");
                        builder.append("|\n");
                    }
                }
            }

            builder.append("+=================== [End Brief Plugin Build Dependency Information]\n\n");
            getLog().debug(builder.toString().replace("\n", NEWLINE));
        }
    }

    private <T> T getInjectedObject(final T objectOrNull, final String objectName) {

        if (objectOrNull == null) {
            getLog().error(
                    "Found null '" + objectName + "', implying that Maven @Component injection was not done properly.");
        }

        return objectOrNull;
    }

    private void updateStaleFileTimestamp() throws MojoExecutionException {

        final File staleFile = getStaleFile();
        if (!staleFile.exists()) {

            // Ensure that the staleFileDirectory exists
            FileSystemUtilities.createDirectory(staleFile.getParentFile(), false);

            try {
                staleFile.createNewFile();

                if (getLog().isDebugEnabled()) {
                    getLog().debug("Created staleFile [" + FileSystemUtilities.getCanonicalPath(staleFile) + "]");
                }
            } catch (IOException e) {
                throw new MojoExecutionException("Could not create staleFile.", e);
            }

        } else {
            if (!staleFile.setLastModified(System.currentTimeMillis())) {
                getLog().warn("Failed updating modification time of staleFile ["
                        + FileSystemUtilities.getCanonicalPath(staleFile) + "]");
            }
        }
    }

    /**
     * Prints out the system properties to the Maven Log at Debug level.
     */
    protected void logSystemPropertiesAndBasedir() {
        if (getLog().isDebugEnabled()) {

            final StringBuilder builder = new StringBuilder();

            builder.append("\n+=================== [System properties]\n");
            builder.append("|\n");

            // Sort the system properties
            final SortedMap<String, Object> props = new TreeMap<String, Object>();
            props.put("basedir", FileSystemUtilities.getCanonicalPath(getProject().getBasedir()));

            for (Map.Entry<Object, Object> current : System.getProperties().entrySet()) {
                props.put("" + current.getKey(), current.getValue());
            }
            for (Map.Entry<String, Object> current : props.entrySet()) {
                builder.append("| [" + current.getKey() + "]: " + current.getValue() + "\n");
            }

            builder.append("|\n");
            builder.append("+=================== [End System properties]\n");

            // All done.
            getLog().debug(builder.toString().replace("\n", NEWLINE));
        }
    }
}
