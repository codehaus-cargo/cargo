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
package org.codehaus.cargo.daemon;

import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.configuration.LocalConfiguration;

/**
 * A handle keeps track of deployed containers.
 *
 * @version $Id$
 */
public class Handle
{
    /**
     * The unique handle identifier of a container.
     */
    private String id;

    /**
     * The installed container.
     */
    private InstalledLocalContainer container;

    /**
     * The configuration.
     */
    private LocalConfiguration configuration;

    /**
     * Constructs a handle.
     *
     * @param id The handle identifier.
     * @param container The container.
     * @param configuration The configuration.
     */
    public Handle(String id, InstalledLocalContainer container, LocalConfiguration configuration)
    {
        this.id = id;
        this.container = container;
        this.configuration = configuration;
    }

    /**
     * @return the handle identifier
     */
    public String getId()
    {
        return id;
    }

    /**
     * Sets the handle identifier.
     *
     * @param id The handle identifier.
     */
    public void setId(String id)
    {
        this.id = id;
    }

    /**
     * @return the container
     */
    public InstalledLocalContainer getContainer()
    {
        return container;
    }

    /**
     * Sets the container.
     *
     * @param container The container.
     */
    public void setContainer(InstalledLocalContainer container)
    {
        this.container = container;
    }

    /**
     * @return the configuration.
     */
    public LocalConfiguration getConfiguration()
    {
        return configuration;
    }

    /**
     * Sets the configuration.
     *
     * @param configuration The configuration
     */
    public void setConfiguration(LocalConfiguration configuration)
    {
        this.configuration = configuration;
    }

    @Override
    public String toString()
    {
        return id;
    }
}
