/*
 * ========================================================================
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
package org.codehaus.cargo.container.jetty;

import java.util.Arrays;
import java.util.List;

import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.spi.deployer.AbstractCopyingInstalledLocalDeployer;

/**
 * Jetty 12.x standalone
 * {@link org.codehaus.cargo.container.spi.configuration.ContainerConfiguration} implementation.
 */
public class Jetty12xStandaloneLocalConfiguration extends Jetty11xStandaloneLocalConfiguration
{
    /**
     * All <code>webdefault-*.xml</code> files from Jetty 12.x.
     */
    private static final List<String> WEBDEFAULT_XML_FILES =
        Arrays.asList("webdefault-ee8.xml", "webdefault-ee9.xml", "webdefault-ee10.xml");

    /**
     * {@inheritDoc}
     * @see Jetty9xStandaloneLocalConfiguration#Jetty9xStandaloneLocalConfiguration(String)
     */
    public Jetty12xStandaloneLocalConfiguration(String dir)
    {
        super(dir);
        setProperty(JettyPropertySet.MODULES, Jetty12xInstalledLocalContainer.DEFAULT_MODULES);
        setProperty(JettyPropertySet.DEPLOYER_EE_VERSION,
            Jetty12xInstalledLocalContainer.DEFAULT_DEPLOYER_EE_VERSION);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<String> getWebdefaultFiles()
    {
        return Jetty12xStandaloneLocalConfiguration.WEBDEFAULT_XML_FILES;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected AbstractCopyingInstalledLocalDeployer createDeployer(
        InstalledLocalContainer container)
    {
        Jetty12xInstalledLocalDeployer deployer = new Jetty12xInstalledLocalDeployer(container);
        return deployer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return "Jetty 12.x Standalone Configuration";
    }

}
