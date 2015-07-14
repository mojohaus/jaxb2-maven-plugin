/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

// Assemble
def validateExistingFile(final File aFile, final int index) {
  final String path = aFile.getCanonicalPath();
  assert aFile.exists() && aFile.isFile(), "Missing required file [" + path + "]";
  println "" + index + ". Expected file exists correctly. [" + path + "]";
}

def validateNonexistentFile(final File aFile, final int index) {
  final String path = aFile.getCanonicalPath();
  assert !aFile.exists(), "File should not exist: [" + path + "]";
  println "" + index + ". File correctly non-existent. [" + path + "]";
}

def validateNonexistentDirectory(final File aDirectory, final int index) {
  final String path = aDirectory.getCanonicalPath();
  assert !aDirectory.exists(), "Directory should not exist: [" + path + "]";
  println "" + index + ". Directory correctly non-existent. [" + path + "]";
}

/*
+=================== [10 SchemaGen Arguments]
|
| [0]: -encoding
| [1]: UTF-8
| [2]: -d
| [3]: /Users/lj/Development/Projects/MojoHaus/jaxb2-maven-plugin/target/it/schemagen-handles-package-info/target/schemagen-work/compile_scope
| [4]: -classpath
| [5]: /Users/lj/Development/Projects/MojoHaus/jaxb2-maven-plugin/target/it/schemagen-handles-package-info/src/main/java/
| [6]: -episode
| [7]: /Users/lj/Development/Projects/MojoHaus/jaxb2-maven-plugin/target/it/schemagen-handles-package-info/target/generated-resources/schemagen/META-INF/sun-jaxb.episode
| [8]: src/main/java/org/testing/xml/MyType.java
| [9]: src/main/java/org/testing/xml/package-info.java
|
+=================== [End 10 SchemaGen Arguments]

 */

final File outputDir = new File(basedir, 'target/generated-resources/schemagen')
final File transformed = new File(outputDir, 'example.xsd')
final File episodeFile = new File(outputDir, 'META-INF/sun-jaxb.episode')

// Assert: Validate existing files
validateExistingFile(transformed, 1)
validateExistingFile(episodeFile, 2)

// Assert: Validate content
def xml = new XmlSlurper().parse(transformed);
assert 1 == xml.complexType.size(), 'Found ' + xml.complexType.size() +
        ' generated complex types in the example.xsd file. (Expected 1).';
assert 'myType' == xml.complexType[0].@name.text();
println "3. Correctly detected generated ComplexType."

assert 1 == xml.element.size(), 'Found ' + xml.element.size() +
        ' generated elements in the example.xsd file. (Expected 1).';
assert 'myType' == xml.element[0].@name.text();
assert 'ex:myType' == xml.element[0].@type.text();
println "3. Correctly detected generated Element."