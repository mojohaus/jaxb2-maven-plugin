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
import java.util.List;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

/**
 * Creates XML Schema file(s) for sources.
 *
 * @author rfscholte
 * @since 1.3
 */
@Mojo( name = "schemagen", defaultPhase = LifecyclePhase.GENERATE_RESOURCES,
       requiresDependencyResolution = ResolutionScope.COMPILE )
public class SchemagenMojo
    extends AbstractSchemagenMojo
{
    /**
     * The source directories containing the sources to be compiled.
     */
    @Parameter( defaultValue = "${project.compileSourceRoots}", readonly = true, required = true )
    private List<String> compileSourceRoots;

    /**
     * The directory where the generated XML Schema file(s) will be placed.
     */
    @Parameter( defaultValue = "${project.build.directory}/generated-resources/schemagen", required = true )
    private File outputDirectory;
    
    /**
     * The name of the directory where copies of the original/generated
     * schema files are stored. Thus, original generated XSD files
     * are preserved for reference.
     */
    @Parameter( defaultValue = "${project.build.directory}/jaxb2/work" )
    private File workDirectory;


    @Override
    protected File getOutputDirectory()
    {
        return outputDirectory;
    }
    
    @Override
    protected File getWorkDirectory()
    {
        return workDirectory;
    }

    @Override
    protected List<String> getCompileSourceRoots()
    {
        return compileSourceRoots;
    }

    @SuppressWarnings( "unchecked" )
    @Override
    protected List<String> getClasspathElements( MavenProject project )
        throws DependencyResolutionRequiredException
    {
        return project.getCompileClasspathElements();
    }
}
