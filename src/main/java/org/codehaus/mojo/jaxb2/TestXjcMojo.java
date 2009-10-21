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
import org.apache.maven.project.MavenProject;

/**
 * <p>parse xsd and binding test resources (xjb) to produce
 * a corresponding object model based on the JAXB Xjc parsing engine.</p>
 *
 * @goal testXjc
 * @phase generate-test-sources
 * @requiresDependencyResolution
 * @description JAXB 2.0 Plugin.
 * @author rfscholte <rfscholte@codehaus.org>
 * @version $Id$
 */
public class TestXjcMojo
    extends AbstractXjcMojo
{

    /**
     * The working directory to create the generated java source files.
     * 
     * @parameter expression="${project.build.directory}/generated-test-sources/jaxb"
     * @required
     */
    private File outputDirectory;

    /**
     * The location of the flag file used to determine if the output is stale.
     * 
     * @parameter default-value="${project.build.directory}/generated-test-sources/jaxb/.staleFlag"
     * @required
     */
    private File staleFile;

    /**
     * The schema directory or xsd files
     * 
     * @parameter expression="${basedir}/src/test/xsd"
     * @required
     */
    private File schemaDirectory;

    /**
     * The binding directory for xjb files
     * 
     * @parameter expression="${basedir}/src/test/xjb"
     */
    private File bindingDirectory;

    @Override
    protected File getOutputDirectory()
    {
        return outputDirectory;
    }

    @Override
    protected File getStaleFile()
    {
        return staleFile;
    }

    @Override
    protected List getClasspathElements( MavenProject project )
        throws DependencyResolutionRequiredException
    {
        return project.getTestClasspathElements();
    }

    @Override
    protected void addCompileSourceRoot( MavenProject project )
    {
        project.addTestCompileSourceRoot( getOutputDirectory().getAbsolutePath() );
    }

    @Override
    protected void addResource( MavenProject project, Resource resource )
    {
        project.addTestResource( resource );
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
