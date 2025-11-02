package org.codehaus.mojo.jaxb2.schemageneration.postprocessing.javadoc;

import java.io.File;
import java.net.URL;
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

        assertInstanceOf(FieldLocation.class, stringFieldLocation);
        assertInstanceOf(FieldLocation.class, integerFieldLocation);
        assertInstanceOf(MethodLocation.class, stringMethodLocation);
        assertInstanceOf(MethodLocation.class, integerMethodLocation);

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

        assertInstanceOf(FieldLocation.class, stringFieldLocation2);
        assertInstanceOf(FieldLocation.class, integerFieldLocation2);
        assertInstanceOf(MethodLocation.class, stringMethodLocation2);
        assertInstanceOf(MethodLocation.class, integerMethodLocation2);

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
        assertInstanceOf(PackageLocation.class, packageLocation);

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
        assertInstanceOf(ClassLocation.class, classLocation);

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
        assertInstanceOf(MethodLocation.class, acceptMethodLocation);

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
}
