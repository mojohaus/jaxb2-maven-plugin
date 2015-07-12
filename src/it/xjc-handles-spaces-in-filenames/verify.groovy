import java.util.regex.Pattern

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

String pathToLeafPackage = "target/generated-sources/jaxb/com/example/myschema/";
File expectedAddressTypeFile = new File(basedir, pathToLeafPackage + "AddressType.java");
File expectedObjectFactoryFile = new File(basedir, pathToLeafPackage + "ObjectFactory.java");

/*
+=================== [11 XJC Arguments]
|
| [0]: -xmlschema
| [1]: -encoding
| [2]: UTF-8
| [3]: -d
| [4]: /Users/lj/Development/Projects/Codehaus/lennartj-jaxb2-maven-plugin/target/it/xjc-handles-spaces-in-filenames/target/generated-sources/jaxb
| [5]: -extension
| [6]: -episode
| [7]: /Users/lj/Development/Projects/Codehaus/lennartj-jaxb2-maven-plugin/target/it/xjc-handles-spaces-in-filenames/target/generated-sources/jaxb/META-INF/sun-jaxb.episode
| [8]: -b
| [9]: /Users/lj/Development/Projects/Codehaus/lennartj-jaxb2-maven-plugin/target/it/xjc-handles-spaces-in-filenames/src/main/xjb/spaced filename.xjb
| [10]: src/main/xsd/address.xsd
|
+=================== [End 11 XJC Arguments]
 */
final String xjcArgumentPatternPrefix = "\\| \\[\\p{Digit}+\\]: ";
Pattern expectedBArgumentPattern  = Pattern.compile(xjcArgumentPatternPrefix + "\\-b");
Pattern expectedXjbArgumentPattern = Pattern.compile(xjcArgumentPatternPrefix + ".*src/main/xjb/spaced filename.xjb");
Pattern expectedSourceArgumentPattern = Pattern.compile(xjcArgumentPatternPrefix + "src/main/xsd/address.xsd");

boolean foundBArgument = false;
boolean foundXjbArgument = false;
boolean foundSourceArgument = false;

// Act
for (line in lines) {

  String trimmedLine = line.trim()
  if (trimmedLine.isEmpty()) {
    continue
  };

  if(!foundBArgument && expectedBArgumentPattern.matcher(trimmedLine).matches()) {
    foundBArgument = true;
  }

  if(!foundXjbArgument && expectedXjbArgumentPattern.matcher(trimmedLine).matches()) {
    foundXjbArgument = true;
  }

  if(!foundSourceArgument && expectedSourceArgumentPattern.matcher(trimmedLine).matches()) {
    foundSourceArgument = true;
  }
}

// Assert
def missingRequired(Pattern pattern) {
  return "Missing required pattern: [" + pattern.pattern() + "]" ;
}

assert foundBArgument, missingRequired(expectedBArgumentPattern);
assert foundXjbArgument, missingRequired(expectedXjbArgumentPattern);
assert foundSourceArgument, missingRequired(expectedSourceArgumentPattern);

assert expectedAddressTypeFile.exists() && expectedAddressTypeFile.isFile(), "Missing required file: [" +
        expectedAddressTypeFile.getPath() + "]" ;
assert expectedObjectFactoryFile.exists() && expectedObjectFactoryFile.isFile(), "Missing required file: [" +
        expectedObjectFactoryFile.getPath() + "]" ;