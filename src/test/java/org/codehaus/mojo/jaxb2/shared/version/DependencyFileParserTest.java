package org.codehaus.mojo.jaxb2.shared.version;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>
 */
public class DependencyFileParserTest {

    // Internal state
    private URL urlToTestJar;
    private static final String DEPS1_PROPERTYFILE = "/testdata/shared/deps1.properties";
    private ClassLoader originalThreadContextClassLoader;

    @Before
    public void setupSharedState() throws Exception {

        // Stash the original ClassLoader
        originalThreadContextClassLoader = Thread.currentThread().getContextClassLoader();

        // Add the local test JAR to the ClassLoader path
        final String jarPath = "testdata/shared/nazgul-tools-validation-aspect-4.0.1.jar";
        final URL extraURL = getClass().getClassLoader().getResource(jarPath);
        Assert.assertNotNull("No resource found for path [" + jarPath + "]", extraURL);

        final URLClassLoader decoratedClassLoader = new URLClassLoader(
                new URL[]{extraURL},
                originalThreadContextClassLoader);
        Assert.assertNotNull("Could not create decorated ClassLoader", decoratedClassLoader);
        Thread.currentThread().setContextClassLoader(decoratedClassLoader);

        // Assert that the decorated ClassLoader can load resource within the extraURL JAR.
        final String internalResourcePath = "META-INF/maven/se.jguru.nazgul.tools.validation.aspect/"
                + "nazgul-tools-validation-aspect/pom.properties";
        final List<URL> resourceList = Collections.list(decoratedClassLoader.getResources(internalResourcePath));
        Assert.assertNotNull(resourceList);
        Assert.assertNotEquals(0, resourceList.size());

        for (URL current : resourceList) {
            if (current.getPath().contains("testdata")) {
                urlToTestJar = current;
                break;
            }
        }
        Assert.assertNotNull(
                "Expected resource not found for internal resource path [" + internalResourcePath + "] ",
                urlToTestJar);
    }

    @After
    public void teardownSharedState() {

        // Restore the original ClassLoader
        Thread.currentThread().setContextClassLoader(originalThreadContextClassLoader);
    }

    @Test
    public void validateParsingDependencyPropertiesFile() {

        // Assemble
        final URL depsPropResource = getClass().getResource(DEPS1_PROPERTYFILE);

        // Act
        final SortedMap<String, String> versionMap = DependsFileParser.getVersionMap(depsPropResource);

        // Assert
        Assert.assertEquals("Wed Nov 19 20:11:15 CET 2014", versionMap.get(DependsFileParser.BUILDTIME_KEY));
        Assert.assertEquals("compile", versionMap.get("javax.xml.bind/jaxb-api/scope"));
        Assert.assertEquals("jar", versionMap.get("javax.xml.bind/jaxb-api/type"));
        Assert.assertEquals("2.2.11", versionMap.get("javax.xml.bind/jaxb-api/version"));

        /*
        for(Map.Entry<String, String> current : versionMap.entrySet()) {
            System.out.println(" [" + current.getKey() + "]: " + current.getValue());
        }
        */
    }

    @Test
    public void validateCreatingDependencyInformationMapFromDependencyPropertiesFile() {

        // Assemble
        final String jaxbApiKey = "javax.xml.bind/jaxb-api";
        final URL depsPropResource = getClass().getResource(DEPS1_PROPERTYFILE);

        // Act
        final SortedMap<String, String> versionMap = DependsFileParser.getVersionMap(depsPropResource);
        final SortedMap<String, DependencyInfo> diMap = DependsFileParser.createDependencyInfoMap(versionMap);

        // Assert
        final DependencyInfo dependencyInfo = diMap.get(jaxbApiKey);
        Assert.assertNotNull(dependencyInfo);

        Assert.assertEquals("javax.xml.bind", dependencyInfo.getGroupId());
        Assert.assertEquals("jaxb-api", dependencyInfo.getArtifactId());
        Assert.assertEquals("2.2.11", dependencyInfo.getVersion());
        Assert.assertEquals("jar", dependencyInfo.getType());
        Assert.assertEquals("compile", dependencyInfo.getScope());

        /*
        for(Map.Entry<String, DependencyInfo> current : diMap.entrySet()) {
            System.out.println(" [" + current.getKey() + "]: " + current.getValue());
        }
        */
    }

    @Test
    public void validateParsingDependencyInformationPackagedInJarFileInClassPath() {

        // Assemble
        final String artifactId = "nazgul-tools-validation-aspect";
        final String groupId = "se.jguru.nazgul.tools.validation.aspect";
        final String slf4jApiKey = "org.slf4j/slf4j-api";

        // Act
        final SortedMap<String, String> versionMap = DependsFileParser.getVersionMap(artifactId);
        final SortedMap<String, DependencyInfo> diMap = DependsFileParser.createDependencyInfoMap(versionMap);

        // Assert
        Assert.assertEquals("Mon Oct 06 07:51:23 CEST 2014", versionMap.get(DependsFileParser.BUILDTIME_KEY));
        Assert.assertEquals(groupId, versionMap.get(DependsFileParser.OWN_GROUPID_KEY));
        Assert.assertEquals(artifactId, versionMap.get(DependsFileParser.OWN_ARTIFACTID_KEY));
        Assert.assertEquals("4.0.1", versionMap.get(DependsFileParser.OWN_VERSION_KEY));

        final DependencyInfo dependencyInfo = diMap.get(slf4jApiKey);
        Assert.assertNotNull(dependencyInfo);

        Assert.assertEquals("org.slf4j", dependencyInfo.getGroupId());
        Assert.assertEquals("slf4j-api", dependencyInfo.getArtifactId());
        Assert.assertEquals("1.7.7", dependencyInfo.getVersion());
        Assert.assertEquals("jar", dependencyInfo.getType());
        Assert.assertEquals("compile", dependencyInfo.getScope());

        /*
        for(Map.Entry<String, DependencyInfo> current : diMap.entrySet()) {
            System.out.println(" [" + current.getKey() + "]: " + current.getValue());
        }
        */
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateExceptionOnAttemptingToParseIncorrectlyFormedPropertiesFile() {

        // Assemble
        final String resourcePath = "testdata/shared/not_a_dependency.properties";
        final URL incorrectResource = Thread.currentThread().getContextClassLoader().getResource(resourcePath);

        // Act & Assert
        DependsFileParser.getVersionMap(incorrectResource);
    }
}
