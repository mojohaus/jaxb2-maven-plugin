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
File buildLog = new File(basedir, 'build.log')
List<String> lines = buildLog.readLines();

/*
[DEBUG] Accepted configured sources [/Users/lj/Development/Projects/Codehaus/github_jaxb2_plugin/target/it/xjc-include-file-patterns/src/main/someOtherXsds]
[DEBUG] Accepted configured sources [/Users/lj/Development/Projects/Codehaus/github_jaxb2_plugin/target/it/xjc-include-file-patterns/src/main/foo/gnat.txt]
[INFO] Ignored given or default sources [src/main/nonexistent/paths], since it is not an existent file or directory.

[INFO] Got resolvedSources: [
/Users/lj/Development/Projects/Codehaus/github_jaxb2_plugin/target/it/xjc-include-file-patterns/src/main/someOtherXsds/fooSchema.txt,
/Users/lj/Development/Projects/Codehaus/github_jaxb2_plugin/target/it/xjc-include-file-patterns/src/main/someOtherXsds/some_schema.bar,
/Users/lj/Development/Projects/Codehaus/github_jaxb2_plugin/target/it/xjc-include-file-patterns/src/main/foo/gnat.txt]

[DEBUG] Processing file [0/5]: se/west/AddressTypeFromGnatTxt.java
[DEBUG] Processing file [1/5]: se/west/FooBar.java
[DEBUG] Processing file [2/5]: se/west/FooBaz.java
[DEBUG] Processing file [3/5]: se/west/ObjectFactory.java
[DEBUG] Processing file [4/5]: se/west/package-info.java
 */

def isProcessingFileLine(final String aLine, final String className) {
  return aLine.contains("[DEBUG] Processing file [") && aLine.contains("se/west/" + className + ".java");
}

String expectedIgnoreLine = "Ignored given or default sources [src/main/nonexistent/paths], " +
        "since it is not an existent file or directory.";
String acceptedLinePrefix = "Accepted configured sources";
String acceptedSomeOtherPath = "src/main/someOtherXsds";
String acceptedFooGnatTxt = "src/main/foo/gnat.txt";

def foundIgnoreLine = false;
def foundSomeOtherPathLine = false;
def foundFooGnatTextLine = false;
def foundProcessingShouldBeIgnoredAddressType = false;
def foundProcessingAddressTypeFromGnatTxtType = false;
def foundProcessingFooBarType = false;
def foundProcessingFooBazType = false;
def foundProcessingObjectFactoryType = false;
def foundProcessingPackageInfoType = false;

// Act
for (line in lines) {

  String trimmedLine = line.trim()

  if (!trimmedLine.isEmpty()) {

    // Check that the appropriate "ignored" lines are present
    if (trimmedLine.contains(expectedIgnoreLine)) {
      foundIgnoreLine = true;
    }

    // Check that the appropriate "accepted" lines are present
    if (trimmedLine.contains(acceptedLinePrefix)) {
      if (trimmedLine.contains(acceptedSomeOtherPath)) {
        foundSomeOtherPathLine = true;
      } else if (trimmedLine.contains(acceptedFooGnatTxt)) {
        foundFooGnatTextLine = true;
      }
    }

    // Check what was processed
    if (isProcessingFileLine(trimmedLine, "FooBar")) {
      foundProcessingFooBarType = true;
    } else if (isProcessingFileLine(trimmedLine, "FooBaz")) {
      foundProcessingFooBazType = true;
    } else if (isProcessingFileLine(trimmedLine, "ObjectFactory")) {
      foundProcessingObjectFactoryType = true;
    } else if (isProcessingFileLine(trimmedLine, "package-info")) {
      foundProcessingPackageInfoType = true;
    } else if(isProcessingFileLine(trimmedLine, "ShouldBeIgnoredAddressType")) {
      foundProcessingShouldBeIgnoredAddressType = true;
    } else if(isProcessingFileLine(trimmedLine, "AddressTypeFromGnatTxt")) {
      foundProcessingAddressTypeFromGnatTxtType = true;
    }
  }
}

// Assert
def missingRequired(value) {
  return "Missing required text: [" + value + "]" ;
}
def illegalButPresent(value) {
  return "Found illegal statement: [" + value + "]" ;
}

assert foundIgnoreLine, missingRequired(expectedIgnoreLine);
assert foundSomeOtherPathLine, missingRequired(acceptedSomeOtherPath);
assert foundFooGnatTextLine, missingRequired(acceptedFooGnatTxt);

assert !foundProcessingShouldBeIgnoredAddressType, illegalButPresent("se/west/ShouldBeIgnoredAddressType.java");
assert foundProcessingFooBazType, missingRequired("se/west/FooBaz.java") ;
assert foundProcessingAddressTypeFromGnatTxtType, missingRequired("se/west/AddressTypeFromGnatTxt.java") ;
assert foundProcessingFooBarType, missingRequired("se/west/FooBar.java");
assert foundProcessingObjectFactoryType, missingRequired("se/west/ObjectFactory.java");
assert foundProcessingPackageInfoType, missingRequired("se/west/package-info.java")