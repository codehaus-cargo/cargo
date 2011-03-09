/*
 * ========================================================================
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
package org.codehaus.cargo.container.jetty;

import java.io.File;

import org.codehaus.cargo.container.ContainerCapability;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.internal.ServletContainerCapability;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.container.spi.AbstractInstalledLocalContainer;
import org.codehaus.cargo.container.spi.jvm.JvmLauncher;

/**
 * Special container support for the Jetty 6.x servlet container.
 * 
 * @version $Id$
 */
public class Jetty6xInstalledLocalContainer extends AbstractInstalledLocalContainer
{
    /**
     * Unique container id.
     */
    public static final String ID = "jetty6x";

    /**
     * Capability of the Jetty container.
     */
    private ContainerCapability capability = new ServletContainerCapability();

    /**
     * Jetty6xInstalledLocalContainer Constructor.
     * @param configuration The configuration associated with the container
     */
    public Jetty6xInstalledLocalContainer(LocalConfiguration configuration)
    {
        super(configuration);
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.Container#getId()
     */
    public final String getId()
    {
        return ID;
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.Container#getName()
     */
    public final String getName()
    {
        return "Jetty 6.x";
    }

    /**
     * {@inheritDoc}
     * @see AbstractInstalledLocalContainer#doStart(JvmLauncher)
     */
    @Override
    public final void doStart(JvmLauncher java) throws Exception
    {
        invoke(java, true);
    }

    /**
     * {@inheritDoc}
     * @see AbstractInstalledLocalContainer#doStop(JvmLauncher)
     */
    @Override
    public final void doStop(JvmLauncher java) throws Exception
    {
        invoke(java, false);
    }

    /**
     * @param java the predefined JVM launcher to use to start the container, passed by Cargo
     * @param isGettingStarted if true then start the container, stop it otherwise
     * @throws Exception in case of startup or shutdown error
     */
    private void invoke(JvmLauncher java, boolean isGettingStarted) throws Exception
    {
        addToolsJarToClasspath(java);

        // If logging is set to "high" the turn it on by setting the DEBUG system property
        if (getConfiguration().getPropertyValue(GeneralPropertySet.LOGGING) != null
            && getConfiguration().getPropertyValue(GeneralPropertySet.LOGGING).equals("high"))
        {
            java.setSystemProperty("DEBUG", "true");
        }

        // Set location where Jetty is installed
        java.setSystemProperty("jetty.home", getHome());

        // Add shutdown port
        java.setSystemProperty("STOP.PORT",
            getConfiguration().getPropertyValue(GeneralPropertySet.RMI_PORT));
        // Add shutdown key
        java.setSystemProperty("STOP.KEY", "secret");

        // Add listening port
        java.setSystemProperty("jetty.port",
            getConfiguration().getPropertyValue(ServletPropertySet.PORT));

        // Define the location of the configuration directory as a System property so that it
        // can be referenced from within the jetty.xml file.
        java.setSystemProperty("config.home", getConfiguration().getHome());

        // Location where logs will be generated
        java.setSystemProperty("jetty.logs",
            getFileHandler().append(getConfiguration().getHome(), "logs"));

        java.setJarFile(new File(getHome(), "start.jar"));

        if (isGettingStarted)
        {
            java.addAppArguments(
                getFileHandler().append(getConfiguration().getHome(), "etc/jetty.xml"));
        }
        else
        {
            java.addAppArguments("--stop");
        }

        // For Jetty to pick up on the extra classpath it needs to export
        // the classpath as an environment variable 'CLASSPATH'
        java.setSystemProperty("CLASSPATH", java.getClasspath());

        java.start();
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.Container#getCapability()
     */
    public ContainerCapability getCapability()
    {
        return this.capability;
    }
}
