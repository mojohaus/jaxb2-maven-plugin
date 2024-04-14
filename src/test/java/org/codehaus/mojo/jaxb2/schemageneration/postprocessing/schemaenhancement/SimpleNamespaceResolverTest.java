package org.codehaus.mojo.jaxb2.schemageneration.postprocessing.schemaenhancement;

import javax.xml.XMLConstants;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.util.FileUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>
 */
public class SimpleNamespaceResolverTest {

    public static final String SCHEMA_DIR = "/org/codehaus/mojo/jaxb2/helpers/";

    private File getSchemaFile(String resource) {
        return FileUtils.toFile(this.getClass().getResource(resource));
    }

    @Test
    void validateCollectingSchemaInfoForSingleNamespaceSchemaFile() {
        // Assemble
        final String schemaFile = "yetAnotherSchema.xsd";
        final File resolvedSchemaFile = getSchemaFile(SCHEMA_DIR + schemaFile);
        final SimpleNamespaceResolver unitUnderTest = new SimpleNamespaceResolver(resolvedSchemaFile);

        // Act
        final Map<String, String> namespaceURI2PrefixMap = unitUnderTest.getNamespaceURI2PrefixMap();

        // Assert
        assertEquals(schemaFile, unitUnderTest.getSourceFilename());
        assertEquals("http://yet/another/namespace", unitUnderTest.getLocalNamespaceURI());

        assertEquals(1, namespaceURI2PrefixMap.size());
        assertEquals("xs", namespaceURI2PrefixMap.get(XMLConstants.W3C_XML_SCHEMA_NS_URI));

        assertEquals(XMLConstants.W3C_XML_SCHEMA_NS_URI, unitUnderTest.getNamespaceURI("xs"));
    }

    @Test
    void validateCollectingSchemaInfoForMultipleNamespaceSchemaFile() {
        // Assemble
        final String schemaFile = "anotherSchema.xsd";
        final SimpleNamespaceResolver unitUnderTest =
                new SimpleNamespaceResolver(getSchemaFile(SCHEMA_DIR + schemaFile));

        // Act
        final Map<String, String> namespaceURI2PrefixMap = unitUnderTest.getNamespaceURI2PrefixMap();

        // Assert
        assertEquals(schemaFile, unitUnderTest.getSourceFilename());
        assertEquals("http://another/namespace", unitUnderTest.getLocalNamespaceURI());

        assertEquals(3, namespaceURI2PrefixMap.size());
        assertEquals("xs", namespaceURI2PrefixMap.get(XMLConstants.W3C_XML_SCHEMA_NS_URI));
        assertEquals("yetAnother", namespaceURI2PrefixMap.get("http://yet/another/namespace"));
        assertEquals("some", namespaceURI2PrefixMap.get("http://some/namespace"));

        for (String current : namespaceURI2PrefixMap.keySet()) {
            final String currentPrefix = namespaceURI2PrefixMap.get(current);
            assertEquals(currentPrefix, unitUnderTest.getPrefix(current));
        }
    }

    @Test
    void validateExceptionOnEmptyRelativePathToXmlFile() {
        assertThrows(IllegalArgumentException.class, () -> {
            // Assemble
            final String incorrectEmpty = "";

            // Act & Assert
            new SimpleNamespaceResolver(getSchemaFile(incorrectEmpty));
            fail("Creating a SimpleNamespaceResolver with empty argument "
                    + "should yield an IllegalArgumentException.");
        });
    }

    @Test
    void validateExceptionOnNonexistentXmlSchemaFile() {
        // Assemble
        final String nonExistentPath = "this/file/does/not/exist.xml";
        final File nonExistent = new File(nonExistentPath);

        // Act & Assert
        try {
            new SimpleNamespaceResolver(nonExistent);
            fail("Creating a SimpleNamespaceResolver connected to a nonexistent file "
                    + "should yield an IllegalArgumentException.");
        } catch (IllegalArgumentException e) {
            // Expected
        } catch (Exception e) {
            fail("Expected IllegalArgumentException, but received ["
                    + e.getClass().getName() + "]");
        }
    }

    @Test
    void validateJaxbNamespaceResolverComplianceInThrowingExceptionOnNullNamespaceResolverArguments() {
        // Assemble
        final String schemaFile = "yetAnotherSchema.xsd";
        final SimpleNamespaceResolver unitUnderTest =
                new SimpleNamespaceResolver(getSchemaFile(SCHEMA_DIR + schemaFile));
        final String incorrectNull = null;

        // Act & Assert
        try {
            unitUnderTest.getPrefix(incorrectNull);
            fail("Running getPrefix with a null argument should yield an IllegalArgumentException.");
        } catch (IllegalArgumentException e) {
            // Expected
        } catch (Exception e) {
            fail("Expected IllegalArgumentException, but received ["
                    + e.getClass().getName() + "]");
        }

        try {
            unitUnderTest.getNamespaceURI(incorrectNull);
            fail("Running getNamespaceURI with a null argument should yield an IllegalArgumentException.");
        } catch (IllegalArgumentException e) {
            // Expected
        } catch (Exception e) {
            fail("Expected IllegalArgumentException, but received ["
                    + e.getClass().getName() + "]");
        }

        try {
            unitUnderTest.getPrefixes(incorrectNull);
            fail("Running getPrefixes with a null argument should yield an IllegalArgumentException.");
        } catch (IllegalArgumentException e) {
            // Expected
        } catch (Exception e) {
            fail("Expected IllegalArgumentException, but received ["
                    + e.getClass().getName() + "]");
        }
    }

    @Test
    void validatePrefixesIterator() {
        // Assemble
        final String schemaFile = "yetAnotherSchema.xsd";
        final SimpleNamespaceResolver unitUnderTest =
                new SimpleNamespaceResolver(getSchemaFile(SCHEMA_DIR + schemaFile));

        // Act
        List<String> prefixesList = new ArrayList<String>();
        for (Iterator<String> it = unitUnderTest.getPrefixes(XMLConstants.W3C_XML_SCHEMA_NS_URI); it.hasNext(); ) {
            prefixesList.add(it.next());
        }

        // Assert
        assertEquals(1, prefixesList.size());
        assertEquals("xs", prefixesList.get(0));
    }
}
