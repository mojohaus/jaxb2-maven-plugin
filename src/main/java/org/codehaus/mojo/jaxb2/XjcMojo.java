/*
 * Copyright 2005 Jeff Genender.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codehaus.mojo.jaxb2;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.project.MavenProject;
import org.apache.maven.plugin.MojoExecutionException;
import org.xml.sax.SAXParseException;

import com.sun.tools.xjc.Driver;
import com.sun.tools.xjc.XJCListener;

/**
 * <p>A Maven 2 plugin which parse xsd and binding files (xjb) and produces
 * a corresponding object model based on the JAXB Xjc parsing engine.</p>
 *
 * @goal xjc
 * @phase generate-sources
 * @requiresDependencyResolution
 * @description JAXB 2.0 Plugin.
 * @author jgenender@apache.org
 * @author jgenender <jgenender@apache.org>
 * @version $Id$
 */
public class XjcMojo extends AbstractMojo {

    /**
     * @parameter expression="${project}"
     * @required
     */
    private MavenProject project;

    /**
     * The working directory to create the generated java source files.
     * 
     * @parameter expression="${basedir}/target/jaxb-source"
     * @required
     */
    protected String outputDirectory;

    /**
     * The package in which the source files will be generated.
     * 
     * @parameter
     */
    protected String packageName;

    /**
     * Catalog file to resolve external entity references support TR9401,
     * XCatalog, and OASIS XML Catalog format.
     * 
     * @parameter
     */
    protected String catalog;

    /**
     * Set HTTP/HTTPS proxy. Format is [user[:password]@]proxyHost[:proxyPort]
     * 
     * @parameter
     */
    protected String httpproxy;

    /**
     * The schema directory or xsd files
     * 
     * @parameter expression="${basedir}/src/main/xsd"
     * @required
     */
    protected String schemaDirectory;

    /**
     * The binding directory for xjb files
     * 
     * @parameter expression="${basedir}/src/main/xjb"
     */
    protected String bindingDirectory;

    /**
     * List of files to use for bindings, comma delimited. If none, then all xjb 
     * files are used in the bindingDirectory
     * 
     * @parameter
     */
    protected String bindingFiles;
    
    /**
     * List of files to use for bindings, comma delimited. If none, then all xsd 
     * files are used in the schemaDirectory
     * 
     * @parameter
     */
    protected String schemaFiles;

    /**
     * Treat input schemas as XML DTD (experimental, unsupported). 
     * 
     * @parameter default-value="false"
     */
    protected boolean dtd;
    
    /**
     * Suppress generation of package level annotations (package-info.java)
     * 
     * @parameter default-value="false"
     */
    protected boolean npa;

    /**
     * Do not perform strict validation of the input schema(s)
     * 
     * @parameter default-value="false"
     */
    protected boolean nv;
    
    /**
     * Treat input schemas as RELAX NG (experimental, unsupported). 
     * 
     * @parameter default-value="false"
     */
    protected boolean relaxng;
    
    /**
     * Treat input as RELAX NG compact syntax (experimental,unsupported)
     * 
     * @parameter default-value="false"
     */
    protected boolean relaxngCompact;

    /**
     * Suppress compiler output
     * 
     * @parameter default-value="false"
     */
    protected boolean quiet;

    /**
     * Generated files will be in read-only mode
     * 
     * @parameter default-value="false"
     */
    protected boolean readOnly;

    /**
     * Be extra verbose
     * 
     * @parameter default-value="false"
     */
    protected boolean verbose;

    /**
     * Treat input as WSDL and compile schemas inside it (experimental,unsupported)
     * 
     * @parameter default-value="false"
     */
    protected boolean wsdl;

    /**
     * Treat input as W3C XML Schema (default)
     * 
     * @parameter default-value="true"
     */
    protected boolean xmlschema;

    /**
     * Allow to use the JAXB Vendor Extensions.
     * 
     * @parameter default-value="false"
     */
    protected boolean extension;

    /**
     * The location of the flag file used to determine if the output is stale.
     * 
     * @parameter expression="${project.build.directory}/jaxb-source/.staleFlag"
     * @required
     */
    protected File staleFile;

    public void execute() throws MojoExecutionException {

        try {
            if (isOutputStale()) {
                getLog().info("Generating source...");
                File outDir = new File(outputDirectory);
                
                //If the directory exists, whack it to start fresh
                if (outDir.exists())
                    deleteDir(outDir);
                boolean success = outDir.mkdirs();
                if (!success) {
                    throw new MojoExecutionException(
                            "Could not create directory " + outputDirectory);
                }

                // Need to build a URLClassloader since Maven removed it form
                // the chain
                ClassLoader parent = this.getClass().getClassLoader();
                List classpathFiles = project.getCompileClasspathElements();
                URL[] urls = new URL[classpathFiles.size() + 1];
                StringBuffer classPath = new StringBuffer();
                for (int i = 0; i < classpathFiles.size(); ++i) {
                    getLog().debug((String) classpathFiles.get(i));
                    urls[i] = new File((String) classpathFiles.get(i)).toURL();
                    classPath.append((String) classpathFiles.get(i));
                    classPath.append(File.pathSeparatorChar);
                }

                urls[classpathFiles.size()] = new File(project.getBuild()
                        .getOutputDirectory()).toURL();
                URLClassLoader cl = new URLClassLoader(urls, parent);

                // Set the new classloader
                Thread.currentThread().setContextClassLoader(cl);

                ArrayList<String> args = getXJCArgs(classPath.toString());

                // Run XJC
                Driver.run((String[]) args.toArray(new String[args.size()]),
                        new MojoXjcListener());

                // Set back the old classloader
                Thread.currentThread().setContextClassLoader(parent);
                touchStaleFile();
            } else {
                getLog().info("No changes detected in schema or binding files, skipping source generation.");
            }

            project.addCompileSourceRoot(outputDirectory);

        } catch (MojoExecutionException e) {
            throw e;
        } catch (Exception e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }

    }

    private ArrayList<String> getXJCArgs(String classPath)
            throws MojoExecutionException {
        ArrayList<String> args = new ArrayList<String>();
        if (npa) {
            args.add("-npa");
        }
        if (nv) {
            args.add("-nv");
        }
        if (dtd) {
            args.add("-dtd");
        }
        if (verbose) {
            args.add("-verbose");
        }
        if (quiet) {
            args.add("-quiet");
        }
        if (readOnly) {
            args.add("-readOnly");
        }
        if (relaxng) {
            args.add("-relaxng");
        }
        if (relaxngCompact) {
            args.add("-relaxng-compact");
        }
        if (wsdl) {
            args.add("-wsdl");
        }
        if (xmlschema) {
            args.add("-xmlschema");
        }

        if (httpproxy != null) {
            args.add("-httpproxy");
            args.add(httpproxy);
        }

        if (packageName != null) {
            args.add("-p");
            args.add(packageName);
        }

        if (catalog != null) {
            args.add("-catalog");
            args.add(catalog);
        }
        args.add("-d");
        args.add(outputDirectory);
        args.add("-classpath");
        args.add(classPath);

        // Bindings
        File bindings[] = getBindingFiles();
        for (int i = 0; i < bindings.length; i++) {
            args.add("-b");
            args.add(bindings[i].getAbsolutePath());
        }
        args.add(schemaDirectory);

        getLog().debug("JAXB 2.0 args: " + args);

        return args;
    }

    /**
     * Returns a file array of xjb files to translate to object models.
     * 
     * @return An array of schema files to be parsed by the schema compiler.
     */
    public final File[] getBindingFiles() {

        List<File> bindings = new ArrayList<File>();
        if (bindingFiles != null) {
            for (StringTokenizer st = new StringTokenizer(bindingFiles, ","); st
                    .hasMoreTokens();) {
                String schemaName = st.nextToken();
                bindings.add(new File(bindingDirectory, schemaName));
            }
        } else {
            getLog().debug("The binding Directory is " + bindingDirectory);
            File bindingDir = new File(bindingDirectory);
            File[] files = bindingDir.listFiles(new XJBFile());
            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    bindings.add(files[i]);
                }
            }
        }

        return (File[]) bindings.toArray(new File[] {});
    }

    /**
     * Returns a file array of xjb files to translate to object models.
     * 
     * @return An array of schema files to be parsed by the schema compiler.
     */
    public final File[] getXSDFiles() {

        List<File> xsdFiles = new ArrayList<File>();
        if (schemaFiles != null) {
            for (StringTokenizer st = new StringTokenizer(schemaFiles, ","); st
                    .hasMoreTokens();) {
                String schemaName = st.nextToken();
                xsdFiles.add(new File(schemaDirectory, schemaName));
            }
        } else {
            getLog().debug("The schema Directory is " + schemaDirectory);
            File schemaDir = new File(schemaDirectory);
            File[] files = schemaDir.listFiles(new XSDFile());
            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    xsdFiles.add(files[i]);
                }
            }
        }

        return (File[]) xsdFiles.toArray(new File[] {});
    }

    /**
     * A class used to look up .xsd documents from a given directory.
     */
    private final class XJBFile implements FileFilter {

        /**
         * Returns true if the file ends with an xsd extension.
         * 
         * @param file
         *            The filed being reviewed by the filter.
         * @return true if an xsd file.
         */
        public boolean accept(final java.io.File file) {
            return file.getName().endsWith(".xjb");
        }

    }

    /**
     * A class used to look up .xsd documents from a given directory.
     */
    private final class XSDFile implements FileFilter {

        /**
         * Returns true if the file ends with an xsd extension.
         * 
         * @param file
         *            The filed being reviewed by the filter.
         * @return true if an xsd file.
         */
        public boolean accept(final java.io.File file) {
            return file.getName().endsWith(".xsd");
        }

    }

    /**
     * Returns true of any one of the files in the XSD/XJB array are more new than
     * the <code>staleFlag</code> file.
     * 
     * @return True if xsd files have been modified since the last build.
     */
    private boolean isOutputStale() {
        File[] sourceXsds = getXSDFiles();
        File[] sourceXjbs = getBindingFiles();
        boolean stale = !staleFile.exists();
        if (!stale) {
            getLog().debug("Stale flag file exists, comparing to xsds and xjbs.");
            long staleMod = staleFile.lastModified();

            for (int i = 0; i < sourceXsds.length; i++) {
                if (sourceXsds[i].lastModified() > staleMod) {
                    getLog().debug(
                            sourceXsds[i].getName()
                                    + " is newer than the stale flag file.");
                    stale = true;
                }
            }
            
            for (int i = 0; i < sourceXjbs.length; i++) {
                if (sourceXjbs[i].lastModified() > staleMod) {
                    getLog().debug(
                            sourceXjbs[i].getName()
                                    + " is newer than the stale flag file.");
                    stale = true;
                }
            }
        }
        return stale;
    }

    private void touchStaleFile() throws IOException {

        if (!staleFile.exists()) {
            staleFile.getParentFile().mkdirs();
            staleFile.createNewFile();
            getLog().debug("Stale flag file created.");
        } else {
            staleFile.setLastModified(System.currentTimeMillis());
        }
    }
    
    public boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i=0; i<children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
    
        return dir.delete();
    }
    
    //Class to tap into Maven's logging facility
    class MojoXjcListener extends XJCListener {

        public void error(SAXParseException arg0) {
            getLog().error(arg0);
        }

        public void fatalError(SAXParseException arg0) {
            getLog().error(arg0);
        }

        public void warning(SAXParseException arg0) {
            getLog().warn(arg0);
        }

        public void info(SAXParseException arg0) {
            getLog().warn(arg0);
        }

        public void message(String arg0) {
            getLog().info(arg0);
        }

        public void generatedFile(String arg0) {
            getLog().info(arg0);
        }

    }
}
