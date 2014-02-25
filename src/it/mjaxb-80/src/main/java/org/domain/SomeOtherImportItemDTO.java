package org.domain;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "someOtherImportItem",
    propOrder = { "someWeirdIdentifier"})
@XmlRootElement(name = "someOtherImportItem")
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
