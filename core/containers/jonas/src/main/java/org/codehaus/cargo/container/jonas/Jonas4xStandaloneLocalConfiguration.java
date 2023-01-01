/*
 * ========================================================================
 *
 * Copyright 2007-2008 OW2. Code from this file
 * was originally imported from the OW2 JOnAS project.
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2023 Ali Tokmen.
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

import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.jonas.internal.AbstractJonasStandaloneLocalConfiguration;

/**
 * Implementation of a standalone {@link org.codehaus.cargo.container.configuration.Configuration}
 * for JOnAS.
 */
public class Jonas4xStandaloneLocalConfiguration extends AbstractJonasStandaloneLocalConfiguration
{
    /**
     * {@inheritDoc}
     * @see AbstractJonasStandaloneLocalConfiguration#AbstractJonasStandaloneLocalConfiguration(String)
     */
    public Jonas4xStandaloneLocalConfiguration(String dir)
    {
        super(dir);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doConfigure(LocalContainer container) throws Exception
    {
        super.doConfigure(container);

        // Deploy with user defined deployables with the appropriate deployer
        Jonas4xInstalledLocalDeployer deployer = new Jonas4xInstalledLocalDeployer(
            (Jonas4xInstalledLocalContainer) installedContainer);
        deployer.deploy(getDeployables());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return "JOnAS 4.x Standalone Configuration";
    }
}
