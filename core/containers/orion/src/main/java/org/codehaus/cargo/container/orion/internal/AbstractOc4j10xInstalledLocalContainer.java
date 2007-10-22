/*
 * ========================================================================
 *
 * Copyright 2003-2004 The Apache Software Foundation. Code from this file
 * was originally imported from the Jakarta Cactus project.
 *
 * Copyright 2004-2006 Vincent Massol.
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
import java.util.Iterator;
import java.util.Set;

import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;
import org.codehaus.cargo.container.ContainerCapability;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.internal.AntContainerExecutorThread;
import org.codehaus.cargo.container.internal.J2EEContainerCapability;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.spi.AbstractInstalledLocalContainer;

/**
 * Abstract class for installed local container for the OC4J 10.x application server.
 *
 * @version $Id: $
 */
public abstract class AbstractOc4j10xInstalledLocalContainer extends AbstractInstalledLocalContainer
{
    /**
     * Capability of the Orion container.
     */
    private ContainerCapability capability = new J2EEContainerCapability();

    public AbstractOc4j10xInstalledLocalContainer(LocalConfiguration configuration)
    {
        super(configuration);
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.Container#getCapability()
     */
    public ContainerCapability getCapability()
    {
        return this.capability;
    }

    /**
     * {@inheritDoc}
     * @see AbstractInstalledLocalContainer#doStop(Java)
     */
    public final void doStop(Java java)
    {
        File adminClientJar = new File(getHome() + "/j2ee/home/admin_client.jar");
        java.setJar(adminClientJar);

        String shutdownURL = "deployer:oc4j:"
            + getConfiguration().getPropertyValue(GeneralPropertySet.HOSTNAME) + ":"
            + getConfiguration().getPropertyValue(GeneralPropertySet.RMI_PORT);

        getLogger().debug("Shutdown URL [" + shutdownURL + "]", this.getClass().getName());

        java.createArg().setValue(shutdownURL);
        java.createArg().setValue("oc4jadmin");
        java.createArg().setValue(getConfiguration().getPropertyValue(Oc4jPropertySet.ADMIN_PWD));
        java.createArg().setValue("-shutdown");

        AntContainerExecutorThread orionRunner = new AntContainerExecutorThread(java);
        orionRunner.start();
    }

    /**
     * {@inheritDoc}
     * @see AbstractInstalledLocalContainer#doStart(Java)
     */
    public final void doStart(Java java) throws Exception
    {
        // Invoke the main class
        Path classpath = java.createClasspath();
        FileSet fileSet = new FileSet();
        fileSet.setDir(new File(getHome()));
        Iterator i = getContainerClasspathIncludes().iterator();
        while(i.hasNext())
        {
            fileSet.createInclude().setName((String)i.next());
        }
        classpath.addFileset(fileSet);
        addToolsJarToClasspath(classpath);
        java.setClassname(getStartClassname());
        java.createArg().setValue("-config");
        java.createArg().setFile(new File(getConfiguration().getHome(), "config/server.xml"));
        java.createArg().setValue("-userThreads");

        AntContainerExecutorThread oc4jRunner = new AntContainerExecutorThread(java);
        oc4jRunner.start();
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
     * @return Set containing Ant-style include strings that sets the classpath
     *         based on the installation's home dir.
     */
    protected abstract Set getContainerClasspathIncludes();
}
