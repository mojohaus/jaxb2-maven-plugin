package org.codehaus.mojo.jaxb2.javageneration;

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

/**
 * <p>Configuration model for a Maven artifact containing a JAXB episode file, to be
 * used as an external binding by XJC when compiling another schema. Declared within
 * the {@code episodes} parameter of the {@code xjc} goal:</p>
 *
 * <pre>
 *     <code>
 *         &lt;episodes&gt;
 *             &lt;episode&gt;
 *                 &lt;groupId&gt;com.acme.schemas&lt;/groupId&gt;
 *                 &lt;artifactId&gt;schema-a&lt;/artifactId&gt;
 *                 &lt;version&gt;1.4.0&lt;/version&gt;
 *                 &lt;!-- optional elements: --&gt;
 *                 &lt;type&gt;jar&lt;/type&gt;
 *                 &lt;classifier&gt;&lt;/classifier&gt;
 *                 &lt;resourcePath&gt;META-INF/sun-jaxb.episode&lt;/resourcePath&gt;
 *                 &lt;optional&gt;false&lt;/optional&gt;
 *             &lt;/episode&gt;
 *         &lt;/episodes&gt;
 *     </code>
 * </pre>
 *
 * @see XjcMojo#episodes
 * @since 4.1.1
 */
public class EpisodeArtifact {

    private String groupId;
    private String artifactId;
    private String version;
    private String type = "jar";
    private String classifier;
    private String resourcePath;
    private boolean optional;

    /**
     * @return The Maven groupId of the episode artifact. Required.
     */
    public String getGroupId() {
        return groupId;
    }

    /**
     * @param groupId The Maven groupId of the episode artifact. Required.
     */
    public void setGroupId(final String groupId) {
        this.groupId = groupId;
    }

    /**
     * @return The Maven artifactId of the episode artifact. Required.
     */
    public String getArtifactId() {
        return artifactId;
    }

    /**
     * @param artifactId The Maven artifactId of the episode artifact. Required.
     */
    public void setArtifactId(final String artifactId) {
        this.artifactId = artifactId;
    }

    /**
     * @return The Maven version of the episode artifact. Required.
     */
    public String getVersion() {
        return version;
    }

    /**
     * @param version The Maven version of the episode artifact. Required.
     */
    public void setVersion(final String version) {
        this.version = version;
    }

    /**
     * @return The Maven artifact type (extension) of the episode artifact. Defaults to {@code jar}.
     */
    public String getType() {
        return type;
    }

    /**
     * @param type The Maven artifact type (extension) of the episode artifact. Defaults to {@code jar}.
     */
    public void setType(final String type) {
        this.type = type;
    }

    /**
     * @return The optional Maven classifier of the episode artifact.
     */
    public String getClassifier() {
        return classifier;
    }

    /**
     * @param classifier The optional Maven classifier of the episode artifact.
     */
    public void setClassifier(final String classifier) {
        this.classifier = classifier;
    }

    /**
     * <p>An explicit path to the episode resource within the artifact, relative to its root
     * (e.g. {@code META-INF/sun-jaxb.episode}). When omitted, the plugin discovers the episode
     * using its standard search order and fails if the result is ambiguous.</p>
     *
     * @return The explicit episode resource path within the artifact, or {@code null} for auto-discovery.
     */
    public String getResourcePath() {
        return resourcePath;
    }

    /**
     * @param resourcePath An explicit path to the episode resource within the artifact, relative to its root.
     */
    public void setResourcePath(final String resourcePath) {
        this.resourcePath = resourcePath;
    }

    /**
     * @return {@code true} if a missing episode resource within this artifact should be logged and
     * skipped rather than failing the build. Defaults to {@code false}.
     */
    public boolean isOptional() {
        return optional;
    }

    /**
     * @param optional {@code true} if a missing episode resource should be skipped rather than failing the build.
     */
    public void setOptional(final boolean optional) {
        this.optional = optional;
    }

    /**
     * Validates that the mandatory elements of this episode artifact are present.
     *
     * @throws IllegalArgumentException if groupId, artifactId or version is missing.
     */
    public void validate() {
        Validate.notEmpty(groupId, "groupId");
        Validate.notEmpty(artifactId, "artifactId");
        Validate.notEmpty(version, "version");
    }

    /**
     * @return The coordinate string {@code groupId:artifactId:type[:classifier]:version}.
     */
    public String getCoordinate() {
        final StringBuilder sb = new StringBuilder()
                .append(groupId)
                .append(':')
                .append(artifactId)
                .append(':')
                .append(type == null ? "jar" : type);
        if (classifier != null && !classifier.isEmpty()) {
            sb.append(':').append(classifier);
        }
        return sb.append(':').append(version).toString();
    }

    @Override
    public String toString() {
        return getCoordinate();
    }
}
