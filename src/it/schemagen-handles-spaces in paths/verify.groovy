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

def validateExistingFile(final File aFile, final int index) {
  final String path = aFile.getCanonicalPath();
  assert aFile.exists() && aFile.isFile(), "Missing required file [" + path + "]";
  println "" + index + ". Expected file exists correctly. [" + path + "]";
}

def outputDir = new File(basedir, 'target/generated-resources/schemagen')
def workDir = new File(basedir, 'target/schemagen-work/compile_scope')

// Act: Validate content
def xml = new XmlSlurper().parse(new File(workDir, 'schema1.xsd'));
assert 1 == xml.complexType.size();
assert 'someType' == xml.complexType[0].@name.text();

// Assert
println "\nValidating work directory content"
println "==================================="
validateExistingFile(new File(workDir, 'schema1.xsd'), 1);

println "\nValidating output directory content"
println "====================================="
validateExistingFile(new File(outputDir, 'schema1.xsd'), 1);
validateExistingFile(new File(outputDir, 'META-INF/JAXB/episode_schemagen.xjb'), 2);
