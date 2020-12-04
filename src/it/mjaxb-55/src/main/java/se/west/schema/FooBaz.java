package se.west.schema;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>
 */
@XmlType(namespace = Namespaces.ANOTHER_NAMESPACE,
        propOrder = {"requiredElement", "anOptionalElementInSomeNamespace",
                "aRequiredElementInYetAnotherNamespace", "requiredAttribute", "optionalAttribute"})
@XmlAccessorType(XmlAccessType.FIELD)
public class FooBaz {

    @XmlElement(required = true, defaultValue = "requiredElementValue")
    private String requiredElement;

    @XmlElement(namespace = Namespaces.YET_ANOTHER_NAMESPACE, required = true, defaultValue = "requiredElementValue")
    private String aRequiredElementInYetAnotherNamespace;

    @XmlElement(namespace = Namespaces.SOME_NAMESPACE, required = false)
    private String anOptionalElementInSomeNamespace;

    @XmlAttribute(required = true)
    private String requiredAttribute;

    @XmlAttribute(required = false)
    private String optionalAttribute;
}
