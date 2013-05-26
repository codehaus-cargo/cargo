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
package org.codehaus.cargo.container.glassfish;

import org.codehaus.cargo.container.ContainerCapability;
import org.codehaus.cargo.container.configuration.RuntimeConfiguration;

/**
 * GlassFish 4.x remote container.
 * 
 * @version $Id$
 */
public class GlassFish4xRemoteContainer extends GlassFish3xRemoteContainer
{
    /**
     * Unique container id.
     */
    private static final String ID = "glassfish4x";

    /**
     * the Capability of the JOnAS container.
     */
    private ContainerCapability capability = new GlassFish4xContainerCapability();

    /**
     * Constructor.
     * 
     * @param configuration the configuration to associate to this container.
     */
    public GlassFish4xRemoteContainer(RuntimeConfiguration configuration)
    {
        super(configuration);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.codehaus.cargo.container.Container#getCapability()
     */
    public ContainerCapability getCapability()
    {
        return capability;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.codehaus.cargo.container.Container#getId()
     */
    public String getId()
    {
        return ID;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.codehaus.cargo.container.Container#getName()
     */
    public String getName()
    {
        return "GlassFish 4.x Remote";
    }

}
