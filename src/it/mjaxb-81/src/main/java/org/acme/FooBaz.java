package org.acme;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType( name = "BazType", namespace = "##default", propOrder = { "requiredElement", "requiredAttribute" } )
@XmlAccessorType( XmlAccessType.FIELD )
public class FooBaz
{
    @XmlElement( required = true, defaultValue = "requiredElementValue" )
    private String requiredElement;

    @XmlAttribute( required = true )
    private String requiredAttribute;
}
