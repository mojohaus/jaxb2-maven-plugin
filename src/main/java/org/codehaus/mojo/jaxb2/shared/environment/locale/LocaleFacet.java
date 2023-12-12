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

import java.util.Locale;
import java.util.StringTokenizer;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.mojo.jaxb2.shared.Validate;
import org.codehaus.mojo.jaxb2.shared.environment.AbstractLogAwareFacet;

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
    @SuppressWarnings("WeakerAccess")
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
            log.error("Could not switch locale to [" + newLocale + "]. Continuing with standard locale.", e);
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
            log.error(
                    "Could not restore locale to [" + originalLocale + "]. Continuing with [" + Locale.getDefault()
                            + "]",
                    e);
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

        // Extract the locale configuration data.
        final String language = tok.nextToken().trim();
        final String country = numTokens > 1 ? tok.nextToken().trim() : null;
        final String variant = numTokens > 2 ? tok.nextToken().trim() : null;

        // All done.
        return new LocaleFacet(log, findOptimumLocale(language, country, variant));
    }

    /**
     * Helper method to find the best matching locale, implying a workaround for problematic
     * case-sensitive Locale detection within the JDK. (C.f. Issue #112).
     *
     * @param language The given Language.
     * @param country  The given Country. May be null or empty to indicate that the Locale returned should not
     *                 contain a Country definition.
     * @param variant  The given Variant. May be null or empty to indicate that the Locale returned should not
     *                 contain a Variant definition.
     * @return The optimally matching Locale.
     */
    @SuppressWarnings("All")
    public static Locale findOptimumLocale(final String language, final String country, final String variant) {

        final boolean hasCountry = country != null && !country.isEmpty();
        final boolean hasVariant = variant != null && !variant.isEmpty();

        final Locale[] availableLocales = Locale.getAvailableLocales();
        for (int i = 0; i < availableLocales.length; i++) {

            final Locale current = availableLocales[i];

            // Extract the language/country/variant of the current Locale.
            final String currentLanguage = current.getLanguage();
            final String currentCountry = current.getCountry();
            final String currentVariant = current.getVariant();

            // Check if the current Locale matches the supplied
            final boolean isLanguageMatch = language.equalsIgnoreCase(currentLanguage);
            final boolean isCountryMatch = (hasCountry && country.equalsIgnoreCase(currentCountry))
                    || (!hasCountry && (currentCountry == null || currentCountry.isEmpty()));
            final boolean isVariantMatch = (hasVariant && variant.equalsIgnoreCase(currentVariant))
                    || (!hasVariant && (currentVariant == null || currentVariant.isEmpty()));

            if (isLanguageMatch && isCountryMatch && isVariantMatch) {
                return current;
            }
        }

        // Default to the default platform locale.
        return Locale.getDefault();
    }
}
