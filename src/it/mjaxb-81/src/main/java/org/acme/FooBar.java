package org.acme;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType( name = "BarType", namespace="##default" )
@XmlAccessorType( XmlAccessType.FIELD )
public class FooBar
{
    @XmlElement( required = false )
    private FooBaz bazElement;
}
