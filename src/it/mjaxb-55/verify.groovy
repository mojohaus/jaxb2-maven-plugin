import org.xmlunit.builder.DiffBuilder
import org.xmlunit.builder.Input
import org.xmlunit.diff.Diff

String generatedPath = "target/generated-resources/schemagen/"
String[] beforeMoveFilenames = ["schema1.xsd", "schema2.xsd", "schema3.xsd"]
String[] afterMoveFilenames = ["some_schema.xsd", "another_schema.xsd", "yet_another_schema.xsd"]

for ( int i = 0; i < beforeMoveFilenames.length; i++ )
{
    // Check that the files were actually renamed as per the configuration within the pom.
    File beforeMove = new File( basedir, generatedPath + beforeMoveFilenames[i] )
    File afterMove = new File( basedir, generatedPath + afterMoveFilenames[i] )
    assert !beforeMove.exists() : "Rename failed. [" + beforeMove.getAbsolutePath() + "] exists."
    assert afterMove.exists() && afterMove.length() > 0 : "Rename failed. [" + afterMove.getAbsolutePath() + "] does not exist."
}

// Validate the content of the generated schema files
String expectedPath = "target/classes/expected/"
for ( int i = 0; i < afterMoveFilenames.length; i++ )
{
    File expected = new File( basedir, expectedPath + afterMoveFilenames[i] )
    File actual = new File( basedir, generatedPath + afterMoveFilenames[i] )

    Diff diff = DiffBuilder.compare( Input.fromFile( expected ).build() )
            .withTest( Input.fromFile( actual ).build() )
            .ignoreComments()
            .ignoreWhitespace()
            .build()
    assert !diff.hasDifferences() : "Failed Transform: [" + ( i + 1 ) + "/" + afterMoveFilenames.length + "] " +
            actual.getPath() + " differs from " + expected.getPath() + ": " + diff
}
