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

// Verify that the toString plugin argument is passed to XJC
File buildLog = new File(basedir, 'build.log')
List<String> lines = buildLog.readLines()

def xjcArgumentPatternPrefix = "\\| \\[\\p{Digit}+\\]: "
def toStringPlugin = 'toString'

final Pattern expectedToStringPluginPattern = Pattern.compile(xjcArgumentPatternPrefix + "-X${toStringPlugin}")

boolean foundToStringPluginArgument = false
def expectedErrorMessage = 'Caused by: com.sun.tools.xjc.BadCommandLineException:'

// Act
for (line in lines) {
    String trimmedLine = line.trim()
    if (trimmedLine.isEmpty()) {
        continue
    }
    
    if(!foundToStringPluginArgument && expectedToStringPluginPattern.matcher(trimmedLine).matches()) {
        foundToStringPluginArgument = true
    }
}

// Assert
static def missingRequired(Pattern pattern) {
    return "Missing required pattern: [" + pattern.pattern() + "]"
}

assert foundToStringPluginArgument, missingRequired(expectedToStringPluginPattern)

println "SUCCESS: toString plugin argument was correctly passed to XJC"

