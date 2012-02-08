package org.codehaus.mojo.jaxb2.helpers;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Node;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>
 */
public class DebugNodeProcessor
    implements NodeProcessor
{
    // Internal state
    private NodeProcessor delegate;

    private List<Node> acceptedNodes = new ArrayList<Node>();

    /**
     * Creates a new DebugNodeProcessor, delegating all calls to the provided NodeProcessor.
     *
     * @param delegate The NodeProcessor to which all calls to this NodeProcessor
     *                 will be delegated.
     */
    public DebugNodeProcessor( NodeProcessor delegate )
    {
        this.delegate = delegate;
    }

    /** {@inheritDoc} */
    public boolean accept( Node aNode )
    {
        final boolean accepted = delegate.accept( aNode );
        if ( accepted )
        {
            acceptedNodes.add( aNode );
        }

        return accepted;
    }

    /** {@inheritDoc} */
    public void process( Node aNode )
    {
        delegate.process( aNode );
    }

    /**
     * @return The ordered List of Nodes accepted by the delegate NodeProcessor.
     */
    public List<Node> getAcceptedNodes()
    {
        return acceptedNodes;
    }
}