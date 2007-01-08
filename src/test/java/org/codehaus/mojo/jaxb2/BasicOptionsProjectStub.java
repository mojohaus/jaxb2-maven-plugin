package org.codehaus.mojo.jaxb2;

import org.apache.maven.model.Build;
import org.apache.maven.plugin.testing.stubs.MavenProjectStub;

import java.util.ArrayList;
import java.util.List;


/**
 * <code>BasicOptionsProjectStub</code> acts as a project stub 
 * for the BasicOptionsTest.java tests - the xjc mojo needs
 * access to basic project information
 *
 * @author <a href="mailto:aronvaughan@hotmail.com">Aron Vaughan</a>
 * @version 1.0
 */
public class BasicOptionsProjectStub extends MavenProjectStub {

    //-------------------
    // member variables
    //-------------------
    protected Build build = new Build();

    public BasicOptionsProjectStub() {
        //setup stubs information so xjc and the mojo behave correctly
        setCompileSourceRoots( new ArrayList(0) );
        setBuildOutputDirectory( System.getProperty("basedir")+"/target" );
    }

    public Build getBuild() {
        return build;
    }

    /**
     * <code>setBuildOutputDirectory</code> sets the outputdirectory
     * for the tests that the XjcMojo uses
     *
     * @param buildOutputDirectory a <code>String</code> value
     */
    public void setBuildOutputDirectory(String buildOutputDirectory) {
        System.out.println( "setting outputdir: "+buildOutputDirectory );
        build.setOutputDirectory( buildOutputDirectory );
    }


}
