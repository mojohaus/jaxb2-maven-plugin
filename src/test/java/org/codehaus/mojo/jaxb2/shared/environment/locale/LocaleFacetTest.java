package org.codehaus.mojo.jaxb2.shared.environment.locale;

import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.mojo.jaxb2.BufferingLog;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class LocaleFacetTest {

    // Shared state
    private static final Locale FRENCH_LOCALE = Locale.FRENCH;
    private Locale defaultLocale;
    private BufferingLog log;

    @Before
    public void setupSharedState() {
        this.log = new BufferingLog(BufferingLog.LogLevel.DEBUG);
        defaultLocale = Locale.getDefault();
    }

    @Test
    public void validateLocaleParsing() throws MojoExecutionException {

        // Assemble
        final String frenchLocaleString = "fr";

        // Act & Assert
        final LocaleFacet facet = LocaleFacet.createFor(frenchLocaleString, log);
        Assert.assertEquals(defaultLocale, Locale.getDefault());

        facet.setup();
        Assert.assertEquals(FRENCH_LOCALE.toString(), Locale.getDefault().toString());

        facet.restore();
        Assert.assertEquals(defaultLocale.toString(), Locale.getDefault().toString());
    }

    @Test
    public void validateOptimalLocaleFindingIgnoringScripts() throws MojoExecutionException {

        // Assemble
        final SortedMap<String, Locale> lang2Locale = new TreeMap<String, Locale>();
        for (Locale current : Locale.getAvailableLocales()) {
            lang2Locale.put(current.toLanguageTag(), current);
        }

        // Act & Assert
        for (Map.Entry<String, Locale> current : lang2Locale.entrySet()) {

            final String language = current.getValue().getLanguage();
            final String country = current.getValue().getCountry();
            final String variant = current.getValue().getVariant();
            final String script = current.getValue().getScript();

            // Ignore Locales with Scripts.
            if (script == null) {
                Assert.assertSame(Locale.forLanguageTag(current.getValue().toLanguageTag()),
                        LocaleFacet.findOptimumLocale(language, country, variant));
            }
        }
    }

    @Test(expected = MojoExecutionException.class)
    public void validateExceptionOnIncorrectLocaleString() throws MojoExecutionException {

        // Act & Assert
        LocaleFacet.createFor("not,a,properly,formatted,locale_string", log);
    }

    @Test
    public void validateNoExceptionOnUnknownLocaleString() throws MojoExecutionException {

        // Act & Assert
        LocaleFacet.createFor("thisIsAnUnknownLocale", log);
    }
}
