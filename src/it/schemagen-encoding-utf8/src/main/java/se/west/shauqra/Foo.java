package se.west.shauqra;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlType(namespace = "test")
@XmlAccessorType(XmlAccessType.FIELD)
public class Foo {
    @XmlElement(name = "utf8-name_äöüÄÖÜáéúàèù")
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
