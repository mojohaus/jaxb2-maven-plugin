package org.domain;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace = "http://schema.domain.org/integration/1.0",
        name = "someOtherImportItem",
        propOrder = {"someWeirdIdentifier"})
@XmlRootElement(namespace = "http://schema.domain.org/integration/1.0",
        name = "someOtherImportItem")
public class SomeOtherImportItemDTO extends ImportItemDTO {

    @XmlElement(name = "someWeirdIdentifier", required = true)
    private String someWeirdIdentifier;

    public String getSomeIdentifier() {
        return someWeirdIdentifier;
    }

    public void setSomeIdentifier(String id) {
        this.someWeirdIdentifier = id;
    }
}
