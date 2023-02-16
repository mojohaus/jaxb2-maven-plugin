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
import groovy.xml.XmlSlurper

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

def validateSingleComplexType(final String submoduleName, final String expectedName, final int index) {

  final File xsdSchemaFile = new File(basedir, submoduleName + '/target/generated-resources/schemagen/schema1.xsd');
  def xml = new XmlSlurper().parse(xsdSchemaFile);

  // Validate that we only have 1 complexType in the XML read from the XSD File.
  assert 1 == xml.complexType.size(), 'Found ' + xml.complexType.size()  +
          ' generated complex types in the ' + xsdSchemaFile.getAbsolutePath() +
          ' file. (Expected ' + expectedName + ').';
  println "" + index + ". Correctly found [" + expectedName + "] ComplexTypes in [" +
          xsdSchemaFile.getAbsolutePath() + "]";

  // Validate the name of the first/single complex type
  assert expectedName == xml.complexType[0].@name.text();
  println "" + index + ". Correctly expected name  [" + expectedName + "] for first ComplexType in [" +
          xsdSchemaFile.absolutePath + "]";
}

// Validate content for first sub-project
println "\nValidating schema for submodule 'first'"
println "========================================="
validateSingleComplexType('first', 'firstFoo', 1);

println "\nValidating non-existent schema for submodule 'second'"
println "======================================================="
validateNonexistentDirectory(new File(basedir, 'second/target/schemagen-work'), 2);

println "\nValidating schema for submodule 'first'"
println "========================================="
validateSingleComplexType('third', 'thirdFoo', 3);


