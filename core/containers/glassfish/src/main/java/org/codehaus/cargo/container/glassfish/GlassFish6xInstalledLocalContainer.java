/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2022 Ali Tokmen.
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
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.glassfish.internal.AbstractGlassFishInstalledLocalDeployer;
import org.codehaus.cargo.container.glassfish.internal.GlassFish6xContainerCapability;

/**
 * GlassFish 6.x installed local container.
 */
public class GlassFish6xInstalledLocalContainer extends GlassFish5xInstalledLocalContainer
{

    /**
     * Container capability instance.
     */
    private static final ContainerCapability CAPABILITY = new GlassFish6xContainerCapability();

    /**
     * Calls parent constructor, which saves the configuration.
     * 
     * @param localConfiguration Configuration.
     */
    public GlassFish6xInstalledLocalContainer(LocalConfiguration localConfiguration)
    {
        super(localConfiguration);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected AbstractGlassFishInstalledLocalDeployer getLocalDeployer()
    {
        return new GlassFish6xInstalledLocalDeployer(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ContainerCapability getCapability()
    {
        return CAPABILITY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getId()
    {
        return "glassfish6x";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName()
    {
        return "GlassFish " + getVersion("6.x");
    }

}
