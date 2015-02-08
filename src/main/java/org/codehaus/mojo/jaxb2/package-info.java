/**
 * <p>The jaxb2-maven-plugin wraps and adapts the <a href="https://jaxb.java.net/">JAXB reference implementation</a>
 * to be useful within the Maven build process.</p>
 * <p>The plugin's code is divided into 3 main blocks, each placed within a separate package structure:</p>
 *
 * <ol>
 *     <li><strong>javageneration</strong>. Contains code involved in creating java code from XSD or DTDs.
 *     This package structure adapts the plugin to using the XJC ("Xml-to-Java-Compiler") from the
 *     JAXB reference implementation.</li>
 *     <li><strong>schemageneration</strong>. Contains code involved in creating XSDs from annotated Java classes.
 *     This package structure adapts the plugin to using the schemagen tool from the JDK. (Typically found in the
 *     bin directory of the java installation).</li>
 *     <li><strong>shared</strong>. Contains shared utility classes used by both the java- and xsd-generation
 *     structure classes.</li>
 * </ol>
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>
 * @see <a href="https://jaxb.java.net/">The JAXB Reference Implementation</a>
 */
package org.codehaus.mojo.jaxb2;