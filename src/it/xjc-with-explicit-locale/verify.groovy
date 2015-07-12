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

File addressType = new File(basedir, 'target/generated-sources/jaxb/com/example/myschema/AddressType.java')
assert addressType.exists()

File testSources = new File(basedir, 'target/generated-test-sources/jaxb/')
assert !testSources.exists()

File addressTypeCompiled = new File(basedir, 'target/classes/com/example/myschema/AddressType.class')
assert addressTypeCompiled.exists()

// Validate that XJC ran with French locale.
int hitCount = 0;
List<String> lines = addressType.readLines();
for (line in lines) {

  String trimmedLine = line.trim()
  if (trimmedLine.isEmpty()) {
    continue
  };

  if (trimmedLine.endsWith("Classe Java pour AddressType complex type.")) {
    hitCount++;
  }
}

assert 1 == hitCount, "Found [" + hitCount + "] . (Expected 1).";
