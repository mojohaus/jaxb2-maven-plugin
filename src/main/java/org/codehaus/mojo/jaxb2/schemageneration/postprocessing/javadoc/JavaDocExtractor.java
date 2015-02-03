package org.codehaus.mojo.jaxb2.schemageneration.postprocessing.javadoc;

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

import com.thoughtworks.qdox.JavaProjectBuilder;
import com.thoughtworks.qdox.model.JavaAnnotatedElement;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaField;
import com.thoughtworks.qdox.model.JavaMethod;
import com.thoughtworks.qdox.model.JavaPackage;
import com.thoughtworks.qdox.model.JavaSource;
import edu.emory.mathcs.backport.java.util.Collections;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.mojo.jaxb2.schemageneration.postprocessing.javadoc.location.ClassLocation;
import org.codehaus.mojo.jaxb2.schemageneration.postprocessing.javadoc.location.FieldLocation;
import org.codehaus.mojo.jaxb2.schemageneration.postprocessing.javadoc.location.MethodLocation;
import org.codehaus.mojo.jaxb2.schemageneration.postprocessing.javadoc.location.PackageLocation;
import org.codehaus.mojo.jaxb2.shared.FileSystemUtilities;
import org.codehaus.mojo.jaxb2.shared.Validate;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;

/**
 * <p>The schemagen tool operates on compiled bytecode, where JavaDoc comments are not present.
 * However, the javadoc documentation present in java source files is required within the generated
 * XSD to increase usability and produce an XSD which does not loose out on important usage information.</p>
 * <p>The JavaDocExtractor is used as a post processor after creating the XSDs within the compilation
 * unit, and injects XSD annotations into the appropriate XSD elements or types.</p>
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 * @since 2.0
 */
public class JavaDocExtractor {

    // Internal state
    private JavaProjectBuilder builder;
    private Log log;

    /**
     * Creates a JavaDocExtractor wrapping the supplied Maven Log.
     *
     * @param log A non-null Log.
     */
    public JavaDocExtractor(final Log log) {

        // Check sanity
        Validate.notNull(log, "log");

        // Create internal state
        this.log = log;
        this.builder = new JavaProjectBuilder();
    }

    /**
     * Adds the supplied sourceCodeFiles for processing by this JavaDocExtractor.
     *
     * @param sourceCodeFiles The non-null List of source code files to add.
     * @return This JavaDocExtractor, for call chaining.
     * @throws IllegalArgumentException If any of the given sourceCodeFiles could not be read properly.
     */
    public JavaDocExtractor addSourceFiles(final List<File> sourceCodeFiles) throws IllegalArgumentException {

        // Check sanity
        Validate.notNull(sourceCodeFiles, "addSourceFiles");

        // Add the files.
        for (File current : sourceCodeFiles) {
            try {
                builder.addSource(current);
            } catch (IOException e) {
                throw new IllegalArgumentException("Could not add file ["
                        + FileSystemUtilities.getCanonicalPath(current) + "]", e);
            }
        }

        // All done.
        return this;
    }

    /**
     * Adds the supplied sourceCodeFiles for processing by this JavaDocExtractor.
     *
     * @param sourceCodeURLs The non-null List of source code URLs to add.
     * @return This JavaDocExtractor, for call chaining.
     * @throws IllegalArgumentException If any of the given sourceCodeURLs could not be read properly.
     */
    public JavaDocExtractor addSourceURLs(final List<URL> sourceCodeURLs) throws IllegalArgumentException {

        // Check sanity
        Validate.notNull(sourceCodeURLs, "sourceCodeURLs");

        // Add the URLs
        for (URL current : sourceCodeURLs) {
            try {
                builder.addSource(current);
            } catch (IOException e) {
                throw new IllegalArgumentException("Could not add URL [" + current.toString() + "]", e);
            }
        }

        // All done
        return this;
    }

    /**
     * Processes all supplied Java source Files and URLs to extract JavaDocData for all ClassLocations from which
     * JavaDoc has been collected.
     *
     * @return A SearchableDocumentation relating SortableLocations and their paths to harvested JavaDocData.
     */
    public SearchableDocumentation process() {

        // Start processing.
        final SortedMap<SortableLocation, JavaDocData> dataHolder = new TreeMap<SortableLocation, JavaDocData>();
        final Collection<JavaSource> sources = builder.getSources();

        if (log.isInfoEnabled()) {
            log.info("Processing [" + sources.size() + "] java sources.");
        }

        for (JavaSource current : sources) {

            // Add the package-level JavaDoc
            final JavaPackage currentPackage = current.getPackage();
            final String packageName = currentPackage.getName();
            addEntry(dataHolder, new PackageLocation(packageName), currentPackage);

            if (log.isDebugEnabled()) {
                log.debug("Added package-level JavaDoc for [" + packageName + "]");
            }

            for (JavaClass currentClass : current.getClasses()) {

                // Add the class-level JavaDoc
                final String simpleClassName = currentClass.getName();
                final ClassLocation classLocation = new ClassLocation(packageName, simpleClassName);
                addEntry(dataHolder, classLocation, currentClass);

                if (log.isDebugEnabled()) {
                    log.debug("Added class-level JavaDoc for [" + classLocation + "]");
                }

                for (JavaField currentField : currentClass.getFields()) {

                    // Add the field-level JavaDoc
                    final FieldLocation fieldLocation = new FieldLocation(
                            packageName,
                            simpleClassName,
                            currentField.getName());

                    addEntry(dataHolder, fieldLocation, currentField);

                    if (log.isDebugEnabled()) {
                        log.debug("Added field-level JavaDoc for [" + fieldLocation + "]");
                    }
                }

                for (JavaMethod currentMethod : currentClass.getMethods()) {

                    // Add the method-level JavaDoc
                    final MethodLocation location = new MethodLocation(packageName,
                            simpleClassName,
                            currentMethod.getName(),
                            currentMethod.getParameters());
                    addEntry(dataHolder, location, currentMethod);

                    if (log.isDebugEnabled()) {
                        log.debug("Added method-level JavaDoc for [" + location + "]");
                    }
                }
            }
        }

        // All done.
        return new ReadOnlySearchableDocumentation(dataHolder);
    }

    //
    // Private helpers
    //

    private void addEntry(final SortedMap<SortableLocation, JavaDocData> map,
                          final SortableLocation key,
                          final JavaAnnotatedElement value) {

        // Check sanity
        if (map.containsKey(key)) {

            // Get something to compare with
            final JavaDocData existing = map.get(key);

            // Is this an empty package-level documentation?
            if (key instanceof PackageLocation) {

                final boolean emptyExisting = existing.getComment() == null || existing.getComment().isEmpty();
                final boolean emptyGiven = value.getComment() == null || value.getComment().isEmpty();

                if (emptyGiven) {
                    if (log.isDebugEnabled()) {
                        log.debug("Skipping processing empty Package javadoc from [" + key + "]");
                    }
                    return;
                } else if (emptyExisting) {
                    if (log.isWarnEnabled()) {
                        log.warn("Overwriting empty Package javadoc from [" + key + "]");
                    }
                }
            } else {
                final String given = "[" + value.getClass().getName() + "]: " + value.getComment();
                throw new IllegalArgumentException("Not processing duplicate SortableLocation [" + key + "]. "
                        + "\n Existing: " + existing
                        + ".\n Given: [" + given + "]");
            }
        }

        // Validate.isTrue(!map.containsKey(key), "Found duplicate SortableLocation [" + key + "] in map. "
        //         + "Current map keySet: " + map.keySet() + ". Got comment: [" + value.getComment() + "]");

        map.put(key, new JavaDocData(value.getComment(), value.getTags()));
    }

    /**
     * Standard read-only SearchableDocumentation implementation.
     */
    class ReadOnlySearchableDocumentation implements SearchableDocumentation {

        // Internal state
        private TreeMap<String, SortableLocation> keyMap;
        private SortedMap<? extends SortableLocation, JavaDocData> valueMap;

        ReadOnlySearchableDocumentation(final SortedMap<SortableLocation, JavaDocData> valueMap) {

            // Create internal state
            this.valueMap = valueMap;

            keyMap = new TreeMap<String, SortableLocation>();
            for (Map.Entry<SortableLocation, JavaDocData> current : valueMap.entrySet()) {

                final SortableLocation key = current.getKey();
                keyMap.put(key.getPath(), key);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public SortedSet<String> getPaths() {
            return Collections.unmodifiableSortedSet(keyMap.navigableKeySet());
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public JavaDocData getJavaDoc(final String path) {

            // Check sanity
            Validate.notNull(path, "path");

            // All done.
            final SortableLocation location = getLocation(path);
            return (location == null) ? null : valueMap.get(location);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        @SuppressWarnings("unchecked")
        public <T extends SortableLocation> T getLocation(final String path) {

            // Check sanity
            Validate.notNull(path, "path");

            // All done
            return (T) keyMap.get(path);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        @SuppressWarnings("unchecked")
        public SortedMap<SortableLocation, JavaDocData> getAll() {
            return Collections.unmodifiableSortedMap(valueMap);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        @SuppressWarnings("unchecked")
        public <T extends SortableLocation> SortedMap<T, JavaDocData> getAll(final Class<T> type) {

            // Check sanity
            Validate.notNull(type, "type");

            // Filter the valueMap.
            final SortedMap<T, JavaDocData> toReturn = new TreeMap<T, JavaDocData>();
            for (Map.Entry<? extends SortableLocation, JavaDocData> current : valueMap.entrySet()) {
                if (type == current.getKey().getClass()) {
                    toReturn.put((T) current.getKey(), current.getValue());
                }
            }

            // All done.
            return toReturn;
        }
    }
}
