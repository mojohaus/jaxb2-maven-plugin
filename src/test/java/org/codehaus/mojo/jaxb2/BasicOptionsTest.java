package org.codehaus.mojo.jaxb2;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.codehaus.plexus.util.FileUtils;
import org.sonatype.plexus.build.incremental.DefaultBuildContext;

/**
 * Test that the basic configuration options work.
 * 
 * @author <a href="mailto:aronvaughan@hotmail.com">Aron Vaughan</a>
 */
public class BasicOptionsTest
    extends AbstractMojoTestCase
{

    protected File outputLocationDirectory;

    protected void setUp()
        throws Exception
    {
        super.setUp();
        outputLocationDirectory = new File( getBasedir() + "/target/test-generated-sources/jaxb2/plugin" );
    }

    protected void tearDown()
        throws Exception
    {
        super.tearDown();
        deleteFiles( outputLocationDirectory );
    }

    /**
     * Pull in a specific test pom and bootstrap the XjcMojo.
     * 
     * @param pomPath a <code>String</code> value
     * @return a <code>XjcMojo</code> value
     * @exception Exception if an error occurs
     */
    private AbstractXjcMojo configureMojo( String pomPath )
        throws Exception
    {
        // configure the mojo with our test pom
        File pom = new File( getBasedir(), pomPath );
        AbstractXjcMojo xjcMojo = new XjcMojo();
        xjcMojo = (AbstractXjcMojo) configureMojo( xjcMojo, "jaxb2-maven-plugin", pom );
        assertNotNull( xjcMojo );

        super.setVariableValueToObject( xjcMojo, "buildContext", new DefaultBuildContext() );

        // return the configured mojo
        return xjcMojo;
    }

    /**
     * Check that the fileNamesThatShouldBeFound exist in the given directoryListing and that only those files are
     * found.
     * 
     * @param fileNamesThatShouldBeFound a <code>String[]</code> value of files to find
     * @param directoryListing a <code>String[]</code> value of files that exist
     */
    private void assertFileNames( String[] fileNamesThatShouldBeFound, String[] directoryListing )
    {
        // check for each file that should be found
        for ( String aFileNameToBeFound : fileNamesThatShouldBeFound )
        {
            boolean found = false;
            for ( String aFileInDirectory : directoryListing )
            {
                if ( aFileNameToBeFound.equals( aFileInDirectory ) )
                {
                    found = true;
                    break;
                }
            }
            assertTrue( "could not find file: " + aFileNameToBeFound + " in directoryListing: "
                            + Arrays.toString( directoryListing ), found );
        }

        // check that the length of the two listings are the same
        assertEquals( "expected: " + Arrays.toString( fileNamesThatShouldBeFound ) + " does not equal actual: "
            + Arrays.toString( directoryListing ), fileNamesThatShouldBeFound.length, directoryListing.length );
    }

    /**
     * Delete all the files in a given directory.
     * 
     * @param directory a <code>File</code> value
     */
    private void deleteFiles( File directory )
    {
        if ( directory != null && directory.isDirectory() )
        {
            for ( String aFile : directory.list() )
            {
                new File( directory, aFile ).delete();
            }

            String[] leftoverFiles = directory.list();
            assertEquals( 0, leftoverFiles.length );
        }
    }

    /**
     * Tests that passing the schemaList variable to the mojo generates and picks up the expected schemas.
     * 
     * @exception Exception if an error occurs
     */
    public void testSchemaListInputOption()
        throws Exception
    {
        // setup test #1
        AbstractXjcMojo xjcMojo = configureMojo( "src/test/resources/test1-pom.xml" );

        // execute the project
        xjcMojo.execute();

        // check output
        String[] filesInOutputLocation = outputLocationDirectory.list();
        assertFileNames( new String[] { "AddressType.java", "ObjectFactory.java", ".staleFlag" }, filesInOutputLocation );
    }

    /**
     * Tests that passing the schemaListFileName variable to the mojo generates and picks up the expected schemas.
     * 
     * @exception Exception if an error occurs
     */
    public void testSchemaListFileNameInputOption()
        throws Exception
    {
        // setup test #2
        AbstractXjcMojo xjcMojo = configureMojo( "src/test/resources/test2-pom.xml" );

        // execute it
        xjcMojo.execute();

        // check output
        assertFileNames( new String[] { "AddressType2.java", "AddressType3.java", ".staleFlag2", "ObjectFactory.java" },
                         outputLocationDirectory.list() );
    }

    /**
     * Tests that passing the schema directory only variable to the mojo generates and picks up the expected schemas.
     * 
     * @exception Exception if an error occurs
     */
    public void testSchemaDirectoryOnlyInputOption()
        throws Exception
    {
        // setup test #3
        AbstractXjcMojo xjcMojo = configureMojo( "src/test/resources/test3-pom.xml" );

        // execute it
        xjcMojo.execute();

        // check output
        assertFileNames( new String[] { "AddressType.java", "AddressType2.java", "AddressType3.java",
            "AddressType4.java", ".staleFlag2", "ObjectFactory.java" }, outputLocationDirectory.list() );
    }

    public void testParameterEncoding()
        throws Exception
    {
        String encode_a = "UTF-8";
        String encode_b = "ISO-8859-1";

        // setup test #4 UTF-8
        AbstractXjcMojo xjcMojo_utf8 = configureMojo( "src/test/resources/test4-utf8-pom.xml" );

        // execute it
        xjcMojo_utf8.execute();

        File outputLocationDirectory_utf8 =
            new File( getBasedir() + "/target/test-generated-sources/utf8/jaxb2/plugin" );
        // check output
        assertFileNames( new String[] { "AddressType4.java", ".staleFlag4-utf8", "ObjectFactory.java" },
                         outputLocationDirectory_utf8.list() );

        // setup test #4 ISO-8859-1
        AbstractXjcMojo xjcMojo_iso88591 = configureMojo( "src/test/resources/test4-iso88591-pom.xml" );

        // execute it
        xjcMojo_iso88591.execute();

        File outputLocationDirectory_iso88591 =
            new File( getBasedir() + "/target/test-generated-sources/iso88591/jaxb2/plugin" );
        // check output
        assertFileNames( new String[] { "AddressType4.java", ".staleFlag4-iso88591", "ObjectFactory.java" },
                         outputLocationDirectory_iso88591.list() );

        String fileName = "AddressType4.java";
        compareFiles( fileName, encode_a, encode_b, outputLocationDirectory_utf8, outputLocationDirectory_iso88591 );
    }

    private void compareFiles( String fileName, String encode_a, String encode_b,
                               File outputLocationDirectory_encode_a, File outputLocationDirectory_encode_b )
        throws IOException
    {
        List<String> lines_encode_a = readFile( outputLocationDirectory_encode_a, fileName, encode_a );
        List<String> lines_encode_b = readFile( outputLocationDirectory_encode_b, fileName, encode_b );

        final String timestamp_string = "// Generated on";
        for ( int i = 0; i < lines_encode_a.size(); i++ )
        {
            // As the timestamp in the generated files could differ we ignore that line
            if ( !lines_encode_a.get( i ).startsWith( timestamp_string )
                || !lines_encode_b.get( i ).startsWith( timestamp_string ) )
            {
                assertEquals( "Encoding comparison failed for " + fileName + " on line " + ( i + 1 ),
                              lines_encode_a.get( i ), lines_encode_b.get( i ) );
            }
        }
    }

    private List<String> readFile( File outputLocationDirectory, String fileName, String encoding )
        throws IOException
    {
        String text = FileUtils.fileRead( new File( outputLocationDirectory, fileName ), encoding );
        BufferedReader reader = new BufferedReader( new StringReader( text ) );
        ArrayList<String> lines = new ArrayList<String>();
        String line;
        while ( ( line = reader.readLine() ) != null )
        {
            lines.add( line );
        }

        return lines;
    }
}
