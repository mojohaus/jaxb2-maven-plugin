package org.codehaus.mojo.jaxb2.schemageneration.postprocessing.javadoc.wrappers;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * A really trivial transport object type for collections, demonstrating that
 * the jaxb2-maven-plugin's schemagen goal correctly can extract XSD documentation
 * annotations when using @XmlElementWrapper annotations placed on Fields.
 *
 * This requires use of the @XmlAccessorType(XmlAccessType.FIELD) on the class in question,
 * or - alternatively - on a package-info.java file containing the equivalent annotation.
 */
@XmlRootElement(namespace = "http://jaxb.mojohaus.org/wrappers")
@XmlType(namespace = "http://jaxb.mojohaus.org/wrappers", propOrder = {"strings", "integerSet"})
@XmlAccessorType(XmlAccessType.FIELD)
public class ExampleXmlWrapperUsingFieldAccess implements Serializable {

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


    public ExampleXmlWrapperUsingFieldAccess() {

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
