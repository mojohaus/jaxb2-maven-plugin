package org.codehaus.mojo.jaxb2.schemageneration.postprocessing.javadoc.wrappers;

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
 * Another trivial transport object type for collections, demonstrating that
 * the jaxb2-maven-plugin's schemagen goal correctly can extract XSD documentation
 * annotations when using @XmlElementWrapper annotations placed on Method accessors
 * (which is the default for SchemaGen).
 */
@XmlRootElement(namespace = "http://jaxb.mojohaus.org/wrappers")
@XmlType(namespace = "http://jaxb.mojohaus.org/wrappers", propOrder = {"methodStrings", "methodIntegerSet"})
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class ExampleXmlWrapperUsingMethodAccess implements Serializable {

    private List<String> methodStrings;

    private SortedSet<Integer> methodIntegerSet;


    public ExampleXmlWrapperUsingMethodAccess() {

        this.methodStrings = new ArrayList<String>();
        this.methodIntegerSet = new TreeSet<Integer>();
    }

    /**
     * JavaBean getter method containing some method strings.
     *
     * @return some method strings.
     */
    @XmlElementWrapper(name = "foobar")
    @XmlElement(name = "aString")
    public List<String> getMethodStrings() {
        return methodStrings;
    }

    /**
     * JavaBean getter method returning Integers.
     *
     * @return a Set of integers.
     * @see #getMethodStrings()
     */
    @XmlElementWrapper
    @XmlElement(name = "anInteger")
    public SortedSet<Integer> getMethodIntegerSet() {
        return methodIntegerSet;
    }
}
