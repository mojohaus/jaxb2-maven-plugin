package enums;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Trivial transport object type for Enums with different representations.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlRootElement(namespace = "http://gnat.west.se/foods")
@XmlType(namespace = "http://gnat.west.se/foods", propOrder = {"coins", "foodPreferences"})
@XmlAccessorType(XmlAccessType.FIELD)
public class ExampleEnumHolder implements Serializable {

    /**
     * List containing all AmericanCoin objects.
     */
    @XmlElementWrapper
    @XmlElement(name = "coin")
    private List<AmericanCoin> coins;

    /**
     * SortedSet containing the FoodPreference objects collected.
     */
    @XmlElementWrapper
    @XmlElement(name = "preference")
    private SortedSet<FoodPreference> foodPreferences;


    public ExampleEnumHolder() {

        this.coins = new ArrayList<AmericanCoin>();
        this.foodPreferences = new TreeSet<FoodPreference>();
    }

    public List<AmericanCoin> getCoins() {
        return coins;
    }

    public SortedSet<FoodPreference> getFoodPreferences() {
        return foodPreferences;
    }
}
