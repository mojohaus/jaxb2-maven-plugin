package org.codehaus.mojo.jaxb2.javageneration;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

import com.sun.tools.xjc.Driver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test verifying that XJC resolves linked schemas and bindings when located in paths
 * containing non-ASCII characters (such as æøå) and spaces.
 */
class XjcNonAsciiPathTest {

    @Test
    void testLinkedSchemaAndBindingInNonAsciiPath(@TempDir Path tempDir) throws Exception {
        // Given
        final Path nonAsciiDir = tempDir.resolve("proj with spaces and æøå test");
        Files.createDirectories(nonAsciiDir);

        final Path outDir = nonAsciiDir.resolve("target");
        Files.createDirectories(outDir);

        final Path episodeFile = outDir.resolve("sun-jaxb.episode");

        final Path subXsd = nonAsciiDir.resolve("sub.xsd");
        Files.writeString(
                subXsd,
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                        + "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" targetNamespace=\"urn:test\" xmlns:tns=\"urn:test\" elementFormDefault=\"qualified\">\n"
                        + "  <xs:complexType name=\"SubType\">\n"
                        + "    <xs:sequence>\n"
                        + "      <xs:element name=\"subField\" type=\"xs:string\"/>\n"
                        + "    </xs:sequence>\n"
                        + "  </xs:complexType>\n"
                        + "</xs:schema>");

        final Path mainXsd = nonAsciiDir.resolve("main.xsd");
        Files.writeString(
                mainXsd,
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                        + "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" targetNamespace=\"urn:test\" xmlns:tns=\"urn:test\" elementFormDefault=\"qualified\">\n"
                        + "  <xs:include schemaLocation=\"sub.xsd\"/>\n"
                        + "  <xs:element name=\"mainElem\" type=\"tns:SubType\"/>\n"
                        + "</xs:schema>");

        final Path bindingXjb = nonAsciiDir.resolve("binding.xjb");
        Files.writeString(
                bindingXjb,
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                        + "<jaxb:bindings xmlns:jaxb=\"https://jakarta.ee/xml/ns/jaxb\" jaxb:version=\"3.0\"\n"
                        + "               xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"\n"
                        + "               schemaLocation=\"main.xsd\" node=\"/xs:schema\">\n"
                        + "    <jaxb:schemaBindings>\n"
                        + "        <jaxb:package name=\"com.example.nonascii\"/>\n"
                        + "    </jaxb:schemaBindings>\n"
                        + "</jaxb:bindings>");

        // 1. Verifying the bug: passing native File path causes Xerces to fail schema resolution
        final ByteArrayOutputStream outStreamFail = new ByteArrayOutputStream();
        final ByteArrayOutputStream errStreamFail = new ByteArrayOutputStream();
        final String[] argsFail = new String[] {
            "-d",
            outDir.toString(),
            "-b",
            bindingXjb.toFile().getPath(),
            mainXsd.toFile().getPath()
        };
        final int resultFail = Driver.run(argsFail, new PrintStream(outStreamFail), new PrintStream(errStreamFail));
        assertEquals(-1, resultFail, "Native File path should fail Xerces schema resolution due to non-ASCII chars");
        assertTrue(
                errStreamFail.toString().contains("Failed to read schema document 'sub.xsd'"),
                "Expected Xerces error about failing to read schema document 'sub.xsd'");

        // 2. Verifying the fix: passing percent-encoded ASCII URIs resolves cleanly
        final ByteArrayOutputStream outStreamPass = new ByteArrayOutputStream();
        final ByteArrayOutputStream errStreamPass = new ByteArrayOutputStream();
        final String[] argsPass = new String[] {
            "-extension",
            "-d",
            outDir.toString(),
            "-episode",
            episodeFile.toString(),
            "-b",
            bindingXjb.toFile().toURI().toASCIIString(),
            mainXsd.toFile().toURI().toASCIIString()
        };
        final int resultPass = Driver.run(argsPass, new PrintStream(outStreamPass), new PrintStream(errStreamPass));
        assertEquals(0, resultPass, "Percent-encoded ASCII URI should succeed in XJC: " + errStreamPass);
        assertFalse(
                errStreamPass.toString().contains("Failed to read schema document 'sub.xsd'"),
                "Should not contain schema resolution error");
        assertTrue(
                Files.exists(outDir.resolve("com/example/nonascii/SubType.java")),
                "Expected SubType.java to be generated");
        assertTrue(
                Files.exists(outDir.resolve("com/example/nonascii/ObjectFactory.java")),
                "Expected ObjectFactory.java to be generated");
        assertTrue(Files.exists(episodeFile), "Expected episode file to be generated");
    }
}
