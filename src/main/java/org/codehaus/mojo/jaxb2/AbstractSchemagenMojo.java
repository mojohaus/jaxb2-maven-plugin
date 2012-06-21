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
import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.jaxb2.helpers.SchemagenHelper;
import org.codehaus.mojo.jaxb2.helpers.SimpleNamespaceResolver;
import org.codehaus.plexus.compiler.util.scan.InclusionScanException;
import org.codehaus.plexus.compiler.util.scan.SimpleSourceInclusionScanner;
import org.codehaus.plexus.compiler.util.scan.SourceInclusionScanner;
import org.codehaus.plexus.compiler.util.scan.StaleSourceScanner;
import org.codehaus.plexus.compiler.util.scan.mapping.SingleTargetSourceMapping;
import org.codehaus.plexus.compiler.util.scan.mapping.SourceMapping;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;

import com.sun.tools.jxc.SchemaGenerator;

/**
 * @author rfscholte
 * @since 1.3
 */
public abstract class AbstractSchemagenMojo
    extends AbstractMojo
{
    /**
     * Name of the generated schema file, emitted by the SchemaGenerator.
     * <br/>
     * According to the JAXB Schema Generator documentation:
     * "There is no way to control the name of the generated schema files at this time."
     */
    private static final String SCHEMAGEN_EMITTED_FILENAME = "schema1.xsd";

    /**
     * The default maven project object.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * A List holding desired schema mappings, each of which binds a schema namespace URI
     * to its desired prefix [optional] and the name of the resulting schema file [optional].
     * All given elements (uri, prefix, file) must be unique within the configuration; no two
     * elements may have the same values.
     * <p/>
     * <p/>
     * <p>The example schema configuration below maps two namespace uris to prefixes and
     * generated file names. This implies that <tt>http://some/namespace</tt> will be represented
     * by the prefix <tt>some</tt> within the generated XML Schema files; creating namespace
     * definitions on the form <tt>xmlns:some="http://some/namespace"</tt>, and corresponding
     * uses on the form <tt>&lt;xs:element minOccurs="0"
     * ref="<strong>some:</strong>anOptionalElementInSomeNamespace"/></tt>. Moreover, the file
     * element defines that the <tt>http://some/namespace</tt> definitions will be written to the
     * file <tt>some_schema.xsd</tt>, and that
     * all import references will be on the form <tt>&lt;xs:import namespace="http://some/namespace"
     * schemaLocation="<strong>some_schema.xsd</strong>"/></tt></p>
     * <p/>
     * <p>
     * The example configuration below also performs identical operations for the namespace uri
     * <tt>http://another/namespace</tt> with the prefix <tt>another</tt> and the file
     * <tt>another_schema.xsd</tt>.</p>
     * <p/>
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
     * @parameter
     */
    private List<TransformSchema> transformSchemas;

    /**
     * A list of inclusion filters for the generator.
     *
     * @parameter
     */
    private Set<String> includes = new HashSet<String>();

    /**
     * A list of exclusion filters for the generator.
     *
     * @parameter
     */
    private Set<String> excludes = new HashSet<String>();

    /**
     * The granularity in milliseconds of the last modification date for testing whether a source needs recompilation.
     *
     * @parameter expression="${lastModGranularityMs}" default-value="0"
     */
    private int staleMillis;

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        if ( "pom".equals( project.getPackaging() ) )
        {
            return;
        }
        
        if ( includes.isEmpty() )
        {
            throw new MojoExecutionException( "At least one file has to be included" );
        }

        // First check out if there are stale sources
        SourceInclusionScanner staleSourceScanner = new StaleSourceScanner( staleMillis, includes, excludes );
        SourceMapping mapping = new SingleTargetSourceMapping( ".java", SCHEMAGEN_EMITTED_FILENAME );
        staleSourceScanner.addSourceMapping( mapping );
        Set<File> staleSources = new HashSet<File>();

        // Look inside every compileSourceRoot
        for ( String path : getCompileSourceRoots() )
        {
            File sourceDir = new File( path );
            try
            {
                staleSources.addAll( staleSourceScanner.getIncludedSources( sourceDir, getOutputDirectory() ) );
            }
            catch ( InclusionScanException e )
            {
                throw new MojoExecutionException(
                    "Error scanning source root: \'" + sourceDir + "\' " + "for stale files to recompile.", e );
            }
        }
        if ( !staleSources.isEmpty() )
        {
            String includePaths = StringUtils.join( includes.toArray(), "," );
            String excludePaths = StringUtils.join( excludes.toArray(), "," );
            Set<String> includedSources = new HashSet<String>();

            SourceInclusionScanner sourceScanner = new SimpleSourceInclusionScanner( includes, excludes );
            sourceScanner.addSourceMapping( mapping );

            for ( String path : getCompileSourceRoots() )
            {
                File sourceDir = new File( path );
                if ( getLog().isDebugEnabled() )
                {
                    getLog().debug( "sourceDir: " + sourceDir.getAbsolutePath() );
                }
                try
                {
                    includedSources.addAll( FileUtils.getFileNames( sourceDir, includePaths, excludePaths, true ) );
                }
                catch ( IOException e )
                {
                    throw new MojoExecutionException( "Error retrieving files in: \'" + sourceDir + "\' ", e );
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
                    if ( getLog().isDebugEnabled() )
                    {
                        getLog().debug( classpathFile );
                    }

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
                    throw new MojoExecutionException(
                        "Could not create directory " + getOutputDirectory().getAbsolutePath() );
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
                    getLog().debug( "args for SchemaGenerator " + args );
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
                SchemagenHelper.replaceNamespacePrefixes( resolverMap, transformSchemas, getLog(),
                                                          getOutputDirectory() );

                // Rename all generated schema files as requested.
                SchemagenHelper.renameGeneratedSchemaFiles( resolverMap, transformSchemas, getLog(),
                                                            getOutputDirectory() );
            }
        }
        else
        {
            getLog().info( "No sources found. Skipping schema generation." );
        }
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