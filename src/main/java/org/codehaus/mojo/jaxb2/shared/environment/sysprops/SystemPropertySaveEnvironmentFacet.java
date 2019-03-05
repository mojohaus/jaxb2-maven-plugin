package org.codehaus.mojo.jaxb2.shared.environment.sysprops;

import org.apache.maven.plugin.logging.Log;
import org.codehaus.mojo.jaxb2.shared.environment.AbstractLogAwareFacet;

/**
 * EnvironmentFacet which saves the value of a system property for the duration
 * of executing a tool. This may be required for tools (such as the XJC tool) which
 * may overwrite property values for its own purpose.
 * 
 * Unlike {@link SystemPropertyChangeEnvironmentFacet}, this does not a set a new
 * property value itself, just saves the old value and later restores or clears it.
 *  
 * This facet accepts the key of the property to save.
 *
 * @author <a href="https://github.com/shelgen">Svein Elgst&oslash;en</a>
 * @since 2.5
 */
public final class SystemPropertySaveEnvironmentFacet extends AbstractLogAwareFacet {
    private final String key;
    private final String originalValue;

    /**
     * Creates a SystemPropertySave which will remember the original value of the
     * supplied system property for the duration of this SystemPropertySave.
     *
     * @param key A non-null key.
     * @param log The active Maven Log.
     */
    public SystemPropertySaveEnvironmentFacet(final String key, final Log log) {
        // Delegate
        super(log);

        // Assign internal state
        this.key = key;
        this.originalValue = System.getProperty(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void restore() {
        if (originalValue != null) {
            System.setProperty(key, originalValue);
        } else {
            System.clearProperty(key);
        }

        if (log.isDebugEnabled()) {
            log.debug("Restored " + toString());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setup() {
        // Do nothing, value is saved in constructor

        if (log.isDebugEnabled()) {
            log.debug("Setup " + toString());
        }
    }

    @Override
    public String toString() {
        return "SysProp key [" + key + "], saved value: [" + originalValue + "]";
    }
}
