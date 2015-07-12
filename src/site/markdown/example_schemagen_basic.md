# Basic Examples - XML Schema Generation

> **Note**: These examples are valid for the 2.x version of the plugin, and do not work for the 
> jaxb2-maven-plugin version 1.x

These basic examples illustrate how to generate XSDs from JAXB-annotated Java classes using the jaxb2-maven-plugin,
and highlight the use of some of the plugin's common configuration options. This plugin runs the JDK distribution's
SchemaGenerator compiler (check JAVA_HOME/bin/schemagen), and integrates the SchemaGenerator's configuration properties
into a Maven project. Also, the jaxb2-maven-plugin
[performs post-processing on generated XSD files](./example_schemagen_postprocessing.html) to improve usability
and quality of the XSD files generated. 

## SchemaGenerator in the JDK

The JDK contains the documentation for the SchemaGenerator arguments and switches. However, that documentation is
somewhat slender and does not provide a thorough explanation for which types of "Java Files" can be used to generate
XML schema:

    Usage: schemagen [-options ...] <java files>
    Options:
        -d <path>             : specify where to place processor and javac generated class files
        -cp <path>            : specify where to find user specified files
        -classpath <path>     : specify where to find user specified files
        -encoding <encoding>  : specify encoding to be used for apt/javac invocation
        -episode <file>       : generate episode file for separate compilation
        -version              : display version information
        -fullversion          : display full version information
        -help                 : display this usage message

The help listing from the schemagen tool above is taken from a JDK 1.7 installation, and the arguments change
a little for other major JDKs. However, it seems that the SchemaGenerator tool expects the following to be true
for each of its "java files" arguments:

<table>
    <tr>
        <th width="35%">Property</th>
        <th width="60%">Description</th>
    </tr>
    <tr>
        <td>Not directories</td>
        <td>If one of the "java files" arguments is a directory, the SchemaGenerator throws an
        IllegalArgumentException with the message <em>directories not supported</em>.</td>
    </tr>
    <tr>
        <td>Java Source Files</td>
        <td>"java files" arguments can be relative paths to source files, calculated from the System property
        <code>user.dir</code> (i.e. <em>not</em> the Maven property <code>basedir</code>), on the form
        <code>src/main/java/se/west/something/SomeClass.java</code>.</td>
    </tr>
    <tr>
        <td>Bytecode Files</td>
        <td>"java files" arguments can be fully qualified class names, such as <code>se.west.gnat.Foo</code>.
        In this case, the class file (<code>Foo.class</code>) should be in the correct directory
        (<code>se/west/gnat</code>) within the classpath (typically <code>src/main/java</code> or
        <code>src/test/java</code>).</td>
    </tr>
</table>

**Note**: `schemagen` does not accept a mix of source and bytecode arguments as "java files".
While the jaxb2-maven-plugin ensures that any sources added to the schemagen goals adheres to
the criteria defined in the table above, it would lead too far to let the plugin validate that
the user does not supply a mix of java source and bytecode files as arguments.

## Recommended JAXB Annotations for XSD generation

XSD generation from JAXB-annotated Java classes uses schemagen from the JAXB specification to generate XSDs from
bytecode. While the default JAXB settings are sometimes good enough for use, tailoring generated XSDs is a task that
more often than not requires quite a lot of annotations injected into the source code. For further/full information
about the JAXB annotations, please refer to the [JAXB Reference Implementation](https://jaxb.java.net/).

However, some best practises are recommended to generate good-quality XSD from annotated Java classes:

1. Always define an @XmlType element containing propOrder and (optionally) namespace elements. The propOrder is used to
   define the XSD sequence of the elements within the ComplexType synthesized from the Java class. You need to ensure
   that the sequence is well-defined, which means that you should define the propOrder.
2. If your java class contains collections, be sure to annotate them with `@XmlElementWrapper` **and** `@XmlElement`.
   It is good practise to let the member value be plural ("guildMemberships" rather than "guildMembership") in the
   java class, since it may contain several objects. The XmlElementWrapper annotation defines the name of the
   surrounding XML container, and the inner XmlElement annotation defines the name of each XML (child) element in the
   collection.
3. Place JavaDoc document comments on the private members. This documentation is entered as Documentation Annotations
   to the respective element definitions within the generated XSD file.
4. Create a TransformSchema configuration to map your namespace (`http://some/namespace`, for example) to a user
   friendly XML namespace prefix and a sensibly named XSD file. See the
   [Postprocessing Example Page](./example_schemagen_postprocessing.html) for examples on how to configure the
   jaxb2-maven-plugin to use TransformSchema configurations.

        package se.west.schema;

        import javax.xml.bind.annotation.XmlAccessType;
        import javax.xml.bind.annotation.XmlAccessorType;
        import javax.xml.bind.annotation.XmlAttribute;
        import javax.xml.bind.annotation.XmlElement;
        import javax.xml.bind.annotation.XmlType;

        /**
         * This documentation will be copied as an XML Documentation Annotation for the ComplexType 'FooBar'.
         * Define it to augment usability/readability within the generated XSD.
         *
         * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>
         */
        @XmlType(namespace = "http://some/namespace",
            propOrder = {"requiredElement", "aRequiredElementInAnotherNamespace",
                    "optionalElement", "requiredAttribute", "optionalAttribute", "guildMembership"})
        @XmlAccessorType(XmlAccessType.FIELD)
        public class FooBar {

        /**
         * This documentation will be copied as an XML Documentation Annotation for the 'requiredElement'.
         * Define it to augment usability/readability within the generated XSD.
         */
        @XmlElement(required = true, defaultValue = "requiredElementValue")
        private String requiredElement;

        @XmlElement(namespace = "http://another/namespace", required = true, defaultValue = "requiredElementValue")
        private String aRequiredElementInAnotherNamespace;

        /**
         * This documentation will be copied as an XML Documentation Annotation for the 'optionalElement'.
         * Define it to augment usability/readability within the generated XSD.
         */
        @XmlElement(required = false)
        private String optionalElement;

        @XmlAttribute(required = true)
        private String requiredAttribute;

        @XmlAttribute(required = false)
        private String optionalAttribute;

        /**
         * The Guild Memberships of this FooBar. A nil value implies no GuildMemberships exist.
         */
        @XmlElementWrapper(name = "guildMemberships", nillable = true, required = false)
        @XmlElement(name = "guildMembership")
        private Set<GuildMembership> guildMemberships;

        ...

        }

The example above is by no means a complete guide to JAXB compliant class design, but serves to illustrate best
practises which eliminate some of the more common mistakes for correct XSD generation. Again, for further/full
information about the JAXB annotations, please refer to the [JAXB Reference Implementation](https://jaxb.java.net/).

## Example 1: Default XSD generation

Generate XSD files from annotated Java classes is simple with the jaxb2-maven-plugin.
Adding the plugin to your project and invoking its `schemagen` goal makes schemagen compile all
java files found under the standard compile source roots (typically `src/main/java`):

            <plugin>
                <groupId>org.codehaus.mojo</groupId>
                <artifactId>jaxb2-maven-plugin</artifactId>
                <executions>
                    <execution>
                        <id>schemagen</id>
                        <goals>
                            <goal>schemagen</goal>
                        </goals>
                    </execution>
                </executions>
                <!--
                    Use default configuration, implying that sources are read
                    from the directory src/main/java below the project basedir.

                    (i.e. getProject().getCompileSourceRoots() in Maven-speak).
                -->
            </plugin>

The output schema will be called `schema1.xsd`, `schema2.xsd` etc. and placed within the
`target/generated-resources/schemagen/` folder.

## Example 2: Exclude source files from processing

The jaxb2-maven-plugin permits you to exclude selected source files from processing, using a set of Filters.
If a Filter matches a candidate source file, it is excluded from processing. In the sample below, any file
whose full path ends with `jaxb.index` will be ignored by schemagen even if found below a `src/main/java`
directory:

            <plugin>
                <groupId>org.codehaus.mojo</groupId>
                <artifactId>jaxb2-maven-plugin</artifactId>
                <executions>
                    <execution>
                        <id>schemagen</id>
                        <goals>
                            <goal>schemagen</goal>
                        </goals>
                    </execution>
                </executions>
                <configuration>
                    <!--
                     SchemaGenerator configuration properties, which excludes files found in the source directories.
                     This consists of a (List of) Filters, all of which should implement the

                     org.codehaus.mojo.jaxb2.shared.filters.Filter

                     interface.
                   -->
                    <schemaSourceExcludeFilters>
                        <noJaxbIndex implementation="org.codehaus.mojo.jaxb2.shared.filters.pattern.PatternFileFilter">
                            <patterns>
                                <pattern>jaxb\.index</pattern>
                            </patterns>
                        </noJaxbIndex>
                    </schemaSourceExcludeFilters>
                </configuration>
            </plugin>

The PatternFileFilter matches the supplied java regexp patterns to a String expression calculated from
each candidate File found under the `src/main/java` directory, which means that the power of regular expressions
can be used to create several patterns to exclude files:

                    <schemaSourceExcludeFilters>
                        <myExcludes implementation="org.codehaus.mojo.jaxb2.shared.filters.pattern.PatternFileFilter">
                            <patterns>
                                <pattern>jaxb\.index</pattern>
                                <pattern>\.txt</pattern>
                                <pattern>\.mdo</pattern>
                                <pattern>.*blep.*</pattern>
                            </patterns>
                        </myExcludes>
                    </schemaSourceExcludeFilters>

**Note**: The name of the element defining the pattern (`myExcludes` in the snippet above) is unimportant. Maven will
create a Filter object for each element to the `schemaSourceExcludeFilters` List, and inject it into the parameter
called `schemaSourceExcludeFilters`. This is a result of the parameter definition:

                @Parameter(required = false)
                private List<Filter<File>> schemaSourceExcludeFilters;

Please refer to the standard
[Maven Documentation for configuring plugins](http://maven.apache.org/guides/mini/guide-configuring-plugins.html) for
further information about configuring plugins.

## Example 3: PatternFileFilter custom configuration

Your project might have complex rules for determining which files should be excluded from a schemagen compilation.
To cater for potentially complex filtering needs, you may either configure the PatternFileFilter to cater for your
needs or implement a custom Filter. This example illustrates how to configure the PatternFileFilter.

PatternFileFilters may use several (optional) configuration settings to customize their work process (look for setter
methods in the PatternFileFilter JavaDoc). Two of the commonly used configuration settings are:

1. *patternPrefix*: Java Regular Expressions can be rather lengthy, and frequently have similar structure with the
   difference only being the last part. The `patternPrefix` property contains a string to prepend to all patterns to
   yield the final java regexp pattern - and unless overridden, the default pattern prefix is
   `(\p{javaLetterOrDigit}|\p{Punct})+`. In the configuration snippet below, the patternPrefix is set to `*` instead.
   Therefore, the regular expression used to match Files with in the example shown below is `.*Bar\.java`.
2. *converter*: Java Regular Expressions compare and match Strings to Patterns. However, each candidate supplied to
   the PatternFileFiler is a `java.io.File` object, and we must therefore somehow convert the File to a String before
   we can match it with the complete regular expression as shown above. This conversion is done by a StringConverter
   (see the [JavaDoc](./javadoc/)) which simply contains a method `String convert(T toConvert);` which emits a String
   synthesized from each File object. The default implementation (below) simply retrieves the canonical absolute path
   for each File, but you may override this and supply a custom implementation - simply implement the
   StringConverter in a manner similar to the following (substituting for your preferred algorithm):

        /**
         * Converter returning the canonical and absolute path for a File.
         */
        public static final StringConverter<File> FILE_PATH_CONVERTER = new StringConverter<File>() {
           @Override
           public String convert(final File toConvert) {
               return FileSystemUtilities.getCanonicalPath(toConvert.getAbsoluteFile());
           }
        };

Finally, the configuration example below shows how to override the default pattern prefix and StringConverter:

                    <!--
                     SchemaGenerator configuration properties, which excludes files found in the source directories.
                     This consists of a (List of) Filters, all of which should implement the

                     org.codehaus.mojo.jaxb2.shared.filters.Filter

                     interface.

                     Also, use a custom StringConverter for the found Files.
                   -->
                    <schemaSourceExcludeFilters>
                        <noBarFile implementation="org.codehaus.mojo.jaxb2.shared.filters.pattern.PatternFileFilter">
                            <patterns>
                                <pattern>Bar\.java</pattern>
                            </patterns>
                            <patternPrefix>.*</patternPrefix>
                            <converter implementation="se.west.converter.FileNameConverter"/>
                        </noBarFile>
                    </schemaSourceExcludeFilters>

If your implementation class `se.west.converter.FileNameConverter` is located in a separate project, remember to
make that implementation class accessible to the jaxb2-maven-plugin. This is done by Maven's default dependency
mechanism for plugins, as illustrated by the full configuration example below.

            <plugin>
                <groupId>org.codehaus.mojo</groupId>
                <artifactId>jaxb2-maven-plugin</artifactId>
                <executions>
                    <execution>
                        <id>schemagen</id>
                        <goals>
                            <goal>schemagen</goal>
                        </goals>
                    </execution>
                </executions>
                <configuration>
                    <!--
                        Place the generated XSDs in a custom location.
                    -->
                    <xsdPathWithinArtifact>generated/xsds</xsdPathWithinArtifact>
                    <!--
                     SchemaGenerator configuration properties, which excludes files found in the source directories.
                     This consists of a (List of) Filters, all of which should implement the

                     org.codehaus.mojo.jaxb2.shared.filters.Filter

                     interface.

                     Also, use a custom StringConverter for the found Files.
                   -->
                    <schemaSourceExcludeFilters>
                        <noBarFile implementation="org.codehaus.mojo.jaxb2.shared.filters.pattern.PatternFileFilter">
                            <patterns>
                                <pattern>Bar\.java</pattern>
                            </patterns>
                            <patternPrefix>.*</patternPrefix>
                            <converter implementation="se.west.converter.FileNameConverter"/>
                        </noBarFile>
                    </schemaSourceExcludeFilters>
                </configuration>
                <dependencies>
                    <dependency>
                        <groupId>se.west.converters</groupId>
                        <artifactId>custom-jaxb-converters</artifactId>
                        <version>1.2.14</version>
                    </dependency>
                </dependencies>
            </plugin>

## Example 4: Debugging jaxb2-maven-plugin executions

If you are curious about the exact java regexp patterns used for matching your files, or simply want to see what the
jaxb2-maven-plugin does internally, run the plugin in debug mode by adding the `-X` switch. The debug log contains
somewhat human-friendly log entries which contains the SchemaGen arguments synthesized by the jaxb2-maven-plugin and
supplied in order:

        +=================== [9 SchemaGen Arguments]
        |
        | [0]: -encoding
        | [1]: UTF-8
        | [2]: -d
        | [3]: /Users/lj/Development/Projects/Codehaus/github_jaxb2_plugin/target/it/schemagen-main/target/schemagen-work/compile_scope
        | [4]: -classpath
        | [5]: /Users/lj/Development/Projects/Codehaus/github_jaxb2_plugin/target/it/schemagen-main/src/main/java/
        | [6]: -episode
        | [7]: /Users/lj/Development/Projects/Codehaus/github_jaxb2_plugin/target/it/schemagen-main/target/generated-resources/schemagen/META-INF/sun-jaxb.episode
        | [8]: src/main/java/se/west/gnat/Foo.java
        |
        +=================== [End 9 SchemaGen Arguments]

If you would like to run SchemaGen manually in the same way as the jaxb2-maven-plugin runs the tool, simply
paste the arguments given in the debug listing into a shell (separate with spaces).

The debug log also shows the configuration and result of the configured PatternFileFilters; as shown in the listing
below, two PatternFileFilters containing 3 java regular expression patterns each are applied to the files found below
the standard directory `src/main/java`. The listing shows the java regexps in use, and the resulting file after
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
        |        3 regularExpressions ...
        |         [0/3]: (\p{javaLetterOrDigit}|\p{Punct})+\.xjb
        |         [1/3]: (\p{javaLetterOrDigit}|\p{Punct})+\.xsd
        |         [2/3]: (\p{javaLetterOrDigit}|\p{Punct})+\.properties
        |
        | 1 Standard Directories:
        | [1/1]: /Users/lj/Development/Projects/Codehaus/github_jaxb2_plugin/target/it/schemagen-main/src/main/java
        |
        | 1 Results:
        | [1/1]: file:/Users/lj/Development/Projects/Codehaus/github_jaxb2_plugin/target/it/schemagen-main/src/main/java/se/west/gnat/Foo.java
        |
        +=================== [End Filtered sources]

Also, the jaxb2-maven-plugin debug log contains debug log statements emitted from the underlying tools themselves
(SchemaGen or XJC). These statements may be formatted in somewhat strange ways, but starts with the name of the tool
encased in brackets. As illustrated below, the SchemaGen tool emitted a debug statement from the createOutput method
with the content `ENTRY schema1.xsd`:

        [DEBUG] [SchemaGen]: feb 03, 2015 3:39:15 EM com.sun.xml.bind.v2.schemagen.FoolProofResolver createOutput
        FINER: ENTRY  schema1.xsd

Yes ... you get the timestamp for free ...

# Example 5: Setting the locale for the SchemaGen execution

If you require the SchemaGen tool to be executed with a default Locale other than your standard default Locale, simply 
use the `locale` configuration parameter and supply a string parseable to a Locale on the form
`<language>[,<country>[,<variant>]]`. For example, to generate the schema using french locale despite running Maven with
another Locale, configure the jaxb2-maven-plugin as follows:

            <plugin>
                <groupId>org.codehaus.mojo</groupId>
                <artifactId>jaxb2-maven-plugin</artifactId>
                <executions>
                    <execution>
                        <id>schemagen</id>
                        <goals>
                            <goal>schemagen</goal>
                        </goals>
                    </execution>
                </executions>
                <configuration>
                    <locale>fr</locale>
                </configuration>
            </plugin>
            
The generated schema 

    /**
     * <p>Classe Java pour AddressType complex type.
     * 
     * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
     * 
     * <pre>
     * &lt;complexType name="AddressType"&gt;
     *   &lt;complexContent&gt;
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *       &lt;sequence&gt;
     *         &lt;element name="Name" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
     *         &lt;element name="Line1" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
     *         &lt;element name="Line2" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
     *         &lt;element name="City" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
     *         &lt;element name="State" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
     *         &lt;element name="ZipCode" type="{http://www.w3.org/2001/XMLSchema}decimal"/&gt;
     *       &lt;/sequence&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "AddressType", propOrder = {
        "name",
        "line1",
        "line2",
        "city",
        "state",
        "zipCode"
    })
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-12T08:33:24+02:00", comments = "JAXB RI v2.2.11")
    public class AddressType { .... }

## Advanced topic: SchemaGen source vs. bytecode arguments

The SchemaGen tool assumes that all relative paths to source files are calculated from the system property
`user.dir`, as described above. For maven multi-module projects the `user.dir` property is defined as the
directory where the `mvn` command was launched - and the jaxb2-maven-plugin calculates the relative path from
this directory to the actual file. This implies that schemagen will need a full path to its source file arguments,
but only a fully qualified classname for bytecode arguments. Also, the schemagen tool requires *either* source
or bytecode arguments - not both.

A native schemagen command will therefore take one of the following forms:

    schemagen -d target/classes src/main/java/se/west/gnat/Foo.java

    ... or, if you generate schema from already compiled bytecode files:

    schemagen -d target/classes se.west.gnat.Bar

While the jaxb2-maven-plugin could potentially include some snazzy and complex code to ensure that
the arguments would be only either source or bytecode files, the underlying schemagen tool will
complain visibly if the argument combinations are incorrect. Thus, the jaxb2-maven-plugin authors
felt it would be better to simply delegate the error handling in this case to schemagen, to avoid
creating a new layer of potentially complex logic within the plugin.