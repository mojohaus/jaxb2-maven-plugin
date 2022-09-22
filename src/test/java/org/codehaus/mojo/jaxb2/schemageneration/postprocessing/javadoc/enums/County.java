package org.codehaus.mojo.jaxb2.schemageneration.postprocessing.javadoc.enums;

import org.codehaus.mojo.jaxb2.schemageneration.postprocessing.javadoc.SomewhatNamedPerson;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlType;
import java.io.Serializable;

/**
 * Swedish County ("län") enumeration.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlType(namespace = SomewhatNamedPerson.NAMESPACE)
@XmlEnum
public enum County implements Serializable {

    @XmlEnumValue("1")stockholm(1, "AB", true, "Stockholm"),
    @XmlEnumValue("3")uppsala(3, "C", false, "Uppsala"),
    @XmlEnumValue("4")sodermanland(4, "D", true, "Södermanland"),
    @XmlEnumValue("5")ostergotland(5, "E", true, "Östergötland"),
    @XmlEnumValue("6")jonkoping(6, "F", true, "Jönköping"),
    @XmlEnumValue("7")kronoberg(7, "G", true, "Kronoberg"),
    @XmlEnumValue("8")kalmar(8, "H", false, "Kalmar"),
    @XmlEnumValue("9")gotland(9, "I", true, "Gotland"),
    @XmlEnumValue("10")blekinge(10, "K", false, "Blekinge"),
    @XmlEnumValue("12")skane(12, "M", false, "Skåne"),
    @XmlEnumValue("13")halland(13, "N", true, "Halland"),
    @XmlEnumValue("14")vastra_gotaland(14, "O", true, "Västra Götaland"),
    @XmlEnumValue("17")varmland(17, "S", true, "Värmland"),
    @XmlEnumValue("18")orebro(18, "T", false, "Örebro"),
    @XmlEnumValue("19")vastmanland(19, "U", true, "Västmanland"),
    @XmlEnumValue("20")dalarna(20, "W", true, "Dalarna"),
    @XmlEnumValue("21")gavleborg(21, "X", true, "Gävleborg"),
    @XmlEnumValue("22")vasternorrland(22, "Y", true, "Västernorrland"),
    @XmlEnumValue("23")jamtland(23, "Z", true, "Jämtland"),
    @XmlEnumValue("24")vasterbotten(24, "AC", true, "Västerbotten"),
    @XmlEnumValue("25")norrbotten(25, "BD", true, "Norrbotten");

    // Internal state
    private int countyId;
    private boolean injectFormalNameS;
    private String countyName;
    private String letterCode;

    /**
     * Make the constructor private for Enum types.
     *
     * @param countyId          The ID of this County.
     * @param letterCode        The letter code of this County.
     * @param injectFormalNameS {@code true} if the formal name should contain an 's' after the name of this County.
     *                          (I.e. Name: {@code Stockholm} yields County formal name:
     *                          {@code Stockholms län} (note the extra 's')).
     * @param countyName        The name of this County.
     */
    County(final int countyId,
            final String letterCode,
            final boolean injectFormalNameS,
            final String countyName) {

        this.letterCode = letterCode;
        this.countyId = countyId;
        this.injectFormalNameS = injectFormalNameS;
        this.countyName = countyName;
    }

    /**
     * @return Retrieves the County ID.
     */
    public int getCountyId() {
        return countyId;
    }

    /**
     * @return The name of this County.
     */
    public String getName() {
        return countyName;
    }

    /**
     * @return The letter code of this County.
     */
    public String getLetterCode() {
        return letterCode;
    }

    /**
     * @return The formal name of the county, which is typical in listings.
     * (I.e. {@code Stockholms län} and equivalent).
     */
    public String getFormalName() {
        return getName() + (injectFormalNameS ? "s" : "") + " län";
    }

    /**
     * Retrieves the County with the supplied ID.
     *
     * @param countyId The id of the County to retrieve.
     * @return The County with the supplied countyId.
     * @throws IllegalArgumentException if no County had the supplied countyId.
     */
    public static County getCountyById(final int countyId) {
        for (County current : values()) {
            if (current.getCountyId() == countyId) {
                return current;
            }
        }

        throw new IllegalArgumentException("No County had countyID [" + countyId + "]");
    }
}