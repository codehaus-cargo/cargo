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
package org.codehaus.cargo.container.jboss;

import org.codehaus.cargo.container.RemoteContainer;

/**
 * Remote deployer that uses the Model Controller Client to deploy to JBoss.
 */
public class JBoss7xRemoteDeployer extends JBoss5xRemoteDeployer
{

    /**
     * @param container the container containing the configuration to use to find the deployer
     * properties such as url, user name and password to use to connect to the deployer
     */
    public JBoss7xRemoteDeployer(RemoteContainer container)
    {
        super(container);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getJBossRemoteDeployerJarName()
    {
        return "jboss-deployer-7";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getJBossConnectorClassName()
    {
        return "org.jboss.as.controller.client.ModelControllerClient";
    }
}
