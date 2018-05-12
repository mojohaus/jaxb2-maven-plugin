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

def outputDir = new File(basedir, 'target/generated-resources/schemagen')
def workDir = new File(basedir, 'target/schemagen-work/compile_scope')
def episodeFile = new File(basedir, 'target/classes/META-INF/JAXB/episode_schemagen.xjb')

/*
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema" version="1.0">

  <xs:complexType name="RenamedFoo">
    <xs:sequence/>
    <xs:attribute name="renamedBar" type="xs:string">
      <xs:annotation>
        <xs:documentation><![CDATA[This is a Bar.]]></xs:documentation>
      </xs:annotation>
    </xs:attribute>
  </xs:complexType>
</xs:schema>
 */

// Act: Validate content
def xml = new XmlSlurper().parse(new File(outputDir, 'schema1.xsd'));
def renamedFooType = xml.complexType[0];
def renamedBarAttribute = renamedFooType.attribute[0];

assert 1 == xml.complexType.size();
assert 'RenamedFoo' == renamedFooType.@name.text();
assert 'renamedBar' == renamedBarAttribute.@name.text();
assert 'xs:string' == renamedBarAttribute.@type.text();
assert 'This is a Bar.' == renamedBarAttribute.annotation[0].documentation[0].text();

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
validateNonexistentDirectory(new File(basedir, 'target/generated-test-resources/schemagen/'), 4);
