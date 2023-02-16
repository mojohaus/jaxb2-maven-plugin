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
import groovy.xml.XmlSlurper

// Assemble
final File outputDir = new File(basedir, 'target/generated-resources/schemagen')
final File workDir = new File(basedir, 'target/schemagen-work/compile_scope')

/*
First Complex Type:

  <xs:complexType name="exampleXmlWrapperUsingFieldAccess">
    <xs:annotation>
      <xs:documentation><![CDATA[Trivial transport object type for collections.]]></xs:documentation>
    </xs:annotation>
    <xs:sequence>
      <xs:element minOccurs="0" name="foobar">
        <xs:annotation>
          <xs:documentation><![CDATA[List containing some strings.]]></xs:documentation>
        </xs:annotation>
        <xs:complexType>
          <xs:sequence>
            <xs:element maxOccurs="unbounded" minOccurs="0" name="aString" type="xs:string"/>
          </xs:sequence>
        </xs:complexType>
      </xs:element>
      <xs:element minOccurs="0" name="integerSet">
        <xs:annotation>
          <xs:documentation><![CDATA[SortedSet containing Integers.]]></xs:documentation>
        </xs:annotation>
        <xs:complexType>
          <xs:sequence>
            <xs:element maxOccurs="unbounded" minOccurs="0" name="anInteger" type="xs:int"/>
          </xs:sequence>
        </xs:complexType>
      </xs:element>
    </xs:sequence>
  </xs:complexType>

Second Complex Type:
  <xs:complexType name="exampleXmlWrapperUsingMethodAccess">
    <xs:annotation>
      <xs:documentation><![CDATA[Another trivial transport object type for collections.]]></xs:documentation>
    </xs:annotation>
    <xs:sequence>
      <xs:element minOccurs="0" name="foobar">
        <xs:annotation>
          <xs:documentation><![CDATA[List containing some methodStrings.]]></xs:documentation>
        </xs:annotation>
        <xs:complexType>
          <xs:sequence>
            <xs:element maxOccurs="unbounded" minOccurs="0" name="aString" type="xs:string"/>
          </xs:sequence>
        </xs:complexType>
      </xs:element>
      <xs:element minOccurs="0" name="methodIntegerSet">
        <xs:annotation>
          <xs:documentation><![CDATA[SortedSet containing Integers.]]></xs:documentation>
        </xs:annotation>
        <xs:complexType>
          <xs:sequence>
            <xs:element maxOccurs="unbounded" minOccurs="0" name="anInteger" type="xs:int"/>
          </xs:sequence>
        </xs:complexType>
      </xs:element>
    </xs:sequence>
  </xs:complexType>
 */

// Act: Validate transformed content
def xml = new XmlSlurper().parse(new File(outputDir, 'schema1.xsd'));
assert 2 == xml.complexType.size(), 'Found ' + xml.complexType.size() + ' generated complex types in the schema1.xsd file. (Expected 2).';

def fieldAccessType = xml.'xs:complexType'.find { it.@name == 'exampleXmlWrapperUsingFieldAccess' }
def methodAccessType = xml.'xs:complexType'.find { it.@name == 'exampleXmlWrapperUsingMethodAccess' }

assert fieldAccessType != null, "Found no FieldAccessType"
assert methodAccessType != null, "Found no MethodAccessType"

println "Got fieldAccessType of type [" + fieldAccessType.getClass().getName() + "]"
println "Got methodAccessType of type [" + methodAccessType.getClass().getName() + "]"

// Assert
println "\nValidating Documentation Annotations for String List"
println "===================================================="

// /xs:schema/xs:complexType/xs:sequence/xs:element/xs:annotation/xs:documentation
def expectedStringListDocComment = "List containing some strings."
def expectedIntegerSetDocComment = "SortedSet containing Integers."

def stringListDocComment = xml.complexType
        .find { it.@name == 'exampleXmlWrapperUsingFieldAccess' }
        .sequence
        .element
        .find { it.@name == 'foobar' }
        .annotation
        .documentation
        .toString()
        .trim()

def intSetDocComment = xml.complexType
        .find { it.@name == 'exampleXmlWrapperUsingFieldAccess' }
        .sequence
        .element
        .find { it.@name == 'integerSet' }
        .annotation
        .documentation
        .toString()
        .trim()

assert expectedStringListDocComment == stringListDocComment, "Expected [" + expectedStringListDocComment +
        "], but got [" + stringListDocComment + "]"
println "Correctly found stringListDocComment [" + stringListDocComment + "] for Field Access type."
assert expectedIntegerSetDocComment == intSetDocComment, "Expected [" + expectedIntegerSetDocComment +
        "], but got [" + intSetDocComment + "]"
println "Correctly found intSetDocComment [" + intSetDocComment + "] for Field Access type."

// <xs:documentation><![CDATA[List containing some strings.]]></xs:documentation>

println "\nValidating Documentation Annotations for int Set"
println "=================================================="

