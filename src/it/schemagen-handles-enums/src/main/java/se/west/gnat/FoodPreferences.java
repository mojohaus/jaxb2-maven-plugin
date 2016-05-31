package se.west.gnat;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

/**
 * Trivial transport object type for enumerations.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlRootElement(namespace = "http://gnat.west.se/foods")
@XmlType(namespace = "http://gnat.west.se/foods", propOrder = {"preferences", "coins"})
@XmlAccessorType(XmlAccessType.FIELD)
public class FoodPreferences implements Serializable {

    /**
     * A List of {@link FoodPreference} instances.
     */
    @XmlElementWrapper
    @XmlElement(name = "preference")
    private List<FoodPreference> preferences;

    /**
     * A List of {@link FoodPreference} instances.
     */
    @XmlElementWrapper
    @XmlElement(name = "coin")
    private List<AmericanCoin> coins;

    public FoodPreferences() {
        preferences = new ArrayList<FoodPreference>();
        coins = new ArrayList<AmericanCoin>();
    }

    public List<FoodPreference> getPreferences() {
        return preferences;
    }

    public List<AmericanCoin> getCoins() {
        return coins;
    }
}
