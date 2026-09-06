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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;

import com.thoughtworks.qdox.JavaProjectBuilder;
import com.thoughtworks.qdox.model.JavaAnnotatedElement;
import com.thoughtworks.qdox.model.JavaAnnotation;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaField;
import com.thoughtworks.qdox.model.JavaMethod;
import com.thoughtworks.qdox.model.JavaPackage;
import com.thoughtworks.qdox.model.JavaSource;
import com.thoughtworks.qdox.model.expression.AnnotationValue;
import com.thoughtworks.qdox.model.expression.BinaryOperator;
import com.thoughtworks.qdox.model.expression.FieldRef;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlType;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.mojo.jaxb2.schemageneration.postprocessing.javadoc.location.ClassLocation;
import org.codehaus.mojo.jaxb2.schemageneration.postprocessing.javadoc.location.FieldLocation;
import org.codehaus.mojo.jaxb2.schemageneration.postprocessing.javadoc.location.MethodLocation;
import org.codehaus.mojo.jaxb2.schemageneration.postprocessing.javadoc.location.PackageLocation;
import org.codehaus.mojo.jaxb2.shared.FileSystemUtilities;
import org.codehaus.mojo.jaxb2.shared.Validate;

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

    /**
     * The default value given as the return value from some annotation classes whenever the attribute
     * has not been supplied within the codebase.
     */
    private static final String DEFAULT_VALUE = "##default";

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
     * Assigns the encoding of the underlying {@link JavaProjectBuilder}.
     *
     * @param encoding The non-empty encoding to be set into the underlying {@link JavaProjectBuilder}.
     */
    public void setEncoding(final String encoding) {
        this.builder.setEncoding(encoding);
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
                throw new IllegalArgumentException(
                        "Could not add file [" + FileSystemUtilities.getCanonicalPath(current) + "]", e);
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
            log.info("Processing [" + sources.size() + "] java sources.");
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
                final String classXmlName = getAnnotationAttributeValueFrom(
                        XmlType.class, "name", currentClass.getAnnotations(), currentClass);

                final ClassLocation classLocation = new ClassLocation(packageName, simpleClassName, classXmlName);
                addEntry(dataHolder, classLocation, currentClass);

                if (log.isDebugEnabled()) {
                    log.debug("Added class-level JavaDoc for [" + classLocation + "]");
                }

                for (JavaField currentField : currentClass.getFields()) {

                    final List<JavaAnnotation> currentFieldAnnotations = currentField.getAnnotations();
                    String annotatedXmlName = null;

                    //
                    // Is this field a collection, annotated with @XmlElementWrapper?
                    // If so, the documentation should pertain to the corresponding XML Sequence,
                    // rather than the individual XML elements.
                    //
                    if (hasAnnotation(XmlElementWrapper.class, currentFieldAnnotations)) {

                        // There are 2 cases here:
                        //
                        // 1: The XmlElementWrapper is named.
                        // ==================================
                        // @XmlElementWrapper(name = "foobar")
                        // @XmlElement(name = "aString")
                        // private List<String> strings;
                        //
                        // ==> annotatedXmlName == "foobar"
                        //
                        // 2: The XmlElementWrapper is not named.
                        // ======================================
                        // @XmlElementWrapper
                        // @XmlElement(name = "anInteger")
                        // private SortedSet<Integer> integerSet;
                        //
                        // ==> annotatedXmlName == "integerSet"
                        //
                        annotatedXmlName = getAnnotationAttributeValueFrom(
                                XmlElementWrapper.class, "name", currentFieldAnnotations, currentClass);

                        if (annotatedXmlName == null || annotatedXmlName.equals(DEFAULT_VALUE)) {
                            annotatedXmlName = currentField.getName();
                        }
                    }

                    // Find the XML name if provided within an annotation.
                    if (annotatedXmlName == null) {
                        annotatedXmlName = getAnnotationAttributeValueFrom(
                                XmlElement.class, "name", currentFieldAnnotations, currentClass);
                    }

                    if (annotatedXmlName == null) {
                        annotatedXmlName = getAnnotationAttributeValueFrom(
                                XmlAttribute.class, "name", currentFieldAnnotations, currentClass);
                    }
                    if (annotatedXmlName == null) {
                        annotatedXmlName = getAnnotationAttributeValueFrom(
                                XmlEnumValue.class, "value", currentFieldAnnotations, currentClass);
                    }

                    // Add the field-level JavaDoc
                    final FieldLocation fieldLocation = new FieldLocation(
                            packageName, simpleClassName, classXmlName, currentField.getName(), annotatedXmlName);

                    addEntry(dataHolder, fieldLocation, currentField);

                    if (log.isDebugEnabled()) {
                        log.debug("Added field-level JavaDoc for [" + fieldLocation + "]");
                    }
                }

                for (JavaMethod currentMethod : currentClass.getMethods()) {

                    final List<JavaAnnotation> currentMethodAnnotations = currentMethod.getAnnotations();
                    String annotatedXmlName = null;

                    //
                    // Is this field a collection, annotated with @XmlElementWrapper?
                    // If so, the documentation should pertain to the corresponding XML Sequence,
                    // rather than the individual XML elements.
                    //
                    if (hasAnnotation(XmlElementWrapper.class, currentMethodAnnotations)) {

                        // There are 2 cases here:
                        //
                        // 1: The XmlElementWrapper is named.
                        // ==================================
                        // @XmlElementWrapper(name = "foobar")
                        // @XmlElement(name = "aString")
                        // public List<String> getStrings() { ... };
                        //
                        // ==> annotatedXmlName == "foobar"
                        //
                        // 2: The XmlElementWrapper is not named.
                        // ======================================
                        // @XmlElementWrapper
                        // @XmlElement(name = "anInteger")
                        // public SortedSet<Integer> getIntegerSet() { ... };
                        //
                        // ==> annotatedXmlName == "getIntegerSet"
                        //
                        annotatedXmlName = getAnnotationAttributeValueFrom(
                                XmlElementWrapper.class, "name", currentMethodAnnotations, currentClass);

                        if (annotatedXmlName == null || annotatedXmlName.equals(DEFAULT_VALUE)) {
                            annotatedXmlName = currentMethod.getName();
                        }
                    }

                    // Find the XML name if provided within an annotation.
                    if (annotatedXmlName == null) {
                        annotatedXmlName = getAnnotationAttributeValueFrom(
                                XmlElement.class, "name", currentMethod.getAnnotations(), currentClass);
                    }

                    if (annotatedXmlName == null) {
                        annotatedXmlName = getAnnotationAttributeValueFrom(
                                XmlAttribute.class, "name", currentMethod.getAnnotations(), currentClass);
                    }

                    // Add the method-level JavaDoc
                    final MethodLocation location = new MethodLocation(
                            packageName,
                            simpleClassName,
                            classXmlName,
                            currentMethod.getName(),
                            annotatedXmlName,
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

    /**
     * Finds the value of the attribute with the supplied name within the first matching JavaAnnotation of
     * the given type encountered in the given annotations List. This is typically used for reading values of
     * annotations such as {@link XmlElement}, {@link XmlAttribute} or {@link XmlEnumValue}.
     *
     * @param annotations    The list of JavaAnnotations to filter from.
     * @param annotationType The type of annotation to read attribute values from.
     * @param attributeName  The name of the attribute the value of which should be returned.
     * @return The first matching JavaAnnotation of type annotationType within the given annotations
     * List, or {@code null} if none was found.
     * @since 2.2
     */
    private String getAnnotationAttributeValueFrom(
            final Class<?> annotationType,
            final String attributeName,
            final List<JavaAnnotation> annotations,
            final JavaClass currentClass) {

        // QDox uses the fully qualified class name of the annotation for comparison.
        // Extract it.
        final String fullyQualifiedClassName = annotationType.getName();

        JavaAnnotation annotation = null;
        String toReturn = null;

        if (annotations != null) {

            for (JavaAnnotation current : annotations) {
                if (current.getType().isA(fullyQualifiedClassName)) {
                    annotation = current;
                    break;
                }
            }

            if (annotation != null) {

                final AnnotationValue propertyValue = annotation.getProperty(attributeName);
                if (propertyValue != null) {
                    toReturn = resolveAnnotationValue(propertyValue, currentClass, this.builder, 0);
                }

                if (toReturn == null) {
                    final Object nameValue = annotation.getNamedParameter(attributeName);

                    if (nameValue instanceof String) {
                        toReturn = ((String) nameValue).trim();

                        // Remove initial and trailing " chars, if present.
                        if (toReturn.startsWith("\"") && toReturn.endsWith("\"") && toReturn.length() >= 2) {
                            toReturn = toReturn.substring(1, toReturn.length() - 1);
                        }
                    }
                }
            }
        }

        // All Done.
        return toReturn;
    }

    private static String resolveAnnotationValue(
            final AnnotationValue val,
            final JavaClass currentClass,
            final JavaProjectBuilder builder,
            final int depth) {

        if (val == null || depth > 5) {
            return null;
        }

        if (val instanceof FieldRef) {
            final FieldRef fr = (FieldRef) val;
            final int partCount = fr.getPartCount();
            if (partCount == 0) {
                return null;
            }

            final String fieldName = fr.getNamePart(partCount - 1);
            JavaClass targetClass = null;

            if (partCount == 1) {
                targetClass = currentClass;
                if (targetClass != null
                        && targetClass.getFieldByName(fieldName) == null
                        && currentClass.getSource() != null
                        && builder != null) {
                    for (String imp : currentClass.getSource().getImports()) {
                        if (imp.endsWith("." + fieldName)) {
                            final String className = imp.substring(0, imp.length() - fieldName.length() - 1);
                            final JavaClass c = builder.getClassByName(className);
                            if (c != null && c.getFieldByName(fieldName) != null) {
                                targetClass = c;
                                break;
                            }
                        }
                    }
                }
            } else {
                final StringBuilder sb = new StringBuilder();
                for (int i = 0; i < partCount - 1; i++) {
                    if (sb.length() > 0) {
                        sb.append(".");
                    }
                    sb.append(fr.getNamePart(i));
                }
                final String className = sb.toString();

                if (currentClass != null
                        && (className.equals(currentClass.getName())
                                || className.equals(currentClass.getFullyQualifiedName()))) {
                    targetClass = currentClass;
                } else if (currentClass != null) {
                    final JavaClass nested = currentClass.getNestedClassByName(className);
                    if (nested != null && nested.getFieldByName(fieldName) != null) {
                        targetClass = nested;
                    }
                    if (targetClass == null && currentClass.getSource() != null && builder != null) {
                        for (String imp : currentClass.getSource().getImports()) {
                            if (imp.equals(className) || imp.endsWith("." + className)) {
                                final JavaClass c = builder.getClassByName(imp);
                                if (c != null && c.getFieldByName(fieldName) != null) {
                                    targetClass = c;
                                    break;
                                }
                            }
                        }
                    }
                    if (targetClass == null && currentClass.getPackage() != null && builder != null) {
                        final String fqcn = currentClass.getPackage().getName() + "." + className;
                        final JavaClass c = builder.getClassByName(fqcn);
                        if (c != null && c.getFieldByName(fieldName) != null) {
                            targetClass = c;
                        }
                    }
                    if (targetClass == null && builder != null) {
                        final JavaClass c = builder.getClassByName(className);
                        if (c != null && c.getFieldByName(fieldName) != null) {
                            targetClass = c;
                        }
                    }
                }
            }

            if (targetClass != null) {
                final JavaField f = targetClass.getFieldByName(fieldName);
                if (f != null) {
                    final String init = f.getInitializationExpression();
                    if (init != null) {
                        final String parsed = parseStringExpression(init);
                        if (parsed != null) {
                            return parsed;
                        }
                        // Chained constant in the same class
                        final JavaField chained = targetClass.getFieldByName(init.trim());
                        if (chained != null) {
                            final String chainedInit = chained.getInitializationExpression();
                            if (chainedInit != null) {
                                return parseStringExpression(chainedInit);
                            }
                        }
                    }
                }
            }

            return null;
        } else if (val instanceof BinaryOperator) {
            final BinaryOperator bin = (BinaryOperator) val;
            final String left = resolveAnnotationValue(bin.getLeft(), currentClass, builder, depth + 1);
            final String right = resolveAnnotationValue(bin.getRight(), currentClass, builder, depth + 1);
            if (left != null && right != null) {
                return left + right;
            }
            return null;
        } else {
            final Object paramVal = val.getParameterValue();
            if (paramVal != null) {
                final String s = paramVal.toString().trim();
                if (s.startsWith("\"") && s.endsWith("\"") && s.length() >= 2) {
                    return s.substring(1, s.length() - 1);
                }
                return s;
            }
            return null;
        }
    }

    private static String parseStringExpression(String expr) {
        if (expr == null) {
            return null;
        }
        expr = expr.trim();
        if (expr.startsWith("\"")
                && expr.endsWith("\"")
                && expr.length() >= 2
                && expr.indexOf('"', 1) == expr.length() - 1) {
            return expr.substring(1, expr.length() - 1);
        }
        if (expr.contains("+") && expr.contains("\"")) {
            final String[] parts = expr.split("\\+");
            final StringBuilder sb = new StringBuilder();
            for (String part : parts) {
                final String trimmed = part.trim();
                if (trimmed.startsWith("\"") && trimmed.endsWith("\"") && trimmed.length() >= 2) {
                    sb.append(trimmed.substring(1, trimmed.length() - 1));
                } else {
                    return null;
                }
            }
            return sb.toString();
        }
        if (expr.startsWith("\"") && expr.endsWith("\"") && expr.length() >= 2) {
            return expr.substring(1, expr.length() - 1);
        }
        return null;
    }

    private static boolean hasAnnotation(final Class<?> annotationType, final List<JavaAnnotation> annotations) {

        if (annotations != null && !annotations.isEmpty() && annotationType != null) {

            final String fullAnnotationClassName = annotationType.getName();

            for (JavaAnnotation current : annotations) {
                if (current.getType().isA(fullAnnotationClassName)) {
                    return true;
                }
            }
        }

        return false;
    }

    //
    // Private helpers
    //

    private void addEntry(
            final SortedMap<SortableLocation, JavaDocData> map,
            final SortableLocation key,
            final JavaAnnotatedElement value) {

        // Check sanity
        if (map.containsKey(key)) {

            // Get something to compare with
            final JavaDocData existing = map.get(key);

            // Is this an empty package-level documentation?
            if (key instanceof PackageLocation) {

                final boolean emptyExisting =
                        existing.getComment() == null || existing.getComment().isEmpty();
                final boolean emptyGiven =
                        value.getComment() == null || value.getComment().isEmpty();

                if (emptyGiven) {
                    if (log.isDebugEnabled()) {
                        log.debug("Skipping processing empty Package javadoc from [" + key + "]");
                    }
                    return;
                } else if (emptyExisting && log.isWarnEnabled()) {
                    log.warn("Overwriting empty Package javadoc from [" + key + "]");
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
    private class ReadOnlySearchableDocumentation implements SearchableDocumentation {

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
            return (SortedMap<SortableLocation, JavaDocData>) Collections.unmodifiableSortedMap(valueMap);
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
