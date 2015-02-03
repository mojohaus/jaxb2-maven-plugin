package org.codehaus.mojo.jaxb2.javageneration;

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

import com.sun.tools.xjc.XJCListener;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.mojo.jaxb2.shared.Validate;
import org.xml.sax.SAXParseException;

/**
 * Adapter implementation emitting XJC events to a Maven Log.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 * @since 2.0
 */
public class XjcLogAdapter extends XJCListener {

    // Internal state
    private Log log;

    /**
     * Creates an XjcLogAdapter which emits all XJC events onto the supplied Maven Log.
     *
     * @param log A non-null Log logging all inbound XJC events.
     */
    public XjcLogAdapter(final Log log) {

        // Check sanity
        Validate.notNull(log, "log");

        // Assign internal state
        this.log = log;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void generatedFile(final String fileName, final int current, final int total) {
        if (log.isDebugEnabled()) {
            log.debug("Processing file [" + current + "/" + total + "]: " + fileName);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void error(final SAXParseException exception) {
        log.error(getLocation(exception), exception);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void fatalError(final SAXParseException exception) {
        log.error(getLocation(exception), exception);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void warning(final SAXParseException exception) {
        log.warn(getLocation(exception), exception);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void info(final SAXParseException exception) {
        log.info(getLocation(exception), exception);
    }

    //
    // Private helpers
    //

    private String getLocation(final SAXParseException e) {

        final String exceptionId = e.getPublicId() == null ? e.getSystemId() : e.getPublicId();
        return exceptionId + " [" + e.getLineNumber() + "," + e.getColumnNumber() + "] ";
    }
}
