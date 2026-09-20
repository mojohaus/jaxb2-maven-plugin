import java.io.*;
import java.util.*;
import java.util.jar.*;

try
{
    File jaxbDir = new File( basedir, "target/generated-sources/jaxb" );
    if ( !jaxbDir.exists() || !jaxbDir.isDirectory() )
    {
        System.err.println( "Could not find directory for JAXB generated sources: " + jaxbDir );
        return false;
    }
    
    File file;
    file = new File( jaxbDir, "com/example/myschema/AType.java" );
    if ( !file.exists() || file.isDirectory() )
    {
        System.err.println( "Could not find file: " + file );
        return false;
    }
    file = new File( jaxbDir, "com/example/myschema/BType.java" );
    if ( !file.exists() || file.isDirectory() )
    {
        System.err.println( "Could not find file: " + file );
        return false;
    }
    file = new File( jaxbDir, "com/example/myschema/CType.java" );
    if ( !file.exists() || file.isDirectory() )
    {
        System.err.println( "Could not find file: " + file );
        return false;
    }
    file = new File( jaxbDir, "com/example/myschema/DType.java" );
    if ( file.exists() )
    {
        System.err.println( "File '" + file + "' found, which shouldn't exist" );
        return false;
    }
}
catch( Throwable t )
{
    t.printStackTrace();
    return false;
}

return true;
