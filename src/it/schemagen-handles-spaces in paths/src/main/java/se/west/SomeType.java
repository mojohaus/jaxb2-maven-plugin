package se.west;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlRootElement(name = "someType", namespace = "http://mojohaus.org/whitespacetest")
@XmlType(name = "someType", namespace = "http://mojohaus.org/whitespacetest")
@XmlAccessorType(XmlAccessType.FIELD)
public class SomeType {

    private String name;

    public SomeType() {
    }

    public SomeType(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
