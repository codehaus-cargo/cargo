package org.codehaus.cargo.container.spi.startup;

import java.util.Arrays;
import java.util.List;

import org.codehaus.cargo.container.Container;
import org.codehaus.cargo.container.startup.ContainerMonitor;

/**
 * Monitor which gathers information from multiple monitors.
 */
public class CombinedContainerMonitor extends AbstractContainerMonitor 
{

    /**
     * Underlying monitors
     */
    private final List<ContainerMonitor> monitors;

    /**
     * Constructor.
     *
     * @param container Container to be monitored.
     * @param monitors Underlying monitors
     */
    public CombinedContainerMonitor(Container container, ContainerMonitor... monitors)
    {
        super(container);
        if (monitors == null || monitors.length == 0) 
        {
            throw new IllegalArgumentException("Specify at least one monitor");
        }
        this.monitors = Arrays.asList(monitors);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isRunning() 
    {
        for (ContainerMonitor monitor : monitors)
        {
            if (!monitor.isRunning()) 
            {
                return false;
            }
        }
        return true;
    }

}
