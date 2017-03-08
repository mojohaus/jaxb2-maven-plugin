# jaxb2-maven-plugin

[![Build Status](https://travis-ci.org/mojohaus/jaxb2-maven-plugin.svg?branch=master)](https://travis-ci.org/mojohaus/jaxb2-maven-plugin)

This plugin uses the Java API for XML Binding (JAXB), version 2+, to generate Java classes from XML 
Schemas (and optionally binding files) and to create XML Schemas from annotated Java classes.
 
Maven site documentation for the plugin is found at
[http://www.mojohaus.org/jaxb2-maven-plugin/](http://www.mojohaus.org/jaxb2-maven-plugin/Documentation/v2.3.1/)
 
The plugin delegates most of its work to one of the two JDK-supplied tools: 

1. XJC with [documentation for Unix](https://docs.oracle.com/javase/8/docs/technotes/tools/unix/xjc.html) 
   and [documentation for Windows](https://docs.oracle.com/javase/8/docs/technotes/tools/windows/xjc.html)
2. Schemagen with [documentation for Unix](https://docs.oracle.com/javase/8/docs/technotes/tools/unix/schemagen.html) 
   and [documentation for Windows](https://docs.oracle.com/javase/8/docs/technotes/tools/windows/schemagen.html)
