package se.west.gnat;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

/**
 * Simple enumeration example defining some Food preferences.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlType(namespace = "http://gnat.west.se/foods")
@XmlEnum(String.class)
@XmlAccessorType(XmlAccessType.FIELD)
public enum FoodPreference {

    /**
     * No special food preferences; eats everything.
     */
    NONE(true, true),

    /**
     * Vegetarian who will not eat meats, but drinks milk.
     */
    LACTO_VEGETARIAN(false, true),

    /**
     * Vegan who will neither eat meats nor drink milk.
     */
    VEGAN(false, false);

    /**
     * Boolean value indicating if this {@link FoodPreference} eats meats.
     */
    @XmlAttribute
    private boolean meatEater;

    /**
     * Boolean value indicating if this {@link FoodPreference} drinks milk.
     */
    @XmlAttribute
    boolean milkDrinker;

    private FoodPreference(final boolean meatEater, final boolean milkDrinker) {
        this.meatEater = meatEater;
        this.milkDrinker = milkDrinker;
    }

    public boolean isMeatEater() {
        return meatEater;
    }

    public boolean isMilkDrinker() {
        return milkDrinker;
    }
}
