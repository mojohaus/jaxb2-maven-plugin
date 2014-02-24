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

File schema = new File( basedir,'target/generated-resources/schemagen/some.xsd' )
assert schema.exists()

// Validate content as reported in issue MJAXB-81
def xml = new XmlSlurper(true, true).parse(schema)
def baz = xml.complexType.findAll{ it.@name.text().equals('BarType') }
assert 'som:BazType' == baz[0].sequence[0].element[0].@type.text()
