package se.west.gnat;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;

@XmlType(name="RenamedFoo")
@XmlAccessorType(XmlAccessType.FIELD)
public class Foo {

    /**
     * This is a Bar.
     */
    @XmlAttribute(name = "renamedBar")
    private String bar;
}
