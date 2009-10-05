package org.codehaus.mojo.jaxb2;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import junit.framework.TestCase;
import org.apache.maven.project.MavenProject;
import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.plugin.testing.stubs.MavenProjectStub;


/**
 * Test that the basic configuration options work
 *
 * @author <a href="mailto:aronvaughan@hotmail.com">Aron Vaughan</a>
 * @version 1.0
 */

public class BasicOptionsTest extends AbstractMojoTestCase { //extends TestCase {

    protected File outputLocationDirectory;

    protected void setUp() throws Exception {
        super.setUp();
        outputLocationDirectory = new File( getBasedir()+"/target/test-generated-sources/jaxb2/plugin" );
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        deleteFiles( outputLocationDirectory );
    }


    /**
     * pull in a specific test pom and bootstrap the XjcMojo
     *
     * @param pomPath a <code>String</code> value
     * @return a <code>XjcMojo</code> value
     * @exception Exception if an error occurs
     */
    private AbstractXjcMojo configureMojo( String pomPath ) throws Exception {
        //configure the mojo with our test pom
        File pom = new File( getBasedir(), pomPath );
        AbstractXjcMojo xjcMojo = new XjcMojo();
        xjcMojo = (AbstractXjcMojo) configureMojo( xjcMojo, "jaxb2-maven-plugin", pom );
        assertNotNull( xjcMojo );

        //return the configured mojo
        return xjcMojo;
    }

    
    /**
     * check that the fileNamesThatShouldBeFound exist in the given 
     * directoryListing and that only those files are found
     *
     * @param fileNamesThatShouldBeFound a <code>String[]</code> value of files to find
     * @param directoryListing a <code>String[]</code> value of files that exist
     */
    private void assertFileNames( String [] fileNamesThatShouldBeFound, 
                                  String [] directoryListing ) {

        
        //check for each file that should be found
        for (String aFileNameToBeFound : fileNamesThatShouldBeFound ) {
            boolean found = false;
            for (String aFileInDirectory : directoryListing ) {
                if ( aFileNameToBeFound.equals( aFileInDirectory ) ) {
                    found = true;
                    break;
                }
            }
            assertTrue( "could not find file: "+aFileNameToBeFound+
                        " in directoryListing: "+Arrays.toString(directoryListing),
                        found );
        }

        //check that the length of the two listings are the same
        assertEquals( "expected: "+ Arrays.toString(fileNamesThatShouldBeFound)+
                      " does not equal actual: "+ Arrays.toString(directoryListing),
                      fileNamesThatShouldBeFound.length, directoryListing.length );
    }

    
    /**
     * delete all the files in a given directoryy
     *
     * @param directory a <code>File</code> value
     */
    private void deleteFiles( File directory ) {

        if (directory != null && directory.isDirectory() ) {
           for (String aFile : directory.list() ) {
               new File( directory, aFile).delete();
           }
 
           String [] leftoverFiles = directory.list();
           assertEquals( 0, leftoverFiles.length );
        }
    }
    
    /**
     * tests that passing the schemaList variable to the mojo
     * generates and picks up the expected schemas
     *
     * @exception Exception if an error occurs
     */
    public void testSchemaListInputOption() throws Exception {

        //setup test #1
        AbstractXjcMojo xjcMojo = configureMojo( "src/test/resources/test1-pom.xml" );

        //execute the project
        xjcMojo.execute();

        //check output
        String [] filesInOutputLocation = outputLocationDirectory.list();
        assertFileNames( new String[] { "AddressType.java", "ObjectFactory.java", 
                                        ".staleFlag" }, filesInOutputLocation );
    }


    /**
     * tests that passing the schemaListFileName variable to the mojo
     * generates and picks up the expected schemas
     *
     * @exception Exception if an error occurs
     */
    public void testSchemaListFileNameInputOption() throws Exception {

        //setup test #2
        AbstractXjcMojo xjcMojo = configureMojo( "src/test/resources/test2-pom.xml" );

        //execute it
        xjcMojo.execute();
        
        //check output
        assertFileNames( new String[] { "AddressType2.java", "AddressType3.java",
                                        ".staleFlag2", "ObjectFactory.java" },
                         outputLocationDirectory.list());
    }

    /**
     * tests that passing the schema directory only variable to the mojo
     * generates and picks up the expected schemas
     *
     * @exception Exception if an error occurs
     */
    public void testSchemaDirectoryOnlyInputOption() throws Exception {

        //setup test #2
        AbstractXjcMojo xjcMojo = configureMojo( "src/test/resources/test3-pom.xml" );

        //execute it
        xjcMojo.execute();
        
        //check output
        assertFileNames( new String[] { "AddressType.java", "AddressType2.java", 
                                        "AddressType3.java", ".staleFlag2", 
                                        "ObjectFactory.java" },
                         outputLocationDirectory.list());
    }


}
