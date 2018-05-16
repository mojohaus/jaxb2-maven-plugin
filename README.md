# About the Jaxb2-Maven-Plugin

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

## [Plugin Documentation Hub](https://www.mojohaus.org/jaxb2-maven-plugin/)

On Github, Maven site documentation is shown for the 
[5 latest releases of the JAXB2 plugin](https://www.mojohaus.org/jaxb2-maven-plugin/).
The root URL for each static site is `http://www.mojohaus.org/jaxb2-maven-plugin/Documentation/v{theVersionNumber}/index.html`
where `{theVersionNumber}` should be replaced with the actual version, such as `2.3.1`. However, 
[the documentation hub page](https://www.mojohaus.org/jaxb2-maven-plugin/) attempts to simplify access by presenting 
version number and link to the documentation for each listed release.    
 
# Release process for Jaxb2-Maven-Plugin 

Due to its documentation structure, the Jaxb2-Maven-Plugin has a slightly different release process - mainly 
due to the difference in publishing its Plugin Site Documentation. This is currently done in two steps.

## Publishing the Plugin artifacts to Maven Central

This part of the release process is more-or-less identical to the standard Mojohaus release process - except 
that we do not automatically publish the plugin documentation. 

#### a. Preparing the binary artifact release

Clone the repo and issue the standard maven release preparation, substituting the appropriate values for the 
release version and tag. Semantic versioning applies, so unless you know that the next upcoming version should
contain only documentation changes, let the development version have its minor version number bumped by 1 
(i.e. use `2.5.0` instead of `2.4.1` in the example below). Of course, since the snapshot/development stream may 
contain unexpected changes, the development version is merely an indication. 

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
    
#### b. Perform normal deployment of the artifact

Checkout the release tag on the master branch, rebuild and deploy to the OSS repository server.

    git checkout jaxb2-maven-plugin-2.4.0
    
    mvn -Pmojo-release clean deploy
    
The normal Mojohaus process for the binary release (send mail to group etc.) follows.
When the release is approved by the community, build and publish the release documentation as 
described below.          

## Publishing the Plugin Documentation to Github

**Note** You must have `Graphviz` installed and within the path in order to be able to generate
some of the release documentation images. Graphviz can be downloaded from its site,
[http://graphviz.org](http://graphviz.org) 

#### a. Checkout the local release and build the release Documentation      

Checkout the newly prepared release and build its artifact and release documentation. 
Note that the system property `https.protocols` is required to generate the site, since
GitHub's api refuses to use the default TLS version of java (i.e. TLSv1.0).

    git checkout jaxb2-maven-plugin-2.4.0
    
    mvn -Dhttps.protocols="TLSv1,TLSv1.1,TLSv1.2" clean package site
    
#### b. Copy the content of the `target/site` directory

The release site documentation is now found within the target/site directory 
of the build. Copy the content of this directory to a temporary place, such as `/tmp`:

    cp -r target/site/* /tmp/
    
#### c. Move the plugin documentation to its gh-pages branch location

Check out the `gh-pages` branch, and copy the documentation to a directory
named `Documentation/v2.4.0` (substitute the version number with the release version).
Simply build on the structure shown in the image below:

![Structure](src/site/resources/images/documentation_structure.png "Documentation Structure")

#### d. Add the documentation to git, and push to publish

Assuming that the repository id is `github`, add the static documentation pages to git, 
commit the addition using a standard message, and push:

    git add Documentation/v2.4.0
    
    git commit -m "Added plugin documentation for version 2.4.0"
    
    git push github 

Following the push, verify that the newly released documentation site is available on github.
Simply originate from the [Plugin Documentation Hub Page](https://www.mojohaus.org/jaxb2-maven-plugin/#/repo), 
which should look somewhat like the image below.

![Documentation Hub](src/site/resources/images/documentation_hub.png "Documentation Hub") 