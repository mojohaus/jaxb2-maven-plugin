package se.west.validation;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.StringWriter;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>
 */
public class XmlNormalizer {

    private static final TransformerFactory FACTORY;

    static {
        FACTORY = TransformerFactory.newInstance();
        FACTORY.setAttribute("indent-number", 2);
    }

    /**
     * Reads the XML file found at the provided filePath, returning a normalized
     * XML form suitable for comparison.
     *
     * @param filePath The path to the XML file to read.
     * @return A normalized, human-readable, version of the given XML document.
     */
    public String getNormalizedXml(String filePath) {

        // Read the provided filename
        final StringWriter toReturn = new StringWriter();
        final BufferedReader in;
        try {
            in = new BufferedReader(new FileReader(new File(filePath)));
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("Could not find file [" + filePath + "]", e);
        }

        // Parse into a Document
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);

        Document document = null;
        try {

            document = factory.newDocumentBuilder().parse(new InputSource(in));
            Transformer transformer = FACTORY.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(new DOMSource(document.getFirstChild()), new StreamResult(toReturn));

        } catch (Exception e) {
            throw new IllegalArgumentException("Could not transform DOM Document", e);
        }

        // All done.
        return toReturn.toString();
    }
}
