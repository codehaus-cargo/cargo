/*
 * ========================================================================
 *
 * Copyright 2007-2008 OW2. Code from this file
 * was originally imported from the OW2 JOnAS project.
 *
 * Codehaus CARGO, copyright 2004-2010 Vincent Massol.
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
package org.codehaus.cargo.container.jonas.internal;

import java.io.File;
import java.util.Map;

import org.apache.tools.ant.taskdefs.Java;
import org.codehaus.cargo.container.ContainerCapability;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.jonas.JonasPropertySet;
import org.codehaus.cargo.container.spi.AbstractInstalledLocalContainer;

/**
 * Support for the JOnAS JEE container.
 *
 * @version $Id$
 */
public abstract class AbstractJonasInstalledLocalContainer extends AbstractInstalledLocalContainer
{
    /**
     * Capability of the JOnAS container.
     */
    private ContainerCapability capability = new JonasContainerCapability();

    /**
     * {@inheritDoc}
     *
     * @see AbstractInstalledLocalContainer#AbstractInstalledLocalContainer(org.codehaus.cargo.container.configuration.LocalConfiguration)
     */
    public AbstractJonasInstalledLocalContainer(final LocalConfiguration configuration)
    {
        super(configuration);
    }

    /**
     * {@inheritDoc}
     *
     * @see AbstractInstalledLocalContainer#doStart(Java)
     */
    @Override
    public abstract void doStart(Java java);

    /**
     * {@inheritDoc}
     *
     * @see AbstractInstalledLocalContainer#doStart(Java)
     */
    @Override
    public abstract void doStop(Java java);

    /**
     * Setup of the target server and domain name for the JOnAS admin command call.
     *
     * @param java the target java ant task to setup
     */
    public void doServerAndDomainNameParam(final Java java)
    {
        String serverName = getConfiguration().getPropertyValue(JonasPropertySet.JONAS_SERVER_NAME);
        if (serverName != null && serverName.trim().length() != 0)
        {
            java.createArg().setValue("-n");
            java.createArg().setValue(serverName);
        }
        doDomainNameArgs(java);
    }

    /**
     * Setup of the target server and domain name for the JOnAS admin command call.
     *
     * @param java the target java ant task to setup
     */
    public void doServerAndDomainNameArgs(final Java java)
    {
        String serverName = getConfiguration().getPropertyValue(JonasPropertySet.JONAS_SERVER_NAME);
        if (serverName == null || serverName.trim().length() == 0)
        {
            serverName = "jonas";
        }
        java.createJvmarg().setValue("-Djonas.name=" + serverName);
        doDomainNameArgs(java);
    }

    /**
     * Setup of the target domain name for the JOnAS admin command call.
     *
     * @param java the target java ant task to setup
     */
    private void doDomainNameArgs(final Java java)
    {
        String domainName = getConfiguration().getPropertyValue(JonasPropertySet.JONAS_DOMAIN_NAME);
        if (domainName == null || domainName.trim().length() == 0)
        {
            domainName = "jonas";
        }
        java.createJvmarg().setValue("-Ddomain.name=" + domainName);
    }

    /**
     * Setup of the required java system properties to configure JOnAS properly.
     *
     * @param java the target java ant task to setup
     */
    public void setupSysProps(final Java java)
    {
        Map configuredSysProps = getSystemProperties();
        addSysProp(java, configuredSysProps, "install.root", new File(getHome()).getAbsolutePath()
            .replace(File.separatorChar, '/'));
        addSysProp(java, configuredSysProps, "jonas.root", new File(getHome()).getAbsolutePath()
            .replace(File.separatorChar, '/'));
        addSysProp(java, configuredSysProps, "jonas.base", new File(getConfiguration().getHome())
            .getAbsolutePath().replace(File.separatorChar, '/'));
        addSysProp(java, configuredSysProps, "java.endorsed.dirs", new File(getFileHandler()
            .append(getHome(), "lib/endorsed")).getAbsolutePath().replace(File.separatorChar, '/'));
        addSysProp(java, configuredSysProps, "java.security.policy", new File(getFileHandler()
            .append(getConfiguration().getHome(), "conf/java.policy")).getAbsolutePath().replace(
            File.separatorChar, '/'));
        addSysProp(java, configuredSysProps, "java.security.auth.login.config", new File(
            getFileHandler().append(getConfiguration().getHome(), "conf/jaas.config"))
            .getAbsolutePath().replace(File.separatorChar, '/'));
        addSysProp(java, configuredSysProps, "jonas.classpath", "");
        addSysProp(java, configuredSysProps, "java.awt.headless", "true");
        setupExtraSysProps(java, configuredSysProps);
    }

    /**
     * Setup of the Extra required java system properties to configure JOnAS properly. The system
     * properties depends on the JOnAS version.
     *
     * @param java the target java ant task to setup
     * @param configuredSysProps the configured system properties
     */
    protected abstract void setupExtraSysProps(Java java, Map configuredSysProps);

    /**
     * Add java system properties (to configure JOnAS properly).
     *
     * @param java the target java ant task on which we add the system properties
     * @param configuredSysProps the configured system Properties.
     * @param name the system property Name
     * @param value the system property Value
     */
    public void addSysProp(final Java java, final Map configuredSysProps, final String name,
        final String value)
    {
        if (configuredSysProps == null || !configuredSysProps.containsKey(name))
        {
            java.addSysproperty(getAntUtils().createSysProperty(name, value));
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see org.codehaus.cargo.container.Container#getCapability()
     */
    public ContainerCapability getCapability()
    {
        return capability;
    }
}
