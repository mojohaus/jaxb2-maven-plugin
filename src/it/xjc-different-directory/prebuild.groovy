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

// Create a directory outside of the source tree in the system tmp directory
// and copy our own test xsd and pom.xml there. Then run a maven build there
// and examine the result.
//
// The directory in /tmp is deleted at the end of the test. Maven output
// from the build over there is available in mvn.out and mvn.err. Find them
// in target/it/xjc-different-directory.
def File tmp = File.createTempFile('jaxb2it', 'diffdir')
assert tmp.delete()
assert tmp.mkdirs()
try {
    def File here = new File("${basedir}")
    def File srcMain = new File(new File(tmp, 'src'), 'main')
    assert srcMain.mkdirs()
    def File src = new File(new File(here, 'testsrc'), 'simple.xsd')
    def File tgt = new File(srcMain, 'simple.xsd')
    tgt.bytes = src.bytes
    // Read the version under test from our own pom
    def File myPom = new File(here, "pom.xml")
    def List<String> myLines = myPom.readLines()
    def String myVersion = null
    for (String l : myLines) {
        if (l.contains('ownversion')) {
            myVersion = (l =~ /<ownversion>([^<]*)<\/ownversion/)[0][1]
            break;
        }
    }
    def List<String> srcLines = new File(src.parentFile, 'pom.xml').readLines()
    tgt = new File(tmp, 'pom.xml')
    tgt.withWriter { out ->
        srcLines.each {
            out.println it.replace('@project.version@', myVersion)
        }
    }
    def String cmd ='mvn clean compile ' +
        "-Dmaven.repo.local=${basedir}${File.separator}..${File.separator}..${File.separator}local-repo " +
        "-f ${tmp.absolutePath}${File.separator}pom.xml " +
        '-B -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn'
    def OutputStream mvnOut = new File(here, 'mvn.out').newOutputStream()
    mvnOut.write("Trying to run ${cmd}\n".bytes)
    def OutputStream mvnErr = new File(here, 'mvn.err').newOutputStream()
    def Process proc = cmd.execute()
    try {
        proc.waitForProcessOutput(mvnOut, mvnErr)
    } finally {
        mvnOut.close()
        mvnErr.close()
    }
    def resultCode = proc.exitValue()
    assert resultCode == 0
    // Check that something was generated
    def File generated = new File(tmp, 'target')
    generated = new File(generated, 'generated-sources')
    generated = new File(generated, 'jaxb')
    generated = new File(generated, 'se')
    generated = new File(generated, 'west')
    generated = new File(generated, 'HasAttribute.java')
    assert generated.isFile()
} finally {
    tmp.deleteDir();
}
