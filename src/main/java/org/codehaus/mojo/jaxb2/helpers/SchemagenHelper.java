package org.codehaus.mojo.jaxb2.helpers;

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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.mojo.jaxb2.TransformSchema;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * Utility class holding algorithms used by the AbstractSchemagenMojo and decendents.
 * 
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>
 */
public final class SchemagenHelper
{
    // Constants
    private static final String MISCONFIG = "Misconfiguration detected: ";

    private static final TransformerFactory FACTORY;

    static
    {
        FACTORY = TransformerFactory.newInstance();
        FACTORY.setAttribute( "indent-number", 2 );
    }

    /**
     * Acquires a map relating generated schema filename to its SimpleNamespaceResolver.
     * 
     * @param outputDirectory The output directory of the generated schema files.
     * @return a map relating generated schema filename to an initialized SimpleNamespaceResolver.
     * @throws MojoExecutionException if two generated schema files used the same namespace URI.
     */
    public static Map<String, SimpleNamespaceResolver> getFileNameToResolverMap( final File outputDirectory )
        throws MojoExecutionException
    {
        final Map<String, SimpleNamespaceResolver> toReturn = new TreeMap<String, SimpleNamespaceResolver>();

        // Each generated schema file should be written to the output directory.
        // Each generated schema file should have a unique targetNamespace.
        File[] generatedSchemaFiles = outputDirectory.listFiles( new FileFilter()
        {
            public boolean accept( File pathname )
            {
                return pathname.getName().startsWith( "schema" ) && pathname.getName().endsWith( ".xsd" );
            }
        } );

        for ( File current : generatedSchemaFiles )
        {
            toReturn.put( current.getName(), new SimpleNamespaceResolver( current ) );
        }

        return toReturn;
    }

    /**
     * Validates that the list of Schemas provided within the configuration all contain unique values. Should a
     * MojoExecutionException be thrown, it contains informative text about the exact nature of the configuration
     * problem - we should simplify for all plugin users.
     * 
     * @param configuredTransformSchemas The List of configuration schemas provided to this mojo.
     * @throws MojoExecutionException if any two configuredSchemas instances contain duplicate values for any of the
     *             properties uri, prefix or file. Also throws a MojoExecutionException if the uri of any Schema is null
     *             or empty, or if none of the 'file' and 'prefix' properties are given within any of the
     *             configuredSchema instances.
     */
    public static void validateSchemasInPluginConfiguration( final List<TransformSchema> configuredTransformSchemas )
        throws MojoExecutionException
    {
        final List<String> uris = new ArrayList<String>();
        final List<String> prefixes = new ArrayList<String>();
        final List<String> fileNames = new ArrayList<String>();

        for ( int i = 0; i < configuredTransformSchemas.size(); i++ )
        {
            final TransformSchema current = configuredTransformSchemas.get( i );
            final String currentURI = current.getUri();
            final String currentPrefix = current.getToPrefix();
            final String currentFile = current.getToFile();

            // We cannot work with a null or empty uri
            if ( StringUtils.isEmpty( currentURI ) )
            {
                throw new MojoExecutionException( MISCONFIG + "Null or empty property 'uri' found in "
                    + "plugin configuration for schema element at index [" + i + "]: " + current );
            }

            // No point in having *only* a namespace.
            if ( StringUtils.isEmpty( currentPrefix ) && StringUtils.isEmpty( currentFile ) )
            {
                throw new MojoExecutionException( MISCONFIG + "Null or empty properties 'prefix' "
                    + "and 'file' found within plugin configuration for schema " + "element at index [" + i + "]: "
                    + current );
            }

            // Validate that all given uris are unique.
            if ( uris.contains( currentURI ) )
            {
                throw new MojoExecutionException( getDuplicationErrorMessage( "uri", currentURI,
                                                                              uris.indexOf( currentURI ), i ) );
            }
            uris.add( currentURI );

            // Validate that all given prefixes are unique.
            if ( prefixes.contains( currentPrefix ) && !( currentPrefix == null ) )
            {
                throw new MojoExecutionException( getDuplicationErrorMessage( "prefix", currentPrefix,
                                                                              prefixes.indexOf( currentPrefix ), i ) );
            }
            prefixes.add( currentPrefix );

            // Validate that all given files are unique.
            if ( fileNames.contains( currentFile ) )
            {
                throw new MojoExecutionException( getDuplicationErrorMessage( "file", currentFile,
                                                                              fileNames.indexOf( currentFile ), i ) );
            }
            fileNames.add( currentFile );
        }
    }

    /**
     * Replaces all namespaces within generated schema files, as instructed by the configured Schema instances.
     * 
     * @param resolverMap The map relating generated schema file name to SimpleNamespaceResolver instances.
     * @param configuredTransformSchemas The Schema instances read from the configuration of this plugin.
     * @param mavenLog The active Log.
     * @param schemaDirectory The directory where all generated schema files reside.
     * @throws MojoExecutionException If the namespace replacement could not be done.
     */
    public static void replaceNamespacePrefixes( final Map<String, SimpleNamespaceResolver> resolverMap,
                                                 final List<TransformSchema> configuredTransformSchemas,
                                                 final Log mavenLog, final File schemaDirectory )
        throws MojoExecutionException
    {
        if ( mavenLog.isDebugEnabled() )
        {
            mavenLog.debug( "Got resolverMap.keySet() [generated filenames]: " + resolverMap.keySet() );
        }

        for ( SimpleNamespaceResolver currentResolver : resolverMap.values() )
        {
            File generatedSchemaFile = new File( schemaDirectory, currentResolver.getSourceFilename() );
            Document generatedSchemaFileDocument = null;

            for ( TransformSchema currentTransformSchema : configuredTransformSchemas )
            {
                // Should we alter the namespace prefix as instructed by the current schema?
                final String newPrefix = currentTransformSchema.getToPrefix();
                final String currentUri = currentTransformSchema.getUri();

                if ( StringUtils.isNotEmpty( newPrefix ) )
                {
                    // Find the old/current prefix of the namespace for the current schema uri.
                    final String oldPrefix = currentResolver.getNamespaceURI2PrefixMap().get( currentUri );

                    if ( StringUtils.isNotEmpty( oldPrefix ) )
                    {
                        // Can we perform the prefix substitution?
                        validatePrefixSubstitutionIsPossible( oldPrefix, newPrefix, currentResolver );

                        if ( mavenLog.isDebugEnabled() )
                        {
                            mavenLog.debug( "Subtituting namespace prefix [" + oldPrefix + "] with [" + newPrefix
                                + "] in file [" + currentResolver.getSourceFilename() + "]." );
                        }

                        // Get the Document of the current schema file.
                        if ( generatedSchemaFileDocument == null )
                        {
                            generatedSchemaFileDocument = parseXmlToDocument( generatedSchemaFile );
                        }

                        // Replace all namespace prefixes within the provided document.
                        process( generatedSchemaFileDocument.getFirstChild(), true,
                                 new ChangeNamespacePrefixProcessor( oldPrefix, newPrefix ) );
                    }
                }
            }

            if ( generatedSchemaFileDocument != null )
            {
                // Overwrite the generatedSchemaFile with the content of the generatedSchemaFileDocument.
                mavenLog.debug( "Overwriting file [" + currentResolver.getSourceFilename() + "] with content ["
                    + getHumanReadableXml( generatedSchemaFileDocument ) + "]" );
                savePrettyPrintedDocument( generatedSchemaFileDocument, generatedSchemaFile );
            }
            else
            {
                mavenLog.debug( "No namespace prefix changes to generated schema file ["
                    + generatedSchemaFile.getName() + "]" );
            }
        }
    }

    /**
     * Updates all schemaLocation attributes within the generated schema files to match the 'file' properties within the
     * Schemas read from the plugin configuration. After that, the files are physically renamed.
     * 
     * @param resolverMap The map relating generated schema file name to SimpleNamespaceResolver instances.
     * @param configuredTransformSchemas The Schema instances read from the configuration of this plugin.
     * @param mavenLog The active Log.
     * @param schemaDirectory The directory where all generated schema files reside.
     */
    public static void renameGeneratedSchemaFiles( final Map<String, SimpleNamespaceResolver> resolverMap,
                                                   final List<TransformSchema> configuredTransformSchemas,
                                                   final Log mavenLog, final File schemaDirectory )
    {
        // Create the map relating namespace URI to desired filenames.
        Map<String, String> namespaceUriToDesiredFilenameMap = new TreeMap<String, String>();
        for ( TransformSchema current : configuredTransformSchemas )
        {
            if ( StringUtils.isNotEmpty( current.getToFile() ) )
            {
                namespaceUriToDesiredFilenameMap.put( current.getUri(), current.getToFile() );
            }
        }

        // Replace the schemaLocation values to correspond to the new filenames
        for ( SimpleNamespaceResolver currentResolver : resolverMap.values() )
        {
            File generatedSchemaFile = new File( schemaDirectory, currentResolver.getSourceFilename() );
            Document generatedSchemaFileDocument = parseXmlToDocument( generatedSchemaFile );

            // Replace all namespace prefixes within the provided document.
            process( generatedSchemaFileDocument.getFirstChild(), true,
                     new ChangeFilenameProcessor( namespaceUriToDesiredFilenameMap ) );

            // Overwrite the generatedSchemaFile with the content of the generatedSchemaFileDocument.
            if ( mavenLog.isDebugEnabled() )
            {
                mavenLog.debug( "Changed schemaLocation entries within [" + currentResolver.getSourceFilename() + "]. "
                    + "Result: [" + getHumanReadableXml( generatedSchemaFileDocument ) + "]" );
            }
            savePrettyPrintedDocument( generatedSchemaFileDocument, generatedSchemaFile );
        }

        // Now, rename the actual files.
        for ( SimpleNamespaceResolver currentResolver : resolverMap.values() )
        {
            final String localNamespaceURI = currentResolver.getLocalNamespaceURI();

            if ( StringUtils.isEmpty( localNamespaceURI ) )
            {
                mavenLog.warn( "SimpleNamespaceResolver contained no localNamespaceURI; aborting rename." );
                continue;
            }

            final String newFilename = namespaceUriToDesiredFilenameMap.get( localNamespaceURI );
            final File originalFile = new File( schemaDirectory, currentResolver.getSourceFilename() );

            if ( StringUtils.isNotEmpty( newFilename ) )
            {
                File renamedFile = FileUtils.resolveFile( schemaDirectory, newFilename );
                String renameResult = ( originalFile.renameTo( renamedFile ) ? "Success " : "Failure " );

                if ( mavenLog.isDebugEnabled() )
                {
                    String suffix = "renaming [" + originalFile.getAbsolutePath() + "] to [" + renamedFile + "]";
                    mavenLog.debug( renameResult + suffix );
                }
            }
        }
    }

    /**
     * Drives the supplied visitor to process the provided Node and all its children, should the recurseToChildren flag
     * be set to <code>true</code>. All attributes of the current node are processed before recursing to children (i.e.
     * breadth first recursion).
     * 
     * @param node The Node to process.
     * @param recurseToChildren if <code>true</code>, processes all children of the supplied node recursively.
     * @param visitor The NodeProcessor instance which should process the nodes.
     */
    public static void process( final Node node, final boolean recurseToChildren, final NodeProcessor visitor )
    {
        // Process the current Node, if the NodeProcessor accepts it.
        if ( visitor.accept( node ) )
        {
            visitor.process( node );
        }

        NamedNodeMap attributes = node.getAttributes();
        for ( int i = 0; i < attributes.getLength(); i++ )
        {
            Node attribute = attributes.item( i );

            // Process the current attribute, if the NodeProcessor accepts it.
            if ( visitor.accept( attribute ) )
            {
                visitor.process( attribute );
            }
        }

        if ( recurseToChildren )
        {
            NodeList children = node.getChildNodes();
            for ( int i = 0; i < children.getLength(); i++ )
            {
                Node child = children.item( i );

                // Recurse to Element children.
                if ( child.getNodeType() == Node.ELEMENT_NODE )
                {
                    process( child, true, visitor );
                }
            }
        }
    }

    /**
     * Parses the provided InputStream to create a dom Document.
     * 
     * @param xmlStream An InputStream connected to an XML document.
     * @return A DOM Document created from the contents of the provided stream.
     */
    public static Document parseXmlStream( final Reader xmlStream )
    {
        // Build a DOM model of the provided xmlFileStream.
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware( true );

        try
        {
            return factory.newDocumentBuilder().parse( new InputSource( xmlStream ) );
        }
        catch ( Exception e )
        {
            throw new IllegalArgumentException( "Could not acquire DOM Document", e );
        }
    }

    /**
     * Converts the provided DOM Node to a pretty-printed XML-formatted string.
     * 
     * @param node The Node whose children should be converted to a String.
     * @return a pretty-printed XML-formatted string.
     */
    protected static String getHumanReadableXml( final Node node )
    {
        StringWriter toReturn = new StringWriter();

        try
        {
            Transformer transformer = FACTORY.newTransformer();
            transformer.setOutputProperty( OutputKeys.INDENT, "yes" );
            transformer.setOutputProperty( OutputKeys.STANDALONE, "yes" );
            transformer.transform( new DOMSource( node ), new StreamResult( toReturn ) );
        }
        catch ( TransformerException e )
        {
            throw new IllegalStateException( "Could not transform node [" + node.getNodeName() + "] to XML", e );
        }

        return toReturn.toString();
    }

    //
    // Private helpers
    //

    private static String getDuplicationErrorMessage( final String propertyName, final String propertyValue,
                                                      final int firstIndex, final int currentIndex )
    {
        return MISCONFIG + "Duplicate '" + propertyName + "' property with value [" + propertyValue
            + "] found in plugin configuration. Correct schema elements index (" + firstIndex + ") and ("
            + currentIndex + "), to ensure that all '" + propertyName + "' values are unique.";
    }

    /**
     * Validates that the transformation from <code>oldPrefix</code> to <code>newPrefix</code> is possible, in that
     * <code>newPrefix</code> is not already used by a schema file. This would corrupt the schema by assigning elements
     * from one namespace to another.
     * 
     * @param oldPrefix The old/current namespace prefix.
     * @param newPrefix The new/future namespace prefix.
     * @param currentResolver The currently active SimpleNamespaceResolver.
     * @throws MojoExecutionException if any schema file currently uses <code>newPrefix</code>.
     */
    private static void validatePrefixSubstitutionIsPossible( final String oldPrefix, final String newPrefix,
                                                              final SimpleNamespaceResolver currentResolver )
        throws MojoExecutionException
    {
        // Make certain the newPrefix does not exist already.
        if ( currentResolver.getNamespaceURI2PrefixMap().containsValue( newPrefix ) )
        {
            throw new MojoExecutionException( MISCONFIG + "Namespace prefix [" + newPrefix + "] is already in use."
                + " Cannot replace namespace prefix [" + oldPrefix + "] with [" + newPrefix + "] in file ["
                + currentResolver.getSourceFilename() + "]." );
        }
    }

    /**
     * Creates a Document from parsing the XML within the provided xmlFile.
     * 
     * @param xmlFile The XML file to be parsed.
     * @return The Document corresponding to the xmlFile.
     */
    private static Document parseXmlToDocument( final File xmlFile )
    {
        Document result = null;
        Reader reader = null;
        try
        {
            reader = new FileReader( xmlFile );
            result = parseXmlStream( reader );
        }
        catch ( FileNotFoundException e )
        {
            // This should never happen...
        }
        finally
        {
            IOUtil.close( reader );
        }

        return result;
    }

    private static void savePrettyPrintedDocument( final Document toSave, final File targetFile )
    {
        Writer out = null;
        try
        {
            out = new BufferedWriter( new FileWriter( targetFile ) );
            out.write( getHumanReadableXml( toSave.getFirstChild() ) );
        }
        catch ( IOException e )
        {
            throw new IllegalStateException( "Could not write to file [" + targetFile.getAbsolutePath() + "]", e );
        }
        finally
        {
            IOUtil.close( out );
        }
    }
}