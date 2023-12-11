package org.codehaus.mojo.jaxb2.shared.arguments;

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

import org.codehaus.mojo.jaxb2.AbstractJaxbMojo;
import org.codehaus.mojo.jaxb2.shared.Validate;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to build an array containing method arguments, as received from a command-line invocation of a tool.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>
 */
public final class ArgumentBuilder {

    // Internal state
    private final Object lock = new Object();
    private static final int NOT_FOUND = -1;
    private static final char DASH = '-';
    private List<String> arguments = new ArrayList<String>();

    /**
     * <p>Retrieves all arguments as a string array, usable by a method accepting a String[] for argument.
     * This would be true of {@code public static void main(String[] args)}, as well as the entry points
     * for both the XJC and the Schemagen tools.</p>
     *
     * @return an array holding all arguments in this ArgumentBuilder.
     */
    public String[] build() {

        synchronized (lock) {
            final String[] toReturn = new String[arguments.size()];
            return arguments.toArray(toReturn);
        }
    }

    /**
     * <p>Adds a flag on the form {@code -someflag} to the list of arguments contained within this ArgumentBuilder.
     * If the {@code flag} argument does not start with a dash ('-'), one will be prepended.</p>
     * <p>Typical usage:</p>
     * <pre><code>
     *     argumentBuilder
     *      .withFlag(someBooleanParameter, "foobar")
     *      .withFlag(someOtherBooleanParameter, "gnat")
     *      .withFlag(someThirdBooleanParameter, "gnu")
     *      ....
     * </code></pre>
     *
     * @param addFlag if {@code true}, the flag will be added to the underlying list of arguments
     *                within this ArgumentBuilder.
     * @param flag    The flag/argument to add. The flag must be a complete word, implying it
     *                cannot contain whitespace.
     * @return This ArgumentBuilder, for chaining.
     */
    public ArgumentBuilder withFlag(final boolean addFlag, final String flag) {

        // Bail out?
        if (!addFlag) {
            return this;
        }

        // Check sanity
        Validate.notEmpty(flag, "flag");
        Validate.isTrue(!AbstractJaxbMojo.CONTAINS_WHITESPACE.matcher(flag).matches(),
                "Flags cannot contain whitespace. Got: [" + flag + "]");

        // Trim, and add the flag as an argument.
        final String trimmed = flag.trim();
        Validate.notEmpty(trimmed, "flag");

        // Prepend the DASH if required
        final String toAdd = trimmed.charAt(0) != DASH ? DASH + trimmed : trimmed;

        // Assign the argument only if not already set.
        if (getIndexForFlag(toAdd) == NOT_FOUND) {
            synchronized (lock) {
                arguments.add(toAdd);
            }
        }

        // All done.
        return this;
    }

    /**
     * <p>Adds a name and an argument on the form {@code -name value} to the list of arguments contained
     * within this ArgumentBuilder. The two parts will yield 2 elements in the underlying argument list.
     * If the {@code name} argument does not start with a dash ('-'), one will be prepended.</p>
     * <p>Typical usage:</p>
     * <pre><code>
     *     // These values should be calculated as part of the business logic
     *     final boolean addFooBar = true;
     *     final boolean addGnat = true;
     *     final boolean addGnu = false;
     *
     *     // Add all relevant arguments
     *     argumentBuilder
     *      .withNamedArgument(addFooBar, "foobar", "foobarValue")
     *      .withNamedArgument(addGnat, "-gnat", "gnatValue")
     *      .withNamedArgument(addGnu, "gnu", "gnuValue")
     *      ....
     * </code></pre>
     *
     * @param addNamedArgument if {@code true}, the named argument (name and value) will be added to
     *                         the underlying list of arguments within this ArgumentBuilder.
     * @param name             The name of the namedArgument to add. Cannot be empty.
     * @param value            The value of the namedArgument to add.
     * @return This ArgumentBuilder, for chaining.
     */
    public ArgumentBuilder withNamedArgument(final boolean addNamedArgument,
                                             final String name,
                                             final String value) {

        // Bail out?
        if (!addNamedArgument) {
            return this;
        }

        // Check sanity
        Validate.notEmpty(name, "name");
        Validate.notEmpty(value, "value");

        // Trim the arguments, and validate again.
        final String trimmedName = name.trim();
        final String trimmedValue = value.trim();
        Validate.notEmpty(trimmedName, "name");
        Validate.notEmpty(trimmedValue, "value");

        // Add or update the name and value.
        if (!updateValueForNamedArgument(name, value)) {
            synchronized (lock) {
                withFlag(true, trimmedName);
                arguments.add(value);
            }
        }

        // All done.
        return this;
    }

    /**
     * Convenience form for the {@code withNamedArgument} method, where a named argument is only added
     * if the value is non-null and non-empty after trimming.
     *
     * @param name  The name of the namedArgument to add. Cannot be empty.
     * @param value The value of the namedArgument to add.
     * @return This ArgumentBuilder, for chaining.
     * @see #withNamedArgument(boolean, String, String)
     */
    public ArgumentBuilder withNamedArgument(final String name, final String value) {

        // Check sanity
        Validate.notEmpty(name, "name");

        // Only add a named argument if the value is non-empty.
        if (value != null && !value.trim().isEmpty()) {
            withNamedArgument(true, name, value.trim());
        }

        // All done.
        return this;
    }

    /**
     * Adds the supplied prefixed arguments in the same order as they were given.
     *
     * @param prefix The prefix to add.
     * @param values A non-null List holding arguments.
     * @return This ArgumentBuilder, for chaining.
     */
    public ArgumentBuilder withPrefixedArguments(String prefix, final List<String> values) {

        // Check sanity
        Validate.notNull(prefix, "prefix");
        Validate.notNull(values, "arguments");

        // Add the arguments in the exact order they were given.
        synchronized (lock) {
            for (String value : values) {
                String trimmedValue = value.trim();
                arguments.add("-" + prefix + trimmedValue);
            }
        }

        // All done.
        return this;
    }

    /**
     * Adds the supplied pre-compiled arguments in the same order as they were given.
     *
     * @param preCompiledArguments A non-null List holding pre-compiled arguments.
     * @return This ArgumentBuilder, for chaining.
     */
    public ArgumentBuilder withPreCompiledArguments(final List<String> preCompiledArguments) {

        // Check sanity
        Validate.notNull(preCompiledArguments, "preCompiledArguments");

        // Add the preCompiledArguments in the exact order they were given.
        synchronized (lock) {
            for (String current : preCompiledArguments) {
                arguments.add(current);
            }
        }

        // All done.
        return this;
    }

    //
    // Private helpers
    //

    private int getIndexForFlag(final String name) {

        // Check sanity
        Validate.notEmpty(name, "name");

        synchronized (lock) {
            for (int i = 0; i < arguments.size(); i++) {
                if (arguments.get(i).equalsIgnoreCase(name)) {
                    return i;
                }
            }
        }

        // Not found.
        return NOT_FOUND;
    }

    private boolean updateValueForNamedArgument(final String name, final String newValue) {

        // Check sanity
        Validate.notEmpty(name, "name");

        int flagIndex = getIndexForFlag(name);
        if (flagIndex == NOT_FOUND) {

            // Nothing updated
            return false;
        }

        // Updating the value of the named argument.
        int valueIndex = flagIndex + 1;
        synchronized (lock) {
            arguments.set(valueIndex, newValue);
        }
        return true;
    }
}
