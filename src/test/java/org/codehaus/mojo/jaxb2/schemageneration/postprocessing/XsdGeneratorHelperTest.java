package org.codehaus.mojo.jaxb2.schemageneration.postprocessing;

import java.io.File;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.transform.TransformerFactory;

import org.codehaus.mojo.jaxb2.schemageneration.XsdGeneratorHelper;
import org.codehaus.mojo.jaxb2.schemageneration.postprocessing.schemaenhancement.ChangeNamespacePrefixProcessor;
import org.codehaus.mojo.jaxb2.schemageneration.postprocessing.schemaenhancement.SimpleNamespaceResolver;
import org.codehaus.mojo.jaxb2.schemageneration.postprocessing.schemaenhancement.TransformSchema;
import org.junit.Assert;

import org.apache.maven.plugin.MojoExecutionException;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.ElementNameAndAttributeQualifier;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Test;
import org.w3c.dom.Document;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>
 */
public class XsdGeneratorHelperTest
{
    private static final TransformerFactory FACTORY = TransformerFactory.newInstance();

    static
    {
        // Configure XMLUnit.
        XMLUnit.setIgnoreWhitespace( true );
        XMLUnit.setIgnoreAttributeOrder( true );

        // Configure the TransformerFactory
        FACTORY.setAttribute( "indent-number", 2 );
    }

    @Test( expected = MojoExecutionException.class )
    public void validateExceptionThrownOnDuplicateURIs()
        throws MojoExecutionException
    {
        // Assemble
        final TransformSchema transformSchema1 = new TransformSchema( "foo", "foo", "foo" );
        final TransformSchema transformSchema2 = new TransformSchema( "foo", "bar", "bar" );

        final List<TransformSchema> transformSchemas = new ArrayList<TransformSchema>();
        transformSchemas.add( transformSchema1 );
        transformSchemas.add( transformSchema2 );

        // Act & Assert
        XsdGeneratorHelper.validateSchemasInPluginConfiguration(transformSchemas);
        Assert.fail( "Two schemas with same URIs should yield a MojoExecutionException." );
    }

    @Test( expected = MojoExecutionException.class )
    public void validateExceptionThrownOnDuplicatePrefixes()
        throws MojoExecutionException
    {

        // Assemble
        final TransformSchema transformSchema1 = new TransformSchema( "foo", "foo", "foo" );
        final TransformSchema transformSchema2 = new TransformSchema( "bar", "foo", "bar" );

        final List<TransformSchema> transformSchemas = new ArrayList<TransformSchema>();
        transformSchemas.add( transformSchema1 );
        transformSchemas.add( transformSchema2 );

        // Act & Assert
        XsdGeneratorHelper.validateSchemasInPluginConfiguration(transformSchemas);
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
            XsdGeneratorHelper.validateSchemasInPluginConfiguration(transformSchemas);
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
            XsdGeneratorHelper.validateSchemasInPluginConfiguration(transformSchemas);
            Assert.fail( "Two schemas with same Files should yield a MojoExecutionException." );
        }
        catch ( MojoExecutionException e )
        {
            // Validate the error message.
            String expectedMessage = "Misconfiguration detected: Duplicate 'file' property with value [foo.xsd] "
                + "found in plugin configuration. Correct schema elements index (0) and (1), "
                + "to ensure that all 'file' values are unique.";
            Assert.assertEquals( expectedMessage, e.getLocalizedMessage() );
        }
    }

    @Test( expected = MojoExecutionException.class )
    public void validateExceptionThrownOnOnlyUriGiven()
        throws MojoExecutionException
    {
        // Assemble
        final TransformSchema transformSchema1 = new TransformSchema( "foo", null, "" );

        final List<TransformSchema> transformSchemas = new ArrayList<TransformSchema>();
        transformSchemas.add( transformSchema1 );

        // Act & Assert
        XsdGeneratorHelper.validateSchemasInPluginConfiguration(transformSchemas);
        Assert.fail( "A schema definition with no prefix or file should yield a MojoExecutionException." );
    }

    @Test( expected = MojoExecutionException.class )
    public void validateExceptionThrownOnNullUri()
        throws MojoExecutionException
    {
        // Assemble
        final TransformSchema transformSchema1 = new TransformSchema( null, "foo", "bar" );

        final List<TransformSchema> transformSchemas = new ArrayList<TransformSchema>();
        transformSchemas.add( transformSchema1 );

        // Act & Assert
        XsdGeneratorHelper.validateSchemasInPluginConfiguration(transformSchemas);
        Assert.fail( "A schema definition with null URI should yield a MojoExecutionException." );
    }

    @Test( expected = MojoExecutionException.class )
    public void validateExceptionThrownOnEmptyUri()
        throws MojoExecutionException
    {
        // Assemble
        final TransformSchema transformSchema1 = new TransformSchema( "", "foo", "bar" );

        final List<TransformSchema> transformSchemas = new ArrayList<TransformSchema>();
        transformSchemas.add( transformSchema1 );

        // Act & Assert
        XsdGeneratorHelper.validateSchemasInPluginConfiguration(transformSchemas);
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
        final Document processedDocument = XsdGeneratorHelper.parseXmlStream(new StringReader(originalXml));
        XsdGeneratorHelper.process(processedDocument.getFirstChild(), true, changeNamespacePrefixProcessor);

        // Assert
        final Document expectedDocument = XsdGeneratorHelper.parseXmlStream(new StringReader(changedXml));
        final Diff diff = new Diff( expectedDocument, processedDocument, null, new ElementNameAndAttributeQualifier() );
        diff.overrideElementQualifier( new ElementNameAndAttributeQualifier() );

        XMLAssert.assertXMLEqual( processedDocument, expectedDocument );
    }

    @Test
    public void validateAcquiringFilenameToResolverMap()
        throws MojoExecutionException
    {
        // Assemble
        final String[] expectedFilenames = { "schema1.xsd", "schema2.xsd", "schema3.xsd" };
        final URL tmpUrl = getClass().getClassLoader().getResource( "generated/schema/schema1.xsd" );
        final File directory = new File( tmpUrl.getFile() ).getParentFile();

        // Act
        final Map<String, SimpleNamespaceResolver> fileNameToResolverMap =
            XsdGeneratorHelper.getFileNameToResolverMap(directory);

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

    private String getXmlDocumentSample( final String namespace )
    {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
            + "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"\n" + "           xmlns:" + namespace
            + "=\"http://the/foo/namespace\" \n"
            // + "           targetNamespace=\"http://yet/another/namespace\"\n"
            + "           version=\"1.0\">\n"
            + "    <xs:element name=\"aRequiredElementInYetAnotherNamespace\" type=\"xs:string\"/>\n" + "    <"
            + namespace + ":aBar name=\"aFooElement\" />\n" + "</xs:schema>\n";
    }
}