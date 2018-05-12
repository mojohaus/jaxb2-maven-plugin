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

/*
+=================== [10 SchemaGen Arguments]
|
| [0]: -encoding
| [1]: UTF-8
| [2]: -d
| [3]: /Users/lj/Development/Projects/Mojohaus/jaxb2-maven-plugin/target/it/mjaxb-64/target/schemagen-work/compile_scope
| [4]: -classpath
| [5]: /Users/lj/Development/Projects/Mojohaus/jaxb2-maven-plugin/target/it/mjaxb-64/src/main/java/
| [6]: -episode
| [7]: /Users/lj/Development/Projects/Mojohaus/jaxb2-maven-plugin/target/it/mjaxb-64/target/classes/META-INF/sun-jaxb.episode
| [8]: src/main/java/se/west/shauqra/FooWithEmptyXmlTypeName.java
| [9]: src/main/java/se/west/shauqra/FooWithSuppliedXmlTypeName.java
|
+=================== [End 10 SchemaGen Arguments]
*/

// Assemble
def expectedXmlForm = '''<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:customer="http://acme.com/customer-api" targetNamespace="http://acme.com/customer-api" version="1.0">

  <xs:element name="customerForNamelessFoo">
    <xs:complexType>
      <xs:sequence>
        <xs:element default="defaultName" name="name" type="xs:string"/>
        <xs:element form="qualified" name="anotherName" type="xs:string"/>
      </xs:sequence>
    </xs:complexType>
  </xs:element>

  <xs:element name="customerForSomeFoo" type="customer:someFoo"/>

  <xs:complexType name="someFoo">
    <xs:sequence>
      <xs:element default="defaultName" name="name" type="xs:string"/>
      <xs:element form="qualified" name="anotherName" type="xs:string"/>
    </xs:sequence>
  </xs:complexType>
</xs:schema>'''

final File classesDir = new File(basedir, 'target/classes')
final File generatedSchemaDir = new File(basedir, 'target/generated-resources/schemagen');
final File vanillaSchema = new File(basedir, 'target/schemagen-work/compile_scope/schema1.xsd');
final File processedSchema = new File(generatedSchemaDir, 'customer-api.xsd');
final File generatedEpisode = new File(classesDir, 'META-INF/JAXB/episode_schemagen.xjb');

assert processedSchema.exists(), "Expected file [" + processedSchema.getAbsolutePath() + "] not found."
assert generatedEpisode.exists(), "Expected file [" + generatedEpisode.getAbsolutePath() + "] not found."
assert vanillaSchema.exists(), "Expected file [" + vanillaSchema.getAbsolutePath() + "] not found."

// Act
def schemaElement = new XmlSlurper().parse(processedSchema)

// Assert
println "\nValidating namespace changes"
println "==================================="
def expectedTargetNS = 'http://acme.com/customer-api';
assert ("" + schemaElement.@targetNamespace) == expectedTargetNS,
        "Incorrect target namespace " + schemaElement.@targetNamespace + ". Expected " + expectedTargetNS + ".";
println "1. Correct target namespace: " + expectedTargetNS;

println "\nValidating schema content"
println "==================================="

def namelessFooRootElements = schemaElement.element.findAll { it.@name == "customerForNamelessFoo" }
def someFooRootElements = schemaElement.element.findAll { it.@name == "customerForSomeFoo" }
def someFooComplexTypes = schemaElement.complexType.findAll { it.@name == "someFoo" }

assert 1 == namelessFooRootElements.size(),
        "Got ${namelessFooRootElements.size} namelessFoo root elements. Expected 1.";
println "1. Got correct size for namelessFooRootElements."

assert 1 == namelessFooRootElements[0].children().size(),
        "Expected 1 child, but got ${namelessFooRootElements[0].children().size()}.";
println "2. Got correct size for namelessFooRootElements children."

assert "${namelessFooRootElements[0].complexType.@name}" == "", "Got unexpected name for namelessFoo complex type.";
println "3. Got correct name (none) for namelessFooRootElements complexType child."

assert 1 == someFooRootElements.size(), "Got ${someFooRootElements.size} someFooRootElements. Expected 1.";
println "4. Got correct size for someFooRootElements."

assert "${someFooRootElements[0].@type}" == 'customer:someFoo',
        "Got unexpected ComplexType type ${someFooRootElements[0].@type} for element " +
                "${someFooRootElements[0].@name} Expected 'customer:someFoo'.";
println "5. Got correct ComplexType type 'customer:someFoo'."

assert 1 == someFooComplexTypes.size(), "Got ${someFooComplexTypes.size} someFooComplexTypes. Expected 1.";
println "6. Got correct size for someFooComplexTypes."