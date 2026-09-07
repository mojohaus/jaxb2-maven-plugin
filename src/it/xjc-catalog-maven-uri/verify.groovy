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

def log = new File(basedir, "build.log").text

// 1. Check that schema-lib packaged its JAR with the schema
def libJar = new File(basedir, "schema-lib/target/xjc-catalog-schema-lib-1.0-SNAPSHOT.jar")
assert libJar.isFile() : "schema-lib JAR not found: " + libJar

// 2. Check that the resolved catalog was generated in target/jaxb2/
def resolvedCatalog = new File(basedir, "consumer/target/jaxb2/resolved-catalog.xml")
assert resolvedCatalog.isFile() : "Resolved catalog file not found: " + resolvedCatalog

def resolvedText = resolvedCatalog.text
assert resolvedText.contains("jar:file:") : "Resolved catalog does not contain jar:file: URL: " + resolvedText
assert !resolvedText.contains("maven:org.codehaus") : "Resolved catalog still contains unreplaced maven: URI: " + resolvedText
assert resolvedText.contains("schemas/") : "Resolved catalog does not reference schemas/: " + resolvedText

// 3. Verify that XJC successfully generated classes from both order.xsd and the resolved address.xsd
def orderType = new File(basedir, "consumer/target/generated-sources/jaxb/com/example/order/OrderType.java")
assert orderType.isFile() : "OrderType.java was not generated: " + orderType

def addressType = new File(basedir, "consumer/target/generated-sources/jaxb/com/example/order/AddressType.java")
assert addressType.isFile() : "AddressType.java (from resolved address.xsd) was not generated: " + addressType

def objectFactory = new File(basedir, "consumer/target/generated-sources/jaxb/com/example/order/ObjectFactory.java")
assert objectFactory.isFile() : "ObjectFactory.java was not generated: " + objectFactory

// 4. Verify OrderType references AddressType
assert orderType.text.contains("AddressType") : "OrderType does not reference AddressType: " + orderType.text

// 5. Verify resolution was logged
assert log.contains("Resolving maven: URIs in catalog") : "Log missing catalog resolution message"
assert log.contains("Resolved catalog written to") : "Log missing resolved catalog written message"
