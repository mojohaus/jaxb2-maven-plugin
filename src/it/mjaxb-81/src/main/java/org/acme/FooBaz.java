package org.acme;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlType( name = "BazType", namespace = "##default", propOrder = { "requiredElement", "requiredAttribute" } )
@XmlAccessorType( XmlAccessType.FIELD )
public class FooBaz
{
    @XmlElement( required = true, defaultValue = "requiredElementValue" )
    private String requiredElement;

    @XmlAttribute( required = true )
    private String requiredAttribute;
}
