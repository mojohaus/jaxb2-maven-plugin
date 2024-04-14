package org.codehaus.mojo.jaxb2.schemageneration.postprocessing.javadoc;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.TreeMap;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import org.codehaus.mojo.jaxb2.schemageneration.postprocessing.javadoc.location.FieldLocation;
import org.codehaus.mojo.jaxb2.schemageneration.postprocessing.schemaenhancement.XmlNameAnnotatedClassWithFieldAccess;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
class FieldLocationTest {

    private Class<XmlNameAnnotatedClassWithFieldAccess> theClass;
    private Map<String, Field> fieldName2MethodMap;

    @BeforeEach
    void setupSharedState() {

        fieldName2MethodMap = new TreeMap<String, Field>();

        theClass = XmlNameAnnotatedClassWithFieldAccess.class;
        for (Field current : theClass.getDeclaredFields()) {

            final String currentName = current.getName();
            fieldName2MethodMap.put(currentName, current);
        }
    }

    @Test
    void validateFieldLocationWithXmlName() throws Exception {

        // Assemble
        final String packageName = theClass.getPackage().getName();
        final String classXmlName = theClass.getAnnotation(XmlType.class).name();
        // @XmlType(name = "AnnotatedXmlNameAnnotatedClassWithFieldAccessTypeName")
        final Field integerField = fieldName2MethodMap.get("integerField");
        final Field stringField = fieldName2MethodMap.get("stringField");

        final String integerFieldXmlName =
                integerField.getAnnotation(XmlAttribute.class).name();
        final String stringFieldXmlName =
                stringField.getAnnotation(XmlElement.class).name();

        final String expectedIntegerFieldPath = packageName + "." + classXmlName + "#" + integerFieldXmlName;
        final String expectedStringFieldPath = packageName + "." + classXmlName + "#" + stringFieldXmlName;

        final String expectedIntegerFieldToString = packageName + "." + classXmlName + " (from: "
                + theClass.getSimpleName() + ")#" + integerFieldXmlName + " (from: "
                + integerField.getName() + ")";

        // Act
        final FieldLocation integerFieldLocation = new FieldLocation(
                packageName, theClass.getSimpleName(), classXmlName, integerField.getName(), integerFieldXmlName);

        final FieldLocation stringFieldLocation = new FieldLocation(
                packageName, theClass.getSimpleName(), classXmlName, stringField.getName(), stringFieldXmlName);

        // Assert
        assertEquals(expectedIntegerFieldPath, integerFieldLocation.getPath());
        assertEquals(expectedStringFieldPath, stringFieldLocation.getPath());
        assertEquals(expectedIntegerFieldToString, integerFieldLocation.toString());
        assertEquals(integerFieldXmlName, integerFieldLocation.getMemberName());
    }
}
