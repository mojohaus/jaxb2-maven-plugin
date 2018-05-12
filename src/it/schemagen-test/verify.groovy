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

// java.lang.AssertionError: Expected file [/Users/lenjor/Development/Projects/Mojohaus/jaxb2-maven-plugin/target/
// it/schemagen-test/target/generated-test-resources/schemagen/META-INF/sun-jaxb.episode] not found..
// Expression: testEpisode.exists()

def testSchema = new File(basedir, 'target/generated-test-resources/schemagen/schema1.xsd');
def testEpisode = new File(basedir, 'target/generated-test-resources/schemagen/META-INF' +
        '/JAXB/episode_schemagen-test.xjb');

assert testSchema.exists(), "Expected file [" + testSchema.getAbsolutePath() + "] not found."
assert testEpisode.exists(), "Expected file [" + testEpisode.getAbsolutePath() + "] not found."

// Validate content
def xml = new XmlSlurper().parse(testSchema)
assert 1 == xml.complexType.size()
assert 'foo' == xml.complexType[0].@name.text()

File mainSchemagen = new File(basedir, 'target/generated-resources/schemagen/')
assert !mainSchemagen.exists(), "Found unexpected generated Resources [" + mainSchemagen.getAbsolutePath() + "]";
