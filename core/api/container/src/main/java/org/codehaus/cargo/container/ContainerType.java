/* 
 * ========================================================================
 * 
 * Codehaus CARGO, copyright 2004-2010 Vincent Massol.
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
package org.codehaus.cargo.container;

/**
 * Types of {@link org.codehaus.cargo.container.Container}s.
 * There are currently 3 types: Local Installed, Local Embedded and Remote.
 *
 * @version $Id$
 */
public class ContainerType
{
    /**
     * Represents a local installed container type.
     */
    public static final ContainerType INSTALLED = new ContainerType("installed");

    /**
     * Represents a local embedded container type.
     */
    public static final ContainerType EMBEDDED = new ContainerType("embedded");

    /**
     * Represents a remote container type.
     */
    public static final ContainerType REMOTE = new ContainerType("remote");

    /**
     * {@inheritDoc}
     * @see #ContainerType(String)
     */
    private String type;

    /**
     * @param type the internal representation of the container type.
     *        For example: "installed","embedded" or "remote".
     */
    public ContainerType(String type)
    {
        this.type = type;
    }

    /**
     * Transform a type represented as a string into a {@link ContainerType} object.
     *
     * @param typeAsString the string to transform
     * @return the {@link ContainerType} object
     */
    public static ContainerType toType(String typeAsString)
    {
        ContainerType type;
        if (typeAsString.equalsIgnoreCase(INSTALLED.getType()))
        {
            type = INSTALLED;
        }
        else if (typeAsString.equalsIgnoreCase(EMBEDDED.getType()))
        {
            type = EMBEDDED;
        }
        else if (typeAsString.equalsIgnoreCase(REMOTE.getType()))
        {
            type = REMOTE;
        }
        else
        {
            type = new ContainerType(typeAsString);
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
        if ((object != null) && (object instanceof ContainerType))
        {
            ContainerType type = (ContainerType) object;
            if (type.getType().equals(getType()))
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
     * @return the container's type as a string
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
        return getType();
    }

    /**
     * @return true if the container type is a local type (installed or embedded)
     */
    public boolean isLocal()
    {
        return ((this == INSTALLED) || (this == EMBEDDED));
    }

    /**
     * @return true if the container type is a remote type
     */
    public boolean isRemote()
    {
        return (this == REMOTE);
    }
}
