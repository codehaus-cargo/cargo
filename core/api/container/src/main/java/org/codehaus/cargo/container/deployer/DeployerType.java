/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2011 Vincent Massol.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * ========================================================================
 */
package org.codehaus.cargo.container.deployer;

import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.ContainerType;

/**
 * Type of Deployer. Can be a installed, embedded or remote.
 * 
 * @version $Id$
 */
public final class DeployerType
{
    /**
     * A deployer type to deploy to an installed local container.
     */
    public static final DeployerType INSTALLED = new DeployerType("installed");

    /**
     * A deployer type to deploy to a remote container.
     */
    public static final DeployerType REMOTE = new DeployerType("remote");

    /**
     * A deployer type to deploy to an embedded local container.
     */
    public static final DeployerType EMBEDDED = new DeployerType("embedded");

    /**
     * A unique id that identifies a deployer's type.
     */
    private String type;

    /**
     * @param type A unique id that identifies a deployer's type
     */
    private DeployerType(String type)
    {
        this.type = type;
    }

    /**
     * Transform a type represented as a string into a {@link DeployerType} object.
     * 
     * @param typeAsString the string to transform
     * @return the {@link DeployerType} object
     */
    public static DeployerType toType(String typeAsString)
    {
        DeployerType type;

        if (typeAsString.equalsIgnoreCase(INSTALLED.type))
        {
            type = INSTALLED;
        }
        else if (typeAsString.equalsIgnoreCase(REMOTE.type))
        {
            type = REMOTE;
        }
        else if (typeAsString.equalsIgnoreCase(EMBEDDED.type))
        {
            type = EMBEDDED;
        }
        else
        {
            type = new DeployerType(typeAsString);
        }

        return type;
    }

    /**
     * Converts a {@link ContainerType} to the corresponding {@link DeployerType}.
     * 
     * @param containerType the container type to be converted.
     * @return the deployer type
     */
    public static DeployerType toType(ContainerType containerType)
    {
        DeployerType type;

        if (containerType == ContainerType.EMBEDDED)
        {
            type = EMBEDDED;
        }
        else if (containerType == ContainerType.INSTALLED)
        {
            type = INSTALLED;
        }
        else if (containerType == ContainerType.REMOTE)
        {
            type = REMOTE;
        }
        else
        {
            throw new ContainerException("Cannot find a deployer matching container type ["
                + containerType + "]");
        }

        return type;
    }

    /**
     * {@inheritDoc}
     * @see Object#equals(Object)
     */
    @Override
    public boolean equals(Object object)
    {
        boolean result = false;
        if (object instanceof DeployerType)
        {
            DeployerType type = (DeployerType) object;
            if (type.type.equalsIgnoreCase(this.type))
            {
                result = true;
            }
        }
        return result;
    }

    /**
     * {@inheritDoc}
     * @see Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        return this.type.hashCode();
    }

    /**
     * @return the deployer's type
     */
    public String getType()
    {
        return this.type;
    }

    /**
     * {@inheritDoc}
     * @see Object#toString()
     */
    @Override
    public String toString()
    {
        return this.type;
    }

    /**
     * @return true if the deployer type is a local type (installed or embedded)
     */
    public boolean isLocal()
    {
        return this == INSTALLED || this == EMBEDDED;
    }

    /**
     * @return true if the deployer type is a remote type
     */
    public boolean isRemote()
    {
        return this == REMOTE;
    }
}
