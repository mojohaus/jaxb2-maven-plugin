package testdata.schemageneration.javadoc.annotated;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

/**
 * This is a XmlType and name-switched class using Method access.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlRootElement
@XmlType(name = "AnnotatedXmlNameAnnotatedClassWithMethodAccessTypeName")
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class XmlNameAnnotatedClassWithMethodAccess {

    private String stringField;

    private Integer integerField;

    public XmlNameAnnotatedClassWithMethodAccess() {
    }

    public XmlNameAnnotatedClassWithMethodAccess(final String stringField, final Integer integerField) {
        this.stringField = stringField;
        this.integerField = integerField;
    }

    /**
     * Getter for the stringField.
     *
     * @return the stringField value
     */
    @XmlElement(name = "annotatedStringMethod")
    public String getStringField() {
        return stringField;
    }

    /**
     * Getter for the integerField.
     *
     * @return the integerField value
     */
    @XmlAttribute(name = "annotatedIntegerMethod")
    public Integer getIntegerField() {
        return integerField;
    }
}
