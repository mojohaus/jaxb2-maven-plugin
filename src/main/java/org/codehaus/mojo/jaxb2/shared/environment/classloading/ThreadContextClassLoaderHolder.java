package org.codehaus.mojo.jaxb2.shared.environment.classloading;

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
 * <p>Specification for how to restore the original ThreadContext ClassLoader to a Thread.
 * When we support JDK 1.7, this should really be an extension of AutoCloseable instead,
 * to support the try-with-resources pattern. Typical use:</p>
 * <pre>
 *     <code>
 *         // Create and set the ThreadContext ClassLoader
 *         ThreadContextClassLoaderHolder holder = null;
 *
 *         try {
 *
 *          holder = ThreadContextClassLoaderBuilder.createFor(getClass())
 *              .addPath("some/path")
 *              .addURL(someURL)
 *              .addPaths(aPathList)
 *              .buildAndSet();
 *
 *          // ... perform operations using the newly set ThreadContext ClassLoader...
 *
 *         } finally {
 *          // Restore the original ClassLoader
 *          holder.restoreClassLoaderAndReleaseThread();
 *         }
 *     </code>
 * </pre>
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 * @since 2.0
 */
public interface ThreadContextClassLoaderHolder {

    /**
     * Restores the original ThreadContext ClassLoader, and nullifies
     * any references to the Thread which had its ThreadContext
     * ClassLoader altered.
     */
    void restoreClassLoaderAndReleaseThread();

    /**
     * Retrieves the ClassPath held by this ThreadContextClassLoaderHolder as a
     * {@code File.pathSeparatorChar}-separated string. This is directly usable as a String argument by
     * any external process.
     *
     * @return the ClassPath as an argument to external processes such as XJC.
     */
    String getClassPathAsArgument();
}
