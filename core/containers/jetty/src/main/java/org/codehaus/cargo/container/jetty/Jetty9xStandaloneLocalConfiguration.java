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

import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.spi.deployer.AbstractCopyingInstalledLocalDeployer;

/**
 * Jetty 9.x standalone
 * {@link org.codehaus.cargo.container.spi.configuration.ContainerConfiguration} implementation.
 * 
 * @version $Id$
 */
public class Jetty9xStandaloneLocalConfiguration extends Jetty8xStandaloneLocalConfiguration
{
    /**
     * The list of files in which to replace <code>jetty.home</code> with
     * <code>config.hoome</code>.
     */
    private static String[] replaceJettyHomeInFiles = new String[]
    {
        "jetty-deploy.xml",
        "jetty-plus.xml",
        "jetty-spdy.xml",
        "jetty-https.xml",
        "test-realm.xml"
    };

    /**
     * {@inheritDoc}
     * @see Jetty8xStandaloneLocalConfiguration#Jetty8xStandaloneLocalConfiguration(String)
     */
    public Jetty9xStandaloneLocalConfiguration(String dir)
    {
        super(dir);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected AbstractCopyingInstalledLocalDeployer createDeployer(
        InstalledLocalContainer container)
    {
        Jetty9xInstalledLocalDeployer deployer = new Jetty9xInstalledLocalDeployer(container);
        return deployer;
    }

    /**
     * {@inheritDoc}
     * @see Object#toString()
     */
    @Override
    public String toString()
    {
        return "Jetty 9.x Standalone Configuration";
    }

    /**
     * {@inheritDoc}
     */
    protected String[] replaceJettyHomeInFiles()
    {
        return Jetty9xStandaloneLocalConfiguration.replaceJettyHomeInFiles;
    }

}
