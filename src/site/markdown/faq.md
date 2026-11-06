# Frequently Asked Questions

## XJC

1. [I have different schemas and want different packages for those schemas, how do I do this?](#Multiple)

## Schemagen

1. [What tool is used by the jaxb2-maven-plugin to generate the XML schema definition files?](#Tooling)
2. [How can I see the log messages output by the schemagen tool?](#LogMessages)
3. [How do generated XML schema files correlate to annotated java files?](#SeveralGeneratedSchemaFiles)
4. [Can I generate XSDs without a namespace prefix on the target namespace?](#SchemagenEmptyNamespacePrefix)
5. [When I try to generate Schema and any path contain whitespace, I get an Exception](#SchemaGenWhitespace)
6. [How can I annotate my java classes to generate XML schema?](#Example)

## XJC

<a id="Multiple"></a>

### I have different schemas and want different packages for those schemas, how do I do this?

You want to create multiple executions in your plugin declaration. This will allow you
to have multiple configuration declarations and you can specify the schema and package to
be used for that execution. Note that you will need to *not* clear the output
directory between executions in this case.

An example is given on the XJC usage page.

## Schemagen

<a id="Tooling"></a>

### What tool is used by the jaxb2-maven-plugin to generate the XML schema definition files?

The *schemagen* tool used by the plugin is part of the JDK distribution since Java 6.

The jaxb2-maven-plugin provides some augmentations to the generated schema files, such
as the ability to define a schema prefix and filename for each generated XML schema.

<a id="LogMessages"></a>

### How can I see the log messages output by the schemagen tool?

Run the jaxb2-maven-plugin in debug mode to see logging from the schemagen tool.
Please refer to the examples for further information.

<a id="SeveralGeneratedSchemaFiles"></a>

### How do generated XML schema files correlate to annotated java files?

The schemagen tool generates one XML schema file *per XML namespace* it finds within
the compilation unit (i.e. all annotated Java files selected for XML schema compilation).

The correlation between java files and generated XML schema files is therefore only indirect;
a single generated XML schema file can hold the result of annotations within several java files - and
a single annotated java source file can give rise to definitions scattered over several XML
schema files.

An example is given on the usage page for the schemagen goal.

<a id="SchemagenEmptyNamespacePrefix"></a>

### Can I generate XSDs without a namespace prefix on the target namespace?

The 2.x line of the Jaxb2-Maven-Plugin requires the namespaceURI to relate to at least one
non-empty value out of the two properties below. Hence, at least one of these values must be
provided and given a non-empty value:

1. `prefix`
2. `toFile`

<a id="SchemaGenWhitespace"></a>

### When I try to generate Schema and any path contain whitespace, I get an Exception

This seems to be caused by internal implementation issues within the SchemaGen tool.
The current workaround is to use a path without whitespace.

<a id="Example"></a>

### How can I annotate my java classes to generate XML schema?

For the full example (including generated XML Schema definitions), please refer to the
schemagen usage page. Provided below are two annotated source files, and a
constants definition interface.

**Namespaces definition interface**. Simply defines constants for the
three namespaces used within the two classes below.

```java
package se.west.schema;

public interface Namespaces {

    public static final String SOME_NAMESPACE = "http://some/namespace";
    public static final String ANOTHER_NAMESPACE = "http://another/namespace";
    public static final String YET_ANOTHER_NAMESPACE = "http://yet/another/namespace";
}
```

**Class FooBar**. By default, all members are compiled to XML schema with
the namespace `"http://some/namespace"`, as defined by the namespace attribute within
the `@XmlType` annotation on line 9. The `aRequiredElementInAnotherNamespace`
member belongs to the namespace `"http://another/namespace"`, as defined on line 18.

```java
package se.west.schema;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(namespace = Namespaces.SOME_NAMESPACE,
        propOrder = {"requiredElement", "aRequiredElementInAnotherNamespace",
                "optionalElement", "requiredAttribute", "optionalAttribute"})
@XmlAccessorType(XmlAccessType.FIELD)
public class FooBar {

    @XmlElement(required = true, defaultValue = "requiredElementValue")
    private String requiredElement;

    @XmlElement(namespace = Namespaces.ANOTHER_NAMESPACE, required = true, defaultValue = "requiredElementValue")
    private String aRequiredElementInAnotherNamespace;

    @XmlElement(required = false)
    private String optionalElement;

    @XmlAttribute(required = true)
    private String requiredAttribute;

    @XmlAttribute(required = false)
    private String optionalAttribute;
}
```

**Class FooBaz**. By default, all members are compiled to XML schema with
the namespace `"http://another/namespace"`, as defined by the namespace attribute within
the `@XmlType` annotation on line 9. The `aRequiredElementInYetAnotherNamespace`
member belongs to the namespace `"http://yet/another/namespace"`, as defined on line 18. The
`anOptionalElementInSomeNamespace` member belongs to the namespace `"http://some/namespace"`,
as defined on line 21.

```java
package se.west.schema;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(namespace = Namespaces.ANOTHER_NAMESPACE,
        propOrder = {"requiredElement", "anOptionalElementInSomeNamespace",
                "aRequiredElementInYetAnotherNamespace", "requiredAttribute", "optionalAttribute"})
@XmlAccessorType(XmlAccessType.FIELD)
public class FooBaz {

    @XmlElement(required = true, defaultValue = "requiredElementValue")
    private String requiredElement;

    @XmlElement(namespace = Namespaces.YET_ANOTHER_NAMESPACE, required = true, defaultValue = "requiredElementValue")
    private String aRequiredElementInYetAnotherNamespace;

    @XmlElement(namespace = Namespaces.SOME_NAMESPACE, required = false)
    private String anOptionalElementInSomeNamespace;

    @XmlAttribute(required = true)
    private String requiredAttribute;

    @XmlAttribute(required = false)
    private String optionalAttribute;
}
```

