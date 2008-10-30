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
import org.codehaus.cargo.util.AntUtils;

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
        doServerAndDomainNameArgs(java);
        java.createArg().setValue("-start");

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
        doServerAndDomainNameArgs(java);
        java.createArg().setValue("-halt");

        AntContainerExecutorThread jonasRunner = new AntContainerExecutorThread(java);
        jonasRunner.start();
    }

    /**
     * {@inheritDoc}<br>
     * This override replaces the CARGO WAR CPC with the JOnAS ping.
     */
    protected void waitForCompletion(final boolean waitForStarting) throws InterruptedException
    {
        while (true)
        {
            Thread.sleep(1000);

            Java java = (Java) new AntUtils().createAntTask("java");
            java.setFork(true);

            doAction(java);
            doServerAndDomainNameArgs(java);
            java.createArg().setValue("-ping");
            // IMPORTANT: impose timeout since default is 100 seconds
            java.createArg().setValue("-timeout");
            // Put as separate argument otherwise the ANT Java task sets the
            // argument to "-timeout 1" (with brackets!)
            java.createArg().setValue("1");
            java.reconfigure();

            int returnCode = java.executeJava();
            if (returnCode != 0 && returnCode != 1 && returnCode != 2)
            {
                throw new IllegalStateException("JonasAdmin ping returned " + returnCode
                        + ", the only values allowed are 0, 1 and 2");
            }
            boolean serverRunning = (returnCode == 0);
            if (serverRunning == waitForStarting)
            {
                break;
            }
        }
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
        String jonasRoot = getHome();
        if (jonasRoot == null)
        {
            LocalConfiguration configuration = getConfiguration();
            if (configuration instanceof Jonas5xStandaloneLocalConfiguration)
            {
                jonasRoot = ((Jonas5xStandaloneLocalConfiguration) configuration).getJonasRoot();
            }
            else
            {
                throw new IllegalStateException("JONAS_ROOT must be set");
            }
        }

        setupSysProps(java);
        java.addSysproperty(getAntUtils().createSysProperty("jonas.root",
            new File(jonasRoot).getAbsolutePath().replace(File.separatorChar, '/')));

        Path classpath = java.createClasspath();
        classpath.createPathElement().setLocation(
            new File(getHome(), "lib/bootstrap/felix-launcher.jar"));
        classpath.createPathElement().setLocation(
            new File(getHome(), "lib/bootstrap/jonas-commands.jar"));
        classpath.createPathElement().setLocation(
            new File(getHome(), "lib/bootstrap/jonas-version.jar"));
        classpath.createPathElement().setLocation(new File(getHome(), "conf"));

        try
        {
            addToolsJarToClasspath(classpath);
        }
        catch (IOException ex)
        {
            throw new ContainerException("IOException occured during java command line setup", ex);
        }

        java.setClassname("org.ow2.jonas.commands.admin.ClientAdmin");

        java.setDir(new File(getConfiguration().getHome()));
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
