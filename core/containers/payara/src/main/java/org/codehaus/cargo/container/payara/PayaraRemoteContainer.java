/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2025 Ali Tokmen.
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
package org.codehaus.cargo.container.payara;

import org.codehaus.cargo.container.ContainerCapability;
import org.codehaus.cargo.container.configuration.RuntimeConfiguration;
import org.codehaus.cargo.container.glassfish.GlassFish5xRemoteContainer;
import org.codehaus.cargo.container.payara.internal.PayaraContainerCapability;

/**
 * Payara remote container.
 */
public class PayaraRemoteContainer extends GlassFish5xRemoteContainer
{
    /**
     * Unique container id.
     */
    public static final String ID = "payara";

    /**
     * the Capability of the JOnAS container.
     */
    private ContainerCapability capability = new PayaraContainerCapability();

    /**
     * Constructor.
     * 
     * @param configuration the configuration to associate to this container.
     */
    public PayaraRemoteContainer(RuntimeConfiguration configuration)
    {
        super(configuration);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ContainerCapability getCapability()
    {
        return capability;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getId()
    {
        return ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName()
    {
        return "Payara Remote";
    }

}
