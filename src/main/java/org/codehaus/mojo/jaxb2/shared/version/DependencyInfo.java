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

/**
 * Trivial holder class for dependency information, as found within a dependencies.properties file.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 * @since 2.0
 */
public class DependencyInfo implements Comparable<DependencyInfo> {

    // Internal state
    private static final String GROUP_ARTIFACT_SEPARATOR = "/";
    private String groupId;
    private String artifactId;
    private String version;
    private String scope = "compile";
    private String type = "jar";

    public DependencyInfo(final String groupId, final String artifactId, final String version) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
    }

    /**
     * @return The GroupId of this DependencyInfo.
     */
    public String getGroupId() {
        return groupId;
    }

    /**
     * @return The ArtifactId of this DependencyInfo.
     */
    public String getArtifactId() {
        return artifactId;
    }

    /**
     * @return The Maven version of this DependencyInfo.
     */
    public String getVersion() {
        return version;
    }

    /**
     * @return The type of this DependencyInfo.
     */
    public String getType() {
        return type;
    }

    /**
     * @return The scope of this DependencyInfo.
     */
    public String getScope() {
        return scope;
    }

    /**
     * Assigns the type of this DependencyInfo.
     *
     * @param type The non-empty type of this DependencyInfo.
     */
    public void setType(final String type) {

        // Check sanity
        Validate.notEmpty(type, "type");

        // Assign internal state
        this.type = type;
    }

    /**
     * Assigns the scope of this DependencyInfo.
     *
     * @param scope The non-empty scope of this DependencyInfo.
     */
    public void setScope(final String scope) {

        // Check sanity
        Validate.notEmpty(scope, "scope");

        // Assign internal state.
        this.scope = scope;
    }

    /**
     * @return A key for use within a SortedSet where this DependencyInfo should be sorted.
     */
    public String getGroupArtifactKey() {
        return getGroupId() + GROUP_ARTIFACT_SEPARATOR + getArtifactId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return groupId.hashCode() + artifactId.hashCode() + version.hashCode() + type.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return groupId + GROUP_ARTIFACT_SEPARATOR
                + artifactId + GROUP_ARTIFACT_SEPARATOR
                + version + GROUP_ARTIFACT_SEPARATOR
                + scope + GROUP_ARTIFACT_SEPARATOR
                + type;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final DependencyInfo that) {

        // Return quick.
        if (that == this) {
            return 0;
        }
        if (that == null) {
            return -1;
        }

        // Compare internal stater
        int toReturn = this.getGroupId().compareTo(that.getGroupId());
        if (toReturn == 0) {
            toReturn = this.getArtifactId().compareTo(that.getArtifactId());
        }
        if (toReturn == 0) {
            toReturn = this.getVersion().compareTo(that.getVersion());
        }
        if (toReturn == 0) {
            toReturn = this.getType().compareTo(that.getType());
        }

        // All done.
        return toReturn;
    }
}
