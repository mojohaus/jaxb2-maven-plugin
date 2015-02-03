package org.codehaus.mojo.jaxb2.shared.filters.pattern;

/**
 * Specification for a converter rendering a T object as a String.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 * @since 2.0
 */
public interface StringConverter<T> {

    /**
     * Converts the supplied T object to a String.
     *
     * @param toConvert The T object to convert to a string. Not {@code null}.
     * @return The string form of the toConvert object.
     */
    String convert(T toConvert);
}
