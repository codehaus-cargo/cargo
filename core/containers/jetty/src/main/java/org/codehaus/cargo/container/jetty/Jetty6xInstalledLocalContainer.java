/*
 * ========================================================================
 *
 * Copyright 2007 Vincent Massol.
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

import org.codehaus.cargo.container.spi.AbstractInstalledLocalContainer;
import org.codehaus.cargo.container.ContainerCapability;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.container.internal.ServletContainerCapability;
import org.codehaus.cargo.container.internal.AntContainerExecutorThread;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.types.Path;

import java.io.File;

/**
 * Special container support for the Jetty 6.x servlet container.
 *
 * @version $Id: $
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
     * @see AbstractInstalledLocalContainer#doStart(Java)
     */
    public final void doStart(Java java) throws Exception
    {
        invoke(java, true);
    }

    /**
     * {@inheritDoc}
     * @see AbstractInstalledLocalContainer#doStop(Java)
     */
    public final void doStop(Java java) throws Exception
    {
        invoke(java, false);
    }

    /**
     * @param java the predefined Ant {@link org.apache.tools.ant.taskdefs.Java} command to use to
     *             start the container, passed by Cargo
     * @param isGettingStarted if true then start the container, stop it otherwise
     * @throws Exception in case of startup or shutdown error
     */
    private void invoke(Java java, boolean isGettingStarted) throws Exception
    {
        Path classpath = java.createClasspath();
        addToolsJarToClasspath(classpath);

        // If logging is set to "high" the turn it on by setting the DEBUG system property
        if ((getConfiguration().getPropertyValue(GeneralPropertySet.LOGGING) != null
            && (getConfiguration().getPropertyValue(GeneralPropertySet.LOGGING).equals("high"))))
        {
            java.addSysproperty(getAntUtils().createSysProperty("DEBUG", "true"));
        }

        // Set location where Jetty is installed
        java.addSysproperty(getAntUtils().createSysProperty("jetty.home", getHome()));

        // Add shutdown port
        java.addSysproperty(getAntUtils().createSysProperty("STOP.PORT",
            getConfiguration().getPropertyValue(GeneralPropertySet.RMI_PORT)));
        // Add shutdown key
        java.addSysproperty(getAntUtils().createSysProperty("STOP.KEY", "secret"));

        // Add listening port
        java.addSysproperty(getAntUtils().createSysProperty("jetty.port",
            getConfiguration().getPropertyValue(ServletPropertySet.PORT)));

        // Define the location of the configuration directory as a System property so that it
        // can be referenced from within the jetty.xml file.
        java.addSysproperty(getAntUtils().createSysProperty("config.home",
            getConfiguration().getHome()));

        // Location where logs will be generated
        java.addSysproperty(getAntUtils().createSysProperty("jetty.logs",
            getFileHandler().append(getConfiguration().getHome(), "logs")));

        java.setJar(new File(getHome(), "start.jar"));

        if (isGettingStarted) {
            java.createArg().setValue(
                getFileHandler().append(getConfiguration().getHome(), "etc/jetty.xml"));
        }
        else
        {
            java.createArg().setValue("--stop");
        }

        AntContainerExecutorThread jettyRunner = new AntContainerExecutorThread(java);
        jettyRunner.start();
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
