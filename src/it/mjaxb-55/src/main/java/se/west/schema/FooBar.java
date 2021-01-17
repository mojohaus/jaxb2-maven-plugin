package se.west.schema;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>
 */
@XmlType(namespace = Namespaces.SOME_NAMESPACE,
        propOrder = {"requiredElement", "aRequiredElementInAnotherNamespace",
                "optionalElement", "requiredAttribute", "optionalAttribute"})
@XmlAccessorType(XmlAccessType.FIELD)
public class FooBar {

    @XmlElement(required = true, defaultValue = "requiredElementValue")
    private String requiredElement;

    @XmlElement(namespace = Namespaces.ANOTHER_NAMESPACE, required = true, defaultValue = "requiredElementValue")
    private String aRequiredElementInAnotherNamespace;

    @XmlElement(required = false)
    private String optionalElement;

    @XmlAttribute(required = true)
    private String requiredAttribute;

    @XmlAttribute(required = false)
    private String optionalAttribute;
}
