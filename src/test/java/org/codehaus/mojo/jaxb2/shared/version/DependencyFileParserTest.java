package org.codehaus.mojo.jaxb2.shared.version;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>
 */
class DependencyFileParserTest {

    // Internal state
    private URL urlToTestJar;
    private static final String DEPS1_PROPERTYFILE = "/testdata/shared/deps1.properties";
    private ClassLoader originalThreadContextClassLoader;

    @BeforeEach
    void setupSharedState() throws Exception {

        // Stash the original ClassLoader
        originalThreadContextClassLoader = Thread.currentThread().getContextClassLoader();

        // Add the local test JAR to the ClassLoader path
        final String jarPath = "testdata/shared/nazgul-tools-validation-aspect-4.0.1.jar";
        final URL extraURL = getClass().getClassLoader().getResource(jarPath);
        assertNotNull(extraURL, "No resource found for path [" + jarPath + "]");

        final URLClassLoader decoratedClassLoader =
                new URLClassLoader(new URL[] {extraURL}, originalThreadContextClassLoader);
        assertNotNull(decoratedClassLoader, "Could not create decorated ClassLoader");
        Thread.currentThread().setContextClassLoader(decoratedClassLoader);

        // Assert that the decorated ClassLoader can load resource within the extraURL JAR.
        final String internalResourcePath = "META-INF/maven/se.jguru.nazgul.tools.validation.aspect/"
                + "nazgul-tools-validation-aspect/pom.properties";
        final List<URL> resourceList = Collections.list(decoratedClassLoader.getResources(internalResourcePath));
        assertNotNull(resourceList);
        assertNotEquals(0, resourceList.size());

        for (URL current : resourceList) {
            if (current.getPath().contains("testdata")) {
                urlToTestJar = current;
                break;
            }
        }
        assertNotNull(
                urlToTestJar, "Expected resource not found for internal resource path [" + internalResourcePath + "] ");
    }

    @AfterEach
    void teardownSharedState() {

        // Restore the original ClassLoader
        Thread.currentThread().setContextClassLoader(originalThreadContextClassLoader);
    }

    @Test
    void validateParsingDependencyPropertiesFile() {

        // Assemble
        final URL depsPropResource = getClass().getResource(DEPS1_PROPERTYFILE);

        // Act
        final SortedMap<String, String> versionMap = DependsFileParser.getVersionMap(depsPropResource);

        // Assert
        assertEquals("Wed Nov 19 20:11:15 CET 2014", versionMap.get(DependsFileParser.BUILDTIME_KEY));
        assertEquals("compile", versionMap.get("jakarta.xml.bind/jaxb-api/scope"));
        assertEquals("jar", versionMap.get("jakarta.xml.bind/jaxb-api/type"));
        assertEquals("3.0.0", versionMap.get("jakarta.xml.bind/jaxb-api/version"));

        /*
        for(Map.Entry<String, String> current : versionMap.entrySet()) {
            System.out.println(" [" + current.getKey() + "]: " + current.getValue());
        }
        */
    }

    @Test
    void validateCreatingDependencyInformationMapFromDependencyPropertiesFile() {

        // Assemble
        final String jaxbApiKey = "jakarta.xml.bind/jaxb-api";
        final URL depsPropResource = getClass().getResource(DEPS1_PROPERTYFILE);

        // Act
        final SortedMap<String, String> versionMap = DependsFileParser.getVersionMap(depsPropResource);
        final SortedMap<String, DependencyInfo> diMap = DependsFileParser.createDependencyInfoMap(versionMap);

        // Assert
        final DependencyInfo dependencyInfo = diMap.get(jaxbApiKey);
        assertNotNull(dependencyInfo);

        assertEquals("jakarta.xml.bind", dependencyInfo.getGroupId());
        assertEquals("jaxb-api", dependencyInfo.getArtifactId());
        assertEquals("3.0.0", dependencyInfo.getVersion());
        assertEquals("jar", dependencyInfo.getType());
        assertEquals("compile", dependencyInfo.getScope());

        /*
        for(Map.Entry<String, DependencyInfo> current : diMap.entrySet()) {
            System.out.println(" [" + current.getKey() + "]: " + current.getValue());
        }
        */
    }

    @Test
    void validateParsingDependencyInformationPackagedInJarFileInClassPath() {

        // Assemble
        final String artifactId = "nazgul-tools-validation-aspect";
        final String groupId = "se.jguru.nazgul.tools.validation.aspect";
        final String slf4jApiKey = "org.slf4j/slf4j-api";

        // Act
        final SortedMap<String, String> versionMap = DependsFileParser.getVersionMap(artifactId);
        final SortedMap<String, DependencyInfo> diMap = DependsFileParser.createDependencyInfoMap(versionMap);

        // Assert
        assertEquals("Mon Oct 06 07:51:23 CEST 2014", versionMap.get(DependsFileParser.BUILDTIME_KEY));
        assertEquals(groupId, versionMap.get(DependsFileParser.OWN_GROUPID_KEY));
        assertEquals(artifactId, versionMap.get(DependsFileParser.OWN_ARTIFACTID_KEY));
        assertEquals("4.0.1", versionMap.get(DependsFileParser.OWN_VERSION_KEY));

        final DependencyInfo dependencyInfo = diMap.get(slf4jApiKey);
        assertNotNull(dependencyInfo);

        assertEquals("org.slf4j", dependencyInfo.getGroupId());
        assertEquals("slf4j-api", dependencyInfo.getArtifactId());
        assertEquals("1.7.7", dependencyInfo.getVersion());
        assertEquals("jar", dependencyInfo.getType());
        assertEquals("compile", dependencyInfo.getScope());

        /*
        for(Map.Entry<String, DependencyInfo> current : diMap.entrySet()) {
            System.out.println(" [" + current.getKey() + "]: " + current.getValue());
        }
        */
    }

    @Test
    void validateExceptionOnAttemptingToParseIncorrectlyFormedPropertiesFile() {
        final String resourcePath = "testdata/shared/not_a_dependency.properties";
        final URL incorrectResource =
                    Thread.currentThread().getContextClassLoader().getResource(resourcePath);
        assertThrows(IllegalArgumentException.class, () ->

            // Act & Assert
            DependsFileParser.getVersionMap(incorrectResource));
    }
}
