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
package org.codehaus.cargo.container.deployable;

import org.codehaus.cargo.container.spi.deployable.AbstractDeployable;

/**
 * Wraps a RAR file that will be deployed in the container.
 * 
 * @version $Id$
 */
public class RAR extends AbstractDeployable
{

    /**
     * The name of this deployable (it can be anything, there's no special rule). If not specified
     * by user, it is computed from the RAR's file name (removing the filename extension).
     */
    private String name;

    /**
     * {@inheritDoc}
     * 
     * @see AbstractDeployable#AbstractDeployable(String)
     */
    public RAR(String rar)
    {
        super(rar);
    }

    /**
     * Parse the file name to set up the RAR name. The parsing occurs only if the user has not
     * already specified a custom name.
     * 
     * @see #setName(String)
     */
    private void parseName()
    {
        if (this.name == null)
        {
            String name = getFileHandler().getName(getFile());
            int nameIndex = name.toLowerCase().lastIndexOf(".rar");
            if (nameIndex >= 0)
            {
                name = name.substring(0, nameIndex);
            }

            getLogger().debug("Parsed RAR name = [" + name + "]", this.getClass().getName());

            setName(name);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see Deployable#getType()
     */
    public DeployableType getType()
    {
        return DeployableType.RAR;
    }

    /**
     * Return whether the RAR is exploded or not.
     * @return true if the RAR is a directory
     * @deprecated Use {@link #isExpanded()} instead.
     */
    @Deprecated
    public boolean isExpandedRar()
    {
        return isExpanded();
    }

    /**
     * Sets the name of this deployable. It can be anything (there's no special rule).
     * @param name the name of this deployable
     */
    public synchronized void setName(String name)
    {
        this.name = name;
    }

    /**
     * Returns the name of this deployable. If not specified by user, it is computed from the
     * RAR's file name (removing the filename extension).
     * @return the name of this deployable
     */
    public synchronized String getName()
    {
        parseName();
        return this.name;
    }

}
