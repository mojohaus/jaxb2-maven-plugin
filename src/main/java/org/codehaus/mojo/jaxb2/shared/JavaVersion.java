package org.codehaus.mojo.jaxb2.shared;

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

/**
 * Helper to extract the runtime Java version from the System.properties.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>
 * @since 2.4
 */
public final class JavaVersion {

    private static final String JAVA_VERSION_PROPERTY = "java.specification.version";

    /**
     * Retrieves the major java runtime version as an integer.
     *
     * @return the major java runtime version as an integer.
     */
    public static int getJavaMajorVersion() {

        final String[] versionElements = System.getProperty(JAVA_VERSION_PROPERTY).split("\\.");
        final int[] versionNumbers = new int[versionElements.length];

        for (int i = 0; i < versionElements.length; i++) {
            try {
                versionNumbers[i] = Integer.parseInt(versionElements[i]);
            } catch (NumberFormatException e) {
                versionNumbers[i] = 0;
            }
        }

        /*
        Java versions 1 - 8 (i.e. jdk 1.1 through 1.8) yields the structure 1.8
        Java versions 9 - yields the structure 10

        JDK 10
        ======
        [java.specification.version]: 10

        JDK 8
        =====
        [java.specification.version]: 1.8

        JDK 7
        =====
        [java.specification.version]: 1.7
        */
        return versionNumbers[0] == 1 ? versionNumbers[1] : versionNumbers[0];
    }

    /**
     * Checks if the runtime java version is JDK 8 or lower.
     *
     * @return true if the runtime java version is JDK 8 or lower.
     */
    public static boolean isJdk8OrLower() {
        return getJavaMajorVersion() <= 8;
    }
}
