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
+=================== [13 XJC Arguments]
|
| [0]: -xmlschema
| [1]: -encoding
| [2]: UTF-8
| [3]: -p
| [4]: com.example.myschema
| [5]: -target
| [6]: 2.0
| [7]: -d
| [8]: /Users/lj/Development/Projects/Codehaus/github_jaxb2_plugin/target/it/mjaxb-16/target/generated-sources/jaxb
| [9]: -extension
| [10]: -episode
| [11]: /Users/lj/Development/Projects/Codehaus/github_jaxb2_plugin/target/it/mjaxb-16/target/generated-sources/jaxb/META-INF/sun-jaxb.episode
| [12]: src/main/xsd/address.xsd
|
+=================== [End 13 XJC Arguments]
 */
File buildLog = new File(basedir, 'build.log')

def logPattern = "\\| \\[\\p{Digit}+\\]: ";
def sep = Pattern.quote(System.getProperty("file.separator"));
Pattern expectedTargetPattern  = Pattern.compile(logPattern + "\\-target");
Pattern expectedTargetVersionPattern = Pattern.compile(logPattern + "2.0");
Pattern expectedExtensionPattern = Pattern.compile(logPattern + "\\-extension");
Pattern expectedEpisodePattern = Pattern.compile(logPattern + "\\-episode");
Pattern expectedEpisodeFilePattern = Pattern.compile(logPattern +
        ".*target" + sep + "generated-sources" + sep + "jaxb" + sep + "META-INF" + sep +
        "JAXB" + sep + "episode_xjc\\.xjb");

boolean foundTarget = false;
boolean foundTargetVersion = false;
boolean foundExtension = false;
boolean foundEpisode = false;
boolean foundEpisodeFile = false;

List<String> lines = buildLog.readLines();
for (line in lines) {

  String trimmedLine = line.trim()
  if (trimmedLine.isEmpty()) {
    continue
  };

  if(!foundTarget && expectedTargetPattern.matcher(trimmedLine).matches()) {
    foundTarget = true;
  }

  if(!foundTargetVersion && expectedTargetVersionPattern.matcher(trimmedLine).matches()) {
    foundTargetVersion = true;
  }

  if(!foundExtension && expectedExtensionPattern.matcher(trimmedLine).matches()) {
    foundExtension = true;
  }

  if(!foundEpisode && expectedEpisodePattern.matcher(trimmedLine).matches()) {
    foundEpisode = true;
  }

  if(!foundEpisodeFile && expectedEpisodeFilePattern.matcher(trimmedLine).matches()) {
    foundEpisodeFile = true;
  }
}

// Assert some
def missingRequired(Pattern pattern) {
  return "Missing required pattern: [" + pattern.pattern() + "]" ;
}

assert foundTarget, missingRequired(expectedTargetPattern);
assert foundTargetVersion, missingRequired(expectedTargetVersionPattern);
assert foundExtension, missingRequired(expectedExtensionPattern);
assert foundEpisode, missingRequired(expectedEpisodePattern);
assert foundEpisodeFile, missingRequired(expectedEpisodeFilePattern);

