package org.acme;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlType( name = "BarType", namespace="##default" )
@XmlAccessorType( XmlAccessType.FIELD )
public class FooBar
{
    @XmlElement( required = false )
    private FooBaz bazElement;
}
