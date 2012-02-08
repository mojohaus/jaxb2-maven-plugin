package se.west.schema;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

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
