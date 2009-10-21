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
import java.util.Set;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
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
     * The default maven project object
     * 
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * A list of inclusion filters for the generator.
     * 
     * @parameter
     */
    private Set < String > includes = new HashSet < String > ();

    /**
     * A list of exclusion filters for the generator.
     * 
     * @parameter
     */
    private Set < String > excludes = new HashSet < String > ();

    /**
     * The granularity in milliseconds of the last modification date for testing whether a source needs recompilation
     * 
     * @parameter expression="${lastModGranularityMs}" default-value="0"
     */
    private int staleMillis;

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        if ( includes.isEmpty() )
        {
            throw new MojoExecutionException( "At least one file has to be included" );
        }

        // First check out if there are stale sources
        SourceInclusionScanner staleSourceScanner = new StaleSourceScanner( staleMillis, includes, excludes );
        SourceMapping mapping = new SingleTargetSourceMapping( ".java", "schema1.xsd" );
        staleSourceScanner.addSourceMapping( mapping );
        Set < File > staleSources = new HashSet < File > ();

        // Look inside every compileSourceRoot
        for ( String path : (List < String > ) getCompileSourceRoots() )
        {
            File sourceDir = new File( path );
            try
            {
                staleSources.addAll( staleSourceScanner.getIncludedSources( sourceDir, getOutputDirectory() ) );
            }
            catch ( InclusionScanException e )
            {
                throw new MojoExecutionException( "Error scanning source root: \'" + sourceDir + "\' "
                    + "for stale files to recompile.", e );
            }
        }
        if ( !staleSources.isEmpty() )
        {
            String includePaths = StringUtils.join( includes.toArray(), "," );
            String excludePaths = StringUtils.join( excludes.toArray(), "," );
            Set < String > includedSources = new HashSet < String > ();

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

            List < String > args = new ArrayList < String > ();
            StringBuilder classPath = new StringBuilder();
            try
            {
                List < String > classpathFiles = getClasspathElements( project );
                classPath = new StringBuilder();
                for ( int i = 0; i < classpathFiles.size(); ++i )
                {
                    if ( getLog().isDebugEnabled() )
                    {
                        getLog().debug( (String) classpathFiles.get( i ) );
                    }
                    classPath.append( (String) classpathFiles.get( i ) );
                    classPath.append( File.pathSeparatorChar );
                }
            }
            catch ( DependencyResolutionRequiredException e )
            {
                throw new MojoExecutionException( e.getMessage(), e );
            }

            if ( !getOutputDirectory().exists() )
            {
                if ( !getOutputDirectory().mkdirs() )
                {
                    throw new MojoExecutionException( "Could not create directory "
                        + getOutputDirectory().getAbsolutePath() );
                }
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
        }
        else
        {
            getLog().info( "no sources found skip generate schema" );
        }
    }

    protected abstract File getOutputDirectory();

    protected abstract List < String > getCompileSourceRoots();

    protected abstract List < String > getClasspathElements( MavenProject project )
        throws DependencyResolutionRequiredException;

}
