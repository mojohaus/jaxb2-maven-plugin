# jaxb2-maven-plugin
The MojoHaus JAXB2 Maven Plugin assists in creating 

This plugin uses the Java API for XML Binding (JAXB), version 2+, to generate Java classes from XML 
Schemas (and optionally binding files) and to create XML Schemas from annotated Java classes. 
The plugin delegates most of its work to either of the two JDK-supplied tools 

1. XJC with [documentation for Unix](https://docs.oracle.com/javase/8/docs/technotes/tools/unix/xjc.html) and [documentation for Windows](https://docs.oracle.com/javase/8/docs/technotes/tools/unix/xjc.html)
2. Schemagen with [documentation for Unix](https://docs.oracle.com/javase/8/docs/technotes/tools/unix/schemagen.html) and [documentation for Windows](https://docs.oracle.com/javase/8/docs/technotes/tools/windows/schemagen.html)
