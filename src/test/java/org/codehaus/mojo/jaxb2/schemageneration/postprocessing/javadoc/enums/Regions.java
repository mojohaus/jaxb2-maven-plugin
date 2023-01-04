package org.codehaus.mojo.jaxb2.schemageneration.postprocessing.javadoc.enums;

import org.codehaus.mojo.jaxb2.schemageneration.postprocessing.javadoc.SomewhatNamedPerson;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlRootElement(namespace = SomewhatNamedPerson.NAMESPACE)
@XmlType(namespace = SomewhatNamedPerson.NAMESPACE, propOrder = {"counties", "municipalities"})
@XmlAccessorType(XmlAccessType.FIELD)
public class Regions {

    @XmlElementWrapper
    @XmlElement(name = "county")
    private Set<County> counties;

    @XmlElementWrapper
    @XmlElement(name = "municipality")
    private Set<Municipality> municipalities;

    public Regions() {
        this.counties = new TreeSet<County>();
        this.municipalities = new TreeSet<Municipality>();
    }

    public Set<County> getCounties() {
        return counties;
    }

    public Set<Municipality> getMunicipalities() {
        return municipalities;
    }
}
