package org.codehaus.mojo.jaxb2.schemageneration.postprocessing.javadoc;

import org.codehaus.mojo.jaxb2.AbstractJaxbMojo;
import org.codehaus.mojo.jaxb2.BufferingLog;
import org.codehaus.mojo.jaxb2.schemageneration.postprocessing.NodeProcessor;
import org.codehaus.mojo.jaxb2.shared.FileSystemUtilities;
import org.codehaus.mojo.jaxb2.shared.Validate;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Assert;
import org.junit.Before;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public abstract class AbstractSourceCodeAwareNodeProcessingTest {

    /**
     * Default systemId for the empty namespace.
     */
    public static final String DEFAULT_EMPTY_NAMESPACE_SYSTEM_ID = "emptyNamespaceSystemId.xsd";

    // Shared state
    protected BufferingLog log;
    protected SearchableDocumentation docs;
    protected SortedMap<String, String> namespace2GeneratedSchemaMap;
    protected SortedMap<String, Document> namespace2DocumentMap;
    protected Map<String, String> namespace2SystemIdMap;
    protected List<String> xsdGenerationWarnings;
    protected final File basedir;
    protected final File testJavaDir;
    protected JAXBContext jaxbContext;
    protected SortedMap<String, Throwable> xsdGenerationLog;

    // Internal state
    private List<Class<?>> jaxbClasses;

    public AbstractSourceCodeAwareNodeProcessingTest() {

        // Setup the basic directories.
        basedir = getBasedir();
        testJavaDir = new File(basedir, "src/test/java");
        Assert.assertTrue(testJavaDir.exists() && testJavaDir.isDirectory());
    }

    @Before
    public final void setupSharedState() throws Exception {

        log = new BufferingLog(BufferingLog.LogLevel.DEBUG);

        // Create internal state for the generated structures.
        namespace2SystemIdMap = new TreeMap<String, String>();
        xsdGenerationWarnings = new ArrayList<String>();
        namespace2DocumentMap = new TreeMap<String, Document>();
        namespace2GeneratedSchemaMap = new TreeMap<String, String>();

        // Pre-populate the namespace2SystemIdMap
        namespace2SystemIdMap.put(SomewhatNamedPerson.NAMESPACE, "somewhatNamedPerson.xsd");
        namespace2SystemIdMap.put("http://jaxb.mojohaus.org/wrappers", "wrapperExample.xsd");
        namespace2SystemIdMap.put("", DEFAULT_EMPTY_NAMESPACE_SYSTEM_ID);

        // Create the JAXBContext
        jaxbClasses = getJaxbAnnotatedClassesForJaxbContext();
        Assert.assertNotNull("getJaxbAnnotatedClassesForJaxbContext() should not return a null List.", jaxbClasses);
        final Class<?>[] classArray = jaxbClasses.toArray(new Class<?>[jaxbClasses.size()]);
        jaxbContext = JAXBContext.newInstance(classArray);

        // Generate the vanilla XSD from JAXB
        final SortedMap<String, StringWriter> tmpSchemaMap = new TreeMap<String, StringWriter>();

        try {
            jaxbContext.generateSchema(new SchemaOutputResolver() {
                @Override
                public Result createOutput(final String namespaceUri,
                                           final String suggestedFileName)
                        throws IOException {

                    // As put in the XmlBinding JAXB implementation of Nazgul Core:
                    //
                    // "The types should really be annotated with @XmlType(namespace = "... something ...")
                    // to avoid using the default ("") namespace".
                    if (namespaceUri.isEmpty()) {
                        xsdGenerationWarnings.add("Got empty namespaceUri for suggestedFileName ["
                                + suggestedFileName + "].");
                    }

                    // Create the result Writer
                    final StringWriter out = new StringWriter();
                    final StreamResult toReturn = new StreamResult(out);

                    // The systemId *must* be non-null, even in this case where we
                    // do not write the XSD to a file.
                    final String effectiveSystemId = namespace2SystemIdMap.get(namespaceUri) == null
                            ? suggestedFileName
                            : namespace2SystemIdMap.get(namespaceUri);
                    toReturn.setSystemId(effectiveSystemId);

                    // Map the namespaceUri to the schemaResult.
                    tmpSchemaMap.put(namespaceUri, out);

                    // All done.
                    return toReturn;
                }
            });
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not acquire Schema snippets.", e);
        }

        // Store all generated XSDs
        for (Map.Entry<String, StringWriter> current : tmpSchemaMap.entrySet()) {
            namespace2GeneratedSchemaMap.put(current.getKey(), current.getValue().toString());
        }

        // Create XML Documents for all generated Schemas
        for (Map.Entry<String, String> current : namespace2GeneratedSchemaMap.entrySet()) {
            final Document document = createDocument(current.getValue());
            namespace2DocumentMap.put(current.getKey(), document);
        }

        // Create the SearchableDocumentation
        final JavaDocExtractor extractor = new JavaDocExtractor(log);
        extractor.addSourceFiles(resolveSourceFiles());
        docs = extractor.process();

        // Stash and clear the log buffer.
        xsdGenerationLog = log.getAndResetLogBuffer();
    }

    /**
     * @return A List containing all classes which should be part of the JAXBContext.
     */
    protected abstract List<Class<?>> getJaxbAnnotatedClassesForJaxbContext();

    /**
     * @return The basedir directory, corresponding to the root of this project.
     */
    protected File getBasedir() {

        // Use the system property if available.
        String basedirPath = System.getProperty("basedir");
        if (basedirPath == null) {
            basedirPath = new File("").getAbsolutePath();
        }

        final File toReturn = new File(basedirPath);
        Assert.assertNotNull("Could not find 'basedir'. Please set the system property 'basedir'.", toReturn);
        Assert.assertTrue("'basedir' must be an existing directory. ", toReturn.exists() && toReturn.isDirectory());

        // All done.
        return toReturn;
    }

    /**
     * Creates a DOM Document from the supplied XML.
     *
     * @param xmlContent The non-empty XML which should be converted into a Document.
     * @return The Document created from the supplied XML Content.
     */
    protected final Document createDocument(final String xmlContent) {

        // Check sanity
        Validate.notEmpty(xmlContent, "xmlContent");

        // Build a DOM model of the provided xmlFileStream.
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);

        try {
            return factory.newDocumentBuilder().parse(new InputSource(new StringReader(xmlContent)));
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not create DOM Document", e);
        }
    }

    /**
     * Drives the supplied visitor to process the provided Node and all its children, should the recurseToChildren flag
     * be set to <code>true</code>. All attributes of the current node are processed before recursing to children (i.e.
     * breadth first recursion).
     *
     * @param node              The Node to process.
     * @param recurseToChildren if <code>true</code>, processes all children of the supplied node recursively.
     * @param visitor           The NodeProcessor instance which should process the nodes.
     */
    public final void process(final Node node, final boolean recurseToChildren, final NodeProcessor visitor) {

        // Process the current Node, if the NodeProcessor accepts it.
        if (visitor.accept(node)) {
            onAcceptedNode(node);
            visitor.process(node);
        }

        NamedNodeMap attributes = node.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
            Node attribute = attributes.item(i);

            // Process the current attribute, if the NodeProcessor accepts it.
            if (visitor.accept(attribute)) {
                onAcceptedAttribute(attribute);
                visitor.process(attribute);
            }
        }

        if (recurseToChildren) {
            NodeList children = node.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);

                // Recurse to Element children.
                if (child.getNodeType() == Node.ELEMENT_NODE) {
                    process(child, true, visitor);
                }
            }
        }
    }

    /**
     * Event callback when a nodeProcessor has accepted a Node.
     *
     * @param aNode the accepted Node
     */
    protected void onAcceptedNode(final Node aNode) {
        // name="firstName"
        final String nodeName = aNode.getAttributes().getNamedItem("name").getNodeValue();
        log.info("Accepted node [" + aNode.getNodeName() + "] " + nodeName);
    }

    /**
     * Event callback when a nodeProcessor has accepted an Attribute.
     *
     * @param anAttribute the accepted attribute.
     */
    protected void onAcceptedAttribute(final Node anAttribute) {
        log.info("Accepted attribute [" + anAttribute.getNodeName() + "]");
    }

    //
    // Private helpers
    //

    /**
     * Utility method to read all (string formatted) data from the given classpath-relative
     * file and return the data as a string.
     *
     * @param path The classpath-relative file path.
     * @return The content of the supplied file.
     */
    protected static String readFully(final String path) {

        final StringBuilder toReturn = new StringBuilder(50);

        try {

            // Will produce a NPE if the path was not directed to a file.
            final InputStream resource = AbstractSourceCodeAwareNodeProcessingTest
                    .class
                    .getClassLoader()
                    .getResourceAsStream(path);
            final BufferedReader tmp = new BufferedReader(new InputStreamReader(resource));

            for (String line = tmp.readLine(); line != null; line = tmp.readLine()) {
                toReturn.append(line).append(AbstractJaxbMojo.NEWLINE);
            }
        } catch (final Exception e) {
            throw new IllegalArgumentException("Resource [" + path + "] not readable.");
        }

        // All done.
        return toReturn.toString();
    }

    /**
     * Compares XML documents provided by the two Readers.
     *
     * @param expected The expected document data.
     * @param actual   The actual document data.
     * @return A DetailedDiff object, describing all differences in documents supplied.
     * @throws org.xml.sax.SAXException If a SAXException was raised during parsing of the two Documents.
     * @throws IOException              If an I/O-related exception was raised while acquiring the data from the Readers.
     */
    protected static Diff compareXmlIgnoringWhitespace(final String expected, final String actual) throws SAXException,
            IOException {

        // Check sanity
        org.apache.commons.lang3.Validate.notNull(expected, "Cannot handle null expected argument.");
        org.apache.commons.lang3.Validate.notNull(actual, "Cannot handle null actual argument.");

        // Ignore whitespace - and also normalize the Documents.
        XMLUnit.setNormalize(true);
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setNormalize(true);

        // Compare and return
        return XMLUnit.compareXML(expected, actual);
    }

    private List<File> resolveSourceFiles() {

        final List<File> sourceDirs = Arrays.<File>asList(new File(basedir, "src/main/java"), testJavaDir);
        final List<File> candidates = FileSystemUtilities.resolveRecursively(sourceDirs, null, log);
        final List<File> toReturn = new ArrayList<File>();

        for (File current : candidates) {
            for (Class<?> currentClass : jaxbClasses) {

                final String expectedFileName = currentClass.getSimpleName() + ".java";
                if (expectedFileName.equalsIgnoreCase(current.getName())) {

                    final String transmutedCanonicalPath = FileSystemUtilities.getCanonicalPath(current)
                            .replace("/", ".")
                            .replace(File.separator, ".");

                    if (transmutedCanonicalPath.contains(currentClass.getPackage().getName())) {
                        toReturn.add(current);
                    }
                }
            }
        }

        // All done.
        return toReturn;
    }

    /**
     * Prints the content of the supplied DOM Document as a string.
     *
     * @param doc A non-null DOM Document.
     * @return A String holding the pretty-printed version of the supplied doc.
     */
    public static String printDocument(final Document doc) {

        try {
            // Create the Unity-Transformer
            final TransformerFactory tf = TransformerFactory.newInstance();
            final Transformer transformer = tf.newTransformer();

            // Make it pretty print stuff.
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

            // Harvest the result, and return.
            final StringWriter out = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(out));
            return out.toString();
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not print document", e);
        }
    }
}
