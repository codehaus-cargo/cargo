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
package org.codehaus.cargo.container.jetty;

import org.codehaus.cargo.container.LocalContainer;

/**
 * A deployer for webapps that deploys to a Jetty 9.x installed instance.
 * 
 */
public class Jetty9xInstalledLocalDeployer extends Jetty7xInstalledLocalDeployer
{
    /**
     * {@inheritDoc}
     * @see Jetty7xInstalledLocalDeployer#Jetty7xInstalledLocalDeployer(org.codehaus.cargo.container.LocalContainer)
     */
    public Jetty9xInstalledLocalDeployer(LocalContainer container)
    {
        super(container);
    }

    @Override
    public String getContextsDir()
    {
        return getFileHandler().append(getContainer().getConfiguration().getHome(), "webapps");
    }

}
