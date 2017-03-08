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
     * List containing some methodStrings.
     */
    @XmlElementWrapper(name = "foobar")
    @XmlElement(name = "aString")
    public List<String> getMethodStrings() {
        return methodStrings;
    }

    /**
     * SortedSet containing Integers.
     */
    @XmlElementWrapper
    @XmlElement(name = "anInteger")
    public SortedSet<Integer> getMethodIntegerSet() {
        return methodIntegerSet;
    }
}
