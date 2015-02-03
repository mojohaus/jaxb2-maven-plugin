package org.codehaus.mojo.jaxb2.shared.filters.pattern;

/**
 * Trivial converter using the {@code toString()} method to convert a T object to a String.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 * @since 2.0
 */
public class ToStringConverter<T> implements StringConverter<T> {

    /**
     * {@inheritDoc}
     */
    @Override
    public String convert(final T toConvert) {
        return toConvert.toString();
    }
}
