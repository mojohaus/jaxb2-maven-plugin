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

// Note: The execution ID is "testSchemagen"
//
def episodeFile = new File(basedir, 'target/test-classes/META-INF/JAXB/episode_testSchemagen.xjb')
def outputDir = new File(basedir, 'target/generated-test-resources/schemagen')
def workDir = new File(basedir, 'target/schemagen-work/test_scope')

// Act: Validate content
def xml = new XmlSlurper().parse(new File(workDir, 'schema1.xsd'));
assert 1 == xml.complexType.size(), 'Found ' + xml.complexType.size() + ' generated complex types in the schema1.xsd file. (Expected 1).';
assert 'foo' == xml.complexType[0].@name.text();

// Assert
println "\nValidating work directory content"
println "==================================="

validateExistingFile(new File(workDir, 'schema1.xsd'), 1);
validateNonexistentFile(new File(workDir, 'META-INF/sun-jaxb.episode'), 2);
validateExistingFile(new File(workDir, 'se/west/gnat/Foo.class'), 3);

println "\nValidating output directory content"
println "====================================="

validateExistingFile(new File(outputDir, 'schema1.xsd'), 1);
validateExistingFile(episodeFile, 2);
validateNonexistentFile(new File(outputDir, 'se/west/gnat/Foo.class'), 3);
validateNonexistentDirectory(new File(basedir, 'target/generated-resources/schemagen/'), 4);
