/*
target/it/mjaxb-37/target/classes/com/example/myschema/AddressType.class
target/it/mjaxb-37/target/classes/com/example/myschema/ObjectFactory.class
target/it/mjaxb-37/target/classes/foo/bar/Main.class
target/it/mjaxb-37/target/classes/xsd/address.xsd

target/it/mjaxb-37/target/generated-sources/jaxb/META-INF/sun-jaxb.episode
target/it/mjaxb-37/target/generated-sources/jaxb/com/example/myschema/AddressType.java
target/it/mjaxb-37/target/generated-sources/jaxb/com/example/myschema/ObjectFactory.java
target/it/mjaxb-37/target/jaxb2/.xjc-xjcStaleFlag
 */

// Assemble
File generatedSourcesDir = new File(basedir, "target/generated-sources")
File classesDir = new File(basedir, "target/classes")
File episodeFile = new File(classesDir, 'META-INF/JAXB/episode_xjc.xjb')

File addressTypeSourceFile = new File(generatedSourcesDir, 'jaxb/com/example/myschema/AddressType.java')
List<String> addressTypeSource = addressTypeSourceFile.readLines();

String expectedPrimitiveBooleanLine = "protected boolean primary;";
String expectedBooleanClassLine = "protected Boolean expensive;";
String expectedBooleanClassAnnotationLine = "@XmlElement(required = true, type = Boolean.class, nillable = true)";

boolean primitiveBooleanPrimaryFound = false;
boolean classBooleanExpensiveFound = false;
boolean expensiveAnnotationLineFound = false;

// Act
for(line in addressTypeSource) {

  String trimmedLine = line.trim()
  if (trimmedLine.isEmpty()) {
    continue
  };

  if(trimmedLine.equals(expectedPrimitiveBooleanLine)) {
    primitiveBooleanPrimaryFound = true;
  }

  if(trimmedLine.equals(expectedBooleanClassLine)) {
    classBooleanExpensiveFound = true;
  }

  if(trimmedLine.equals(expectedBooleanClassAnnotationLine)) {
    expensiveAnnotationLineFound = true;
  }
}

// Assert
def missingRequired(String trimmedLine) {
  return "Missing required line: [" + trimmedLine + "]" ;
}

assert primitiveBooleanPrimaryFound, missingRequired(expectedPrimitiveBooleanLine);
assert classBooleanExpensiveFound, missingRequired(expectedBooleanClassLine);
assert expensiveAnnotationLineFound, missingRequired(expectedBooleanClassAnnotationLine);
assert episodeFile.exists() && episodeFile.isFile(), "Episode file [" + episodeFile.getPath() + "] not found"