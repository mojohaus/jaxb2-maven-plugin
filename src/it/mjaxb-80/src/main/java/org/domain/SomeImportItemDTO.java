package org.domain;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "someImportItem",
        propOrder = {"someIdentifier"})
@XmlRootElement(name = "someImportItem")
public class SomeImportItemDTO extends ImportItemDTO {

    @XmlElement(name = "someIdentifier", required = true)
    private String someIdentifier;

    public String getSomeIdentifier() {
        return someIdentifier;
    }

    public void setSomeIdentifier(String id) {
        this.someIdentifier = id;
    }
}
