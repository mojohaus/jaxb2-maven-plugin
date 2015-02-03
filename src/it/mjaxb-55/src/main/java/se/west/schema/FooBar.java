package se.west.schema;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

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
