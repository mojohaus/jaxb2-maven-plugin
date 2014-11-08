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
import org.apache.maven.model.Resource;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

/**
 * Generates Java sources from XML Schema(s) and binding file(s)
 * using the JAXB Binding Compiler (XJC).
 *
 * @author jgenender@apache.org
 * @author jgenender <jgenender@apache.org>
 * @version $Id$
 */
@Mojo( name = "xjc", defaultPhase = LifecyclePhase.GENERATE_SOURCES,
       requiresDependencyResolution = ResolutionScope.COMPILE,
       threadSafe = true)
public class XjcMojo
    extends AbstractXjcMojo
{
    /**
     * The working directory where the generated Java source files are created.
     */
    @Parameter( defaultValue = "${project.build.directory}/generated-sources/jaxb", required = true )
    private File outputDirectory;

    /**
     * The directory for XML Schema files (XSDs).
     */
    @Parameter( defaultValue = "${project.basedir}/src/main/xsd", required = true )
    private File schemaDirectory;

    /**
     * The directory for JAXB binding files.
     */
    @Parameter( defaultValue = "${project.basedir}/src/main/xjb" )
    private File bindingDirectory;

    @Override
    protected File getOutputDirectory()
    {
        return outputDirectory;
    }

    @Override
    protected String getStaleFileExtensionSuffix()
    {
        return "xjcStaleFlag";
    }

    @SuppressWarnings( "unchecked" )
    @Override
    protected List<String> getClasspathElements( MavenProject project )
        throws DependencyResolutionRequiredException
    {
        return project.getCompileClasspathElements();
    }

    @Override
    protected void addCompileSourceRoot( MavenProject project )
    {
        project.addCompileSourceRoot( getOutputDirectory().getAbsolutePath() );
    }

    @Override
    protected void addResource( MavenProject project, Resource resource )
    {
        project.addResource( resource );
    }

    @Override
    protected File getSchemaDirectory()
    {
        return schemaDirectory;
    }

    @Override
    protected File getBindingDirectory()
    {
        return bindingDirectory;
    }
}