import se.jguru.nazgul.test.xmlbinding.XmlTestUtils

import java.util.regex.Pattern

final String[] beforeMoveFilenames = ["schema1.xsd", "schema2.xsd", "schema3.xsd"];
final String[] afterMoveFilenames = ["some_schema.xsd", "yet_another_schema.xsd", "schema3.xsd"];

// Validate content
final String targetDirPath = new File(basedir, 'target').getAbsolutePath();
final Pattern TRIVIAL_PATTERN = Pattern.compile("/entityTransporter\\[\\d+\\]/entityClasses\\[\\d+\\]" +
        "(/entityClass\\[\\d+\\](/.*)?)?");
for(int i = 0; i < 3; i++) {

  final leftXML = XmlTestUtils.readFully(targetDirPath + "/generated-resources/schemagen/" + beforeMoveFilenames[i]);
  final rightXML = XmlTestUtils.readFully(targetDirPath + "/classes/expected/" + afterMoveFilenames[i]);
  assert XmlTestUtils.validateIdenticalContent(leftXML, rightXML, )
}
XmlTestUtils.validateIdenticalContent()
def xml = new XmlSlurper().parse(generatedSchema)
assert 1 == xml.complexType.size()
assert 'foo' == xml.complexType[0].@name.text()

final File testSchemagen = new File(basedir, 'target/generated-test-resources/schemagen/')
assert !testSchemagen.exists(), "Found unexpected generated TestResources [" + testSchemagen.getAbsolutePath() + "]";

