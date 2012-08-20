import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType( name = "", namespace = "http://acme.com/customer-api" )
@XmlRootElement( name = "customer", namespace = "http://acme.com/customer-api" )
@XmlAccessorType( XmlAccessType.FIELD )
public class Foo
{
    @XmlElement( required = true, defaultValue = "defaultName" )
    private String name;

    @XmlElement( required = true, namespace = "http://acme.com/customer-api" )
    private String anotherName;

    public String getName()
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public String getAnotherName()
    {
        return anotherName;
    }

    public void setAnotherName( String name )
    {
        this.anotherName = name;
    }
}
