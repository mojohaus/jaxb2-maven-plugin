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
 * Another trivial transport object type for collections.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlRootElement(namespace = "http://jaxb.mojohaus.org/wrappers")
@XmlType(namespace = "http://jaxb.mojohaus.org/wrappers", propOrder = {"strings", "integerSet"})
public class ExampleXmlWrapperUsingMethodAccess implements Serializable {

    private List<String> strings2;

    private SortedSet<Integer> integerSet2;


    public ExampleXmlWrapperUsingMethodAccess() {

        this.strings2 = new ArrayList<String>();
        this.integerSet2 = new TreeSet<Integer>();
    }

    /**
     * List containing some strings.
     */
    @XmlElementWrapper(name = "foobar")
    @XmlElement(name = "aString")
    public List<String> getStrings() {
        return strings;
    }

    /**
     * SortedSet containing Integers.
     */
    @XmlElementWrapper
    @XmlElement(name = "anInteger")
    public SortedSet<Integer> getIntegerSet() {
        return integerSet;
    }
}
