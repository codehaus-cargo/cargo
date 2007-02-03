package org.codehaus.cargo.sample.java.validator;

import org.codehaus.cargo.generic.ContainerFactory;
import org.codehaus.cargo.generic.DefaultContainerFactory;
import org.codehaus.cargo.container.ContainerType;

/**
 * Validate that a container id has an installed local container implementation.
 *
 * @version $Id$
 */
public class HasInstalledLocalContainerValidator implements Validator
{
    private ContainerFactory factory = new DefaultContainerFactory();

    /**
     * @return true if the container id has an installed local container implementation available,
     *         false otherwise
     */
    public boolean validate(String containerId, ContainerType type)
    {
        return this.factory.isContainerRegistered(containerId, ContainerType.INSTALLED);
    }
}
