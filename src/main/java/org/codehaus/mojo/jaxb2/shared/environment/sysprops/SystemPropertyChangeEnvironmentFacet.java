package org.codehaus.mojo.jaxb2.shared.environment.sysprops;

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

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.logging.Log;
import org.codehaus.mojo.jaxb2.AbstractJaxbMojo;
import org.codehaus.mojo.jaxb2.shared.Validate;
import org.codehaus.mojo.jaxb2.shared.environment.AbstractLogAwareFacet;

/**
 * EnvironmentFacet which changes the value of a system property for the duration
 * of executing a tool. This is required for tools (such as the JDK SchemaGen) which
 * relies on environment or system properties being set for their execution.
 * This faced accepts one key and two values (original/new values).
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 * @since 2.1
 */
public final class SystemPropertyChangeEnvironmentFacet extends AbstractLogAwareFacet {

    /**
     * Operation definitions indicating how a System property should be changed by this EnvironmentFacet.
     */
    public enum ChangeType {

        /**
         * Indicates that a System property should be added during {@code setup()}
         * and removed during {@code restore()}. If the property was already present,
         * this behaves like {@code #CHANGE}.
         */
        ADD,

        /**
         * Indicates that a System property should be removed during {@code setup()}
         * and restored/re-added during {@code restore()}
         */
        REMOVE,

        /**
         * Indicates that a System property should be altered during {@code setup()}
         * and restored during {@code restore()}
         */
        CHANGE
    }

    // Internal state
    private ChangeType type;
    private String key;
    private String newValue;
    private String originalValue;

    /**
     * Creates a SystemPropertyChange which will remove the supplied system property for the
     * duration of this SystemPropertyChange. No exception will be thrown if the supplied System property
     * key is not found in the present System.properties.
     *
     * @param log The active Maven Log.
     * @param key A non-null key.
     * @see SystemPropertyChangeEnvironmentFacet.ChangeType#REMOVE
     */
    private SystemPropertyChangeEnvironmentFacet(final Log log, final String key) {

        // Delegate
        super(log);

        // Assign internal state
        this.key = key;
        this.type = ChangeType.REMOVE;
    }

    /**
     * Creates a SystemPropertyChange which stores the current
     *
     * @param log      The active Maven Log.
     * @param key      The key of the System property managed by this SystemPropertyChange.
     * @param newValue The new value of this SystemPropertyChange.
     */
    private SystemPropertyChangeEnvironmentFacet(final Log log, final String key, final String newValue) {

        // Delegate
        super(log);

        // Assign internal state
        this.key = key;
        this.originalValue = System.getProperty(key);
        this.newValue = newValue;
        this.type = existsAsSystemProperty(key) ? ChangeType.CHANGE : ChangeType.ADD;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setup() {

        if (type == ChangeType.REMOVE) {
            System.clearProperty(key);
        } else {
            System.setProperty(key, newValue);
        }

        if (log.isDebugEnabled()) {
            log.debug("Setup " + toString());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void restore() {

        if (type == ChangeType.ADD) {
            System.clearProperty(key);
        } else {
            System.setProperty(key, originalValue);
        }

        if (log.isDebugEnabled()) {
            log.debug("Restored " + toString());
        }
    }

    /**
     * @return A Debug string representation of this SystemPropertyChangeEnvironmentFacet.
     */
    @Override
    public String toString() {
        final String toReturn = "SysProp key [" + key + "]\n"
                + "  ... Original value: [" + originalValue + "]\n"
                + "  ... Changed value : [" + newValue + "]";
        return toReturn.replace("\n", AbstractJaxbMojo.NEWLINE);
    }

    /**
     * Creates a SystemPropertyChangesBuilder which uses the supplied active Maven Log.
     *
     * @param mavenLog The active Maven Log to be used by all SystemPropertyChange objects created
     *                 by this SystemPropertyChangesBuilder.
     * @return A SystemPropertyChangesBuilder ready for use.
     */
    public static SystemPropertyChangesBuilder getBuilder(final Log mavenLog) {
        return new SystemPropertyChangesBuilder(mavenLog);
    }

    /**
     * Builder class intended to simplify creating SystemPropertyChange EnvironmentFacets.
     */
    public static class SystemPropertyChangesBuilder {

        // Internal state
        private List<SystemPropertyChangeEnvironmentFacet> toReturn;
        private Log mavenLog;

        private SystemPropertyChangesBuilder(final Log mavenLog) {

            // Check sanity
            Validate.notNull(mavenLog, "mavenLog");

            // Assign internal state
            this.toReturn = new ArrayList<SystemPropertyChangeEnvironmentFacet>();
            this.mavenLog = mavenLog;
        }

        /**
         * Adds a SystemPropertyChange which removes the named System property.
         *
         * @param propertyName The name of the system property for which to create a REMOVE-type SystemPropertyChange.
         * @return This builder, for chaining.
         * @see SystemPropertyChangeEnvironmentFacet.ChangeType#REMOVE
         */
        public SystemPropertyChangesBuilder remove(final String propertyName) {

            // Check sanity
            checkSanity(propertyName);

            // Add the SystemPropertyChange.
            toReturn.add(new SystemPropertyChangeEnvironmentFacet(mavenLog, propertyName));

            // All done.
            return this;
        }

        /**
         * Adds a SystemPropertyChange which adds or changes the named System property.
         *
         * @param propertyName The name of the system property for which to create an
         *                     ADD- or CREATE-type SystemPropertyChange.
         * @param value        The new value of the system property to set.
         * @return This builder, for chaining.
         * @see SystemPropertyChangeEnvironmentFacet.ChangeType#ADD
         * @see SystemPropertyChangeEnvironmentFacet.ChangeType#CHANGE
         */
        public SystemPropertyChangesBuilder addOrChange(final String propertyName, final String value) {

            // Check sanity
            checkSanity(propertyName);

            // Add the SystemPropertyChange.
            toReturn.add(new SystemPropertyChangeEnvironmentFacet(mavenLog, propertyName, value));

            // All done.
            return this;
        }

        /**
         * @return A List of SystemPropertyChange EnvironmentFacets which can be included as required into the
         * ToolExecutionEnvironment.
         */
        public List<SystemPropertyChangeEnvironmentFacet> build() {
            return toReturn;
        }

        //
        // Private helpers
        //

        private void checkSanity(final String propertyName) {

            // Check sanity
            Validate.notEmpty(propertyName, "propertyName");

            // Validate that the property name is not already present as a SystemPropertyChange.
            for (SystemPropertyChangeEnvironmentFacet current : toReturn) {
                if (current.key.equals(propertyName)) {
                    throw new IllegalArgumentException("A SystemPropertyChange for propertyName '"
                            + propertyName + "' is already present. Only one SystemPropertyChange per propertyName "
                            + "should be supplied.");
                }
            }
        }
    }

    //
    // Private helpers
    //

    private boolean existsAsSystemProperty(final String key) {
        return System.getProperties().keySet().contains(key);
    }
}
