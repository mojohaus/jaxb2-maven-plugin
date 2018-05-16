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

import com.sun.tools.jxc.SchemaGenerator;
import com.thoughtworks.qdox.JavaProjectBuilder;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaPackage;
import com.thoughtworks.qdox.model.JavaSource;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.mojo.jaxb2.AbstractJaxbMojo;
import org.codehaus.mojo.jaxb2.schemageneration.postprocessing.javadoc.DefaultJavaDocRenderer;
import org.codehaus.mojo.jaxb2.schemageneration.postprocessing.javadoc.JavaDocExtractor;
import org.codehaus.mojo.jaxb2.schemageneration.postprocessing.javadoc.JavaDocRenderer;
import org.codehaus.mojo.jaxb2.schemageneration.postprocessing.javadoc.SearchableDocumentation;
import org.codehaus.mojo.jaxb2.schemageneration.postprocessing.schemaenhancement.SimpleNamespaceResolver;
import org.codehaus.mojo.jaxb2.schemageneration.postprocessing.schemaenhancement.TransformSchema;
import org.codehaus.mojo.jaxb2.shared.FileSystemUtilities;
import org.codehaus.mojo.jaxb2.shared.arguments.ArgumentBuilder;
import org.codehaus.mojo.jaxb2.shared.environment.EnvironmentFacet;
import org.codehaus.mojo.jaxb2.shared.environment.ToolExecutionEnvironment;
import org.codehaus.mojo.jaxb2.shared.environment.classloading.ThreadContextClassLoaderBuilder;
import org.codehaus.mojo.jaxb2.shared.environment.locale.LocaleFacet;
import org.codehaus.mojo.jaxb2.shared.environment.logging.LoggingHandlerEnvironmentFacet;
import org.codehaus.mojo.jaxb2.shared.filters.Filter;
import org.codehaus.mojo.jaxb2.shared.filters.pattern.PatternFileFilter;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.util.FileUtils;

import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.regex.Pattern;

/**
 * <p>Abstract superclass for Mojos that generate XSD files from annotated Java Sources.
 * This Mojo delegates execution to the {@code schemagen} tool to perform the XSD file
 * generation. Moreover, the AbstractXsdGeneratorMojo provides an augmented processing
 * pipeline by optionally letting a set of NodeProcessors improve the 'vanilla' XSD files.</p>
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>
 * @see <a href="https://jaxb.java.net/">The JAXB Reference Implementation</a>
 */
public abstract class AbstractXsdGeneratorMojo extends AbstractJaxbMojo {

    /**
     * <p>Pattern matching the names of files emitted by the JAXB/JDK SchemaGenerator.
     * According to the JAXB Schema Generator documentation:</p>
     * <blockquote>There is no way to control the name of the generated schema files at this time.</blockquote>
     */
    public static final Pattern SCHEMAGEN_EMITTED_FILENAME = Pattern.compile("schema\\p{javaDigit}+.xsd");

    /**
     * <p>The default JavaDocRenderer used unless another JavaDocRenderer should be used.</p>
     *
     * @see #javaDocRenderer
     * @since 2.0
     */
    public static final JavaDocRenderer STANDARD_JAVADOC_RENDERER = new DefaultJavaDocRenderer();

    /**
     * <p>Default exclude file name suffixes for testSources, used unless overridden by an
     * explicit configuration in the {@code testSourceExcludeSuffixes} parameter.
     * </p>
     * <pre>
     *     <code>
     *         final List<Filter<File>> schemagenTmp = new ArrayList<Filter<File>>();
     *         schemagenTmp.addAll(AbstractJaxbMojo.STANDARD_EXCLUDE_FILTERS);
     *         schemagenTmp.add(new PatternFileFilter(Arrays.asList("\\.java", "\\.scala", "\\.mdo"), false));
     *         STANDARD_BYTECODE_EXCLUDE_FILTERS = Collections.unmodifiableList(schemagenTmp);
     *     </code>
     * </pre>
     */
    public static final List<Filter<File>> STANDARD_BYTECODE_EXCLUDE_FILTERS;

    /**
     * Filter list containing a PatternFileFilter including ".class" files.
     */
    public static final List<Filter<File>> CLASS_INCLUDE_FILTERS;

    /**
     * Specification for packages which must be loaded using the SystemToolClassLoader (and not in the plugin's
     * ThreadContext ClassLoader). The SystemToolClassLoader is used by SchemaGen to process some stuff from the
     * {@code tools.jar} archive, in particular its exception types used to signal JAXB annotation Exceptions.
     *
     * @see ToolProvider#getSystemToolClassLoader()
     */
    public static final List<String> SYSTEM_TOOLS_CLASSLOADER_PACKAGES = Arrays.asList(
            "com.sun.source.util",
            "com.sun.source.tree");

    static {

        final List<Filter<File>> schemagenTmp = new ArrayList<Filter<File>>();
        schemagenTmp.addAll(AbstractJaxbMojo.STANDARD_EXCLUDE_FILTERS);
        schemagenTmp.add(new PatternFileFilter(Arrays.asList("\\.java", "\\.scala", "\\.mdo"), false));
        STANDARD_BYTECODE_EXCLUDE_FILTERS = Collections.unmodifiableList(schemagenTmp);

        CLASS_INCLUDE_FILTERS = new ArrayList<Filter<File>>();
        CLASS_INCLUDE_FILTERS.add(new PatternFileFilter(Arrays.asList("\\.class"), true));
    }

    // Internal state
    private static final int SCHEMAGEN_INCORRECT_OPTIONS = -1;
    private static final int SCHEMAGEN_COMPLETED_OK = 0;
    private static final int SCHEMAGEN_JAXB_ERRORS = 1;

    /**
     * <p>A List holding desired schema mappings, each of which binds a schema namespace URI to its desired prefix
     * [optional] and the name of the resulting schema file [optional]. All given elements (uri, prefix, file) must be
     * unique within the configuration; no two elements may have the same values.</p>
     * <p>The example schema configuration below maps two namespace uris to prefixes and generated file names. This implies
     * that <tt>http://some/namespace</tt> will be represented by the prefix <tt>some</tt> within the generated XML
     * Schema files; creating namespace definitions on the form <tt>xmlns:some="http://some/namespace"</tt>, and
     * corresponding uses on the form <tt>&lt;xs:element minOccurs="0"
     * ref="<strong>some:</strong>anOptionalElementInSomeNamespace"/></tt>. Moreover, the file element defines that the
     * <tt>http://some/namespace</tt> definitions will be written to the file <tt>some_schema.xsd</tt>, and that all
     * import references will be on the form <tt>&lt;xs:import namespace="http://some/namespace"
     * schemaLocation="<strong>some_schema.xsd</strong>"/></tt></p>
     * <p>The example configuration below also performs identical operations for the namespace uri
     * <tt>http://another/namespace</tt> with the prefix <tt>another</tt> and the file <tt>another_schema.xsd</tt>.
     * </p>
     * <pre>
     *     <code>
     * &lt;transformSchemas>
     *   &lt;transformSchema>
     *     &lt;uri>http://some/namespace&lt;/uri>
     *     &lt;toPrefix>some&lt;/toPrefix>
     *     &lt;toFile>some_schema.xsd&lt;/toFile>
     *   &lt;transformSchema>
     *     &lt;uri>http://another/namespace&lt;/uri>
     *     &lt;toPrefix>another&lt;/toPrefix>
     *     &lt;toFile>another_schema.xsd&lt;/toFile>
     *   &lt;/transformSchema>
     * &lt;/transformSchemas>
     *     </code>
     * </pre>
     *
     * @since 1.4
     */
    @Parameter
    private List<TransformSchema> transformSchemas;

    /**
     * <strong>Deprecated - will be removed in a future release</strong>
     * <p>From plugin version 2.4, this parameter will not be used.
     * Instead, episode files are generated by default with all JAXB operations.</p>
     * <p>Starting with plugin version 2.4, use the parameter {@link #episodeFileName} to provide a custom
     * name of the generated episode File (or rely on the standard file name {@link #STANDARD_EPISODE_FILENAME}).</p>
     *
     * @since 2.0
     * @deprecated
     */
    @Deprecated
    @Parameter(defaultValue = "true")
    protected boolean generateEpisode;

    /**
     * <p>Corresponding SchemaGen parameter: {@code episode}.</p>
     * <p>Generate an episode file with the supplied name from this XSD generation, so that other schemas that rely
     * on this schema can be compiled later and rely on classes that are generated from this compilation.
     * The generated episode file is simply a JAXB customization file (but with vendor extensions), normally known
     * as a <em>binding file</em> with the suffix <code>.xjb</code>.</p>
     * <p>If the <code>episodeFileName</code> parameter is not given, the episode file name is synthesized on the form
     * <code>"episode_" + executionID + ".xjb"</code> - typically something like <em>episode_schemagen.xjb</em>, but
     * it depends on the actual ID given in the execution element:</p>
     * <pre>
     *     <code>
     * &lt;executions&gt;
     *     &lt;execution&gt;
     *         &lt;id&gt;schemagen&lt;/id&gt;
     *         &lt;goals&gt;
     *             &lt;goal&gt;schemagen&lt;/goal&gt;
     *         &lt;/goals&gt;
     *     &lt;/execution&gt;
     * &lt;/executions&gt;
     *      </code>
     * </pre>
     *
     * @see #STANDARD_EPISODE_FILENAME
     * @since 2.4
     */
    @Parameter
    protected String episodeFileName;

    /**
     * <p>If {@code true}, Elements or Attributes in the generated XSD files will be annotated with any
     * JavaDoc found for their respective properties. If {@code false}, no XML documentation annotations will be
     * generated in post-processing any results from the JAXB SchemaGenerator.</p>
     *
     * @since 2.0
     */
    @Parameter(defaultValue = "true")
    protected boolean createJavaDocAnnotations;

    /**
     * <p>A renderer used to create XML annotation text from JavaDoc comments found within the source code.
     * Unless another implementation is provided, the standard JavaDocRenderer used is
     * {@linkplain org.codehaus.mojo.jaxb2.schemageneration.postprocessing.javadoc.DefaultJavaDocRenderer}.</p>
     *
     * @see org.codehaus.mojo.jaxb2.schemageneration.postprocessing.javadoc.DefaultJavaDocRenderer
     * @since 2.0
     */
    @Parameter
    protected JavaDocRenderer javaDocRenderer;

    /**
     * <p>Removes all files from the output directory before running SchemaGenerator.</p>
     *
     * @since 2.0
     */
    @Parameter(defaultValue = "true")
    protected boolean clearOutputDir;

    /**
     * <p>XSD schema files are not generated from POM projects or if no includes have been supplied.</p>
     * {@inheritDoc}
     */
    @Override
    protected boolean shouldExecutionBeSkipped() {

        boolean toReturn = false;

        if ("pom".equalsIgnoreCase(getProject().getPackaging())) {
            warnAboutIncorrectPluginConfiguration("packaging", "POM-packaged projects should not generate XSDs.");
            toReturn = true;
        }

        if (getSources().isEmpty()) {
            warnAboutIncorrectPluginConfiguration("sources", "At least one Java Source file has to be included.");
            toReturn = true;
        }

        // All done.
        return toReturn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isReGenerationRequired() {

        //
        // Use the stale flag method to identify if we should re-generate the XSDs from the sources.
        // Basically, we should re-generate the XSDs if:
        //
        // a) The staleFile does not exist
        // b) The staleFile exists and is older than one of the sources (Java or XJB files).
        //    "Older" is determined by comparing the modification timestamp of the staleFile and the source files.
        //
        final File staleFile = getStaleFile();
        final String debugPrefix = "StaleFile [" + FileSystemUtilities.getCanonicalPath(staleFile) + "]";

        boolean stale = !staleFile.exists();
        if (stale) {
            getLog().debug(debugPrefix + " not found. XML Schema (re-)generation required.");
        } else {

            final List<URL> sources = getSources();

            if (getLog().isDebugEnabled()) {
                getLog().debug(debugPrefix + " found. Checking timestamps on source Java "
                        + "files to determine if XML Schema (re-)generation is required.");
            }

            final long staleFileLastModified = staleFile.lastModified();
            for (URL current : sources) {

                final URLConnection sourceFileConnection;
                try {
                    sourceFileConnection = current.openConnection();
                    sourceFileConnection.connect();
                } catch (Exception e) {

                    if (getLog().isDebugEnabled()) {
                        getLog().debug("Could not open a sourceFileConnection to [" + current + "]", e);
                    }

                    // Can't determine if the staleFile is younger than this source.
                    // Re-generate to be on the safe side.
                    stale = true;
                    break;
                }

                try {
                    if (sourceFileConnection.getLastModified() > staleFileLastModified) {

                        if (getLog().isDebugEnabled()) {
                            getLog().debug(current.toString() + " is newer than the stale flag file.");
                        }
                        stale = true;
                    }
                } finally {
                    if (sourceFileConnection instanceof HttpURLConnection) {
                        ((HttpURLConnection) sourceFileConnection).disconnect();
                    }
                }
            }
        }

        // All done.
        return stale;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean performExecution() throws MojoExecutionException, MojoFailureException {

        boolean updateStaleFileTimestamp = false;
        ToolExecutionEnvironment environment = null;

        try {

            //
            // Ensure that classes that SchemaGen expects to be loaded in the SystemToolClassLoader
            // is delegated to that ClassLoader, to comply with SchemaGen's internal reflective loading
            // of classes. Otherwise we will have ClassCastExceptions instead of proper execution.
            //
            final ClassRealm localRealm = (ClassRealm) getClass().getClassLoader();
            for (String current : SYSTEM_TOOLS_CLASSLOADER_PACKAGES) {
                localRealm.importFrom(ToolProvider.getSystemToolClassLoader(), current);
            }

            // Configure the ThreadContextClassLoaderBuilder, to enable synthesizing a correct ClassPath for the tool.
            final ThreadContextClassLoaderBuilder classLoaderBuilder = ThreadContextClassLoaderBuilder
                    .createFor(this.getClass(), getLog(), getEncoding(false))
                    .addPaths(getClasspath())
                    .addPaths(getProject().getCompileSourceRoots());

            final LocaleFacet localeFacet = locale == null ? null : LocaleFacet.createFor(locale, getLog());

            // Create the execution environment as required by the XJC tool.
            environment = new ToolExecutionEnvironment(
                    getLog(),
                    classLoaderBuilder,
                    LoggingHandlerEnvironmentFacet.create(getLog(), getClass(), getEncoding(false)),
                    localeFacet);
            final String projectBasedirPath = FileSystemUtilities.getCanonicalPath(getProject().getBasedir());

            // Add any extra configured EnvironmentFacets, as configured in the POM.
            if (extraFacets != null) {
                for (EnvironmentFacet current : extraFacets) {
                    environment.add(current);
                }
            }

            // Setup the environment.
            environment.setup();

            // Compile the SchemaGen arguments
            final File episodeFile = getEpisodeFile(episodeFileName);
            final List<URL> sources = getSources();
            final String[] schemaGenArguments = getSchemaGenArguments(
                    environment.getClassPathAsArgument(),
                    episodeFile,
                    sources);

            // Ensure that the outputDirectory and workDirectory exists.
            // Clear them if configured to do so.
            FileSystemUtilities.createDirectory(getOutputDirectory(), clearOutputDir);
            FileSystemUtilities.createDirectory(getWorkDirectory(), clearOutputDir);

            // Re-generate the episode file's parent directory.
            getEpisodeFile(episodeFileName);
            // Do we need to re-create the episode file's parent directory?
            /*final boolean reCreateEpisodeFileParentDirectory = generateEpisode && clearOutputDir;
            if (reCreateEpisodeFileParentDirectory) {

            }
            */

            try {

                // Check the system properties.
                // logSystemPropertiesAndBasedir();

                // Fire the SchemaGenerator
                final int result = SchemaGenerator.run(
                        schemaGenArguments,
                        Thread.currentThread().getContextClassLoader());

                if (SCHEMAGEN_INCORRECT_OPTIONS == result) {
                    printSchemaGenCommandAndThrowException(projectBasedirPath,
                            sources,
                            schemaGenArguments,
                            result,
                            null);
                } else if (SCHEMAGEN_JAXB_ERRORS == result) {

                    // TODO: Collect the error message(s) which was emitted by SchemaGen. How can this be done?
                    throw new MojoExecutionException("JAXB errors arose while SchemaGen compiled sources to XML.");
                }

                // Copy generated XSDs and episode files from the WorkDirectory to the OutputDirectory,
                // but do not copy the intermediary bytecode files generated by schemagen.
                final List<Filter<File>> exclusionFilters = PatternFileFilter.createIncludeFilterList(
                        getLog(), "\\.class");

                final List<File> toCopy = FileSystemUtilities.resolveRecursively(
                        Arrays.asList(getWorkDirectory()),
                        exclusionFilters, getLog());
                for (File current : toCopy) {

                    // Get the path to the current file
                    final String currentPath = FileSystemUtilities.getCanonicalPath(current.getAbsoluteFile());
                    final File target = new File(getOutputDirectory(),
                            FileSystemUtilities.relativize(currentPath, getWorkDirectory(), true));

                    // Copy the file to the same relative structure within the output directory.
                    FileSystemUtilities.createDirectory(target.getParentFile(), false);
                    FileUtils.copyFile(current, target);
                }

                //
                // The XSD post-processing should be applied in the following order:
                //
                // 1. [XsdAnnotationProcessor]:            Inject JavaDoc annotations for Classes.
                // 2. [XsdEnumerationAnnotationProcessor]: Inject JavaDoc annotations for Enums.
                // 3. [ChangeNamespacePrefixProcessor]:    Change namespace prefixes within XSDs.
                // 4. [ChangeFilenameProcessor]:           Change the fileNames of XSDs.
                //

                final boolean performPostProcessing = createJavaDocAnnotations || transformSchemas != null;
                if (performPostProcessing) {

                    // Map the XML Namespaces to their respective XML URIs (and reverse)
                    // The keys are the generated 'vanilla' XSD file names.
                    final Map<String, SimpleNamespaceResolver> resolverMap =
                            XsdGeneratorHelper.getFileNameToResolverMap(getOutputDirectory());

                    if (createJavaDocAnnotations) {

                        if (getLog().isInfoEnabled()) {
                            getLog().info("XSD post-processing: Adding JavaDoc annotations in generated XSDs.");
                        }

                        // Resolve the sources
                        final List<File> fileSources = new ArrayList<File>();
                        for (URL current : sources) {
                            if ("file".equalsIgnoreCase(current.getProtocol())) {
                                final File toAdd = new File(current.getPath());
                                if (toAdd.exists()) {
                                    fileSources.add(toAdd);
                                } else {
                                    if (getLog().isWarnEnabled()) {
                                        getLog().warn("Ignoring URL [" + current + "] as it is a nonexistent file.");
                                    }
                                }
                            }
                        }

                        final List<File> files = FileSystemUtilities.resolveRecursively(
                                fileSources, null, getLog());

                        // Acquire JavaDocs
                        final JavaDocExtractor extractor = new JavaDocExtractor(getLog()).addSourceFiles(files);
                        final SearchableDocumentation javaDocs = extractor.process();

                        // Modify the 'vanilla' generated XSDs by inserting the JavaDoc as annotations
                        final JavaDocRenderer renderer = javaDocRenderer == null
                                ? STANDARD_JAVADOC_RENDERER
                                : javaDocRenderer;
                        final int numProcessedFiles = XsdGeneratorHelper.insertJavaDocAsAnnotations(getLog(),
                                getOutputDirectory(),
                                javaDocs,
                                renderer);

                        if (getLog().isDebugEnabled()) {
                            getLog().info("XSD post-processing: " + numProcessedFiles + " files processed.");
                        }
                    }

                    if (transformSchemas != null) {

                        if (getLog().isInfoEnabled()) {
                            getLog().info("XSD post-processing: Renaming and converting XSDs.");
                        }

                        // Transform all namespace prefixes as requested.
                        XsdGeneratorHelper.replaceNamespacePrefixes(resolverMap,
                                transformSchemas,
                                getLog(),
                                getOutputDirectory());

                        // Rename all generated schema files as requested.
                        XsdGeneratorHelper.renameGeneratedSchemaFiles(resolverMap,
                                transformSchemas,
                                getLog(),
                                getOutputDirectory());
                    }
                }

            } catch (MojoExecutionException e) {
                throw e;
            } catch (Exception e) {

                // Find the root exception, and print its stack trace to the Maven Log.
                // These invocation target exceptions tend to produce really deep stack traces,
                // hiding the actual root cause of the exception.
                Throwable current = e;
                while (current.getCause() != null) {
                    current = current.getCause();
                }

                getLog().error("Execution failed.");

                //
                // Print a stack trace
                //
                StringBuilder rootCauseBuilder = new StringBuilder();
                rootCauseBuilder.append("\n");
                rootCauseBuilder.append("[Exception]: " + current.getClass().getName() + "\n");
                rootCauseBuilder.append("[Message]: " + current.getMessage() + "\n");
                for (StackTraceElement el : current.getStackTrace()) {
                    rootCauseBuilder.append("         " + el.toString()).append("\n");
                }
                getLog().error(rootCauseBuilder.toString().replaceAll("[\r\n]+", "\n"));

                printSchemaGenCommandAndThrowException(projectBasedirPath,
                        sources,
                        schemaGenArguments,
                        -1,
                        current);

            }

            // Indicate that the output directory was updated.
            getBuildContext().refresh(getOutputDirectory());

            // Update the modification timestamp of the staleFile.
            updateStaleFileTimestamp = true;

        } finally {

            // Restore the environment
            if (environment != null) {
                environment.restore();
            }
        }

        // Add generated directories to

        // All done.
        return updateStaleFileTimestamp;
    }

    /**
     * @return The working directory to which the SchemaGenerator should initially copy all its generated files,
     * including bytecode files, compiled from java sources.
     */
    protected abstract File getWorkDirectory();

    /**
     * Finds a List containing URLs to compiled bytecode files within this Compilation Unit.
     * Typically this equals the resolved files under the project's build directories, plus any
     * JAR artifacts found on the classpath.
     *
     * @return A non-null List containing URLs to bytecode files within this compilation unit.
     * Typically this equals the resolved files under the project's build directories, plus any JAR
     * artifacts found on the classpath.
     */
    protected abstract List<URL> getCompiledClassNames();

    /**
     * Override this method to acquire a List holding all URLs to the SchemaGen Java sources for which this
     * AbstractXsdGeneratorMojo should generate Xml Schema Descriptor files.
     *
     * @return A non-null List holding URLs to sources for the XSD generation.
     */
    @Override
    protected abstract List<URL> getSources();

    //
    // Private helpers
    //

    private String[] getSchemaGenArguments(final String classPath,
                                           final File episodeFile,
                                           final List<URL> sources)
            throws MojoExecutionException {

        final ArgumentBuilder builder = new ArgumentBuilder();

        // Add all flags on the form '-flagName'
        // builder.withFlag();

        // Add all arguments on the form '-argumentName argumentValue'
        // (i.e. in 2 separate elements of the returned String[])
        builder.withNamedArgument("encoding", getEncoding(true));
        builder.withNamedArgument("d", getWorkDirectory().getAbsolutePath());
        builder.withNamedArgument("classpath", classPath);

        // From 2.4: Always generate an episode file.
        //
        builder.withNamedArgument("episode", FileSystemUtilities.getCanonicalPath(episodeFile));

        try {

            //
            // The SchemaGenerator does not support directories as arguments:
            // "Caused by: java.lang.IllegalArgumentException: directories not supported"
            // ... implying we must resolve source files in the compilation unit.
            //
            // There seems to be two ways of adding sources to the SchemaGen tool:
            // 1) Using java source files
            //    Define the relative paths to source files, calculated from the System.property "user.dir"
            //    (i.e. *not* the Maven "basedir" property) on the form 'src/main/java/se/west/something/SomeClass.java'.
            //    Sample: javac -d . ../github_jaxb2_plugin/src/it/schemagen-main/src/main/java/se/west/gnat/Foo.java
            //
            // 2) Using bytecode files
            //    Define the CLASSPATH to point to build output directories (such as target/classes), and then use
            //    package notation arguments on the form 'se.west.something.SomeClass'.
            //    Sample: schemagen -d . -classpath brat se.west.gnat.Foo
            //
            // The jaxb2-maven-plugin uses these two methods in the order given.
            //
            builder.withPreCompiledArguments(getSchemaGeneratorSourceFiles(sources));
        } catch (IOException e) {
            throw new MojoExecutionException("Could not compile source paths for the SchemaGenerator", e);
        }

        // All done.
        return logAndReturnToolArguments(builder.build(), "SchemaGen");
    }

    /**
     * <p>The SchemaGenerator does not support directories as arguments, implying we must resolve source
     * files in the compilation unit. This fact is shown when supplying a directory argument as source, when
     * the tool emits:
     * <blockquote>Caused by: java.lang.IllegalArgumentException: directories not supported</blockquote></p>
     * <p>There seems to be two ways of adding sources to the SchemaGen tool:</p>
     * <dl>
     * <dt>1. <strong>Java Source</strong> files</dt>
     * <dd>Define the relative paths to source files, calculated from the System.property {@code user.dir}
     * (i.e. <strong>not</strong> the Maven {@code basedir} property) on the form
     * {@code src/main/java/se/west/something/SomeClass.java}.<br/>
     * <em>Sample</em>: {@code javac -d . .
     * ./github_jaxb2_plugin/src/it/schemagen-main/src/main/java/se/west/gnat/Foo.java}</dd>
     * <dt>2. <strong>Bytecode</strong> files</dt>
     * <dd>Define the {@code CLASSPATH} to point to build output directories (such as target/classes), and then
     * use package notation arguments on the form {@code se.west.something.SomeClass}.<br/>
     * <em>Sample</em>: {@code schemagen -d . -classpath brat se.west.gnat.Foo}</dd>
     * </dl>
     * <p>The jaxb2-maven-plugin uses these two methods in the order given</p>
     *
     * @param sources The compiled sources (as calculated from the local project's
     *                source paths, {@code getSources()}).
     * @return A sorted List holding all sources to be used by the SchemaGenerator. According to the SchemaGenerator
     * documentation, the order in which the source arguments are provided is irrelevant.
     * The sources are to be rendered as the final (open-ended) argument to the schemagen execution.
     * @see #getSources()
     */
    private List<String> getSchemaGeneratorSourceFiles(final List<URL> sources)
            throws IOException, MojoExecutionException {

        final SortedMap<String, String> className2SourcePath = new TreeMap<String, String>();
        final File baseDir = getProject().getBasedir();
        final File userDir = new File(System.getProperty("user.dir"));
        final String encoding = getEncoding(true);

        // 1) Find/add all sources available in the compilation unit.
        for (URL current : sources) {

            final File sourceCodeFile = FileSystemUtilities.getFileFor(current, encoding);

            // Calculate the relative path for the current source
            final String relativePath = FileSystemUtilities.relativize(
                    FileSystemUtilities.getCanonicalPath(sourceCodeFile),
                    userDir,
                    true);

            if (getLog().isDebugEnabled()) {
                getLog().debug("SourceCodeFile ["
                        + FileSystemUtilities.getCanonicalPath(sourceCodeFile)
                        + "] and userDir [" + FileSystemUtilities.getCanonicalPath(userDir)
                        + "] ==> relativePath: "
                        + relativePath
                        + ". (baseDir: " + FileSystemUtilities.getCanonicalPath(baseDir) + "]");
            }

            // Find the Java class(es) within the source.
            final JavaProjectBuilder builder = new JavaProjectBuilder();
            builder.setEncoding(encoding);

            //
            // Ensure that we include package-info.java classes in the SchemaGen compilation.
            //
            if (sourceCodeFile.getName().trim().equalsIgnoreCase(PACKAGE_INFO_FILENAME)) {

                // For some reason, QDox requires the package-info.java to be added as a URL instead of a File.
                builder.addSource(current);
                final Collection<JavaPackage> packages = builder.getPackages();
                if (packages.size() != 1) {
                    throw new MojoExecutionException("Exactly one package should be present in file ["
                            + sourceCodeFile.getPath() + "]");
                }

                // Make the key indicate that this is the package-info.java file.
                final JavaPackage javaPackage = packages.iterator().next();
                className2SourcePath.put("package-info for (" + javaPackage.getName() + ")", relativePath);
                continue;
            }

            // This is not a package-info.java file, so QDox lets us add this as a File.
            builder.addSource(sourceCodeFile);

            // Map any found FQCN to the relativized path of its source file.
            for (JavaSource currentJavaSource : builder.getSources()) {
                for (JavaClass currentJavaClass : currentJavaSource.getClasses()) {

                    final String className = currentJavaClass.getFullyQualifiedName();
                    if (className2SourcePath.containsKey(className)) {
                        if (getLog().isWarnEnabled()) {
                            getLog().warn("Already mapped. Source class [" + className + "] within ["
                                    + className2SourcePath.get(className)
                                    + "]. Not overwriting with [" + relativePath + "]");
                        }
                    } else {
                        className2SourcePath.put(className, relativePath);
                    }
                }
            }
        }

        /*
        // 2) Find any bytecode available in the compilation unit, and add its file as a SchemaGen argument.
        //
        //    The algorithm is:
        //    1) Add bytecode classpath unless its class is already added in source form.
        //    2) SchemaGen cannot handle directory arguments, so any bytecode files in classpath directories
        //       must be resolved.
        //    3) All JARs in the classpath should be added as arguments to SchemaGen.
        //
        //    .... Gosh ...
        //
        for (URL current : getCompiledClassNames()) {
            getLog().debug(" (compiled ClassName) --> " + current.toExternalForm());
        }

        Filters.initialize(getLog(), CLASS_INCLUDE_FILTERS);

        final List<URL> classPathURLs = new ArrayList<URL>();
        for (String current : getClasspath()) {

            final File currentFile = new File(current);
            if (FileSystemUtilities.EXISTING_FILE.accept(currentFile)) {

                // This is a file/JAR. Simply add its path to SchemaGen's arguments.
                classPathURLs.add(FileSystemUtilities.getUrlFor(currentFile));

            } else if (FileSystemUtilities.EXISTING_DIRECTORY.accept(currentFile)) {

                // Resolve all bytecode files within this directory.
                // FileSystemUtilities.filterFiles(baseDir, )
                if (getLog().isDebugEnabled()) {
                    getLog().debug("TODO: Resolve and add bytecode files within: ["
                            + FileSystemUtilities.getCanonicalPath(currentFile) + "]");
                }

                // Find the byte code files within the current directory.
                final List<File> byteCodeFiles = new ArrayList<File>();
                for(File currentResolvedFile : FileSystemUtilities.resolveRecursively(
                        Arrays.asList(currentFile), null, getLog())) {

                    if(Filters.matchAtLeastOnce(currentResolvedFile, CLASS_INCLUDE_FILTERS)) {
                        byteCodeFiles.add(currentResolvedFile);
                    }
                }

                for(File currentByteCodeFile : byteCodeFiles) {

                    final String currentCanonicalPath = FileSystemUtilities.getCanonicalPath(
                            currentByteCodeFile.getAbsoluteFile());

                    final String relativized = FileSystemUtilities.relativize(currentCanonicalPath,
                            FileSystemUtilities.getCanonicalFile(currentFile.getAbsoluteFile()));
                    final String pathFromUserDir = FileSystemUtilities.relativize(currentCanonicalPath, userDir);

                    final String className = relativized.substring(0, relativized.indexOf(".class"))
                            .replace("/", ".")
                            .replace(File.separator, ".");

                    if(!className2SourcePath.containsKey(className)) {
                        className2SourcePath.put(className, pathFromUserDir);

                        if(getLog().isDebugEnabled()) {
                            getLog().debug("Adding ByteCode [" + className + "] at relativized path ["
                                    + pathFromUserDir + "]");
                        }
                    } else {
                        if(getLog().isDebugEnabled()) {
                            getLog().debug("ByteCode [" + className + "] already added. Not re-adding.");
                        }
                    }
                }

            } else if (getLog().isWarnEnabled()) {

                final String suffix = !currentFile.exists() ? " nonexistent" : " was neither a File nor a Directory";
                getLog().warn("Classpath part [" + current + "] " + suffix + ". Ignoring it.");
            }
        }

        /*
        for (URL current : getCompiledClassNames()) {

            // TODO: FIX THIS!
            // Get the class information data from the supplied URL
            for (String currentClassPathElement : getClasspath()) {

                if(getLog().isDebugEnabled()) {
                    getLog().debug("Checking class path element: [" + currentClassPathElement + "]");
                }
            }

            if(getLog().isDebugEnabled()) {
                getLog().debug("Processing compiledClassName: [" + current + "]");
            }

            // Find the Java class(es) within the source.
            final JavaProjectBuilder builder = new JavaProjectBuilder();
            builder.setEncoding(getEncoding(true));
            builder.addSource(current);

            for (JavaSource currentSource : builder.getSources()) {
                for (JavaClass currentClass : currentSource.getClasses()) {

                    final String className = currentClass.getFullyQualifiedName();
                    if (className2SourcePath.containsKey(className)) {
                        if (getLog().isWarnEnabled()) {
                            getLog().warn("Already mapped. Source class [" + className + "] within ["
                                    + className2SourcePath.get(className)
                                    + "]. Not overwriting with [" + className + "]");
                        }
                    } else {
                        className2SourcePath.put(className, className);
                    }
                }
            }
        }
        */

        if (getLog().isDebugEnabled()) {

            final int size = className2SourcePath.size();
            getLog().debug("[ClassName-2-SourcePath Map (size: " + size + ")] ...");

            int i = 0;
            for (Map.Entry<String, String> current : className2SourcePath.entrySet()) {
                getLog().debug("  " + (++i) + "/" + size + ": [" + current.getKey() + "]: "
                        + current.getValue());
            }
            getLog().debug("... End [ClassName-2-SourcePath Map]");
        }

        // Sort the source paths and place them first in the argument array
        final ArrayList<String> toReturn = new ArrayList<String>(className2SourcePath.values());
        Collections.sort(toReturn);

        // All Done.
        return toReturn;
    }

    private void printSchemaGenCommandAndThrowException(final String projectBasedirPath,
                                                        final List<URL> sources,
                                                        final String[] schemaGenArguments,
                                                        final int result,
                                                        final Throwable cause) throws MojoExecutionException {

        final StringBuilder errorMsgBuilder = new StringBuilder();
        errorMsgBuilder.append("\n+=================== [SchemaGenerator Error '"
                + (result == -1 ? "<unknown>" : result) + "']\n");
        errorMsgBuilder.append("|\n");
        errorMsgBuilder.append("| SchemaGen did not complete its operation correctly.\n");
        errorMsgBuilder.append("|\n");
        errorMsgBuilder.append("| To re-create the error (and get a proper error message), cd to:\n");
        errorMsgBuilder.append("| ").append(projectBasedirPath).append("\n");
        errorMsgBuilder.append("| ... and fire the following on a command line/in a shell:\n");
        errorMsgBuilder.append("|\n");

        final StringBuilder builder = new StringBuilder("schemagen ");
        for (String current : schemaGenArguments) {
            builder.append(current).append(" ");
        }

        errorMsgBuilder.append("| " + builder.toString() + "\n");
        errorMsgBuilder.append("|\n");
        errorMsgBuilder.append("| The following source files should be processed by schemagen:\n");

        for (int i = 0; i < sources.size(); i++) {
            errorMsgBuilder.append("| " + i + ": ").append(sources.get(i).toString()).append("\n");
        }

        errorMsgBuilder.append("|\n");
        errorMsgBuilder.append("+=================== [End SchemaGenerator Error]\n");

        final String msg = errorMsgBuilder.toString().replaceAll("[\r\n]+", "\n");
        if (cause != null) {
            throw new MojoExecutionException(msg, cause);
        } else {
            throw new MojoExecutionException(msg);
        }
    }
}
