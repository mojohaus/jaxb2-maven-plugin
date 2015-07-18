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

// Assemble
def validateExistingFile(final File aFile, final int index) {
  final String path = aFile.getCanonicalPath();
  assert aFile.exists() && aFile.isFile(), "Missing required file [" + path + "]";
  println "" + index + ". Expected file exists correctly. [" + path + "]";
}

def validateNonexistentFile(final File aFile, final int index) {
  final String path = aFile.getCanonicalPath();
  assert !aFile.exists(), "File should not exist: [" + path + "]";
  println "" + index + ". File correctly non-existent. [" + path + "]";
}

def validateNonexistentDirectory(final File aDirectory, final int index) {
  final String path = aDirectory.getCanonicalPath();
  assert !aDirectory.exists(), "Directory should not exist: [" + path + "]";
  println "" + index + ". Directory correctly non-existent. [" + path + "]";
}

final File workDir = new File(basedir, 'target/schemagen-work/compile_scope')

// Validate that no schemas was produced by this invocation.
def normalResult = new File(workDir, 'schema1.xsd');
validateNonexistentFile(normalResult, 1);

// Validate that the expected JAXB error message is stashed in the log
def expectedErrorMessage = 'Class has two properties of the same name "bar"';
List<String> lines = new File(basedir, 'build.log').readLines();

boolean foundExpectedErrorMessage = false;

for(line in lines) {
  if(line.trim().startsWith(expectedErrorMessage)) {
    foundExpectedErrorMessage = true;
  }
}

assert foundExpectedErrorMessage, "Expected JAXB error message not found."