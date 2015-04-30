# What is the JAXB2 Maven Plugin?

This plugin uses the Java API for XML Binding (JAXB), version 2+, to generate Java 
classes from XML Schemas (and optionally binding files) and to create XML Schemas from 
annotated Java classes. The plugin delegates most of its work to either of the 
two JDK-supplied tools XJC and Schemagen, through its 	
[JAXB implementation dependencies](./dependencies.html). 

## Goals Overview

The jaxb2-maven-plugin has four main goals, listed below. 
Detailed information about each goal can be found on their respective documentation page.

1. [jaxb2:schemagen](./schemagen-mojo.html) Creates XML Schema Definition (XSD) file(s)
   from annotated Java sources.
2. [jaxb2:testSchemagen](./testSchemagen-mojo.html) Creates XML Schema Definition (XSD) 
   file(s) from annotated Java test sources.
3. [jaxb2:xjc](./xjc-mojo.html) Generates Java sources from XML Schema(s).
4. [jaxb2:testXjc](./testXjc-mojo.html) Generates Java test sources from XML Schema(s).

## Usage examples

A set of usage examples are found within the following pages:

<table>
    <tr>
        <th width="35%">Example page</th>
        <th width="60%">Description</th>
    </tr>
    <tr>
        <td><a href="./example_xjc_basic.html">XJC: Basic Examples</a></td>
        <td>General usage and basic plugin configuration examples.</td>
    </tr>
    <tr>
        <td><a href="./example_schemagen_basic.html">SchemaGen: Basic Examples</a></td>
        <td>General usage and basic plugin configuration examples.</td>
    </tr>
    <tr>
        <td><a href="./example_schemagen_postprocessing.html">SchemaGen: Post-processing</a></td>
        <td>Postprocessing of generated schema, such as namespace prefix changes or
        copying JavaDoc to annotations within generated XSDs.</td>
    </tr>
    <tr>
        <td><a href="./filters.html">Filters and Filtering</a></td>
        <td>Defining Filters to exclude (some) files from processing. These constructs 
        can be used by all Mojos (for all goals).</td>
    </tr>
</table>

### If you still have questions ...

Two sets of documentation may give you better insight in the tools used by this jaxb2-maven-plugin:

1. The [JAXB Reference Implementation](https://jaxb.java.net/) holds documentation
   about JAXB and the XJC compiler.
2. The [SchemaGen Documentation](http://docs.oracle.com/javase/8/docs/technotes/tools/unix/schemagen.html) gives a
   brief overview on the SchemaGen (in this case for Java SE 8; compensate if you use an earlier Java version).

In case you still have questions regarding the plugin's usage, please feel free to contact the
[user mailing list](./mail-lists.html). The posts to the mailing list are archived and could already contain
the answer to your question as part of an older thread. Hence, it is also worth browsing/searching
the [mail archive](./mail-lists.html).

The jaxb2-maven-plugin is compatible with [m2e](http://eclipse.org/m2e/) and
integrates with the Eclipse build, providing incremental build support in the IDE. 
This requires m2e v1.1 or later.

### Defects?

If you feel like the plugin is missing a feature or has a defect, you can fill a feature 
request or bug report in our [issue tracker](./issue-tracking.html). When creating a new 
issue, please provide a comprehensive description of your concern, preferably along with 
a patch. If you do not supply a patch for the problem, it is important that the 
developers can reproduce your problem. For this reason, entire debug logs, POMs or most 
preferably little demo projects attached to the issue are very much appreciated. 
Contributors can check out the project from our 
[source repository](./source-repository.html) and will find supplementary 
information in the
[guide to helping with Maven](http://maven.apache.org/guides/development/guide-helping.html).

### Behaviour and `@since`

The 2.0 release branch of the jaxb2-maven-plugin marks a slight difference in using the
`@since` parameter in the generated documentation. Our current semantics are to indicate 
that "the current functionality has been present `@since` a certain version", permitting 
plugin users to see which pieces of the plugin should be validated in their respective 
build reactors after upgrading the plugin. 

As the plugin is re-implemented from the ground up in version 2.0, liberal application of
the `@since` annotation has been applied to the codebase. This shows up in the plugin 
documentation, to highlight to users when something was changed. 