import org.xmlunit.builder.DiffBuilder
import org.xmlunit.builder.Input
import org.xmlunit.diff.Diff

import javax.xml.transform.Source

String generatedPath = "target/generated-resources/schemagen/";
String[] beforeMoveFilenames = ["schema1.xsd", "schema2.xsd", "schema3.xsd"]
String[] afterMoveFilenames = ["some_schema.xsd", "yet_another_schema.xsd", "schema3.xsd"]

for (int i = 0; i < beforeMoveFilenames.length; i++) {

    // Check that the files were actually renamed as per the configuration within the pom.
    File beforeMove = new File(basedir, generatedPath + beforeMoveFilenames[i]);
    File afterMove = new File(basedir, generatedPath + afterMoveFilenames[i]);

    if(beforeMove.absolutePath != afterMove.absolutePath) {
        if (beforeMove.exists()) {
            println("Rename failed. [" + beforeMove.getAbsolutePath() + "] exists.");
            return false;
        }

        if (!afterMove.exists() || afterMove.length() == 0) {
            println("Rename failed. [" + afterMove.getAbsolutePath() + "] does not exist.");
            return false;
        }

        println("Successful rename of [" + beforeMove.getName() + "] to [" + afterMove.getName() + "]");

    } else {
        println("Success: Not renaming file [" + beforeMove.getName() + "] as expected.");
    }
}

// Validate the content of the generated schema files
String expectedPath = "target/classes/expected/";
boolean allTransformationsAreCorrect = true;
for (int i = 0; i < afterMoveFilenames.length; i++) {

    // Read the file content.
    File expected = new File(basedir, expectedPath + afterMoveFilenames[i]);
    File actual = new File(basedir, generatedPath + afterMoveFilenames[i]);

    // Normalize the XML
    Source expectedXML = Input.fromFile(expected).build()
    Source actualXML = Input.fromFile(actual).build()

    // Check contents.
    Diff anyDiffs = DiffBuilder.compare(expectedXML)
            .withTest(actualXML)
            .ignoreComments()
            .ignoreWhitespace()
            .build()

    if (!anyDiffs.hasDifferences()) {

        println("[Correct Transform]: [" + (i + 1) + "/" + afterMoveFilenames.length
                + "] for XML Schema file " + expected.getName());

    } else {

        println("Found differences: " + anyDiffs.toString())

        println("[Failed Transform]: [" + (i + 1) + "/" + afterMoveFilenames.length
                + "] for XML Schema file " + actual.getName());
        println("[Failed Transform]: [" + (i + 1) + "/" + afterMoveFilenames.length
                + "] Actual: [" + actual.getPath() + "]");
        println("[Failed Transform]: [" + (i + 1) + "/" + afterMoveFilenames.length
                + "] Expected: [" + expected.getPath() + "]");

        // Mismatch.
        allTransformationsAreCorrect = false;
    }
}

return allTransformationsAreCorrect;