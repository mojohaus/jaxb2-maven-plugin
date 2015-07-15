package se.west.gnat;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class IncorrectJaxbAnnotationClass {

    // Internal state
    private String bar;

    @XmlElement(required = true)
    private String getBar() {
        return bar;
    }
}