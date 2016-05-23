package testdata.schemageneration.javadoc.annotated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

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
