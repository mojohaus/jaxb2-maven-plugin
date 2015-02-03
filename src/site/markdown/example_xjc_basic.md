# Basic Examples - Java Generation

These basic examples show how to generate Java code from JAXB using the jaxb2-maven-plugin,
and highlight the use of some of the plugin's common configuration options.

This plugin runs the JAXB distribution's XJC compiler from the JAXB distribution, and integrates
XJC's configuration properties into a Maven project. The plugin will delegate all JAXB-related work
to its [JAXB implementation dependencies](./dependencies.html). The actual JAXB dependencies used
when compiling the plugin will also be collected and listed when the plugin is run in debug mode.

Due to the construction of the Java platform the JAXB API used is the one defined by the platform.
This means that the used JAXB API version will be the endorsed one of the JDK, regardless of what
is specified in the plugin (or its dependencies). You may override the endorsed API outside of Maven
and this plugin, but that is typically viewed as advanced usage. A better option is to align the
version of the compiled code with the requirement on JAXB runtime environment of the generated code.

## Example 1: Generate Java code within provided package

The plugin will process all XSD files found within the [schema directory](xjc-mojo.html#schemaDirectory),
and create Java source code in the given package (`com.example.myschema`) as defined by the `packageName`
configuration parameter. The required JAXB runtime version of the generated code will be the same as the
plugin's JAXB implementation dependency (see the [target parameter](./xjc-mojo.html#target)).

    <project>
    ...
    <dependencies>
        <!--
            You need the JAXB API to be able to annotate your classes.
            However, starting with Java 6 that API is included in the
            Java SE platform so there is no need to declare a dependency.
        -->
        ...
    </dependencies>
    ...
    <build>
        <pluginManagement>
            <plugins>
                <!--
                    If we e.g. execute on JDK 1.7, we should compile for Java 7 to get
                    the same (or higher) JAXB API version as used during the xjc execution.
                -->
                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-compiler-plugin</artifactId>
                    <configuration>
                        <source>1.7</source>
                        <target>1.7</target>
                    </configuration>
                </plugin>
            </plugins>
        </pluginManagement>
        <plugins>
            <plugin>
                <groupId>org.codehaus.mojo</groupId>
                <artifactId>jaxb2-maven-plugin</artifactId>
                <version>${project.version}</version>
                <executions>
                    <execution>
                        <id>xjc</id>
                        <goals>
                            <goal>xjc</goal>
                        </goals>
                    </execution>
                </executions>
                <configuration>
                    <!-- The package of your generated sources -->
                    <packageName>com.example.myschema</packageName>
                </configuration>
            </plugin>
            ...
        </plugins>
    <build>
    ...
    </project>

## Example 2: Aligning JDK with JAXB API and JAXB runtime environment

So you want to use JAXB on JDK 1.6? You would then need to configure the jaxb2-maven-plugin for a Java 6+ project,
which implies two separate configuration properties:

1. *maven-compiler-plugin*: Define java version 1.6 for the compiler, to use the Java 6+ platform as the runtime
    environment for the compiled code. Use 1.6 for both the `source` and `target` properties.
2. *jaxb-maven-plugin*: Use the `target` configuration parameter to set the JAXB runtime version to 2.1, which is
    included in the JDK 1.6 (starting with JDK 1.6u4).

The project can now be built with JDK 1.6.

    <project>
        ...
        <build>
            <pluginManagement>
                <plugins>
                    <plugin>
                        <groupId>org.apache.maven.plugins</groupId>
                        <artifactId>maven-compiler-plugin</artifactId>
                        <configuration>
                            <source>1.6</source>
                            <target>1.6</target>
                        </configuration>
                    </plugin>
                </plugins>
            </pluginManagement>
            <plugins>
                <plugin>
                    <groupId>org.codehaus.mojo</groupId>
                    <artifactId>jaxb2-maven-plugin</artifactId>
                    <version>${project.version}</version>
                    <executions>
                        <execution>
                            <id>xjc</id>
                            <goals>
                                <goal>xjc</goal>
                            </goals>
                        </execution>
                    </executions>
                    <configuration>
                        <target>2.1</target>
                        ...
                    </configuration>
                </plugin>
                ...
            </plugins>
        <build>
        ...
    </project>

## Example 3: Using another type of input source

Normally, you would use XML Schema Definitions as the standard type of JAXB source.
However, other standards can be used as source type provided that the configuration parameter `sourceType`
is assigned one of the existing values of the
[org.codehaus.mojo.jaxb2.javageneration.SourceContentType](./apidocs/index.html) enum.
In the example below, the source files contain
[Document Type Descriptions](http://en.wikipedia.org/wiki/Document_type_definition) instead of XML Schema, and
they are placed within the `src/main/dtd` directory.

Note that the `sourceType` configuration parameter is case sensitive for matching the enum values.
Refer to the plugin's [JavaDoc](./apidocs/index.html) to see all possible values.

            <plugin>
                <groupId>org.codehaus.mojo</groupId>
                <artifactId>jaxb2-maven-plugin</artifactId>
                <version>${project.version}</version>
                <executions>
                    <execution>
                        <id>xjc</id>
                        <goals>
                            <goal>xjc</goal>
                        </goals>
                    </execution>
                </executions>
                <configuration>
                    <!-- Set the package of the generated code -->
                    <packageName>com.example.myschema</packageName>

                    <!-- Indicate that we should use DTD input instead of XSDs -->
                    <sourceType>dtd</sourceType>

                    <!-- Define the directory where we should find the DTD files -->
                    <sources>
                        <source>src/main/dtd</source>
                    </sources>
                </configuration>
            </plugin>

## Example 4: Defining sources and exclude regularExpressions

By default, the jaxb2-maven-plugin examines the directory `src/main/xsd` for XML schema files
which should be used by JAXB to create Java source code (and `src/test/xsd` for test XSD sources).
If you would like to place your XSD somewhere else, you need to define source elements
as shown in the configuration below. The paths given are interpreted relative to the `basedir` property,
which is set to reference the maven project directory.

Files found (using a recursive search) within the sources elements are read and used by the XJC tool
only if they do **not** match any sourceExcludeSuffix. This means that all source files with file names
suffixes (endings) given in any sourceExcludeSuffix are excluded from being used as sources by XJC.
File name comparisons are case-insensitive.

Therefore, the configuration below should include `src/main/some/other/xsds/aFile.txt` as an XSD source,
but exclude `src/main/some/other/xsds/thisIsASource.xsd` due to the sourceExcludeSuffix "xsd".

            <plugin>
                <groupId>org.codehaus.mojo</groupId>
                <artifactId>jaxb2-maven-plugin</artifactId>
                <version>${project.version}</version>
                <executions>
                    <execution>
                        <id>xjc</id>
                        <goals>
                            <goal>xjc</goal>
                        </goals>
                    </execution>
                </executions>
                <configuration>
                    <!--
                        Include the sources from 3 locations:
                        1) a directory (including recursively finding all files in it)
                        2) an explicitly named file
                        3) a non-existent path, which is silently ignored
                    -->
                    <sources>
                        <source>src/main/some/other/xsds</source>
                        <source>src/main/foo/gnat.xsd</source>
                        <source>src/main/a/nonexistent/path</source>
                    </sources>
                    <!--
                        When providing xjcSourceExcludeFilters, the default exclude
                        Filter definitions are overridden by the Patterns supplied.
                    -->
                    <xjcSourceExcludeFilters>
                        <filter implementation="org.codehaus.mojo.jaxb2.shared.filters.pattern.PatternFileFilter">
                            <patterns>
                                <pattern>\.xsd</pattern>
                                <pattern>\.foo</pattern>
                            </patterns>
                        </filter>
                    </xjcSourceExcludeFilters>
                    <!--
                        Package name of the generated sources.
                    -->
                    <packageName>se.west</packageName>

                    <!--
                        Copy all source XSDs into the generate artifact.
                        Place them at the supplied xsdPathWithinArtifact, that is within the given directory.
                    -->
                    <xsdPathWithinArtifact>source/xsds</xsdPathWithinArtifact>
                </configuration>
            </plugin>

## Example 5: Debugging jaxb2-maven-plugin executions

If you are curious about the exact java regexp patterns used for matching your files, or simply want to see what the
jaxb2-maven-plugin does internally, run the plugin in debug mode by adding the `-debug` switch. The debug log contains
somewhat human-friendly log entries which contains the XJC arguments synthesized by the jaxb2-maven-plugin and
supplied in order:

        +=================== [11 XJC Arguments]
        |
        | [0]: -xmlschema
        | [1]: -encoding
        | [2]: UTF-8
        | [3]: -p
        | [4]: com.example.myschema
        | [5]: -d
        | [6]: /Users/lj/Development/Projects/Codehaus/github_jaxb2_plugin/target/it/xjc-main/target/generated-sources/jaxb
        | [7]: -extension
        | [8]: -episode
        | [9]: /Users/lj/Development/Projects/Codehaus/github_jaxb2_plugin/target/it/xjc-main/target/generated-sources/jaxb/META-INF/sun-jaxb.episode
        | [10]: src/main/xsd/address.xsd
        |
        +=================== [End 11 XJC Arguments]

If you would like to run XJC manually in the same way as the jaxb2-maven-plugin runs the tool, simply
paste the arguments given in the debug listing into a shell (separate with spaces).

The debug log also shows the configuration and result of the configured PatternFileFilters; as shown in the listing
below, two PatternFileFilters containing 3 java regular expression patterns each are applied to the files found below
the standard directory `src/main/xsd`. The listing shows the java regexps in use, and the resulting file after
removing all files identified by the two PatternFileFilters:

        +=================== [Filtered sources]
        |
        | 2 Exclude patterns:
        | [1/2]: Filter [PatternFileFilter]
        |        Processes nulls: [false]
        |        Accept on match: [true]
        |        3 regularExpressions ...
        |         [0/3]: (\p{javaLetterOrDigit}|\p{Punct})+README.*
        |         [1/3]: (\p{javaLetterOrDigit}|\p{Punct})+\.xml
        |         [2/3]: (\p{javaLetterOrDigit}|\p{Punct})+\.txt
        | [2/2]: Filter [PatternFileFilter]
        |        Processes nulls: [false]
        |        Accept on match: [true]
        |        1 regularExpressions ...
        |         [0/1]: (\p{javaLetterOrDigit}|\p{Punct})+\.xjb
        |
        | 1 Standard Directories:
        | [1/1]: src/main/xsd
        |
        | 1 Results:
        | [1/1]: file:/Users/lj/Development/Projects/Codehaus/github_jaxb2_plugin/target/it/xjc-main/src/main/xsd/address.xsd
        |
        +=================== [End Filtered sources]

Also, the jaxb2-maven-plugin debug log contains debug log statements emitted from the underlying tools themselves
(SchemaGen or XJC). These statements may be formatted in somewhat strange ways, but starts with the name of the tool
encased in brackets. As illustrated below, the XJC tool emitted debug statements, one of which is from the
allowExternalAccess method indicating that the property `http://javax.xml.XMLConstants/property/accessExternalSchema`
is supported and successfully set:

        [DEBUG] Using explicitly configured encoding [UTF-8]
        [DEBUG] [XJC]: feb 03, 2015 3:39:32 EM com.sun.xml.bind.v2.util.XmlFactory createSchemaFactory
        FINE: SchemaFactory instance: com.sun.org.apache.xerces.internal.jaxp.validation.XMLSchemaFactory@5b6813df

        [DEBUG] [XJC]: feb 03, 2015 3:39:32 EM com.sun.xml.bind.v2.util.XmlFactory allowExternalAccess
        FINE: Property "http://javax.xml.XMLConstants/property/accessExternalSchema" is supported and has been
        successfully set by used JAXP implementation.


Yes ... you get the timestamp for free ...