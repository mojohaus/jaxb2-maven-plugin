package org.codehaus.mojo.jaxb2.shared.environment.locale;

import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.mojo.jaxb2.BufferingLog;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Locale;

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
