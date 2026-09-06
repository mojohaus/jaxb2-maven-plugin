package org.codehaus.mojo.jaxb2.schemageneration.postprocessing.javadoc;

import java.io.File;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.codehaus.mojo.jaxb2.BufferingLog;
import org.codehaus.mojo.jaxb2.schemageneration.postprocessing.javadoc.location.ClassLocation;
import org.codehaus.mojo.jaxb2.schemageneration.postprocessing.javadoc.location.FieldLocation;
import org.codehaus.mojo.jaxb2.schemageneration.postprocessing.javadoc.location.MethodLocation;
import org.codehaus.mojo.jaxb2.schemageneration.postprocessing.javadoc.location.PackageLocation;
import org.codehaus.mojo.jaxb2.shared.FileSystemUtilities;
import org.codehaus.mojo.jaxb2.shared.Validate;
import org.codehaus.mojo.jaxb2.shared.filters.Filter;
import org.codehaus.mojo.jaxb2.shared.filters.Filters;
import org.codehaus.mojo.jaxb2.shared.filters.pattern.PatternFileFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
class JavaDocExtractorTest {

    // Shared state
    private File javaDocBasicDir;
    private File javaDocAnnotatedDir;
    private File javaDocEnumsDir;
    private File javaDocXmlWrappersDir;
    // Directory holding Java sources with JDK 23 Markdown /// documentation comments (issue #404).
    private File javaDocMarkdownDir;
    private BufferingLog log;

    @BeforeEach
    void setupSharedState() {

        log = new BufferingLog(BufferingLog.LogLevel.DEBUG);

        // Find the desired directory
        final URL dirURL = getClass().getClassLoader().getResource("testdata/schemageneration/javadoc/basic");
        this.javaDocBasicDir = new File(dirURL.getPath());
        assertTrue(javaDocBasicDir.exists() && javaDocBasicDir.isDirectory());

        final URL annotatedDirURL =
                getClass().getClassLoader().getResource("testdata/schemageneration/javadoc/annotated");
        this.javaDocAnnotatedDir = new File(annotatedDirURL.getPath());
        assertTrue(javaDocAnnotatedDir.exists() && javaDocAnnotatedDir.isDirectory());

        final URL enumsDirURL = getClass().getClassLoader().getResource("testdata/schemageneration/javadoc/enums");
        this.javaDocEnumsDir = new File(enumsDirURL.getPath());
        assertTrue(javaDocEnumsDir.exists() && javaDocEnumsDir.isDirectory());

        final URL wrappersDirURL =
                getClass().getClassLoader().getResource("testdata/schemageneration/javadoc/xmlwrappers");
        this.javaDocXmlWrappersDir = new File(wrappersDirURL.getPath());
        assertTrue(javaDocXmlWrappersDir.exists() && javaDocXmlWrappersDir.isDirectory());

        // Set up the directory with JDK 23 Markdown /// documentation comments.
        final URL markdownDirURL =
                getClass().getClassLoader().getResource("testdata/schemageneration/javadoc/markdown");
        this.javaDocMarkdownDir = new File(markdownDirURL.getPath());
        assertTrue(javaDocMarkdownDir.exists() && javaDocMarkdownDir.isDirectory());
    }

    @Test
    void validateLogStatementsDuringProcessing() {

        // Assemble
        final JavaDocExtractor unitUnderTest = new JavaDocExtractor(log);
        final List<File> sourceDirs = Arrays.<File>asList(javaDocBasicDir);
        final List<File> sourceFiles = FileSystemUtilities.resolveRecursively(sourceDirs, null, log);

        // Act
        unitUnderTest.addSourceFiles(sourceFiles);
        final SearchableDocumentation ignoredResult = unitUnderTest.process();

        // Assert
        final SortedMap<String, Throwable> logBuffer = log.getLogBuffer();
        final List<String> keys = new ArrayList<String>(logBuffer.keySet());

        /*
         * 000: (DEBUG) Accepted file [/Users/lj/Development/Projects/Codehaus/github_jaxb2_plugin/target/test-classes/testdata/schemageneration/javadoc/basic/NodeProcessor.java],
         * 001: (INFO) Processing [1] java sources.,
         * 002: (DEBUG) Added package-level JavaDoc for [basic],
         * 003: (DEBUG) Added class-level JavaDoc for [basic.NodeProcessor],
         * 004: (DEBUG) Added method-level JavaDoc for [basic.NodeProcessor#accept(org.w3c.dom.Node)],
         * 005: (DEBUG) Added method-level JavaDoc for [basic.NodeProcessor#process(org.w3c.dom.Node)]]
         */
        assertEquals(6, keys.size());
        assertEquals("001: (INFO) Processing [1] java sources.", keys.get(1));
        assertEquals("002: (DEBUG) Added package-level JavaDoc for [basic]", keys.get(2));
        assertEquals("003: (DEBUG) Added class-level JavaDoc for [basic.NodeProcessor]", keys.get(3));
        assertEquals(
                "004: (DEBUG) Added method-level JavaDoc for [basic.NodeProcessor#accept(org.w3c.dom.Node)]",
                keys.get(4));
        assertEquals(
                "005: (DEBUG) Added method-level JavaDoc for [basic.NodeProcessor#process(org.w3c.dom.Node)]",
                keys.get(5));
    }

    @Test
    @Disabled
    void validateExtractingXmlAnnotatedName() throws Exception {

        // Assemble
        final JavaDocExtractor unitUnderTest = new JavaDocExtractor(log);

        // Act
        final SearchableDocumentation result = getSearchableDocumentationFor(unitUnderTest, 2, javaDocAnnotatedDir);

        // Assert
        final String prefix = "testdata.schemageneration.javadoc.annotated.";
        final String fieldAccessPrefix = prefix + "AnnotatedXmlNameAnnotatedClassWithFieldAccessTypeName#";
        final String methodAccessPrefix = prefix + "AnnotatedXmlNameAnnotatedClassWithMethodAccessTypeName#";

        // First, check the field-annotated class.
        final SortableLocation stringFieldLocation = result.getLocation(fieldAccessPrefix + "annotatedStringField");
        final SortableLocation integerFieldLocation = result.getLocation(fieldAccessPrefix + "annotatedIntegerField");
        final SortableLocation stringMethodLocation = result.getLocation(fieldAccessPrefix + "getStringField()");
        final SortableLocation integerMethodLocation = result.getLocation(fieldAccessPrefix + "getIntegerField()");

        assertTrue(stringFieldLocation instanceof FieldLocation);
        assertTrue(integerFieldLocation instanceof FieldLocation);
        assertTrue(stringMethodLocation instanceof MethodLocation);
        assertTrue(integerMethodLocation instanceof MethodLocation);

        assertNull(stringMethodLocation.getAnnotationRenamedTo());
        assertNull(integerMethodLocation.getAnnotationRenamedTo());
        assertEquals("annotatedStringField", stringFieldLocation.getAnnotationRenamedTo());
        assertEquals("annotatedIntegerField", integerFieldLocation.getAnnotationRenamedTo());

        assertEquals(
                JavaDocData.NO_COMMENT,
                result.getJavaDoc(stringMethodLocation.getPath()).getComment());
        assertEquals(
                JavaDocData.NO_COMMENT,
                result.getJavaDoc(integerMethodLocation.getPath()).getComment());
        assertEquals(
                "This is a string field.",
                result.getJavaDoc(stringFieldLocation.getPath()).getComment());
        assertEquals(
                "This is an integer field.",
                result.getJavaDoc(integerFieldLocation.getPath()).getComment());

        // Secondly, check the method-annotated class.
        final SortableLocation stringFieldLocation2 = result.getLocation(methodAccessPrefix + "stringField");
        final SortableLocation integerFieldLocation2 = result.getLocation(methodAccessPrefix + "integerField");
        final SortableLocation stringMethodLocation2 =
                result.getLocation(methodAccessPrefix + "annotatedStringMethod()");
        final SortableLocation integerMethodLocation2 =
                result.getLocation(methodAccessPrefix + "annotatedIntegerMethod()");

        assertTrue(stringFieldLocation2 instanceof FieldLocation);
        assertTrue(integerFieldLocation2 instanceof FieldLocation);
        assertTrue(stringMethodLocation2 instanceof MethodLocation);
        assertTrue(integerMethodLocation2 instanceof MethodLocation);

        assertNull(stringFieldLocation2.getAnnotationRenamedTo());
        assertNull(integerFieldLocation2.getAnnotationRenamedTo());
        assertEquals("annotatedStringMethod", stringMethodLocation2.getAnnotationRenamedTo());
        assertEquals("annotatedIntegerMethod", integerMethodLocation2.getAnnotationRenamedTo());

        assertEquals(
                "Getter for the stringField.",
                result.getJavaDoc(stringMethodLocation2.getPath()).getComment());
        assertEquals(
                "Getter for the integerField.",
                result.getJavaDoc(integerMethodLocation2.getPath()).getComment());
        assertEquals(
                JavaDocData.NO_COMMENT,
                result.getJavaDoc(stringFieldLocation2.getPath()).getComment());
        assertEquals(
                JavaDocData.NO_COMMENT,
                result.getJavaDoc(integerFieldLocation2.getPath()).getComment());
    }

    @Test
    @Disabled
    void validateJavaDocsForXmlEnumsAreCorrectlyApplied() {

        // Assemble
        final JavaDocExtractor unitUnderTest = new JavaDocExtractor(log);

        // Act
        final SearchableDocumentation result = getSearchableDocumentationFor(unitUnderTest, 3, javaDocEnumsDir);
        final MapWrapper mapWrapper = new MapWrapper(result);

        // Assert
        assertEquals(21, mapWrapper.sortableLocations2JavaDocDataMap.size());

        final List<String> paths = Arrays.asList(
                "enums",
                "enums.AmericanCoin",
                "enums.AmericanCoin#1",
                "enums.AmericanCoin#5",
                "enums.AmericanCoin#10",
                "enums.AmericanCoin#25",
                "enums.AmericanCoin#getValue()",
                "enums.AmericanCoin#value",
                "enums.ExampleEnumHolder",
                "enums.ExampleEnumHolder#coins",
                "enums.ExampleEnumHolder#foodPreferences",
                "enums.ExampleEnumHolder#getCoins()",
                "enums.ExampleEnumHolder#getFoodPreferences()",
                "enums.FoodPreference",
                "enums.FoodPreference#LACTO_VEGETARIAN",
                "enums.FoodPreference#NONE",
                "enums.FoodPreference#VEGAN",
                "enums.FoodPreference#isMeatEater()",
                "enums.FoodPreference#isMilkDrinker()",
                "enums.FoodPreference#meatEater",
                "enums.FoodPreference#milkDrinker");
        for (String current : paths) {
            assertTrue(
                    mapWrapper.path2LocationMap.keySet().contains(current.trim()),
                    "Required path [" + current + "] not found.");
        }

        // Finally, validate that the injected XML document comments
        // match the expected/corresponding JavaDoc comments.
        mapWrapper.validateJavaDocCommentText(
                "Simple enumeration example defining some Food preferences.", "enums.FoodPreference");

        mapWrapper.validateJavaDocCommentText(
                "No special food preferences; eats everything.", "enums.FoodPreference#NONE");

        mapWrapper.validateJavaDocCommentText(
                "Vegan who will neither eat meats nor drink milk.", "enums.FoodPreference#VEGAN");

        mapWrapper.validateJavaDocCommentText(
                "Vegetarian who will not eat meats, but drinks milk.", "enums.FoodPreference#LACTO_VEGETARIAN");

        mapWrapper.validateJavaDocCommentText("A Penny, worth 1 cent.", "enums.AmericanCoin#1");

        mapWrapper.validateJavaDocCommentText("A Nickel, worth 5 cents.", "enums.AmericanCoin#5");

        mapWrapper.validateJavaDocCommentText("A Dime, worth 10 cents.", "enums.AmericanCoin#10");

        mapWrapper.validateJavaDocCommentText("A Quarter, worth 25 cents.", "enums.AmericanCoin#25");
    }

    @Test
    @Disabled
    void validateJavaDocsForXmlWrapperAnnotatedFieldsAndMethodsAreCorrectlyApplied() throws Exception {

        // Assemble
        final JavaDocExtractor unitUnderTest = new JavaDocExtractor(log);

        // Act
        final SearchableDocumentation result = getSearchableDocumentationFor(unitUnderTest, 2, javaDocXmlWrappersDir);
        final MapWrapper mapWrapper = new MapWrapper(result);

        // Assert
        assertEquals(11, mapWrapper.sortableLocations2JavaDocDataMap.size());

        final String packagePrefix = "org.codehaus.mojo.jaxb2.schemageneration.postprocessing.javadoc.wrappers";
        final List<String> paths = new ArrayList<String>();
        for (String current : Arrays.asList(
                "",
                ".ExampleXmlWrapperUsingFieldAccess",
                ".ExampleXmlWrapperUsingFieldAccess#foobar",
                ".ExampleXmlWrapperUsingFieldAccess#getIntegerSet()",
                ".ExampleXmlWrapperUsingFieldAccess#getStrings()",
                ".ExampleXmlWrapperUsingFieldAccess#integerSet",
                ".ExampleXmlWrapperUsingMethodAccess",
                ".ExampleXmlWrapperUsingMethodAccess#foobar()",
                ".ExampleXmlWrapperUsingMethodAccess#getMethodIntegerSet()",
                ".ExampleXmlWrapperUsingMethodAccess#methodIntegerSet",
                ".ExampleXmlWrapperUsingMethodAccess#methodStrings")) {
            paths.add(packagePrefix + current);
        }

        for (String current : paths) {
            assertTrue(
                    mapWrapper.path2LocationMap.keySet().contains(current.trim()),
                    "Required path [" + current + "] not found.");
        }

        mapWrapper.validateJavaDocCommentText(
                "List containing some strings.", packagePrefix + ".ExampleXmlWrapperUsingFieldAccess#foobar");

        mapWrapper.validateJavaDocCommentText(
                "SortedSet containing Integers.", packagePrefix + ".ExampleXmlWrapperUsingFieldAccess#integerSet");

        mapWrapper.validateJavaDocCommentText(
                "List containing some methodStrings.", packagePrefix + ".ExampleXmlWrapperUsingMethodAccess#foobar()");

        mapWrapper.validateJavaDocCommentText(
                "SortedSet containing Integers.",
                packagePrefix + ".ExampleXmlWrapperUsingMethodAccess#getMethodIntegerSet()");
    }

    @Test
    void validatePathsFromProcessing() {

        // Assemble
        final JavaDocExtractor unitUnderTest = new JavaDocExtractor(log);

        // Act
        final SearchableDocumentation result = getSearchableDocumentationFor(unitUnderTest, 1, javaDocBasicDir);

        // Assert
        final ArrayList<SortableLocation> sortableLocations =
                new ArrayList<SortableLocation>(result.getAll().keySet());
        assertEquals(4, sortableLocations.size());

        final List<String> paths = new ArrayList<String>(result.getPaths());
        assertEquals(4, paths.size());
        assertEquals("basic", paths.get(0));
        assertEquals("basic.NodeProcessor", paths.get(1));
        assertEquals("basic.NodeProcessor#accept(org.w3c.dom.Node)", paths.get(2));
        assertEquals("basic.NodeProcessor#process(org.w3c.dom.Node)", paths.get(3));
    }

    @Test
    void validateJavaDocDataFromProcessing() {

        // Assemble
        final String basicPackagePath = "basic";
        final String nodeProcessorClassPath = "basic.NodeProcessor";
        final String acceptMethodPath = "basic.NodeProcessor#accept(org.w3c.dom.Node)";
        final String processMethodPath = "basic.NodeProcessor#process(org.w3c.dom.Node)";

        final JavaDocExtractor unitUnderTest = new JavaDocExtractor(log);
        final List<File> sourceDirs = Collections.<File>singletonList(javaDocBasicDir);
        final List<File> sourceFiles = FileSystemUtilities.resolveRecursively(sourceDirs, null, log);

        // Act
        unitUnderTest.addSourceFiles(sourceFiles);
        final SearchableDocumentation result = unitUnderTest.process();

        // Assert
        /*
        +=================
        | Comment:
        | No JavaDoc tags.
        +=================
        */
        final SortableLocation packageLocation = result.getLocation(basicPackagePath);
        final JavaDocData basicPackageJavaDoc = result.getJavaDoc(basicPackagePath);
        assertTrue(packageLocation instanceof PackageLocation);

        final PackageLocation castPackageLocation = (PackageLocation) packageLocation;
        assertEquals("basic", castPackageLocation.getPackageName());
        assertEquals(JavaDocData.NO_COMMENT, basicPackageJavaDoc.getComment());
        assertEquals(0, basicPackageJavaDoc.getTag2ValueMap().size());

        /*
        +=================
        | Comment: Processor/visitor pattern specification for DOM Nodes.
        | 2 JavaDoc tags ...
        | author: <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, Mr. Foo
        | see: org.w3c.dom.Node
        +=================
        */
        final SortableLocation classLocation = result.getLocation(nodeProcessorClassPath);
        final JavaDocData nodeProcessorClassJavaDoc = result.getJavaDoc(nodeProcessorClassPath);
        assertTrue(classLocation instanceof ClassLocation);

        final ClassLocation castClassLocation = (ClassLocation) classLocation;
        assertEquals("basic", castClassLocation.getPackageName());
        assertEquals("NodeProcessor", castClassLocation.getClassName());
        assertEquals("Processor/visitor pattern specification for DOM Nodes.", nodeProcessorClassJavaDoc.getComment());

        final SortedMap<String, String> classTag2ValueMap = nodeProcessorClassJavaDoc.getTag2ValueMap();
        assertEquals(2, classTag2ValueMap.size());
        assertEquals("org.w3c.dom.Node", classTag2ValueMap.get("see"));
        assertEquals(
                "<a href=\"mailto:lj@jguru.se\">Lennart J&ouml;relid</a>, Mr. Foo", classTag2ValueMap.get("author"));

        /*
        +=================
        | Comment: Defines if this visitor should process the provided node.
        | 2 JavaDoc tags ...
        | param: aNode The DOM node to process.
        | return: <code>true</code> if the provided Node should be processed by this NodeProcessor.
        +=================
        */
        final SortableLocation acceptMethodLocation = result.getLocation(acceptMethodPath);
        final JavaDocData acceptMethodClassJavaDoc = result.getJavaDoc(acceptMethodPath);
        assertTrue(acceptMethodLocation instanceof MethodLocation);

        final MethodLocation castMethodLocation = (MethodLocation) acceptMethodLocation;
        assertEquals("basic", castMethodLocation.getPackageName());
        assertEquals("NodeProcessor", castMethodLocation.getClassName());
        assertEquals("(org.w3c.dom.Node)", castMethodLocation.getParametersAsString());
        assertEquals(
                "Defines if this visitor should process the provided node.", acceptMethodClassJavaDoc.getComment());

        final SortedMap<String, String> methodTag2ValueMap = acceptMethodClassJavaDoc.getTag2ValueMap();
        assertEquals(2, methodTag2ValueMap.size());
        assertEquals("aNode The DOM node to process.", methodTag2ValueMap.get("param"));
        assertEquals(
                "<code>true</code> if the provided Node should be processed by this NodeProcessor.",
                methodTag2ValueMap.get("return"));
    }

    @Test
    void validateXmlTypeConstantName(@TempDir File tempDir) throws Exception {
        // Assemble
        final String javaCode = "package com.example;\n"
                + "import jakarta.xml.bind.annotation.XmlType;\n"
                + "/**\n"
                + " * Class documentation.\n"
                + " */\n"
                + "@XmlType(name = MyAnnotatedClass.CUSTOM_NAME)\n"
                + "public class MyAnnotatedClass {\n"
                + "    public static final String CUSTOM_NAME = \"MyCustomXmlType\";\n"
                + "}\n";
        File javaFile = new File(tempDir, "MyAnnotatedClass.java");
        Files.write(javaFile.toPath(), javaCode.getBytes(StandardCharsets.UTF_8));

        JavaDocExtractor extractor = new JavaDocExtractor(log);
        extractor.setEncoding("UTF-8");
        extractor.addSourceFiles(Collections.singletonList(javaFile));

        // Act
        SearchableDocumentation result = extractor.process();

        // Assert
        ClassLocation loc = result.getLocation("com.example.MyCustomXmlType");
        assertNotNull(loc);
        assertEquals("MyCustomXmlType", loc.getClassName());
        assertEquals("MyCustomXmlType", loc.getAnnotationRenamedTo());
    }

    @Test
    void validateXmlTypeConstantFromExternalClass(@TempDir File tempDir) throws Exception {
        // Assemble
        final String constCode = "package com.example;\n"
                + "public class Constants {\n"
                + "    public static final String EXTERNAL_NAME = \"ExternalXmlType\";\n"
                + "}\n";
        final String classCode = "package com.example;\n"
                + "import jakarta.xml.bind.annotation.XmlType;\n"
                + "/**\n"
                + " * Class documentation.\n"
                + " */\n"
                + "@XmlType(name = Constants.EXTERNAL_NAME)\n"
                + "public class MyClass {\n"
                + "}\n";
        File constFile = new File(tempDir, "Constants.java");
        File classFile = new File(tempDir, "MyClass.java");
        Files.write(constFile.toPath(), constCode.getBytes(StandardCharsets.UTF_8));
        Files.write(classFile.toPath(), classCode.getBytes(StandardCharsets.UTF_8));

        JavaDocExtractor extractor = new JavaDocExtractor(log);
        extractor.setEncoding("UTF-8");
        extractor.addSourceFiles(Arrays.asList(constFile, classFile));

        // Act
        SearchableDocumentation result = extractor.process();

        // Assert
        ClassLocation loc = result.getLocation("com.example.ExternalXmlType");
        assertNotNull(loc);
        assertEquals("ExternalXmlType", loc.getClassName());
        assertEquals("ExternalXmlType", loc.getAnnotationRenamedTo());
    }

    @Test
    void validateXmlElementAndAttributeConstantName(@TempDir File tempDir) throws Exception {
        // Assemble
        final String javaCode = "package com.example;\n"
                + "import jakarta.xml.bind.annotation.XmlType;\n"
                + "import jakarta.xml.bind.annotation.XmlElement;\n"
                + "import jakarta.xml.bind.annotation.XmlAttribute;\n"
                + "@XmlType\n"
                + "public class MyElementClass {\n"
                + "    public static final String ELEM_NAME = \"customElement\";\n"
                + "    public static final String ATTR_NAME = \"customAttribute\";\n"
                + "    @XmlElement(name = ELEM_NAME)\n"
                + "    private String aField;\n"
                + "    @XmlAttribute(name = ATTR_NAME)\n"
                + "    private int anAttr;\n"
                + "}\n";
        File javaFile = new File(tempDir, "MyElementClass.java");
        Files.write(javaFile.toPath(), javaCode.getBytes(StandardCharsets.UTF_8));

        JavaDocExtractor extractor = new JavaDocExtractor(log);
        extractor.setEncoding("UTF-8");
        extractor.addSourceFiles(Collections.singletonList(javaFile));

        // Act
        SearchableDocumentation result = extractor.process();

        // Assert
        FieldLocation elemLoc = result.getLocation("com.example.MyElementClass#customElement");
        assertNotNull(elemLoc, "Should find field by customElement name");
        assertEquals("customElement", elemLoc.getAnnotationRenamedTo());

        FieldLocation attrLoc = result.getLocation("com.example.MyElementClass#customAttribute");
        assertNotNull(attrLoc, "Should find field by customAttribute name");
        assertEquals("customAttribute", attrLoc.getAnnotationRenamedTo());
    }

    @Test
    void validateConcatenatedConstantName(@TempDir File tempDir) throws Exception {
        // Assemble
        final String javaCode = "package com.example;\n"
                + "import jakarta.xml.bind.annotation.XmlType;\n"
                + "@XmlType(name = \"Prefix_\" + MyConcatClass.SUFFIX)\n"
                + "public class MyConcatClass {\n"
                + "    public static final String SUFFIX = \"CustomSuffix\";\n"
                + "}\n";
        File javaFile = new File(tempDir, "MyConcatClass.java");
        Files.write(javaFile.toPath(), javaCode.getBytes(StandardCharsets.UTF_8));

        JavaDocExtractor extractor = new JavaDocExtractor(log);
        extractor.setEncoding("UTF-8");
        extractor.addSourceFiles(Collections.singletonList(javaFile));

        // Act
        SearchableDocumentation result = extractor.process();

        // Assert
        ClassLocation loc = result.getLocation("com.example.Prefix_CustomSuffix");
        assertNotNull(loc);
        assertEquals("Prefix_CustomSuffix", loc.getClassName());
        assertEquals("Prefix_CustomSuffix", loc.getAnnotationRenamedTo());
    }

    //
    // Private helpers
    //

    /**
     * Simple helper class wrapping the path2LocationMap and the sortableLocations2JavaDocDataMap.
     */
    class MapWrapper {

        SortedMap<String, SortableLocation> path2LocationMap;
        SortedMap<SortableLocation, JavaDocData> sortableLocations2JavaDocDataMap;

        /**
         * Creates a MapWrapper using the data retrieved from a SearchableDocumentation
         *
         * @param searchableDocumentation A non-null SearchableDocumentation instance.
         */
        public MapWrapper(final SearchableDocumentation searchableDocumentation) {

            // Check sanity
            Validate.notNull(searchableDocumentation, "searchableDocumentation");

            // Assign state
            this.sortableLocations2JavaDocDataMap = searchableDocumentation.getAll();
            this.path2LocationMap = new TreeMap<String, SortableLocation>();

            for (Map.Entry<SortableLocation, JavaDocData> current : sortableLocations2JavaDocDataMap.entrySet()) {
                path2LocationMap.put(current.getKey().getPath(), current.getKey());
            }
        }

        /**
         * Validates that the JavaDoc found at the supplied SortableLocation path equals the expected value.
         *
         * @param expected The expected JavaDoc comment text.
         * @param path     The SortableLocation path where the text was expected.
         * @see SortableLocation#getPath()
         */
        public void validateJavaDocCommentText(final String expected, final String path) {

            final SortableLocation sortableLocation = path2LocationMap.get(path);
            final JavaDocData xmlWrapperJavaDocData = sortableLocations2JavaDocDataMap.get(sortableLocation);

            // All Done.
            assertEquals(expected, xmlWrapperJavaDocData.getComment());
        }
    }

    private void validateJavaDocCommentText(final MapWrapper wrapper, final String expected, final String path) {

        final SortableLocation sortableLocation = wrapper.path2LocationMap.get(path);
        final JavaDocData xmlWrapperJavaDocData = wrapper.sortableLocations2JavaDocDataMap.get(sortableLocation);

        // All Done.
        assertEquals(expected, xmlWrapperJavaDocData.getComment());
    }

    private SearchableDocumentation getSearchableDocumentationFor(
            final JavaDocExtractor unitUnderTest,
            final int expectedNumberOfFiles,
            final File... sourceFileDirectories) {

        // Ensure that the encoding is correctly set
        unitUnderTest.setEncoding("UTF-8");

        // Convert the supplied directory Files to a List
        final List<File> sourceDirs = new ArrayList<File>();
        Collections.addAll(sourceDirs, sourceFileDirectories);

        // Exclude any ".xsd" files found within the source directory files given
        final List<Filter<File>> excludeFilesMatching = new ArrayList<Filter<File>>();
        excludeFilesMatching.add(new PatternFileFilter(Collections.singletonList("\\.xsd")));
        Filters.initialize(log, excludeFilesMatching);

        // Find all normal Files not being ".xsd" files below the supplied sourceDirs
        final List<File> sourceFiles = FileSystemUtilities.resolveRecursively(sourceDirs, excludeFilesMatching, log);
        assertEquals(expectedNumberOfFiles, sourceFiles.size());

        // Add the found files as source files
        unitUnderTest.addSourceFiles(sourceFiles);

        // Launch the JavaDocExtractor and find
        // the resulting SearchableDocumentation.
        return unitUnderTest.process();
    }

    // ========================================================================================
    // Tests for JDK 23 Markdown /// documentation comment support (issue #404)
    // ========================================================================================

    /**
     * Verifies that a single-line Markdown /// comment is converted to a one-line Javadoc block.
     *
     * <p>Input:  {@code "    /// Controls whether the feature is enabled."}</p>
     * <p>Expected output is a {@code /** ... *&#47;} block with the same content.</p>
     */
    @Test
    void convertMarkdownComments_singleLine() {

        // Assemble: a class with a single /// line above a field
        final String source = "public class Foo {\n"
                + "    /// Controls whether the feature is enabled.\n"
                + "    private boolean enabled;\n"
                + "}\n";

        // Act
        final String converted = JavaDocExtractor.convertMarkdownCommentsToJavadoc(source);

        // Assert: the /// line must become a proper /** ... */ block
        assertTrue(converted.contains("/**"), "Expected opening Javadoc delimiter '/**'");
        assertTrue(
                converted.contains("* Controls whether the feature is enabled."),
                "Expected comment body line with ' * <content>'");
        assertTrue(converted.contains("*/"), "Expected closing Javadoc delimiter '*/'");
        // The /// marker itself must not appear in the output
        assertFalse(converted.contains("///"), "Unexpected '///' in converted output");
    }

    /**
     * Verifies that multi-line consecutive Markdown /// comments are merged into a single Javadoc block.
     *
     * <p>JEP 467 specifies that consecutive {@code ///} lines form one documentation comment,
     * analogous to how consecutive lines inside a {@code /** ... *&#47;} block form one comment.</p>
     */
    @Test
    void convertMarkdownComments_multiLine() {

        // Assemble: three consecutive /// lines
        final String source = "public class Foo {\n"
                + "    /// First line.\n"
                + "    ///\n"
                + "    /// Third line after blank.\n"
                + "    private int count;\n"
                + "}\n";

        // Act
        final String converted = JavaDocExtractor.convertMarkdownCommentsToJavadoc(source);

        // Assert: all three lines are inside one /** ... */ block
        assertTrue(converted.contains("/**"), "Expected opening Javadoc delimiter '/**'");
        assertTrue(converted.contains("* First line."), "Expected first content line");
        // The blank /// becomes a blank body line " * "
        assertTrue(converted.contains("* Third line after blank."), "Expected third content line");
        assertTrue(converted.contains("*/"), "Expected closing Javadoc delimiter '*/'");
        assertFalse(converted.contains("///"), "Unexpected '///' in converted output");
        // Verify only one Javadoc opening — three /// lines produce a single /** block
        assertEquals(
                1,
                countOccurrences(converted, "/**"),
                "Three consecutive /// lines should produce exactly one /** block");
    }

    /**
     * Verifies that two separate groups of /// lines each produce their own Javadoc block.
     *
     * <p>When /// comment runs are separated by non-/// lines they form independent doc comments,
     * just as separate {@code /** ... *&#47;} blocks do in traditional Javadoc.</p>
     */
    @Test
    void convertMarkdownComments_separateGroups() {

        // Assemble: two independent comment groups separated by a field declaration
        final String source = "public class Foo {\n"
                + "    /// First field comment.\n"
                + "    private boolean enabled;\n"
                + "\n"
                + "    /// Second field comment.\n"
                + "    private String name;\n"
                + "}\n";

        // Act
        final String converted = JavaDocExtractor.convertMarkdownCommentsToJavadoc(source);

        // Assert: two separate /** ... */ blocks
        assertEquals(
                2,
                countOccurrences(converted, "/**"),
                "Two separate /// groups should each become their own /** block");
        assertTrue(converted.contains("* First field comment."), "Expected first comment body");
        assertTrue(converted.contains("* Second field comment."), "Expected second comment body");
        assertFalse(converted.contains("///"), "Unexpected '///' in converted output");
    }

    /**
     * Verifies that files without any /// comment lines are returned unmodified.
     *
     * <p>This is a fast path — the implementation short-circuits on {@code !source.contains("///")}
     * so files using only traditional Javadoc are never passed through the conversion logic.</p>
     */
    @Test
    void convertMarkdownComments_noMarkdownLines_returnsUnchanged() {

        // Assemble: a file with only traditional Javadoc
        final String source = "/**\n" + " * Traditional Javadoc.\n" + " */\n" + "public class Foo {}\n";

        // Act
        final String converted = JavaDocExtractor.convertMarkdownCommentsToJavadoc(source);

        // Assert: source is returned unchanged when no /// markers are present
        assertEquals(source, converted, "Source without /// lines must be returned unchanged");
    }

    /**
     * Integration test: verifies that {@link JavaDocExtractor} correctly extracts documentation
     * from a Java source file that uses JDK 23 Markdown {@code ///} documentation comments.
     *
     * <p>The test data file {@code MarkdownDocBean.java} in the {@code markdown} test resource
     * directory uses {@code ///} comments on the class and three fields. After processing, the
     * extractor must expose JavaDocData for each documented element.</p>
     */
    @Test
    void extractJavaDocFromMarkdownComments() {

        // Assemble
        final JavaDocExtractor unitUnderTest = new JavaDocExtractor(log);
        unitUnderTest.setEncoding("UTF-8");

        final List<File> sourceDirs = Arrays.<File>asList(javaDocMarkdownDir);
        final List<File> sourceFiles = FileSystemUtilities.resolveRecursively(sourceDirs, null, log);

        // Act
        unitUnderTest.addSourceFiles(sourceFiles);
        final SearchableDocumentation result = unitUnderTest.process();

        // Assert: the class-level comment must be captured
        final SortedMap<SortableLocation, JavaDocData> allDocs = result.getAll();
        assertFalse(allDocs.isEmpty(), "Expected at least one documented element");

        // Find class-level documentation for markdown.MarkdownDocBean
        boolean foundClassDoc = false;
        boolean foundEnabledFieldDoc = false;
        boolean foundNameFieldDoc = false;
        boolean foundMaxRetriesFieldDoc = false;

        for (Map.Entry<SortableLocation, JavaDocData> entry : allDocs.entrySet()) {
            final String text = entry.getValue().getComment();
            if (text == null) {
                continue;
            }
            if (text.contains("A simple configuration bean")) {
                foundClassDoc = true;
            }
            if (text.contains("Controls whether the feature is enabled")) {
                foundEnabledFieldDoc = true;
            }
            if (text.contains("The display name shown to the user")) {
                foundNameFieldDoc = true;
            }
            if (text.contains("The maximum retry count")) {
                foundMaxRetriesFieldDoc = true;
            }
        }

        assertTrue(foundClassDoc, "Expected class-level Markdown /// comment to be extracted as JavaDoc");
        assertTrue(foundEnabledFieldDoc, "Expected 'enabled' field Markdown /// comment to be extracted as JavaDoc");
        assertTrue(foundNameFieldDoc, "Expected 'name' field Markdown /// comment to be extracted as JavaDoc");
        assertTrue(
                foundMaxRetriesFieldDoc, "Expected 'maxRetries' field Markdown /// comment to be extracted as JavaDoc");
    }

    /**
     * Counts the number of non-overlapping occurrences of {@code needle} in {@code haystack}.
     *
     * @param haystack the string to search in
     * @param needle   the substring to count
     * @return the number of occurrences
     */
    private static int countOccurrences(final String haystack, final String needle) {
        int count = 0;
        int index = 0;
        while ((index = haystack.indexOf(needle, index)) != -1) {
            count++;
            index += needle.length();
        }
        return count;
    }
}
