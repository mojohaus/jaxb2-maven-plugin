package foo.bar;

import com.example.myschema.*;

public class Main
{

    public static void main( String args )
    {
        ObjectFactory objectFactory = new ObjectFactory();
        AddressType address = objectFactory.createAddressType();
        boolean primary = address.isPrimary();
        Boolean expensive = address.getExpensive();
    }
    
}
