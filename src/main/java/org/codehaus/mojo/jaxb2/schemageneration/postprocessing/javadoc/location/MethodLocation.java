package org.codehaus.mojo.jaxb2.schemageneration.postprocessing.javadoc.location;

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

import com.thoughtworks.qdox.model.JavaParameter;
import org.codehaus.mojo.jaxb2.shared.Validate;

import java.util.List;

/**
 * Comparable path structure to locate a particular method within compilation unit.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 * @since 2.0
 */
public class MethodLocation extends FieldLocation {

    /**
     * Signature for a method without any parameters.
     */
    public static final String NO_PARAMETERS = "()";

    /**
     * Separator for a method's parameters.
     */
    public static final String PARAMETER_SEPARATOR = ",";

    // Internal state
    private String parameters = NO_PARAMETERS;

    public MethodLocation(final String packageName,
                          final String className,
                          final String memberName,
                          final List<JavaParameter> parameters) {

        super(packageName, className, memberName);

        // Check sanity
        Validate.notNull(parameters, "parameters");

        // Stringify the parameter types
        if(parameters.size() > 0) {
            final StringBuilder builder = new StringBuilder();

            for(JavaParameter current : parameters) {
                builder.append(current.getType().getFullyQualifiedName()).append(PARAMETER_SEPARATOR);
            }
            this.parameters = "(" + builder.substring(0, builder.lastIndexOf(PARAMETER_SEPARATOR)) + ")";
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return super.toString() + parameters;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }

    /**
     * @return The parameters, concatenated into a String.
     */
    public String getParametersAsString() {
        return parameters;
    }

    /**
     * @return True if this MethodLocation has no parameters.
     */
    public boolean hasNoParameters() {
        return NO_PARAMETERS.equals(parameters);
    }
}
