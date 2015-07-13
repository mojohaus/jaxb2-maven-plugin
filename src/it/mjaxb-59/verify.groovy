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
File buildLog = new File(basedir, 'build.log');
List<String> lines = buildLog.readLines();

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
| [1/1]: /Users/lj/Development/Projects/Codehaus/github_jaxb2_plugin/target/it/mjaxb-59/module1/src/main/java
|
| 1 Results:
| [1/1]: /Users/lj/Development/Projects/Codehaus/github_jaxb2_plugin/target/it/mjaxb-59/module1/src/main/java/se/west/gnat/Foo.java
|
+=================== [End Filtered sources]

...

+=================== [9 SchemaGen Arguments]
|
| [0]: -encoding
| [1]: UTF-8
| [2]: -d
| [3]: /Users/lj/Development/Projects/Codehaus/github_jaxb2_plugin/target/it/mjaxb-59/module1/target/generated-resources/schemagen
| [4]: -classpath
| [5]: /Users/lj/Development/Projects/Codehaus/github_jaxb2_plugin/target/it/mjaxb-59/module1/src/main/java/
| [6]: -episode
| [7]: /Users/lj/Development/Projects/Codehaus/github_jaxb2_plugin/target/it/mjaxb-59/module1/target/generated-resources/schemagen/META-INF/sun-jaxb.episode
| [8]: module1/src/main/java/se/west/gnat/Foo.java
|
+=================== [End 9 SchemaGen Arguments]
 */

def xjcArgumentPatternPrefix = "\\| \\[\\p{Digit}+\\]: ";
def sep = Pattern.quote(System.getProperty("file.separator"));

final Pattern expectedSourcePattern  = Pattern.compile(xjcArgumentPatternPrefix
        + ("module1/src/main/java/se/west/gnat/Foo.java").replace("/", sep));

boolean foundSourceArgument = false;

// Act
for (line in lines) {

  String trimmedLine = line.trim()
  if (trimmedLine.isEmpty()) {
    continue
  };

  if(!foundSourceArgument && expectedSourcePattern.matcher(trimmedLine).matches()) {
    foundSourceArgument = true;
  }
}

// Assert
def missingRequired(Pattern pattern) {
  return "Missing required pattern: [" + pattern.pattern() + "]" ;
}

assert foundSourceArgument, missingRequired(expectedSourcePattern);
