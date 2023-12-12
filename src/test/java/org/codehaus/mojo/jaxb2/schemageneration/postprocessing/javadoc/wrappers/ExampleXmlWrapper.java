package org.codehaus.mojo.jaxb2.schemageneration.postprocessing.javadoc.wrappers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

/**
 * Trivial transport object type for collections.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlRootElement(namespace = "http://jaxb.mojohaus.org/wrappers")
@XmlType(
        namespace = "http://jaxb.mojohaus.org/wrappers",
        propOrder = {"strings", "integerSet"})
@XmlAccessorType(XmlAccessType.FIELD)
public class ExampleXmlWrapper implements Serializable {

    /**
     * List containing some strings.
     */
    @XmlElementWrapper(name = "foobar")
    @XmlElement(name = "aString")
    private List<String> strings;

    /**
     * SortedSet containing Integers.
     */
    @XmlElementWrapper
    @XmlElement(name = "anInteger")
    private SortedSet<Integer> integerSet;

    public ExampleXmlWrapper() {

        this.strings = new ArrayList<String>();
        this.integerSet = new TreeSet<Integer>();
    }

    public List<String> getStrings() {
        return strings;
    }

    public SortedSet<Integer> getIntegerSet() {
        return integerSet;
    }
}
