package se.west.converter;

import org.codehaus.mojo.jaxb2.shared.filters.pattern.StringConverter;

import java.io.File;

public class FileNameConverter implements StringConverter<File> {

    @Override
    public String convert(final File toConvert) {
        return toConvert.getName();
    }
}