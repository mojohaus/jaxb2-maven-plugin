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

final File outputDir = new File(basedir, 'target/generated-resources/schemagen')
final File workDir = new File(basedir, 'target/schemagen-work/compile_scope')

/*
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:tns="http://gnat.west.se/foods" targetNamespace="http://gnat.west.se/foods" version="1.0">

  <xs:element name="foodPreferences" type="tns:foodPreferences"/>

  <xs:complexType name="foodPreferences">
    <xs:annotation>
      <xs:documentation><![CDATA[Trivial transport object type for enumerations.

(author): <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB]]></xs:documentation>
    </xs:annotation>
    <xs:sequence>
      <xs:element minOccurs="0" name="preferences">
        <xs:annotation>
          <xs:documentation><![CDATA[A List of {@link FoodPreference} instances.]]></xs:documentation>
        </xs:annotation>
        <xs:complexType>
          <xs:sequence>
            <xs:element maxOccurs="unbounded" minOccurs="0" name="preference" type="tns:foodPreference"/>
          </xs:sequence>
        </xs:complexType>
      </xs:element>
      <xs:element minOccurs="0" name="coins">
        <xs:annotation>
          <xs:documentation><![CDATA[A List of {@link FoodPreference} instances.]]></xs:documentation>
        </xs:annotation>
        <xs:complexType>
          <xs:sequence>
            <xs:element maxOccurs="unbounded" minOccurs="0" name="coin" type="tns:americanCoin"/>
          </xs:sequence>
        </xs:complexType>
      </xs:element>
    </xs:sequence>
  </xs:complexType>

  <xs:simpleType name="americanCoin">
    <xs:annotation>
      <xs:documentation><![CDATA[Simple enumeration example defining standard US coins.

(author): <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB]]></xs:documentation>
    </xs:annotation>
    <xs:restriction base="xs:int">
      <xs:enumeration value="1"/>
      <xs:enumeration value="5"/>
      <xs:enumeration value="10"/>
      <xs:enumeration value="25"/>
    </xs:restriction>
  </xs:simpleType>

  <xs:simpleType name="foodPreference">
    <xs:annotation>
      <xs:documentation><![CDATA[Simple enumeration example defining some Food preferences.

(author): <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB]]></xs:documentation>
    </xs:annotation>
    <xs:restriction base="xs:string">
      <xs:enumeration value="NONE"/>
      <xs:enumeration value="LACTO_VEGETARIAN"/>
      <xs:enumeration value="VEGAN"/>
    </xs:restriction>
  </xs:simpleType>
</xs:schema>
*/

// Act: Validate content
def xml = new XmlSlurper().parse(new File(workDir, 'schema1.xsd'));
assert 1 == xml.complexType.size();

def americanCoinElement = xml.simpleType.find { it.@name == 'americanCoin' }
def foodPreferenceElement = xml.simpleType.find { it.@name == 'foodPreference' }

assert null != americanCoinElement.annotation.documentation.text()
assert null != foodPreferenceElement.annotation.documentation.text()

// Assert
println "\nValidating work directory content"
println "==================================="

validateExistingFile(new File(workDir, 'schema1.xsd'), 1);
validateNonexistentFile(new File(workDir, 'META-INF/sun-jaxb.episode'), 2);
validateExistingFile(new File(workDir, 'se/west/gnat/AmericanCoin.class'), 3);
validateExistingFile(new File(workDir, 'se/west/gnat/FoodPreference.class'), 4);
validateExistingFile(new File(workDir, 'se/west/gnat/FoodPreferences.class'), 5);

println "\nValidating output directory content"
println "====================================="

validateExistingFile(new File(outputDir, 'schema1.xsd'), 1);
validateExistingFile(new File(outputDir, 'META-INF/sun-jaxb.episode'), 2);
validateNonexistentDirectory(new File(basedir, 'target/generated-test-resources/schemagen/'), 4);
