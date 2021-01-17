package enums;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlType;

/**
 * Simple enumeration example defining standard US coins.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlType(namespace = "http://gnat.west.se/foods")
@XmlEnum(Integer.class)
@XmlAccessorType(XmlAccessType.FIELD)
public enum AmericanCoin {

    /**
     * A Penny, worth 1 cent.
     */
    @XmlEnumValue("1") PENNY(1),

    /**
     * A Nickel, worth 5 cents.
     */
    @XmlEnumValue("5") NICKEL(5),

    /**
     * A Dime, worth 10 cents.
     */
    @XmlEnumValue("10") DIME(10),

    /**
     * A Quarter, worth 25 cents.
     */
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
