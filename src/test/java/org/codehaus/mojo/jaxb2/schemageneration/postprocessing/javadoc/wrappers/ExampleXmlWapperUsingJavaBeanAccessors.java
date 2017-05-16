package org.codehaus.mojo.jaxb2.schemageneration.postprocessing.javadoc.wrappers;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class ExampleXmlWapperUsingJavaBeanAccessors implements Serializable {

    private List<String> strings;
    private SortedSet<Integer> integerSet;


    public ExampleXmlWapperUsingJavaBeanAccessors() {

        this.strings = new ArrayList<String>();
        this.integerSet = new TreeSet<Integer>();
    }

    /**
     * JavaBean getter for a List containing some strings.
     */
    @XmlElementWrapper(name = "foobar")
    @XmlElement(name = "aString")
    public List<String> getStrings() {
        return strings;
    }

    /**
     * JavaBean getter for a SortedSet containing Integers.
     */
    @XmlElementWrapper
    @XmlElement(name = "anInteger")
    public SortedSet<Integer> getIntegerSet() {
        return integerSet;
    }
}
