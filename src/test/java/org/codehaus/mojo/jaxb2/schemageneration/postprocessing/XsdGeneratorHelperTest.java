package org.codehaus.mojo.jaxb2.schemageneration.postprocessing;

import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.mojo.jaxb2.BufferingLog;
import org.codehaus.mojo.jaxb2.schemageneration.XsdGeneratorHelper;
import org.codehaus.mojo.jaxb2.schemageneration.postprocessing.javadoc.AbstractSourceCodeAwareNodeProcessingTest;
import org.codehaus.mojo.jaxb2.schemageneration.postprocessing.javadoc.JavaDocExtractor;
import org.codehaus.mojo.jaxb2.schemageneration.postprocessing.javadoc.NoAuthorJavaDocRenderer;
import org.codehaus.mojo.jaxb2.schemageneration.postprocessing.javadoc.SearchableDocumentation;
import org.codehaus.mojo.jaxb2.schemageneration.postprocessing.javadoc.XsdAnnotationProcessor;
import org.codehaus.mojo.jaxb2.schemageneration.postprocessing.javadoc.XsdEnumerationAnnotationProcessor;
import org.codehaus.mojo.jaxb2.schemageneration.postprocessing.schemaenhancement.ChangeNamespacePrefixProcessor;
import org.codehaus.mojo.jaxb2.schemageneration.postprocessing.schemaenhancement.SimpleNamespaceResolver;
import org.codehaus.mojo.jaxb2.schemageneration.postprocessing.schemaenhancement.TransformSchema;
import org.codehaus.mojo.jaxb2.shared.FileSystemUtilities;
import org.codehaus.mojo.jaxb2.shared.filters.Filter;
import org.codehaus.mojo.jaxb2.shared.filters.Filters;
import org.codehaus.mojo.jaxb2.shared.filters.pattern.PatternFileFilter;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.ElementNameAndAttributeQualifier;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import se.jguru.shared.algorithms.api.resources.PropertyResources;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerFactory;
import java.io.File;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.Ignore;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>
 */
public class XsdGeneratorHelperTest
{

    private static TransformerFactory factory;

    @BeforeClass
    public static void setupSharedState()
    {

        // Configure XMLUnit.
        XMLUnit.setIgnoreWhitespace( true );
        XMLUnit.setIgnoreAttributeOrder( true );

        // Configure the TransformerFactory
        try
        {

            factory = TransformerFactory.newInstance();
            final CodeSource codeSource = factory.getClass().getProtectionDomain().getCodeSource();

            final String location = codeSource == null ? "Unknown" : codeSource.getLocation().toString();
            System.out.println(
                    "-- Found TransformerFactory of type [" + factory.getClass().getName() + "] loaded from [" + location + "]" );

        }
        catch ( Exception ex )
        {
            ex.printStackTrace();
        }
    }

    @Test( expected = MojoExecutionException.class )
    public void validateExceptionThrownOnDuplicateURIs() throws MojoExecutionException
    {

        // Assemble
        final TransformSchema transformSchema1 = new TransformSchema( "foo", "foo", "foo" );
        final TransformSchema transformSchema2 = new TransformSchema( "foo", "bar", "bar" );

        final List<TransformSchema> transformSchemas = new ArrayList<TransformSchema>();
        transformSchemas.add( transformSchema1 );
        transformSchemas.add( transformSchema2 );

        // Act & Assert
        XsdGeneratorHelper.validateSchemasInPluginConfiguration( transformSchemas );
        Assert.fail( "Two schemas with same URIs should yield a MojoExecutionException." );
    }

    @Test( expected = MojoExecutionException.class )
    public void validateExceptionThrownOnDuplicatePrefixes() throws MojoExecutionException
    {

        // Assemble
        final TransformSchema transformSchema1 = new TransformSchema( "foo", "foo", "foo" );
        final TransformSchema transformSchema2 = new TransformSchema( "bar", "foo", "bar" );

        final List<TransformSchema> transformSchemas = new ArrayList<TransformSchema>();
        transformSchemas.add( transformSchema1 );
        transformSchemas.add( transformSchema2 );

        // Act & Assert
        XsdGeneratorHelper.validateSchemasInPluginConfiguration( transformSchemas );
        Assert.fail( "Two schemas with same Prefixes should yield a MojoExecutionException." );
    }

    @Test
    public void validateNoExceptionThrownOnDuplicateNullPrefixes()
    {
        // Assemble
        final TransformSchema transformSchema1 = new TransformSchema( "foo", null, "foo" );
        final TransformSchema transformSchema2 = new TransformSchema( "bar", null, "bar" );

        final List<TransformSchema> transformSchemas = new ArrayList<TransformSchema>();
        transformSchemas.add( transformSchema1 );
        transformSchemas.add( transformSchema2 );

        // Act & Assert
        try
        {
            XsdGeneratorHelper.validateSchemasInPluginConfiguration( transformSchemas );
        }
        catch ( MojoExecutionException e )
        {
            Assert.fail( "Two schemas with null Prefix should not yield a MojoExecutionException." );
        }
    }

    @Test
    public void validateExceptionThrownOnDuplicateFiles()
    {

        // Assemble
        final TransformSchema transformSchema1 = new TransformSchema( "foo", "foo", "foo.xsd" );
        final TransformSchema transformSchema2 = new TransformSchema( "bar", "bar", "foo.xsd" );

        final List<TransformSchema> transformSchemas = new ArrayList<TransformSchema>();
        transformSchemas.add( transformSchema1 );
        transformSchemas.add( transformSchema2 );

        // Act & Assert
        try
        {
            XsdGeneratorHelper.validateSchemasInPluginConfiguration( transformSchemas );
            Assert.fail( "Two schemas with same Files should yield a MojoExecutionException." );
        }
        catch ( MojoExecutionException e )
        {
            // Validate the error message.
            String expectedMessage = "Misconfiguration detected: Duplicate 'file' property with value [foo.xsd] " + "found in plugin configuration. Correct schema elements index (0) and (1), " + "to ensure that all 'file' values are unique.";
            Assert.assertEquals( expectedMessage, e.getLocalizedMessage() );
        }
    }

    @Test( expected = MojoExecutionException.class )
    public void validateExceptionThrownOnOnlyUriGiven() throws MojoExecutionException
    {
        // Assemble
        final TransformSchema transformSchema1 = new TransformSchema( "foo", null, "" );

        final List<TransformSchema> transformSchemas = new ArrayList<TransformSchema>();
        transformSchemas.add( transformSchema1 );

        // Act & Assert
        XsdGeneratorHelper.validateSchemasInPluginConfiguration( transformSchemas );
        Assert.fail( "A schema definition with no prefix or file should yield a MojoExecutionException." );
    }

    @Test( expected = MojoExecutionException.class )
    public void validateExceptionThrownOnNullUri() throws MojoExecutionException
    {

        // Assemble
        final TransformSchema transformSchema1 = new TransformSchema( null, "foo", "bar" );

        final List<TransformSchema> transformSchemas = new ArrayList<TransformSchema>();
        transformSchemas.add( transformSchema1 );

        // Act & Assert
        XsdGeneratorHelper.validateSchemasInPluginConfiguration( transformSchemas );
        Assert.fail( "A schema definition with null URI should yield a MojoExecutionException." );
    }

    @Test( expected = MojoExecutionException.class )
    public void validateExceptionThrownOnEmptyUri() throws MojoExecutionException
    {

        // Assemble
        final TransformSchema transformSchema1 = new TransformSchema( "", "foo", "bar" );

        final List<TransformSchema> transformSchemas = new ArrayList<TransformSchema>();
        transformSchemas.add( transformSchema1 );

        // Act & Assert
        XsdGeneratorHelper.validateSchemasInPluginConfiguration( transformSchemas );
        Assert.fail( "A schema definition with empty URI should yield a MojoExecutionException." );
    }

    @Test
    public void validateProcessingNodes()
    {

        // Assemble
        final String newPrefix = "changedFoo";
        final String oldPrefix = "foo";
        final String originalXml = getXmlDocumentSample( oldPrefix );
        final String changedXml = getXmlDocumentSample( newPrefix );
        final NodeProcessor changeNamespacePrefixProcessor = new ChangeNamespacePrefixProcessor( oldPrefix, newPrefix );

        // Act
        final Document processedDocument = XsdGeneratorHelper.parseXmlStream( new StringReader( originalXml ) );
        XsdGeneratorHelper.process( processedDocument.getFirstChild(), true, changeNamespacePrefixProcessor );

        // Assert
        final Document expectedDocument = XsdGeneratorHelper.parseXmlStream( new StringReader( changedXml ) );
        final Diff diff = new Diff( expectedDocument, processedDocument, null, new ElementNameAndAttributeQualifier() );
        diff.overrideElementQualifier( new ElementNameAndAttributeQualifier() );

        XMLAssert.assertXMLEqual( processedDocument, expectedDocument );
    }

    @Test
    @Ignore
    public void validateProcessingXSDsWithEnumerations() throws Exception
    {

        // Assemble
        final BufferingLog log = new BufferingLog();
        final JavaDocExtractor extractor = new JavaDocExtractor( log );
        extractor.setEncoding( "UTF-8" );

        final String parentPath = "testdata/schemageneration/javadoc/enums/";
        final URL parentPathURL = getClass().getClassLoader().getResource( parentPath );
        Assert.assertNotNull( parentPathURL );

        final File parentDir = new File( parentPathURL.getPath() );
        Assert.assertTrue( parentDir.exists() && parentDir.isDirectory() );

        final List<Filter<File>> excludeFilesMatching = new ArrayList<Filter<File>>();
        excludeFilesMatching.add( new PatternFileFilter( Collections.singletonList( "\\.xsd" ) ) );
        Filters.initialize( log, excludeFilesMatching );

        final List<File> allSourceFiles = FileSystemUtilities.filterFiles( parentDir, null, parentDir.getAbsolutePath(),
                log, "allJavaFiles", excludeFilesMatching );
        Assert.assertEquals( 3, allSourceFiles.size() );

        final List<URL> urls = new ArrayList<URL>();
        for ( File current : allSourceFiles )
        {
            try
            {
                urls.add( current.toURI().toURL() );
            }
            catch ( MalformedURLException e )
            {
                throw new IllegalArgumentException(
                        "Could not convert file [" + current.getAbsolutePath() + "] to a URL", e );
            }
        }
        Assert.assertEquals( 3, urls.size() );

        extractor.addSourceURLs( urls );
        final SearchableDocumentation docs = extractor.process();

        // #1) The raw / un-processed XSD (containing the 'before' state)
        // #2) The processed XSD (containing the 'expected' state)
        final String rawEnumSchema = PropertyResources.readFully( parentPath + "rawEnumSchema.xsd" );
        final String processedEnumSchema = PropertyResources.readFully( parentPath + "processedEnumSchema.xsd" );
        final NodeProcessor enumProcessor = new XsdEnumerationAnnotationProcessor( docs,
                new NoAuthorJavaDocRenderer() );

        // Act
        final Document processedDocument = XsdGeneratorHelper.parseXmlStream( new StringReader( rawEnumSchema ) );
        XsdGeneratorHelper.process( processedDocument.getFirstChild(), true, enumProcessor );
        // System.out.println("Got: " + AbstractSourceCodeAwareNodeProcessingTest.printDocument(processedDocument));

        // Assert
        final Document expectedDocument = XsdGeneratorHelper.parseXmlStream( new StringReader( processedEnumSchema ) );
        final Diff diff = new Diff( expectedDocument, processedDocument, null, new ElementNameAndAttributeQualifier() );
        diff.overrideElementQualifier( new ElementNameAndAttributeQualifier() );

        XMLAssert.assertXMLEqual( processedDocument, expectedDocument );
    }

    @Test
    public void validateXmlDocumentationForWrappers() throws Exception
    {

        // Assemble
        final BufferingLog log = new BufferingLog();
        final JavaDocExtractor extractor = new JavaDocExtractor( log );
        extractor.setEncoding( "UTF-8" );

        final String parentPath = "testdata/schemageneration/javadoc/xmlwrappers/";
        final URL parentPathURL = getClass().getClassLoader().getResource( parentPath );
        Assert.assertNotNull( parentPathURL );

        final String schemaGenCreatedSchema = PropertyResources.readFully( parentPath + "expectedRawXmlWrappers.xsd" );

        final File parentDir = new File( parentPathURL.getPath() );
        Assert.assertTrue( parentDir.exists() && parentDir.isDirectory() );

        final List<Filter<File>> excludeFilesMatching = new ArrayList<Filter<File>>();
        excludeFilesMatching.add( new PatternFileFilter( Collections.singletonList( "\\.xsd" ) ) );
        Filters.initialize( log, excludeFilesMatching );

        final List<File> allSourceFiles = FileSystemUtilities.filterFiles( parentDir, null, parentDir.getAbsolutePath(),
                log, "allJavaFiles", excludeFilesMatching );
        Assert.assertEquals( 2, allSourceFiles.size() );

        final List<URL> urls = new ArrayList<URL>();
        for ( File current : allSourceFiles )
        {
            try
            {
                urls.add( current.toURI().toURL() );
            }
            catch ( MalformedURLException e )
            {
                throw new IllegalArgumentException(
                        "Could not convert file [" + current.getAbsolutePath() + "] to a URL", e );
            }
        }
        Assert.assertEquals( 2, urls.size() );

        // Act
        extractor.addSourceURLs( urls );
        final SearchableDocumentation docs = extractor.process();

        final XsdAnnotationProcessor processor = new XsdAnnotationProcessor( docs, new NoAuthorJavaDocRenderer() );
        final Document schemaGenCreatedDocument = XsdGeneratorHelper.parseXmlStream(
                new StringReader( schemaGenCreatedSchema ) );
        XsdGeneratorHelper.process( schemaGenCreatedDocument.getFirstChild(), true, processor );
        System.out.println(
                "Got: " + AbstractSourceCodeAwareNodeProcessingTest.printDocument( schemaGenCreatedDocument ) );

        // Assert
    }

    @Test
    public void validateAcquiringFilenameToResolverMap() throws MojoExecutionException
    {

        // Assemble
        final String[] expectedFilenames = {"schema1.xsd", "schema2.xsd", "schema3.xsd"};
        final URL tmpUrl = getClass().getClassLoader().getResource( "generated/schema/schema1.xsd" );
        final File directory = new File( tmpUrl.getFile() ).getParentFile();

        // Act
        final Map<String, SimpleNamespaceResolver> fileNameToResolverMap = XsdGeneratorHelper.getFileNameToResolverMap(
                directory );

        // Assert
        Assert.assertEquals( 3, fileNameToResolverMap.size() );
        for ( String current : expectedFilenames )
        {
            Assert.assertTrue( fileNameToResolverMap.keySet().contains( current ) );
        }

        SimpleNamespaceResolver schema1Resolver = fileNameToResolverMap.get( "schema1.xsd" );
        Assert.assertEquals( "http://yet/another/namespace", schema1Resolver.getLocalNamespaceURI() );
        Assert.assertEquals( "schema1.xsd", schema1Resolver.getSourceFilename() );
        final Map<String, String> schema1NamespaceURI2PrefixMap = schema1Resolver.getNamespaceURI2PrefixMap();
        Assert.assertEquals( 1, schema1NamespaceURI2PrefixMap.size() );
        Assert.assertEquals( "xs", schema1NamespaceURI2PrefixMap.get( "http://www.w3.org/2001/XMLSchema" ) );

        SimpleNamespaceResolver schema2Resolver = fileNameToResolverMap.get( "schema2.xsd" );
        Assert.assertEquals( "http://some/namespace", schema2Resolver.getLocalNamespaceURI() );
        Assert.assertEquals( "schema2.xsd", schema2Resolver.getSourceFilename() );
        final Map<String, String> schema2NamespaceURI2PrefixMap = schema2Resolver.getNamespaceURI2PrefixMap();
        Assert.assertEquals( 2, schema2NamespaceURI2PrefixMap.size() );
        Assert.assertEquals( "ns1", schema2NamespaceURI2PrefixMap.get( "http://another/namespace" ) );
        Assert.assertEquals( "xs", schema2NamespaceURI2PrefixMap.get( "http://www.w3.org/2001/XMLSchema" ) );

        SimpleNamespaceResolver schema3Resolver = fileNameToResolverMap.get( "schema3.xsd" );
        Assert.assertEquals( "http://another/namespace", schema3Resolver.getLocalNamespaceURI() );
        Assert.assertEquals( "schema3.xsd", schema3Resolver.getSourceFilename() );
        final Map<String, String> schema3NamespaceURI2PrefixMap = schema3Resolver.getNamespaceURI2PrefixMap();
        Assert.assertEquals( 3, schema3NamespaceURI2PrefixMap.size() );
        Assert.assertEquals( "ns2", schema3NamespaceURI2PrefixMap.get( "http://yet/another/namespace" ) );
        Assert.assertEquals( "ns1", schema3NamespaceURI2PrefixMap.get( "http://some/namespace" ) );
        Assert.assertEquals( "xs", schema3NamespaceURI2PrefixMap.get( "http://www.w3.org/2001/XMLSchema" ) );
    }

    //
    // Private helpers
    //

    private static DocumentBuilderFactory getDocumentBuilderFactory()
    {

        final DocumentBuilderFactory toReturn = DocumentBuilderFactory.newInstance();
        toReturn.setNamespaceAware( true );
        return toReturn;
    }

    private static DocumentBuilder getDocumentBuilder()
    {
        try
        {
            return getDocumentBuilderFactory().newDocumentBuilder();
        }
        catch ( ParserConfigurationException e )
        {
            throw new IllegalStateException( "Could not create DocumentBuilder", e );
        }
    }

    private static Document createEmptyDocument( final DocumentBuilder builder )
    {
        return builder.newDocument();
    }

    private String getXmlDocumentSample( final String namespace )
    {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"\n" + "           xmlns:" + namespace + "=\"http://the/foo/namespace\" \n"
                // + "           targetNamespace=\"http://yet/another/namespace\"\n"
                + "           version=\"1.0\">\n" + "    <xs:element name=\"aRequiredElementInYetAnotherNamespace\" type=\"xs:string\"/>\n" + "    <" + namespace + ":aBar name=\"aFooElement\" />\n" + "</xs:schema>\n";
    }
}