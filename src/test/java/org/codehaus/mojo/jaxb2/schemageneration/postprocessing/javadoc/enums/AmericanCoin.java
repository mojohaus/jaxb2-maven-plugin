package org.codehaus.mojo.jaxb2.schemageneration.postprocessing.javadoc.enums;

import org.codehaus.mojo.jaxb2.schemageneration.postprocessing.javadoc.SomewhatNamedPerson;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

/**
 * Simple enumeration example defining standard US coins.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlType(namespace = SomewhatNamedPerson.NAMESPACE)
@XmlEnum(Integer.class)
@XmlAccessorType(XmlAccessType.FIELD)
public enum AmericanCoin {

    @XmlEnumValue("1") PENNY(1),
    @XmlEnumValue("5") NICKEL(5),
    @XmlEnumValue("10") DIME(10),
    @XmlEnumValue("25") QUARTER(25);

    // Internal state
    private int value;

    AmericanCoin(final int value) {
        this.value = value;
    }

    /**
     * The value - in cents - of this coin.
     *
     * @return the value - in cents - of this coin.
     */
    public int getValue() {
        return value;
    }
}
