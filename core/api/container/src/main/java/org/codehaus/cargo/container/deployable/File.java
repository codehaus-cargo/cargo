package org.codehaus.cargo.container.deployable;

import org.codehaus.cargo.container.spi.deployable.AbstractDeployable;

/**
 * Wraps a file that will be deployed into the container.
 * The file type can be used to deploy an generic file or directory
 * to the containers deploy directory. 
 * 
 * @version $Id$
 *
 */
public class File extends AbstractDeployable
{

    /**
     * Constructor.
     * @param file The file name
     */
    public File(String file)
    {
        super(file);
    }

    /**
     * {@inheritDoc}
     */
    public DeployableType getType()
    {
        return DeployableType.FILE;
    }

}
