# jaxb2-maven-plugin

[![Build Status](https://travis-ci.org/mojohaus/jaxb2-maven-plugin.svg?branch=master)](https://travis-ci.org/mojohaus/jaxb2-maven-plugin)

This Maven plugin uses the Java API for XML Binding (JAXB), version 2+, to perform one of 2 main tasks:

1. Generate Java classes from XML Schemas (and optionally binding files). 
   This is done by delegating work to the XJC tool, bundled with the Java SDK.
   Documentation for the XJC tool is found at two places - the [Unix Documentation](https://docs.oracle.com/javase/8/docs/technotes/tools/unix/xjc.html) 
   and the [Windows Documentation](https://docs.oracle.com/javase/8/docs/technotes/tools/windows/xjc.html). 
2. Create XML Schemas from annotated Java classes.
   This is done by delegating work to the Schemagen tool, bundled with the Java SDK.
   Documentation for the Schemagen tool is also found at two places - the [Unix Documentation](https://docs.oracle.com/javase/8/docs/technotes/tools/unix/schemagen.html)
   and the [Windows Documentation](https://docs.oracle.com/javase/8/docs/technotes/tools/windows/schemagen.html) 

## Plugin Documentation

Maven site documentation is shown 
[for the 5 latest releases of the JAXB2 plugin](https://www.mojohaus.org/jaxb2-maven-plugin/).
The root URL for each static site is `http://www.mojohaus.org/jaxb2-maven-plugin/Documentation/v{theVersionNumber}/index.html`
where `{theVersionNumber}` should be replaced with the actual version, such as `2.3.1`. However, the landing page
attempts to simplify access by presenting version number and link to the documentation for each listed release.    
 
### Releasing the Jaxb2-Maven-Plugin and publishing its Site Documentation

The process for publishing the Maven site documentation is currently done in two main manual steps.
Note that you must have `Graphviz` installed and within the path in order to be able to generate
some of the release documentation images. Graphviz can be downloaded from its site,
[http://graphviz.org](http://graphviz.org) 

#### a. Preparing the binary artifact release

Clone the repo and issue the standard maven release preparation, substituting the appropriate values for the 
release version and tag. Unless you have a good reason not to, let the upcoming development version have its 
minor version number bumped by 1 (i.e. use `2.5.0` instead of `2.4.1` in the example below). 

    mvn -DpushChanges=false -DreleaseVersion=2.4.0 -DdevelopmentVersion=2.5.0-SNAPSHOT -Dtag=jaxb2-maven-plugin-2.4.0 release:prepare
    
If the release preparation build completed without errors, your local release repository should now contain 
two new commits with the commit message starting with `[maven-release-plugin]` on the form shown below. 
We have still not pushed anything to any source code or artifact repository.  

    * b229a34 - Lennart Jörelid (20 seconds ago) (HEAD -> master)
    |   [maven-release-plugin] prepare for next development iteration
    * 31f498f - Lennart Jörelid (20 seconds ago) (tag: jaxb2-maven-plugin-2.4.0)
    |   [maven-release-plugin] prepare release jaxb2-maven-plugin-2.4.0
    * b4499f9 - Lennart Jörelid (9 hours ago) (origin/master, origin/HEAD)
    |   Using only JDK 8 since that is the defaults in travis.
    
#### b. Checkout the local release and build the release Documentation      

Checkout the newly prepared release and build its artifact and release documentation:

    git checkout jaxb2-maven-plugin-2.4.0
    
    mvn clean package site
    
#### c. Copy the content of the `target/site` directory

The release site documentation is now found within the target/site directory 
of the build. Copy the content of this directory to a temporary place, such as `/tmp`:

    cp -r target/site/* /tmp/
    
#### d. Move the release documentation to its gh-branch location

Check out the `gh-pages` branch, and copy the documentation to a directory
named `Documentation/v2.4.0` (substitute the version number with the release version).
Simply build on the structure shown in the image below:

![Structure](src/site/resources/images/documentation_structure.png "Documentation Structure")
     
