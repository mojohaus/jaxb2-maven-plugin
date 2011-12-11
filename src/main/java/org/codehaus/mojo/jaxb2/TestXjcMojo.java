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
 * Generates Java test sources from XML Schema(s) and binding file(s)
 * using the JAXB Binding Compiler (XJC).
 *
 * @author rfscholte <rfscholte@codehaus.org>
 * @version $Id$
 * @goal testXjc
 * @phase generate-test-sources
 * @requiresDependencyResolution
 * @description JAXB 2.0 Plugin.
 */
public class TestXjcMojo
    extends AbstractXjcMojo
{

    /**
     * The working directory where the generated Java test source files are created.
     *
     * @parameter default-value="${project.build.directory}/generated-test-sources/jaxb"
     * @required
     */
    private File outputDirectory;

    /**
     * The location of the flag file used to determine if the output is stale.
     *
     * @parameter default-value="${project.build.directory}/jaxb2/.testXjcStaleFlag"
     * @required
     */
    private File staleFile;

    /**
     * The directory for XML Schema files (XSDs).
     *
     * @parameter default-value="${basedir}/src/test/xsd"
     * @required
     */
    private File schemaDirectory;

    /**
     * The directory for JAXB binding files.
     *
     * @parameter default-value="${basedir}/src/test/xjb"
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
