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
 * Simple argument validator, inspired by the commons-lang.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>
 * @since 2.0
 */
public final class Validate {

    /**
     * Hide constructor for utility classes.
     */
    private Validate() {
    }

    /**
     * Validates that the supplied object is not null, and throws a NullPointerException otherwise.
     *
     * @param object       The object to validate for {@code null}-ness.
     * @param argumentName The argument name of the object to validate. If supplied (i.e. non-{@code null}),
     *                     this value is used in composing a better exception message.
     */
    public static void notNull(final Object object, final String argumentName) {
        if (object == null) {
            throw new NullPointerException(getMessage("null", argumentName));
        }
    }

    /**
     * Validates that the supplied object is not null, and throws an IllegalArgumentException otherwise.
     *
     * @param aString      The string to validate for emptyness.
     * @param argumentName The argument name of the object to validate.
     *                     If supplied (i.e. non-{@code null}), this value is used in composing
     *                     a better exception message.
     */
    public static void notEmpty(final String aString, final String argumentName) {

        // Check sanity
        notNull(aString, argumentName);

        if (aString.length() == 0) {
            throw new IllegalArgumentException(getMessage("empty", argumentName));
        }
    }

    /**
     * Validates that the supplied condition is true, and throws an IllegalArgumentException otherwise.
     *
     * @param condition The condition to validate for truth.
     * @param message   The exception message used within the IllegalArgumentException if the condition is false.
     */
    public static void isTrue(final boolean condition, final String message) {

        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }

    //
    // Private helpers
    //

    private static String getMessage(final String exceptionDefinition, final String argumentName) {
        return "Cannot handle "
                + exceptionDefinition
                + (argumentName == null ? "" : " '" + argumentName + "'")
                + " argument.";
    }
}
