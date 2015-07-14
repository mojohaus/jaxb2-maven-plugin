package org.testing.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This is a class-level comment.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class MyType {

    /**
     * This is a field-level comment.
     */
    @XmlElement(required = true)
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}