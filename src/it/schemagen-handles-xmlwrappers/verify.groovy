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
import groovy.util.slurpersupport.*;

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

def validateXmlTextAt(final GPathResult selection, final String attributeName, final String expectedText) {
  selection.
}

final File outputDir = new File(basedir, 'target/generated-resources/schemagen')
final File workDir = new File(basedir, 'target/schemagen-work/compile_scope')

// Act: Validate transformed content
def xml = new XmlSlurper().parse(new File(outputDir, 'schema1.xsd'));
assert 2 == xml.complexType.size(), 'Found ' + xml.complexType.size() + ' generated complex types in the schema1.xsd file. (Expected 2).';

def fieldAccessType = xml.'xs:complexType'.find { it.@name == 'exampleXmlWrapperUsingFieldAccess'}
def methodAccessType = xml.'xs:complexType'.find { it.@name == 'exampleXmlWrapperUsingMethodAccess'}

assert fieldAccessType != null, "Found no FieldAccessType"
assert methodAccessType != null, "Found no MethodAccessType"

println "Got fieldAccessType of type [" + fieldAccessType.getClass().getName() + "]"
println "Got methodAccessType of type [" + methodAccessType.getClass().getName() + "]"

assert 'List containing some strings.' == fieldAccessType.'xs:sequence'.'xs:element'.'xs:annotation'.'xs:documentation'[0].text()

// /xs:schema/xs:complexType/xs:sequence/xs:element/xs:annotation/xs:documentation
// <xs:documentation><![CDATA[List containing some strings.]]></xs:documentation>

// Assert
println "\nValidating work directory content"
println "==================================="

validateExistingFile(new File(workDir, 'schema1.xsd'), 1);
validateNonexistentFile(new File(workDir, 'META-INF/sun-jaxb.episode'), 2);
validateExistingFile(new File(workDir, 'org/codehaus/mojo/jaxb2/schemageneration/postprocessing/javadoc/wrappers/ExampleXmlWrapperUsingFieldAccess.class'), 3);
validateExistingFile(new File(workDir, 'org/codehaus/mojo/jaxb2/schemageneration/postprocessing/javadoc/wrappers/ExampleXmlWrapperUsingMethodAccess.class'), 4);

println "\nValidating output directory content"
println "====================================="

validateExistingFile(new File(outputDir, 'schema1.xsd'), 1);
validateExistingFile(new File(outputDir, 'META-INF/sun-jaxb.episode'), 2);
validateExistingFile(new File(workDir, 'org/codehaus/mojo/jaxb2/schemageneration/postprocessing/javadoc/wrappers/ExampleXmlWrapperUsingFieldAccess.class'), 3);
validateExistingFile(new File(workDir, 'org/codehaus/mojo/jaxb2/schemageneration/postprocessing/javadoc/wrappers/ExampleXmlWrapperUsingMethodAccess.class'), 4);
validateNonexistentDirectory(new File(basedir, 'target/generated-test-resources/schemagen/'), 5);
