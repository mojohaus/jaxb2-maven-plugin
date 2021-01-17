package org.domain;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlSeeAlso;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace = "http://schema.domain.org/integration/1.0",
        name = "importItem")
@XmlSeeAlso({ SomeImportItemDTO.class, SomeOtherImportItemDTO.class })
public abstract class ImportItemDTO {

    @XmlAttribute
    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
