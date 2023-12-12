package org.codehaus.mojo.jaxb2.schemageneration.postprocessing.schemaenhancement;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

/**
 * This is a XmlType and name-switched class using Field access.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlRootElement
@XmlType(name = "AnnotatedXmlNameAnnotatedClassWithFieldAccessTypeName")
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlNameAnnotatedClassWithFieldAccess {

    /**
     * This is a string field.
     */
    @XmlElement(name = "annotatedStringField")
    private String stringField;

    /**
     * This is an integer field.
     */
    @XmlAttribute(name = "annotatedIntegerField")
    private Integer integerField;

    public XmlNameAnnotatedClassWithFieldAccess() {}

    public XmlNameAnnotatedClassWithFieldAccess(final String stringField, final Integer integerField) {
        this.stringField = stringField;
        this.integerField = integerField;
    }

    public String getStringField() {
        return stringField;
    }

    public Integer getIntegerField() {
        return integerField;
    }
}
