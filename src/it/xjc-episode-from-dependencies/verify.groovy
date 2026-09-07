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

import java.util.jar.JarFile

def log = new File(basedir, "build.log").text

// 1. Module A generated its own classes and packaged the episode in its JAR.
def aJar = new File(basedir, "schema-a/target/xjc-episode-schema-a-1.0-SNAPSHOT.jar")
assert aJar.isFile() : "Module A JAR not found: " + aJar

def jarFile = new JarFile(aJar)
def jarEntries = []
jarFile.entries().each { jarEntries << it.name }
jarFile.close()
assert jarEntries.contains("META-INF/JAXB/schema-a-episode.xjb")
    : "Episode missing from module A JAR; entries: " + jarEntries

// 2. Module B generated its own classes.
def bObjectFactory = new File(basedir, "schema-b/target/generated-sources/jaxb/com/example/b/ObjectFactory.java")
assert bObjectFactory.isFile() : "Module B ObjectFactory not generated."

// 3. Module B did NOT regenerate module A's classes (episode applied).
def aClassesInB = new File(basedir, "schema-b/target/generated-sources/jaxb/com/example/a")
assert !aClassesInB.exists()
    : "Module B re-generated module A classes; episode was not applied."

// 4. B's generated code references A's reused types (AType from the dependency).
def bType = new File(basedir, "schema-b/target/generated-sources/jaxb/com/example/b/BType.java")
assert bType.isFile() : "Module B BType not generated."
assert bType.text.contains("com.example.a") : "Module B generated sources do not reference module A types."

// 5. The episode was extracted beneath the deterministic extraction directory in module B.
def extracted = new File(
    basedir,
    "schema-b/target/jaxb2/episodes/"
        + "org.codehaus.mojo.jaxb2.its/xjc-episode-schema-a/1.0-SNAPSHOT/"
        + "META-INF/JAXB/schema-a-episode.xjb")
assert extracted.isFile() : "Episode was not extracted to " + extracted

// 6. The mojo logged the resolved episode.
assert log.contains("Resolved JAXB episode org.codehaus.mojo.jaxb2.its:xjc-episode-schema-a:jar:1.0-SNAPSHOT")
    : "Mojo did not log episode resolution."
