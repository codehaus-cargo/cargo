package org.codehaus.cargo.sample.java.validator;

import org.codehaus.cargo.generic.packager.PackagerFactory;
import org.codehaus.cargo.generic.packager.DefaultPackagerFactory;
import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.packager.PackagerType;

/**
 * Validate that the specified container has a directory packager registered.
 *
 * @version $Id: HasRemoteDeployerValidator.java 1048 2006-05-29 19:38:52Z vmassol $
 */
public class HasDirectoryPackagerValidator implements Validator
{
    private PackagerFactory factory = new DefaultPackagerFactory();

    public boolean validate(String containerId, ContainerType type)
    {
        return this.factory.isPackagerRegistered(containerId, PackagerType.DIRECTORY);
    }
}
