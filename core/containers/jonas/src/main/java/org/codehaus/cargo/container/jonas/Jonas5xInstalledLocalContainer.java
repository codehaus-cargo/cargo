/*
 * ========================================================================
 *
 * Copyright 2007-2008 OW2.
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
package org.codehaus.cargo.container.jonas;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.types.Path;
import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.internal.AntContainerExecutorThread;
import org.codehaus.cargo.container.jonas.internal.AbstractJonasInstalledLocalContainer;

/**
 * Support for the JOnAS JEE container.
 *
 * @version $Id$
 */
public class Jonas5xInstalledLocalContainer extends AbstractJonasInstalledLocalContainer
{
    /**
     * {@inheritDoc}
     *
     * @see AbstractJonasInstalledLocalContainer#AbstractJonasInstalledLocalContainer(org.codehaus.cargo.container.configuration.LocalConfiguration)
     */
    public Jonas5xInstalledLocalContainer(final LocalConfiguration configuration)
    {
        super(configuration);
    }

    /**
     * {@inheritDoc}
     *
     * @see AbstractJonasInstalledLocalContainer#doStart(Java)
     */
    public void doStart(final Java java)
    {
        doAction(java);
        java.createArg().setValue("org.ow2.jonas.commands.admin.ClientAdmin");
        java.createArg().setValue("start");
        doServerNameArgs(java);
        java.createArg().setValue("-fg");

        AntContainerExecutorThread jonasRunner = new AntContainerExecutorThread(java);
        jonasRunner.start();
    }

    /**
     * {@inheritDoc}
     *
     * @see AbstractJonasInstalledLocalContainer#doStop(Java)
     */
    public void doStop(final Java java)
    {
        doAction(java);
        java.createArg().setValue("org.ow2.jonas.commands.admin.ClientAdmin");
        doServerNameParam(java);
        java.createArg().setValue("halt");

        AntContainerExecutorThread jonasRunner = new AntContainerExecutorThread(java);
        jonasRunner.start();
    }

    /**
     * {@inheritDoc}
     *
     * @see AbstractJonasInstalledLocalContainer#setupExtraSysProps(Java, Map)
     */
    protected void setupExtraSysProps(final Java java, final Map configuredSysProps)
    {
        addSysProp(java, configuredSysProps, "org.omg.CORBA.ORBClass", "org.jacorb.orb.ORB");
        addSysProp(java, configuredSysProps, "org.omg.CORBA.ORBSingletonClass",
            "org.jacorb.orb.ORBSingleton");
        addSysProp(java, configuredSysProps,
            "org.omg.PortableInterceptor.ORBInitializerClass.standard_init",
            "org.jacorb.orb.standardInterceptors.IORInterceptorInitializer");
        addSysProp(java, configuredSysProps, "com.sun.CORBA.ORBDynamicStubFactoryFactoryClass",
            "com.sun.corba.se.impl.presentation.rmi.StubFactoryFactoryStaticImpl");
    }

    /**
     * Configuring the target java ant task to launch a JOnAS command.
     *
     * @param java the target java ant task to setup
     */
    public void doAction(final Java java)
    {
        setupSysProps(java);
        Variable jonasRoot = new Variable();
        jonasRoot.setKey("jonas.root");
        jonasRoot.setValue(getHome());
        java.addSysproperty(jonasRoot);

        java.setClassname("org.ow2.jonas.client.boot.Bootstrap");

        Path classpath = java.createClasspath();
        classpath.createPathElement().setLocation(
            new File(getHome(), "lib/bootstrap/client-bootstrap.jar"));
        classpath.createPathElement().setLocation(
            new File(getHome(), "lib/bootstrap/felix-launcher.jar"));
        classpath.createPathElement().setLocation(
            new File(getHome(), "lib/bootstrap/jonas-commands.jar"));
        classpath.createPathElement().setLocation(
            new File(getHome(), "lib/bootstrap/jonas-version.jar"));
        classpath.createPathElement().setLocation(new File(getConfiguration().getHome(), "conf"));
        try
        {
            addToolsJarToClasspath(classpath);
        }
        catch (IOException ex)
        {
            throw new ContainerException("IOException occured during java command line setup", ex);
        }

        java.setDir(new File(getConfiguration().getHome()));
        java.createArg().setValue("-start");
    }

    /**
     * {@inheritDoc}
     *
     * @see org.codehaus.cargo.container.Container#getId()
     */
    public String getId()
    {
        return "jonas5x";
    }

    /**
     * {@inheritDoc}
     *
     * @see org.codehaus.cargo.container.Container#getName()
     */
    public String getName()
    {
        return "JOnAS 5.x";
    }
}
