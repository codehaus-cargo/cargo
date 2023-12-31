/*
 * ========================================================================
 *
 * Copyright 2003-2004 The Apache Software Foundation. Code from this file
 * was originally imported from the Jakarta Cactus project.
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2024 Ali Tokmen.
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
package org.codehaus.cargo.container.orion.internal;

import java.io.File;

import org.codehaus.cargo.container.ContainerCapability;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.internal.J2EEContainerCapability;
import org.codehaus.cargo.container.orion.Oc4jPropertySet;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.spi.AbstractInstalledLocalContainer;
import org.codehaus.cargo.container.spi.jvm.JvmLauncher;

/**
 * Abstract class for installed local container for the OC4J 10.x application server.
 */
public abstract class AbstractOc4j10xInstalledLocalContainer extends
    AbstractInstalledLocalContainer
{
    /**
     * Capability of the Orion container.
     */
    private ContainerCapability capability = new J2EEContainerCapability();

    /**
     * Constructor.
     * @param configuration The configuration for the container
     */
    public AbstractOc4j10xInstalledLocalContainer(LocalConfiguration configuration)
    {
        super(configuration);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ContainerCapability getCapability()
    {
        return this.capability;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doStop(JvmLauncher java)
    {
        File adminClientJar = new File(getHome() + "/j2ee/home/admin_client.jar");
        java.setJarFile(adminClientJar);

        String shutdownURL = "deployer:oc4j:"
            + getConfiguration().getPropertyValue(GeneralPropertySet.HOSTNAME) + ":"
            + getConfiguration().getPropertyValue(GeneralPropertySet.RMI_PORT);

        getLogger().debug("Shutdown URL [" + shutdownURL + "]", this.getClass().getName());

        java.addAppArguments(shutdownURL);
        java.addAppArguments("oc4jadmin");
        java.addAppArguments(getConfiguration().getPropertyValue(Oc4jPropertySet.ADMIN_PWD));
        java.addAppArguments("-shutdown");

        java.start();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doStart(JvmLauncher java) throws Exception
    {
        for (String path : getContainerClasspathIncludes())
        {
            java.addClasspathEntries(new File(path));
        }

        java.setMainClass(getStartClassname());
        java.addAppArguments("-config");
        java.addAppArgument(new File(getConfiguration().getHome(), "config/server.xml"));
        java.addAppArguments("-userThreads");

        java.start();
    }

    /**
     * @return name of the class to use when starting the container.
     */
    protected abstract String getStartClassname();

    /**
     * @return name of the class to use when stopping the container.
     */
    protected abstract String getStopClassname();

    /**
     * @return JAR files to include in the classpath based on the installation's home dir.
     */
    protected abstract String[] getContainerClasspathIncludes();
}
