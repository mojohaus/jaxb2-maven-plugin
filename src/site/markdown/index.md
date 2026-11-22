# What is the JAXB2 Maven Plugin?

This plugin uses Jakarta XML Binding (JAXB) to generate Java
classes from XML Schemas (and optionally binding files) and to create XML Schemas from
annotated Java classes. The plugin delegates most of its work to either of the
two tools XJC and Schemagen, which are no longer part of the JDK and come with the plugin
as its [JAXB implementation dependencies](./dependencies.html).

> **Note**: This documentation is released with and valid for its corresponding
> plugin version as indicated in the top right corner of each documentation page.
> The 3.x versions use the `jakarta.xml.bind` namespace where 2.x used `javax.xml.bind`, and
> the plugin configuration is *not backwards compatible with versions 1.x*.

### Upgrading from version 1.x

The jaxb2-maven-plugin was completely reimplemented for version 2, which means that
some/most parameters work differently from the 1.x versions of the plugin.
The plugin's configuration is therefore not backwards compatible with the 1.x versions.

The usage examples below provide guides to configuring the 2.x version of the plugin.
In most cases, only the source properties (which can contain several paths in the 2.x version)
should need your attention unless you use a nonstandard project layout; most default settings
from the 1.x versions of the plugin have been preserved. Another source of information can be
found in the integration tests of the plugin itself.

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
        <td><a href="./example_xjc_episodes.html">XJC: Dependency Episodes</a></td>
        <td>Generating Java code from schemas that depend on the XSDs of other artifacts.</td>
    </tr>
    <tr>
        <td><a href="./example_xjc_catalogs.html">XJC: Catalogs &amp; Dependency XSDs</a></td>
        <td>Resolving imported schemas through XML catalogs and XSD files in dependencies.</td>
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

1. The [JAXB Reference Implementation](https://eclipse-ee4j.github.io/jaxb-ri/) holds documentation
   about JAXB, the XJC compiler and the Schemagen tool.
2. The [Schemagen issue tracker](https://github.com/eclipse-ee4j/jaxb-ri/labels/Component%3A%20schemagen)
   lists the known problems of the tool the plugin delegates to.

In case you still have questions regarding the plugin's usage, please feel free to contact the
[user mailing list](./mailing-lists.html). The posts to the mailing list are archived and could already contain
the answer to your question as part of an older thread. Hence, it is also worth browsing/searching
the [mail archive](./mailing-lists.html).

The jaxb2-maven-plugin is compatible with [m2e](https://eclipse.dev/m2e/) and
integrates with the Eclipse build, providing incremental build support in the IDE.
This requires m2e v1.1 or later.

### Defects?

If you feel like the plugin is missing a feature or has a defect, you can fill a feature
request or bug report in our [issue tracker](./issue-management.html). When creating a new
issue, please provide a comprehensive description of your concern, preferably along with
a patch. If you do not supply a patch for the problem, it is important that the
developers can reproduce your problem. For this reason, entire debug logs, POMs or most
preferably little demo projects attached to the issue are very much appreciated.
Contributors can check out the project from our
[source repository](./scm.html) and will find supplementary
information in the
[guide to helping with Maven](https://maven.apache.org/guides/development/guide-helping.html).
