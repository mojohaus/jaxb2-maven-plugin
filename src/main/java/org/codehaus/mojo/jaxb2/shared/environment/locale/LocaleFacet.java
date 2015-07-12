package org.codehaus.mojo.jaxb2.shared.environment.locale;

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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.mojo.jaxb2.shared.Validate;
import org.codehaus.mojo.jaxb2.shared.environment.AbstractLogAwareFacet;

import java.util.Locale;
import java.util.StringTokenizer;

/**
 * EnvironmentFacet implementation which alters the default Locale for the
 * remainder of the tool execution.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class LocaleFacet extends AbstractLogAwareFacet {

    // Internal state
    private Locale originalLocale;
    private Locale newLocale;

    /**
     * Compound constructor creating a LocaleFacet wrapping the supplied instances.
     *
     * @param log       The active Maven Log.
     * @param newLocale The non-null Locale to be set by this LocaleFacet during execution.
     */
    public LocaleFacet(final Log log, final Locale newLocale) {
        super(log);

        // Check sanity
        Validate.notNull(newLocale, "usedLocale");

        // Assign internal state
        this.originalLocale = Locale.getDefault();
        this.newLocale = newLocale;
    }

    /**
     * {@inheritDoc}
     * <p>Changes the Locale during the execution of the plugin.</p>
     */
    @Override
    public void setup() {

        if (log.isInfoEnabled()) {
            log.info("Setting default locale to [" + newLocale + "]");
        }

        try {
            Locale.setDefault(newLocale);
        } catch (Exception e) {
            log.error("Could not switch locale to ["
                    + newLocale + "]. Continuing with standard locale.", e);
        }
    }

    /**
     * {@inheritDoc}
     * <p>Restores the original locale following the plugin's execution.</p>
     */
    @Override
    public void restore() {

        if (log.isInfoEnabled()) {
            log.info("Restoring default locale to [" + originalLocale + "]");
        }

        try {
            Locale.setDefault(originalLocale);
        } catch (Exception e) {
            log.error("Could not restore locale to [" + originalLocale + "]. Continuing with ["
                    + Locale.getDefault() + "]", e);
        }
    }

    /**
     * Helper method used to parse a locale configuration string into a Locale instance.
     *
     * @param localeString A configuration string parameter on the form
     *                     {@code &lt;language&gt;[,&lt;country&gt;[,&lt;variant&gt;]]}
     * @param log          The active Maven Log. Cannot be null.
     * @return A fully constructed Locale.
     * @throws MojoExecutionException if the localeString was not supplied on the required form.
     */
    public static LocaleFacet createFor(final String localeString, final Log log) throws MojoExecutionException {

        // Check sanity
        Validate.notNull(log, "log");
        Validate.notEmpty(localeString, "localeString");

        final StringTokenizer tok = new StringTokenizer(localeString, ",", false);
        final int numTokens = tok.countTokens();
        if (numTokens > 3 || numTokens == 0) {
            throw new MojoExecutionException("A localeString must consist of up to 3 comma-separated parts on the "
                    + "form <language>[,<country>[,<variant>]]. Received incorrect value '" + localeString + "'");
        }

        Locale locale = null;
        switch (numTokens) {
            case 3:
                locale = new Locale(tok.nextToken().trim(), tok.nextToken().trim(), tok.nextToken().trim());
                break;

            case 2:
                locale = new Locale(tok.nextToken().trim(), tok.nextToken().trim());
                break;

            default:
                locale = new Locale(tok.nextToken().trim());
                break;
        }

        // All done.
        return new LocaleFacet(log, locale);
    }
}
