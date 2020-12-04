package se.west.gnat;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class IncorrectJaxbAnnotationClass {

    // Internal state
    private String bar;

    @XmlElement(required = true)
    private String getBar() {
        return bar;
    }
}