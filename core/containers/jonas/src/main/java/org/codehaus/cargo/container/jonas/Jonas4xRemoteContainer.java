/* 
 * ========================================================================
 * 
 * Copyright 2007-2008 OW2.
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
package org.codehaus.cargo.container.jonas;

import org.codehaus.cargo.container.ContainerCapability;
import org.codehaus.cargo.container.configuration.RuntimeConfiguration;
import org.codehaus.cargo.container.jonas.internal.JonasContainerCapability;
import org.codehaus.cargo.container.spi.AbstractRemoteContainer;

/**
 * JOnAS remote container.
 * 
 * @version $Id: Jonas4xRemoteContainer.java 14641 2008-07-25 11:46:29Z alitokmen $
 */
public class Jonas4xRemoteContainer extends AbstractRemoteContainer
{
    /**
     * Unique container id.
     */
    public static final String ID = "jonas4x";

    /**
     * the Capability of the JOnAS container.
     */
    private ContainerCapability capability = new JonasContainerCapability();

    /**
     * {@link Jonas4xRemoteContainer} Default constructor.
     * 
     * @param configuration the configuration to associate to this container.
     */
    public Jonas4xRemoteContainer(RuntimeConfiguration configuration)
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
        return "JOnAS 4.x Remote";
    }

}
