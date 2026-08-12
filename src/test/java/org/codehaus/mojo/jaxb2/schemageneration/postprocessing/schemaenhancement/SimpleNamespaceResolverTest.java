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

    private static final String STUDENT_NAMESPACE = "http://schemas.acme.com/student";

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
    void validateTnsIsCanonicalPrefixWhenDeclaredBeforeItsAliases() {
        // Assemble
        final SimpleNamespaceResolver unitUnderTest =
                new SimpleNamespaceResolver(getSchemaFile(SCHEMA_DIR + "tnsAliasFirstSchema.xsd"));

        // Act
        final Map<String, String> namespaceURI2PrefixMap = unitUnderTest.getNamespaceURI2PrefixMap();

        // Assert
        assertEquals("tns", namespaceURI2PrefixMap.get(STUDENT_NAMESPACE));
    }

    @Test
    void validateTnsIsCanonicalPrefixWhenDeclaredAfterItsAliases() {
        // Assemble
        final SimpleNamespaceResolver unitUnderTest =
                new SimpleNamespaceResolver(getSchemaFile(SCHEMA_DIR + "tnsAliasLastSchema.xsd"));

        // Act
        final Map<String, String> namespaceURI2PrefixMap = unitUnderTest.getNamespaceURI2PrefixMap();

        // Assert
        assertEquals("tns", namespaceURI2PrefixMap.get(STUDENT_NAMESPACE));
    }

    @Test
    void validateAliasedPrefixesRemainResolvable() {
        // Assemble
        final SimpleNamespaceResolver unitUnderTest =
                new SimpleNamespaceResolver(getSchemaFile(SCHEMA_DIR + "tnsAliasFirstSchema.xsd"));

        // Act & Assert
        // Prefix-to-URI is a many-to-one relation: every prefix the document declares must resolve,
        // even though only one of them is the canonical prefix for the URI.
        assertEquals(STUDENT_NAMESPACE, unitUnderTest.getNamespaceURI("tns"));
        assertEquals(STUDENT_NAMESPACE, unitUnderTest.getNamespaceURI("base"));
        assertEquals(STUDENT_NAMESPACE, unitUnderTest.getNamespaceURI("extra"));
    }

    @Test
    void validateRedundantlyRedeclaredPrefixIsAccepted() {
        // Assemble
        final File schemaFile = getSchemaFile(SCHEMA_DIR + "redeclaredPrefixSchema.xsd");

        // Act
        final SimpleNamespaceResolver unitUnderTest = new SimpleNamespaceResolver(schemaFile);

        // Assert
        // Rebinding a prefix to the URI it is already bound to changes nothing, and is not a conflict.
        assertEquals(STUDENT_NAMESPACE, unitUnderTest.getNamespaceURI("tns"));
        assertEquals("tns", unitUnderTest.getNamespaceURI2PrefixMap().get(STUDENT_NAMESPACE));
    }

    @Test
    void validateExceptionThrownOnCompetingPrefixesForTheSameUri() {
        // Assemble
        final File schemaFile = getSchemaFile(SCHEMA_DIR + "conflictingPrefixSchema.xsd");

        // Act & Assert
        final IllegalStateException exception =
                assertThrows(IllegalStateException.class, () -> new SimpleNamespaceResolver(schemaFile));
        assertTrue(
                exception.getMessage().contains("Replaced prefix"),
                "Expected a 'Replaced prefix' message, but got: " + exception.getMessage());
    }

    @Test
    void validateExceptionThrownOnPrefixReboundToAnotherUri() {
        // Assemble
        final File schemaFile = getSchemaFile(SCHEMA_DIR + "reboundPrefixSchema.xsd");

        // Act & Assert
        final IllegalStateException exception =
                assertThrows(IllegalStateException.class, () -> new SimpleNamespaceResolver(schemaFile));
        assertTrue(
                exception.getMessage().contains("Replaced URI"),
                "Expected a 'Replaced URI' message, but got: " + exception.getMessage());
    }
}
