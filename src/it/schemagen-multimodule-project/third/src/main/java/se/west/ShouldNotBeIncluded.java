package se.west;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.FIELD)
public class ShouldNotBeIncluded {

    // Internal state
    private String someInternalState;
}