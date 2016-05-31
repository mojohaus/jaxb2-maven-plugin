package org.codehaus.mojo.jaxb2.schemageneration.postprocessing.javadoc.enums;

import org.codehaus.mojo.jaxb2.schemageneration.postprocessing.javadoc.SomewhatNamedPerson;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
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
