package org.testing.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

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