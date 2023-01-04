package se.west.shauqra;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlRootElement(name = "customerForNamelessFoo", namespace = "http://acme.com/customer-api")
@XmlType(name = "", namespace = "http://acme.com/customer-api")
@XmlAccessorType(XmlAccessType.FIELD)
public class FooWithEmptyXmlTypeName {
    @XmlElement(required = true, defaultValue = "defaultName")
    private String name;

    @XmlElement(required = true, namespace = "http://acme.com/customer-api")
    private String anotherName;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAnotherName() {
        return anotherName;
    }

    public void setAnotherName(String name) {
        this.anotherName = name;
    }
}
