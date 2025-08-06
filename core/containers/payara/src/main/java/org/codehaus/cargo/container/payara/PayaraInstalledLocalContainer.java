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
import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.glassfish.GlassFish5xInstalledLocalContainer;
import org.codehaus.cargo.container.glassfish.internal.AbstractGlassFishInstalledLocalDeployer;
import org.codehaus.cargo.container.payara.internal.PayaraContainerCapability;
import org.codehaus.cargo.container.spi.jvm.JvmLauncher;

/**
 * Payara installed local container.
 */
public class PayaraInstalledLocalContainer extends GlassFish5xInstalledLocalContainer
{

    /**
     * Container capability instance.
     */
    private static final ContainerCapability CAPABILITY = new PayaraContainerCapability();

    /**
     * Calls parent constructor, which saves the configuration.
     * 
     * @param localConfiguration Configuration.
     */
    public PayaraInstalledLocalContainer(LocalConfiguration localConfiguration)
    {
        super(localConfiguration);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected AbstractGlassFishInstalledLocalDeployer getLocalDeployer()
    {
        return new PayaraInstalledLocalDeployer(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doStart(JvmLauncher java) throws Exception
    {
        String h2db = getFileHandler().append(getHome(), "h2db");
        if (getFileHandler().isDirectory(h2db))
        {
            java.setEnvironmentVariable("AS_H2_INSTALL", h2db);
        }

        super.doStart(java);
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
        return "payara";
    }

    /**
     * Parses and returns current major Payara version.
     * @return Major Payara version.
     */
    public int getVersion()
    {
        String version = getVersion("");
        if (version.isEmpty())
        {
            throw new ContainerException("Cannot read Payara version");
        }
        return Integer.parseInt(version.substring(0, version.indexOf(".")));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName()
    {
        String version = getVersion("");
        if (!version.isEmpty())
        {
            version = " " + version;
        }
        return "Payara" + version;
    }

}
