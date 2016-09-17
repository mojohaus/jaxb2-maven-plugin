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

def expectedSchemaPath = 'target/generated-resources/schemagen/smooth.xsd'
File schema = new File( basedir, expectedSchemaPath)
println "\nValidating that schema exists at expected path \"" + expectedSchemaPath + "\""
assert schema.exists()

// Validate content as reported in issue MJAXB-80
def xml = new XmlSlurper().parse(schema)

// 1) Validate the required Element tags
/*
<xs:element name="importItems" type="smoothIntegration:importItems"/>
<xs:element name="someImportItem" type="smoothIntegration:someImportItem"/>
<xs:element name="someOtherImportItem" type="smoothIntegration:someOtherImportItem"/>
 */
println "\nElement namespace change validation"
println "==================================="
def importItemsElement = xml.element.findAll{ it.@name.text().equals('importItems') }
def someImportItemElement = xml.element.findAll{ it.@name.text().equals('someImportItem') }
def someOtherImportItemElement = xml.element.findAll{ it.@name.text().equals('someOtherImportItem') }

assert 'smoothIntegration:importItems' == importItemsElement.@type.text()
println "Namespace change in element 'importItems': OK"

assert 'smoothIntegration:someImportItem' == someImportItemElement.@type.text()
println "Namespace change in element 'someImportItem': OK"

assert 'smoothIntegration:someOtherImportItem' == someOtherImportItemElement.@type.text()
println "Namespace change in element 'someOtherImportItem': OK"

// 2) Validate the complex types
/*
  <xs:complexType name="importItems">
    <xs:sequence>
      <xs:element maxOccurs="unbounded" minOccurs="0" ref="smoothIntegration:someImportItem"/>
      <xs:element maxOccurs="unbounded" minOccurs="0" ref="smoothIntegration:someOtherImportItem"/>
    </xs:sequence>
  </xs:complexType>

  <xs:complexType name="someImportItem">
    <xs:complexContent>
      <xs:extension base="smoothIntegration:importItem">
        <xs:sequence>
          <xs:element name="someIdentifier" type="xs:string"/>
        </xs:sequence>
      </xs:extension>
    </xs:complexContent>
  </xs:complexType>

  <xs:complexType abstract="true" name="importItem">
    <xs:sequence/>
    <xs:attribute name="id" type="xs:string"/>
  </xs:complexType>

  <xs:complexType name="someOtherImportItem">
    <xs:complexContent>
      <xs:extension base="smoothIntegration:importItem">
        <xs:sequence>
          <xs:element name="someWeirdIdentifier" type="xs:string"/>
        </xs:sequence>
      </xs:extension>
    </xs:complexContent>
  </xs:complexType>
 */
println "\nSequence ref namespace change validation"
println "========================================"

def importItemsType = xml.complexType.findAll{ it.@name.text().equals('importItems') }
assert 'smoothIntegration:someImportItem' == importItemsType[0].sequence[0].element[0].@ref.text()
println "Namespace change in sequence ref 'someImportItem': OK"

assert 'smoothIntegration:someOtherImportItem' == importItemsType[0].sequence[0].element[1].@ref.text()
println "Namespace change in sequence ref 'someOtherImportItem': OK"


println "\nExtension base namespace change validation"
println "=========================================="

def someImportItemType = xml.complexType.findAll{ it.@name.text().equals('someImportItem') }
assert 'smoothIntegration:importItem' == someImportItemType[0].complexContent.extension.@base.text()
println "Namespace change in sequence ref 'someImportItem': OK"

def someOtherImportItemType = xml.complexType.findAll{ it.@name.text().equals('someOtherImportItem') }
assert 'smoothIntegration:importItem' == someOtherImportItemType[0].complexContent.extension.@base.text()
println "Namespace change in sequence ref 'someOtherImportItem': OK"

println "\nValidation script done."