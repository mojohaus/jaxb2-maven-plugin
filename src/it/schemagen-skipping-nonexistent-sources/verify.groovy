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

/*
+=================== [Filtered sources]
|
| 6 Exclude patterns:
| [1/6]: (\p{javaLetterOrDigit}|\p{Punct})+README.*
| [2/6]: (\p{javaLetterOrDigit}|\p{Punct})+\.xml
| [3/6]: (\p{javaLetterOrDigit}|\p{Punct})+\.txt
| [4/6]: (\p{javaLetterOrDigit}|\p{Punct})+\.xjb
| [5/6]: (\p{javaLetterOrDigit}|\p{Punct})+\.xsd
| [6/6]: (\p{javaLetterOrDigit}|\p{Punct})+\.properties
|
| 1 Sources:
| [1/1]: /Users/lj/Development/Projects/Codehaus/github_jaxb2_plugin/target/it/schemagen-skipping-nonexistent-sources/src/main/java
|
| 0 Results:
|
+=================== [End Filtered sources]

... and ...

+=================== [Incorrect Plugin Configuration Detected]
|
| Property : sources
| Problem  : At least one Java Source file has to be included.
|
+=================== [End Incorrect Plugin Configuration Detected]
 */
File buildLog = new File(basedir, 'build.log')

final String expectedConfigWarning = "| Problem  : At least one Java Source file has to be included.";
final String expectedSkippingExecution = "[DEBUG] Skipping execution, as instructed.";

boolean foundIncorrectConfigWarning = false;
boolean foundExpectedSkippingExecution = false;

List<String> lines = buildLog.readLines();
for (line in lines) {

  String trimmedLine = line.trim()
  if (trimmedLine.isEmpty()) {
    continue
  };

  if(!foundIncorrectConfigWarning && trimmedLine.equalsIgnoreCase(expectedConfigWarning)) {
    foundIncorrectConfigWarning = true;
  }

  if(!foundExpectedSkippingExecution && trimmedLine.equalsIgnoreCase(expectedSkippingExecution)) {
    foundExpectedSkippingExecution = true;
  }
}

// Assert
assert foundIncorrectConfigWarning, "Missing required line: [" + expectedConfigWarning + "]"
assert foundExpectedSkippingExecution, "Missing required line: [" + expectedSkippingExecution + "]"

