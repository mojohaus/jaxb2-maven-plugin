package org.codehaus.mojo.jaxb2.schemageneration.postprocessing.schemaenhancement;

import javax.xml.XMLConstants;

import java.io.File;
import java.nio.file.Files;
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

    @Test
    void validateCollectingSchemaInfoWithTnsPrefix() {
        // Assemble
        final String schemaFile = "tnsSchema.xsd";
        final SimpleNamespaceResolver unitUnderTest =
                new SimpleNamespaceResolver(getSchemaFile(SCHEMA_DIR + schemaFile));

        // Act
        final Map<String, String> namespaceURI2PrefixMap = unitUnderTest.getNamespaceURI2PrefixMap();

        // Assert
        assertEquals(schemaFile, unitUnderTest.getSourceFilename());
        assertEquals("http://schemas.acme.com/student", unitUnderTest.getLocalNamespaceURI());

        // Verify that tns prefix is correctly handled (should be overridden without error)
        assertEquals(3, namespaceURI2PrefixMap.size());
        assertEquals("xs", namespaceURI2PrefixMap.get(XMLConstants.W3C_XML_SCHEMA_NS_URI));
        assertEquals("base", namespaceURI2PrefixMap.get("http://schemas.acme.com"));
        assertEquals("tns", namespaceURI2PrefixMap.get("http://schemas.acme.com/student"));
    }

    @Test
    void validateTnsPrefixIsNotOverwritten() {
        // Assemble: XML with two prefixes for the same URI, one is 'tns'
        final String xmlStream =
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + "<xs:schema version=\"1.0\"\n"
                        + "           targetNamespace=\"http://some/namespace\"\n"
                        + "           xmlns:tns=\"http://some/namespace\"\n"
                        + "           xmlns:other=\"http://some/namespace\"\n"
                        + "           xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">\n"
                        + "  <xs:element name=\"foo\" type=\"xs:string\"/>\n"
                        + "</xs:schema>\n";
        // Write to temp file
        try {
            File tempFile = File.createTempFile("test-schema", ".xsd");
            tempFile.deleteOnExit();
            Files.write(tempFile.toPath(), xmlStream.getBytes(java.nio.charset.StandardCharsets.UTF_8));

            // Act
            SimpleNamespaceResolver resolver = new SimpleNamespaceResolver(tempFile);
            Map<String, String> uri2Prefix = resolver.getNamespaceURI2PrefixMap();

            // Assert: 'tns' should be the prefix for the URI, not 'other'
            assertEquals("tns", uri2Prefix.get("http://some/namespace"));
        } catch (Exception e) {
            fail("Exception during test: " + e.getMessage());
        }
    }

    @Test
    void validateExceptionThrownOnReplacedUri() throws Exception {
        // Assemble: nested elements with the same prefix but different URIs
        final String xmlStream = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
                + "<xs:schema xmlns:foo=\"http://uri1/\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">\n"
                + "  <xs:element name=\"bar\" type=\"xs:string\"/>\n"
                + "  <xs:element name=\"baz\">\n"
                + "    <xs:complexType>\n"
                + "      <xs:sequence>\n"
                + "        <xs:element name=\"qux\" type=\"xs:string\" xmlns:foo=\"http://uri2/\"/>\n"
                + "      </xs:sequence>\n"
                + "    </xs:complexType>\n"
                + "  </xs:element>\n"
                + "</xs:schema>\n";
        File tempFile = File.createTempFile("test-schema-uri", ".xsd");
        tempFile.deleteOnExit();
        Files.write(tempFile.toPath(), xmlStream.getBytes(java.nio.charset.StandardCharsets.UTF_8));

        // Act & Assert
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            new SimpleNamespaceResolver(tempFile);
        });
        assertTrue(ex.getMessage().contains("Replaced URI"));
    }

    @Test
    void validateExceptionThrownOnReplacedPrefix() throws Exception {
        // Assemble: two different prefixes for the same URI (not tns)
        final String xmlStream =
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + "<xs:schema version=\"1.0\"\n"
                        + "           xmlns:foo=\"http://uri/\"\n"
                        + "           xmlns:bar=\"http://uri/\"\n"
                        + "           xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">\n"
                        + "  <xs:element name=\"baz\" type=\"xs:string\"/>\n"
                        + "</xs:schema>\n";
        File tempFile = File.createTempFile("test-schema-prefix", ".xsd");
        tempFile.deleteOnExit();
        Files.write(tempFile.toPath(), xmlStream.getBytes(java.nio.charset.StandardCharsets.UTF_8));

        // Act & Assert
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            new SimpleNamespaceResolver(tempFile);
        });
        assertTrue(ex.getMessage().contains("Replaced prefix"));
    }

    /**
     * Verifies that prefix2Uri and uri2Prefix stay bidirectionally consistent when
     * both a "tns" prefix and another prefix (e.g. "other") are declared for the
     * same namespace URI. The "tns" prefix must win as the canonical mapping, and
     * the non-"tns" prefix must not leave a stale entry in prefix2Uri.
     *
     * <p>This is independent of DOM attribute iteration order: regardless of whether
     * "tns" or "other" is encountered first, the end state must have "tns" as the
     * sole prefix for the URI in both maps.</p>
     */
    @Test
    void validateBidirectionalMapConsistencyWhenTnsAndOtherPrefixShareUri() throws Exception {
        // Assemble: schema where "tns" and "other" both declare the same namespace URI
        final String xmlStream = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
                + "<xs:schema version=\"1.0\"\n"
                + "           targetNamespace=\"http://some/namespace\"\n"
                + "           xmlns:tns=\"http://some/namespace\"\n"
                + "           xmlns:other=\"http://some/namespace\"\n"
                + "           xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">\n"
                + "  <xs:element name=\"foo\" type=\"xs:string\"/>\n"
                + "</xs:schema>\n";

        File tempFile = File.createTempFile("test-schema-bidir", ".xsd");
        tempFile.deleteOnExit();
        Files.write(tempFile.toPath(), xmlStream.getBytes(java.nio.charset.StandardCharsets.UTF_8));

        // Act
        SimpleNamespaceResolver resolver = new SimpleNamespaceResolver(tempFile);
        Map<String, String> uri2PrefixMap = resolver.getNamespaceURI2PrefixMap();

        // Assert 1: uri2Prefix → prefix2Uri round-trip is consistent.
        // For every (uri → prefix) entry, getNamespaceURI(prefix) must return the same uri.
        for (Map.Entry<String, String> entry : uri2PrefixMap.entrySet()) {
            String uri = entry.getKey();
            String prefix = entry.getValue();
            assertEquals(
                    uri,
                    resolver.getNamespaceURI(prefix),
                    "Round-trip inconsistency: uri2Prefix maps [" + uri + "] to prefix [" + prefix
                            + "], but getNamespaceURI(\"" + prefix + "\") returns a different URI");
        }

        // Assert 2: "other" must not be resolvable as a prefix.
        // Since "tns" is the canonical prefix for the URI, the discarded "other" prefix
        // must not remain in the internal prefix2Uri map.
        assertNull(
                resolver.getNamespaceURI("other"),
                "The non-canonical prefix 'other' should not be resolvable via getNamespaceURI(). "
                        + "When 'tns' is the canonical prefix for a URI, competing prefixes must be "
                        + "removed from prefix2Uri to maintain bidirectional map consistency.");
    }
}
