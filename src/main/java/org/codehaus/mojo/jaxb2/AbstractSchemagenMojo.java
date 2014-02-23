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
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.jaxb2.helpers.SchemagenHelper;
import org.codehaus.mojo.jaxb2.helpers.SimpleNamespaceResolver;
import org.codehaus.plexus.compiler.util.scan.InclusionScanException;
import org.codehaus.plexus.compiler.util.scan.SourceInclusionScanner;
import org.codehaus.plexus.compiler.util.scan.StaleSourceScanner;
import org.codehaus.plexus.compiler.util.scan.mapping.SingleTargetSourceMapping;
import org.codehaus.plexus.compiler.util.scan.mapping.SourceMapping;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.Scanner;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.plexus.build.incremental.BuildContext;
import org.sonatype.plexus.build.incremental.DefaultBuildContext;

import com.sun.tools.jxc.SchemaGenerator;

/**
 * @author rfscholte
 * @since 1.3
 */
public abstract class AbstractSchemagenMojo
    extends AbstractMojo
{
    /**
     * Name of the generated schema file, emitted by the SchemaGenerator. <br/>
     * According to the JAXB Schema Generator documentation:
     * "There is no way to control the name of the generated schema files at this time."
     */
    private static final String SCHEMAGEN_EMITTED_FILENAME = "schema1.xsd";

    @Component
    private BuildContext buildContext;

    /**
     * The default maven project object.
     */
    @Component
    private MavenProject project;

    /**
     * A List holding desired schema mappings, each of which binds a schema namespace URI to its desired prefix
     * [optional] and the name of the resulting schema file [optional]. All given elements (uri, prefix, file) must be
     * unique within the configuration; no two elements may have the same values.
     * <p/>
     * <p/>
     * <p>
     * The example schema configuration below maps two namespace uris to prefixes and generated file names. This implies
     * that <tt>http://some/namespace</tt> will be represented by the prefix <tt>some</tt> within the generated XML
     * Schema files; creating namespace definitions on the form <tt>xmlns:some="http://some/namespace"</tt>, and
     * corresponding uses on the form <tt>&lt;xs:element minOccurs="0"
     * ref="<strong>some:</strong>anOptionalElementInSomeNamespace"/></tt>. Moreover, the file element defines that the
     * <tt>http://some/namespace</tt> definitions will be written to the file <tt>some_schema.xsd</tt>, and that all
     * import references will be on the form <tt>&lt;xs:import namespace="http://some/namespace"
     * schemaLocation="<strong>some_schema.xsd</strong>"/></tt>
     * </p>
     * <p/>
     * <p>
     * The example configuration below also performs identical operations for the namespace uri
     * <tt>http://another/namespace</tt> with the prefix <tt>another</tt> and the file <tt>another_schema.xsd</tt>.
     * </p>
     * <p/>
     * 
     * <pre>
     * &lt;schemas>
     *   &lt;schema>
     *     &lt;uri>http://some/namespace&lt;/uri>
     *     &lt;toPrefix>some&lt;/toPrefix>
     *     &lt;toFile>some_schema.xsd&lt;/toFile>
     *   &lt;schema>
     *     &lt;uri>http://another/namespace&lt;/uri>
     *     &lt;toPrefix>another&lt;/toPrefix>
     *     &lt;toFile>another_schema.xsd&lt;/toFile>
     *   &lt;/schema>
     * &lt;/schemas>
     * </pre>
     * 
     * @since 1.4
     */
    @Parameter
    private List<TransformSchema> transformSchemas;

    /**
     * A list of inclusion filters for the generator. At least one file has to be specified.
     */
    @Parameter( required = true )
    private Set<String> includes = new HashSet<String>();

    /**
     * A list of exclusion filters for the generator.
     */
    @Parameter
    private Set<String> excludes = new HashSet<String>();

    /**
     * The granularity in milliseconds of the last modification date for testing whether a source needs recompilation.
     */
    @Parameter( property = "lastModGranularityMs", defaultValue = "0" )
    private int staleMillis;

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        if ( getLog().isDebugEnabled() )
        {
            Package jaxbImplPackage = SchemaGenerator.class.getPackage();
            getLog().debug( "Using SchemaGen of " + jaxbImplPackage.getImplementationTitle() + " version "
                                + jaxbImplPackage.getImplementationVersion() );
        }

        if ( "pom".equals( project.getPackaging() ) )
        {
            return;
        }

        if ( includes.isEmpty() )
        {
            throw new MojoExecutionException( "At least one file has to be included" );
        }

        if ( isOutputStale() )
        {
            String includePaths = StringUtils.join( includes.toArray(), "," );
            String excludePaths = StringUtils.join( excludes.toArray(), "," );
            Set<String> includedSources = new HashSet<String>();

            for ( String path : getCompileSourceRoots() )
            {
                File sourceDir = new File( path );
                if ( sourceDir.exists() && sourceDir.isDirectory() )
                {
                    try
                    {
                        includedSources.addAll( FileUtils.getFileNames( sourceDir, includePaths, excludePaths, true ) );
                    }
                    catch ( IOException e )
                    {
                        throw new MojoExecutionException( "Error retrieving files in: \'" + sourceDir + "\' ", e );
                    }
                }
                else
                {
                    getLog().info( "Source directory \'" + sourceDir
                                       + "\' doesn't exist. Ignoring directory in schema generation." );
                }
            }

            List<String> args = new ArrayList<String>();
            StringBuilder classPath = new StringBuilder();
            try
            {
                List<String> classpathFiles = getClasspathElements( project );
                classPath = new StringBuilder();
                for ( String classpathFile : classpathFiles )
                {
                    classPath.append( classpathFile );
                    classPath.append( File.pathSeparatorChar );
                }
            }
            catch ( DependencyResolutionRequiredException e )
            {
                throw new MojoExecutionException( e.getMessage(), e );
            }

            if ( !getOutputDirectory().exists() && !getOutputDirectory().mkdirs() )
            {
                throw new MojoExecutionException( "Could not create directory "
                    + getOutputDirectory().getAbsolutePath() );
            }
            try
            {
                args.add( "-d" );
                args.add( getOutputDirectory().getAbsolutePath() );
                args.add( "-classpath" );
                args.add( classPath.toString() );
                args.addAll( includedSources );
                if ( getLog().isDebugEnabled() )
                {
                    getLog().debug( "Args for SchemaGenerator: " + args );
                }
                SchemaGenerator.run( args.toArray( new String[0] ) );
            }
            catch ( Exception e )
            {
                throw new MojoExecutionException( "Failed to generate schema", e );
            }

            if ( transformSchemas != null )
            {
                // Check configuration - we cannot have duplicate namespace URI, prefix or file.
                SchemagenHelper.validateSchemasInPluginConfiguration( transformSchemas );

                if ( hasRenamingSchemas() )
                {
                    try
                    {
                        FileUtils.copyDirectory( getOutputDirectory(), getWorkDirectory() );
                    }
                    catch ( IOException e )
                    {
                        throw new MojoExecutionException( e.getMessage() );
                    }
                }

                // Acquire resolvers for all generated files.
                final Map<String, SimpleNamespaceResolver> resolverMap =
                    SchemagenHelper.getFileNameToResolverMap( getOutputDirectory() );

                // Transform all namespace prefixes as requested.
                SchemagenHelper.replaceNamespacePrefixes( resolverMap, transformSchemas, getLog(), getOutputDirectory() );

                // Rename all generated schema files as requested.
                SchemagenHelper.renameGeneratedSchemaFiles( resolverMap, transformSchemas, getLog(),
                                                            getOutputDirectory() );
            }

            buildContext.refresh( getOutputDirectory() );
        }
        else
        {
            getLog().info( "No updated sources found - skipping schema generation." );
        }
    }

    /**
     * Checks if there have been any changes to the sources since the last build and therefore the generated files are
     * not up-to-date and need to be re-generated.
     */
    private boolean isOutputStale()
        throws MojoExecutionException
    {
        if ( buildContext instanceof DefaultBuildContext )
        {
            // Here we can't use the buildContext to determine if everything is up-to-date, as
            // DefaultBuildContext behaves as if all files were just created.
            return commandLineStalenessCheck();
        }
        else
        {
            return buildContextStalenessCheck();
        }
    }

    private boolean commandLineStalenessCheck()
        throws MojoExecutionException
    {
        SourceInclusionScanner staleSourceScanner = new StaleSourceScanner( staleMillis, includes, excludes );
        SourceMapping mapping = new SingleTargetSourceMapping( ".java", SCHEMAGEN_EMITTED_FILENAME );
        staleSourceScanner.addSourceMapping( mapping );

        // Look inside every compileSourceRoot
        for ( String path : getCompileSourceRoots() )
        {
            File sourceDir = new File( path );
            try
            {
                Set<File> includedSources = staleSourceScanner.getIncludedSources( sourceDir, getOutputDirectory() );
                if ( !includedSources.isEmpty() )
                {
                    return true;
                }
            }
            catch ( InclusionScanException e )
            {
                throw new MojoExecutionException( "Error scanning source root: \'" + sourceDir + "\' "
                    + "for stale files to recompile.", e );
            }
        }

        return false;
    }

    private boolean buildContextStalenessCheck()
    {
        String[] includesArray = this.includes.toArray( new String[0] );
        String[] excludesArray = this.excludes.toArray( new String[0] );

        // Check for modified files
        for ( String path : getCompileSourceRoots() )
        {
            File sourceDir = new File( path );

            Scanner modifiedScanner = this.buildContext.newScanner( sourceDir );
            modifiedScanner.setIncludes( includesArray );
            modifiedScanner.setExcludes( excludesArray );
            modifiedScanner.scan();

            String[] includedFiles = modifiedScanner.getIncludedFiles();
            if ( includedFiles.length != 0 )
            {
                return true;
            }
        }

        // Check for deleted files
        for ( String path : getCompileSourceRoots() )
        {
            File sourceDir = new File( path );

            Scanner deletedScanner = this.buildContext.newDeleteScanner( sourceDir );
            deletedScanner.setIncludes( includesArray );
            deletedScanner.setExcludes( excludesArray );
            deletedScanner.scan();

            String[] includedFiles = deletedScanner.getIncludedFiles();
            if ( includedFiles.length != 0 )
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Check if any schema will be renamed.
     * 
     * @return {@code true} if a schema will be renamed, otherwise {@code false}
     */
    private boolean hasRenamingSchemas()
    {
        if ( transformSchemas != null )
        {
            for ( TransformSchema schema : transformSchemas )
            {
                if ( schema.getToFile() != null )
                {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * @return The directory where the schema files should be placed.
     */
    protected abstract File getOutputDirectory();

    /**
     * If files will be renamed, keep the original files here.
     * 
     * @return the work directory
     */
    protected abstract File getWorkDirectory();

    protected abstract List<String> getCompileSourceRoots();

    protected abstract List<String> getClasspathElements( MavenProject project )
        throws DependencyResolutionRequiredException;
}