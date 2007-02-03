/*
 * ========================================================================
 *
 * Copyright 2006 Vincent Massol.
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
package org.codehaus.cargo.generic.internal.util;

import org.codehaus.cargo.container.ContainerType;

/**
 * Represents a container identified by its id and type.
 *
 * @version $Id: $
 */
public class FullContainerIdentity extends SimpleContainerIdentity
{
    /**
     * The container type.
     */
    private ContainerType type;

    /**
     * @param id the container id
     * @param type the container type
     */
    public FullContainerIdentity(String id, ContainerType type)
    {
        super(id);
        this.type = type;
    }

    /**
     * @return the container type
     */
    public ContainerType getType()
    {
        return this.type;
    }

    /**
     * Differentiate two identities.
     * {@inheritDoc}
     * @see Object#equals(java.lang.Object)
     */
    public boolean equals(Object identity)
    {
        boolean result = false;

        if ((identity != null) && (identity instanceof FullContainerIdentity))
        {
            FullContainerIdentity id = (FullContainerIdentity) identity;
            if (id.getId().equals(getId()) && (id.getType().equals(getType())))
            {
                result = true;
            }
        }

        return result;
    }

    /**
     * Allows quick verification to check is two identities are different.
     * {@inheritDoc}
     * @see Object#hashCode()
     */
    public int hashCode()
    {
        return (getId() + getType()).hashCode();
    }

    /**
     * {@inheritDoc}
     * @see Object#toString()
     */
    public String toString()
    {
        return "id = [" + getId() + "], type = [" + getType().getType() + "]";
    }
}
