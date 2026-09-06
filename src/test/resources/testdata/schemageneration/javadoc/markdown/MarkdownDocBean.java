package markdown;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

/// A simple configuration bean demonstrating JDK 23 Markdown documentation comments.
///
/// This class uses `///` style comments (JEP 467) on both the class level
/// and individual fields.
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MarkdownDocBean")
public class MarkdownDocBean {

    /// Controls whether the feature is enabled.
    ///
    /// Defaults to `false`.
    @XmlAttribute
    private boolean enabled;

    /// The display name shown to the user.
    @XmlElement
    private String name;

    /// The maximum retry count.
    /// Must be a positive integer.
    @XmlElement
    private int maxRetries;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(final int maxRetries) {
        this.maxRetries = maxRetries;
    }
}
