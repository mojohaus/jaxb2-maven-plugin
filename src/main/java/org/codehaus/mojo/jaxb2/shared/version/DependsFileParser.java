package org.codehaus.mojo.jaxb2.shared.version;

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

import org.codehaus.mojo.jaxb2.shared.Validate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.StringTokenizer;
import java.util.TreeMap;

/**
 * Trivial parser to handle depends-plugin-style files.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public final class DependsFileParser {

    /**
     * String indicating that a line in a dependencies.properties file contains a version definition.
     */
    private static final String VERSION_LINE_INDICATOR = "/version";

    /**
     * String indicating that a line in a dependencies.properties file contains a type definition.
     */
    private static final String TYPE_LINE_INDICATOR = "/type";

    /**
     * String indicating that a line in a dependencies.properties file contains a scope definition.
     */
    private static final String SCOPE_LINE_INDICATOR = "/scope";

    // Internal state
    private static final String GROUP_ARTIFACT_SEPARATOR = "/";
    private static final String KEY_VALUE_SEPARATOR = "=";
    private static final String DEPENDENCIES_PROPERTIES_FILE = "META-INF/maven/dependencies.properties";
    private static final String GENERATION_PREFIX = "# Generated at: ";

    /**
     * The key where the build time as found within the dependencies.properties file is found.
     */
    public static final String BUILDTIME_KEY = "buildtime";

    /**
     * The key holding the artifactId of this plugin (within the dependencies.properties file).
     */
    public static final String OWN_ARTIFACTID_KEY = "artifactId";

    /**
     * The key holding the groupId of this plugin (within the dependencies.properties file).
     */
    public static final String OWN_GROUPID_KEY = "groupId";

    /**
     * The key holding the version of this plugin (within the dependencies.properties file).
     */
    public static final String OWN_VERSION_KEY = "version";

    /**
     * Hide constructors for utility classes
     */
    private DependsFileParser() {
    }

    /**
     * Extracts all build-time dependency information from a dependencies.properties file
     * embedded in this plugin's JAR.
     *
     * @param artifactId This plugin's artifactId.
     * @return A SortedMap relating [groupId]/[artifactId] keys to DependencyInfo values.
     * @throws java.lang.IllegalStateException if no artifact in the current Thread's context ClassLoader
     *                                         contained the supplied artifactNamePart.
     */
    public static SortedMap<String, String> getVersionMap(final String artifactId) {

        // Check sanity
        Validate.notEmpty(artifactId, "artifactNamePart");

        Exception extractionException = null;

        try {
            // Get the ClassLoader used
            final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
            final List<URL> manifestURLs = Collections.list(
                    contextClassLoader.getResources(DEPENDENCIES_PROPERTIES_FILE));

            // Find the latest of the URLs matching, to cope with test-scope dependencies.
            URL matching = null;
            for (URL current : manifestURLs) {
                if (current.toString().contains(artifactId)) {
                    matching = current;
                }
            }

            if(matching != null) {
                return getVersionMap(matching);
            }

        } catch (Exception e) {
            extractionException = e;
        }

        // We should never wind up here ...
        if (extractionException != null) {
            throw new IllegalStateException("Could not read data from manifest.", extractionException);
        } else {
            throw new IllegalStateException("Found no manifest corresponding to artifact name snippet '"
                    + artifactId + "'.");
        }
    }

    /**
     * Extracts all build-time dependency information from a dependencies.properties file
     * embedded in this plugin's JAR.
     *
     * @param anURL The non-empty URL to a dependencies.properties file.
     * @return A SortedMap holding all entries in the dependencies.properties file, plus its build
     * time which is found under the {@code buildtime} key.
     * @throws java.lang.IllegalStateException if no artifact in the current Thread's context ClassLoader
     *                                         contained the supplied artifactNamePart.
     */
    public static SortedMap<String, String> getVersionMap(final URL anURL) {

        // Check sanity
        Validate.notNull(anURL, "anURL");

        final SortedMap<String, String> toReturn = new TreeMap<String, String>();

        try {
            final BufferedReader in = new BufferedReader(new InputStreamReader(anURL.openStream()));
            String aLine = null;

            while ((aLine = in.readLine()) != null) {

                final String trimmedLine = aLine.trim();

                if (trimmedLine.contains(GENERATION_PREFIX)) {
                    toReturn.put(BUILDTIME_KEY, aLine.substring(GENERATION_PREFIX.length()));
                } else if ("".equals(trimmedLine) || trimmedLine.startsWith("#")) {
                    // Empty lines and comments should be ignored.
                    continue;
                } else if (trimmedLine.contains("=")) {

                    // Stash this for later use.
                    StringTokenizer tok = new StringTokenizer(trimmedLine, KEY_VALUE_SEPARATOR, false);
                    Validate.isTrue(tok.countTokens() == 2, "Found incorrect dependency.properties line ["
                            + aLine + "]");

                    final String key = tok.nextToken().trim();
                    final String value = tok.nextToken().trim();

                    toReturn.put(key, value);
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not parse dependency properties '" + anURL.toString() + "'", e);
        }

        // All done.
        return toReturn;
    }

    /**
     * Converts a SortedMap received from a {@code getVersionMap} call to hold DependencyInfo values,
     * and keys on the form {@code groupId/artifactId}.
     *
     * @param versionMap A non-null Map, as received from a call to {@code getVersionMap}.
     * @return a SortedMap received from a {@code getVersionMap} call to hold DependencyInfo values,
     * and keys on the form {@code groupId/artifactId}.
     */
    public static SortedMap<String, DependencyInfo> createDependencyInfoMap(
            final SortedMap<String, String> versionMap) {

        // Check sanity
        Validate.notNull(versionMap, "anURL");

        final SortedMap<String, DependencyInfo> toReturn = new TreeMap<String, DependencyInfo>();

        // First, only find the version lines.
        for (Map.Entry<String, String> current : versionMap.entrySet()) {

            final String currentKey = current.getKey().trim();
            if (currentKey.contains(VERSION_LINE_INDICATOR)) {

                final StringTokenizer tok = new StringTokenizer(currentKey, GROUP_ARTIFACT_SEPARATOR, false);
                Validate.isTrue(tok.countTokens() == 3, "Expected key on the form [groupId]"
                        + GROUP_ARTIFACT_SEPARATOR + "[artifactId]" + VERSION_LINE_INDICATOR + ", but got ["
                        + currentKey + "]");

                final String groupId = tok.nextToken();
                final String artifactId = tok.nextToken();

                final DependencyInfo di = new DependencyInfo(groupId, artifactId, current.getValue());
                toReturn.put(di.getGroupArtifactKey(), di);
            }
        }

        for (Map.Entry<String, DependencyInfo> current : toReturn.entrySet()) {

            final String currentKey = current.getKey();
            final DependencyInfo di = current.getValue();

            final String scope = versionMap.get(currentKey + SCOPE_LINE_INDICATOR);
            final String type = versionMap.get(currentKey + TYPE_LINE_INDICATOR);

            if (scope != null) {
                di.setScope(scope);
            }
            if (type != null) {
                di.setType(type);
            }
        }

        // All done.
        return toReturn;
    }
}
