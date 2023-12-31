/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2024 Ali Tokmen.
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

/**
 * Represents a container identified only by its id.
 */
public class SimpleContainerIdentity implements ContainerIdentity
{
    /**
     * The container id.
     */
    private String id;

    /**
     * @param id the container id
     */
    public SimpleContainerIdentity(String id)
    {
        this.id = id;
    }

    /**
     * @return the container id
     */
    public String getId()
    {
        return id;
    }

    /**
     * Differentiate two identities. {@inheritDoc}
     */
    @Override
    public boolean equals(Object identity)
    {
        boolean result = false;

        if (identity != null && identity instanceof SimpleContainerIdentity)
        {
            SimpleContainerIdentity id = (SimpleContainerIdentity) identity;
            if (id.getId().equals(getId()))
            {
                result = true;
            }
        }

        return result;
    }

    /**
     * Allows quick verification to check is two identities are different. {@inheritDoc}
     */
    @Override
    public int hashCode()
    {
        return getId().hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return "id = [" + getId() + "]";
    }
}
