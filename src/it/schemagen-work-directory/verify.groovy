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

final File generatedSchema = new File(basedir, 'target/generated-resources/schemagen/schema1.xsd')
final File generatedEpisode = new File(basedir, 'target/classes/META-INF/JAXB/episode_schemagen.xjb')

assert generatedSchema.exists(), "Expected file [" + generatedSchema.getAbsolutePath() + "] not found."
assert generatedEpisode.exists(), "Expected file [" + generatedEpisode.getAbsolutePath() + "] not found."

// Validate content
def xml = new XmlSlurper().parse(generatedSchema)
assert 1 == xml.complexType.size()
assert 'foo' == xml.complexType[0].@name.text()

final File testSchemagen = new File(basedir, 'target/generated-test-resources/schemagen/')
assert !testSchemagen.exists(), "Found unexpected generated TestResources [" + testSchemagen.getAbsolutePath() + "]";
