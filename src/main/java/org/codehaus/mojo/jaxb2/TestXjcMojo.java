package org.codehaus.mojo.jaxb2;

import java.io.File;
import java.util.List;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.project.MavenProject;

/**
 * <p>A Maven 2 plugin which parse xsd and binding files (xjb) and produces
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
    protected List getClasspathElements(MavenProject project) throws DependencyResolutionRequiredException
    {
        return project.getTestClasspathElements();
    }

	@Override
    protected void addCompileSourceRoot(MavenProject project) 
    {
      project.addTestCompileSourceRoot( getOutputDirectory().getAbsolutePath() );
    }

	@Override
	protected File getSchemaDirectory() {
		return schemaDirectory;
	}

	@Override
	protected File getBindingDirectory() {
		return bindingDirectory;
	}
}
