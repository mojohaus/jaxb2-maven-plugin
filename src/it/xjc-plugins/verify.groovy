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
import java.util.regex.Pattern

File buildLog = new File(basedir, 'build.log');
List<String> lines = buildLog.readLines();
/*
[DEBUG]
+=================== [14 XJC Arguments]
|
| [0]: -xmlschema
| [1]: -extension
| [2]: -encoding
| [3]: UTF-8
| [4]: -p
| [5]: com.example.myschema
| [6]: -d
| [7]: /jaxb2-maven-plugin/target/it/xjc-plugins/target/generated-sources/jaxb
| [8]: -classpath
| [9]: file:/jaxb2-maven-plugin/target/local-repo/com/sun/xml/bind/jaxb-xjc/3.0.2/jaxb-xjc-3.0.2.jar!/META-INF/versions/9/;file:/jaxb2-maven-plugin/target/local-repo/com/sun/xml/bind/jaxb-core/3.0.2/jaxb-core-3.0.2.jar!/META-INF/versions/9/;file:/jaxb2-maven-plugin/target/local-repo/jakarta/xml/bind/jakarta.xml.bind-api/3.0.1/jakarta.xml.bind-api-3.0.1.jar!/META-INF/versions/9/;file:/jaxb2-maven-plugin/target/local-repo/com/sun/xml/bind/jaxb-jxc/3.0.2/jaxb-jxc-3.0.2.jar!/META-INF/versions/9/;file:/jaxb2-maven-plugin/target/local-repo/com/sun/xml/bind/jaxb-impl/3.0.2/jaxb-impl-3.0.2.jar!/META-INF/versions/9/;file:/jaxb2-maven-plugin/target/local-repo/org/codehaus/plexus/plexus-utils/4.0.1/plexus-utils-4.0.1.jar!/META-INF/versions/11/;file:/jaxb2-maven-plugin/target/local-repo/jakarta/xml/bind/jakarta.xml.bind-api/3.0.0/jakarta.xml.bind-api-3.0.0.jar!/META-INF/versions/9/
| [10]: -episode
| [11]: /jaxb2-maven-plugin/target/it/xjc-plugins/target/generated-sources/jaxb/META-INF/JAXB/episode_xjc.xjb
| [12]: -Xfluent-api
| [13]: -Xinheritance
| [14]: /jaxb2-maven-plugin/target/it/xjc-plugins/src/main/xsd/address.xsd
|
+=================== [End 14 XJC Arguments]
*/
def xjcArgumentPatternPrefix = "\\| \\[\\p{Digit}+\\]: ";
def fluentApiPlugin = 'fluent-api';
def inheritancePlugin = 'inheritance';

final Pattern expectedFluentApiPluginPattern  = Pattern.compile(xjcArgumentPatternPrefix
        + "-X${fluentApiPlugin}");
final Pattern expectedInheritancePluginPattern  = Pattern.compile(xjcArgumentPatternPrefix
        + "-X${inheritancePlugin}");


boolean foundFluentApiPluginArgument = false;
boolean foundInheritancePluginArgument = false;
boolean foundExpectedErrorMessage = false;
def expectedErrorMessage = 'Caused by: com.sun.tools.xjc.BadCommandLineException:';

// Act
for (line in lines) {

    String trimmedLine = line.trim()
    if (trimmedLine.isEmpty()) {
        continue
    };

    if(!foundFluentApiPluginArgument && expectedFluentApiPluginPattern.matcher(trimmedLine).matches()) {
        foundFluentApiPluginArgument = true;
    }
    if(!foundInheritancePluginArgument && expectedInheritancePluginPattern.matcher(trimmedLine).matches()) {
        foundInheritancePluginArgument = true;
    }
    if(line.trim().startsWith(expectedErrorMessage) && line.trim().endsWith("-X${fluentApiPlugin}")) {
        foundExpectedErrorMessage = true;
    }
}

// Assert
static def missingRequired(Pattern pattern) {
    return "Missing required pattern: [" + pattern.pattern() + "]" ;
}

assert foundFluentApiPluginArgument, missingRequired(expectedFluentApiPluginPattern);
assert foundInheritancePluginArgument, missingRequired(expectedInheritancePluginPattern);

assert foundExpectedErrorMessage, "Expected XJC error message not found."
