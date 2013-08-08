/*
 * ========================================================================
 *
 * Copyright 2007-2008 OW2. Code from this file
 * was originally imported from the OW2 JOnAS project.
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
package org.codehaus.cargo.container.jonas.internal;

import java.io.File;
import java.util.Map;

import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.jonas.JonasPropertySet;
import org.codehaus.cargo.container.spi.AbstractInstalledLocalContainer;
import org.codehaus.cargo.container.spi.jvm.JvmLauncher;

/**
 * Support for the JOnAS JEE container.
 * 
 * @version $Id$
 */
public abstract class AbstractJonasInstalledLocalContainer extends AbstractInstalledLocalContainer
{
    /**
     * {@inheritDoc}
     * 
     * @see AbstractInstalledLocalContainer#AbstractInstalledLocalContainer(org.codehaus.cargo.container.configuration.LocalConfiguration)
     */
    public AbstractJonasInstalledLocalContainer(LocalConfiguration configuration)
    {
        super(configuration);
    }

    /**
     * {@inheritDoc}
     * 
     * @see AbstractInstalledLocalContainer#doStart(JvmLauncher)
     */
    @Override
    public abstract void doStart(JvmLauncher java);

    /**
     * {@inheritDoc}
     * 
     * @see AbstractInstalledLocalContainer#doStart(JvmLauncher)
     */
    @Override
    public abstract void doStop(JvmLauncher java);

    /**
     * Setup of the target server and domain name for the JOnAS admin command call.
     * 
     * @param java the target JVM launcher to setup
     */
    public void doServerAndDomainNameParam(JvmLauncher java)
    {
        String serverName = getConfiguration().getPropertyValue(JonasPropertySet.JONAS_SERVER_NAME);
        if (serverName != null && serverName.trim().length() != 0)
        {
            java.addAppArguments("-n");
            java.addAppArguments(serverName);
        }
        doDomainNameArgs(java);
    }

    /**
     * Setup of the target server and domain name for the JOnAS admin command call.
     * 
     * @param java the target JVM launcher to setup
     */
    public void doServerAndDomainNameArgs(JvmLauncher java)
    {
        String serverName = getConfiguration().getPropertyValue(JonasPropertySet.JONAS_SERVER_NAME);
        if (serverName == null || serverName.trim().length() == 0)
        {
            serverName = "jonas";
        }
        java.addJvmArguments("-Djonas.name=" + serverName);
        doDomainNameArgs(java);
    }

    /**
     * Setup of the target domain name for the JOnAS admin command call.
     * 
     * @param java the target JVM launcher to setup
     */
    private void doDomainNameArgs(JvmLauncher java)
    {
        String domainName = getConfiguration().getPropertyValue(JonasPropertySet.JONAS_DOMAIN_NAME);
        if (domainName == null || domainName.trim().length() == 0)
        {
            domainName = "jonas";
        }
        java.addJvmArguments("-Ddomain.name=" + domainName);
    }

    /**
     * Setup of the required java system properties to configure JOnAS properly.
     * 
     * @param java the target JVM launcher to setup
     */
    public void setupSysProps(JvmLauncher java)
    {
        Map<String, String> configuredSysProps = getSystemProperties();
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
     * @param java the target JVM launcher to setup
     * @param configuredSysProps the configured system properties
     */
    protected abstract void setupExtraSysProps(JvmLauncher java,
        Map<String, String> configuredSysProps);

    /**
     * Add java system properties (to configure JOnAS properly).
     * 
     * @param java the target JVM launcher on which we add the system properties
     * @param configuredSysProps the configured system Properties.
     * @param name the system property Name
     * @param value the system property Value
     */
    public void addSysProp(JvmLauncher java, final Map<String, String> configuredSysProps,
        final String name, final String value)
    {
        if (configuredSysProps == null || !configuredSysProps.containsKey(name))
        {
            java.setSystemProperty(name, value);
        }
    }
}
