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

import java.util.HashMap;
import java.util.Map;

import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.property.ServletPropertySet;
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
    protected void doConfigure(LocalContainer container) throws Exception
    {
        super.doConfigure(container);

        InstalledLocalContainer installedContainer = (InstalledLocalContainer) container;

        // Create directories required by Jettu 9.1.1 onwards
        getFileHandler().createDirectory(getHome(), "lib");
        getFileHandler().createDirectory(getHome(), "lib/ext");
        getFileHandler().createDirectory(getHome(), "resources");

        getFileHandler().copyDirectory(
            installedContainer.getHome() + "/start.d", getHome() + "/start.d");
        String httpIni = getFileHandler().append(getHome(), "start.d/http.ini");
        if (getFileHandler().exists(httpIni))
        {
            Map<String, String> httpIniReplacements = new HashMap<String, String>(1);
            httpIniReplacements.put("8080", getPropertyValue(ServletPropertySet.PORT));
            getFileHandler().replaceInFile(httpIni, httpIniReplacements, "UTF-8", false);
        }

        String libExt = getHome() + "/lib/ext";
        for (String extraClasspath : installedContainer.getExtraClasspath())
        {
            String destinationFile = libExt + "/" + getFileHandler().getName(extraClasspath);
            getFileHandler().copyFile(extraClasspath, destinationFile);
        }
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

}
