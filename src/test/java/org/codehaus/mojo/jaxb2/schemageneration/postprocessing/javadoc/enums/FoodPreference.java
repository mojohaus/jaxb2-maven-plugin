package org.codehaus.mojo.jaxb2.schemageneration.postprocessing.javadoc.enums;

import org.codehaus.mojo.jaxb2.schemageneration.postprocessing.javadoc.SomewhatNamedPerson;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;

/**
 * Simple enumeration example defining some Food preferences.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlType(namespace = SomewhatNamedPerson.NAMESPACE)
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
