/* 
 * ========================================================================
 * 
 * Copyright 2005-2006 Vincent Massol.
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
package org.codehaus.cargo.container.weblogic;

import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.deployer.Deployer;
import org.codehaus.cargo.container.spi.deployer.AbstractSwitchableLocalDeployer;

/**
 * Changes config.xml if the server is down. Otherwise, adds applications to the config directory.
 * 
 * @version $Id$
 */
public class WebLogic8xSwitchableLocalDeployer extends AbstractSwitchableLocalDeployer
{

    /**
     * deployer used when server is up.
     */
    private Deployer hotDeployer;

    /**
     * deployer used when server is down.
     */
    private Deployer coldDeployer;

    /**
     * {@inheritDoc}
     * 
     * @param container container to configure
     */
    public WebLogic8xSwitchableLocalDeployer(InstalledLocalContainer container)
    {
        super(container);
        hotDeployer = new WebLogicCopyingInstalledLocalDeployer(container);
        coldDeployer = new WebLogic8xConfigXmlInstalledLocalDeployer(container);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Deployer getColdDeployer()
    {
        return coldDeployer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Deployer getHotDeployer()
    {
        return hotDeployer;
    }

}
