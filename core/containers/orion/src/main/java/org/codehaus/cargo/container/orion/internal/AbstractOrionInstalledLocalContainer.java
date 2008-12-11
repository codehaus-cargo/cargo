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

import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;
import org.codehaus.cargo.container.ContainerCapability;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.internal.AntContainerExecutorThread;
import org.codehaus.cargo.container.internal.J2EEContainerCapability;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.spi.AbstractInstalledLocalContainer;

import java.io.File;

/**
 * Basic support for the Orion application server.
 * 
 * @version $Id$
 */
public abstract class AbstractOrionInstalledLocalContainer extends AbstractInstalledLocalContainer
{
    /**
     * Capability of the Orion container.
     */
    private ContainerCapability capability = new J2EEContainerCapability();

    /**
     * {@inheritDoc}
     * @see AbstractInstalledLocalContainer#AbstractInstalledLocalContainer(org.codehaus.cargo.container.configuration.LocalConfiguration)
     */
    public AbstractOrionInstalledLocalContainer(LocalConfiguration configuration)
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
        // invoke the main class
        Path classpath = java.createClasspath();
        FileSet fileSet = new FileSet();
        fileSet.setDir(new File(getHome()));
        fileSet.createInclude().setName(getContainerClasspathIncludes());
        classpath.addFileset(fileSet);
        java.setClassname(getStopClassname());

        String shutdownURL = "ormi://"
            + getConfiguration().getPropertyValue(GeneralPropertySet.HOSTNAME) + ":" 
            + getConfiguration().getPropertyValue(GeneralPropertySet.RMI_PORT) + "/";
        
        getLogger().debug("Shutdown URL [" + shutdownURL + "]", this.getClass().getName());
        
        java.createArg().setValue(shutdownURL);
        java.createArg().setValue("cargo");
        java.createArg().setValue("cargo");
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
        fileSet.createInclude().setName(getContainerClasspathIncludes());
        classpath.addFileset(fileSet);
        addToolsJarToClasspath(classpath);
        java.setClassname(getStartClassname());
        java.createArg().setValue("-config");
        java.createArg().setFile(new File(getConfiguration().getHome(), "conf/server.xml"));

        // Add the tools.jar to the classpath.
        addToolsJarToClasspath(classpath);
        
        AntContainerExecutorThread orionRunner = new AntContainerExecutorThread(java);
        orionRunner.start();
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
     * @return Ant-style include string that sets the classpath based on the installation's
     *         home dir.
     */
    protected abstract String getContainerClasspathIncludes();
}
