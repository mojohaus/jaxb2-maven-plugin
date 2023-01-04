import java.util.zip.ZipEntry
import java.util.zip.ZipFile

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

def addressType = new File( basedir,'target/generated-test-sources/jaxb/com/example/myschema/AddressType.java' )
assert addressType.exists()

def mainSources = new File( basedir,'target/generated-sources/jaxb/' )
assert !mainSources.exists()

def addressTypeCompiled = new File( basedir,'target/test-classes/com/example/myschema/AddressType.class' )
assert addressTypeCompiled.exists()

// Check test-sources JAR artifact content
def testSourceJar = new File(basedir, "target/mjaxb-134-1.0-SNAPSHOT-test-sources.jar")
assert  testSourceJar.exists()

def testSourceFiles = collectZip(testSourceJar)
assert testSourceFiles.containsAll([
    'Test.java',
    'com/example/myschema/AddressType.java',
    'com/example/myschema/ObjectFactory.java',
])

// Check sources JAR artifact content
def sourceJar = new File(basedir, "target/mjaxb-134-1.0-SNAPSHOT-sources.jar")
assert sourceJar.exists()

def sourceFiles = collectZip(sourceJar)
assert ['Main.java'] == sourceFiles


//region Methods
static def collectZip(File file) {
  def entries = new ZipFile(file).entries()
  def filter = entries.findAll { !it.directory && !it.name.startsWith('META-INF') }
  //noinspection GroovyAssignabilityCheck
  return filter.collect { ZipEntry it ->
    it.name
  }
}

//endregion