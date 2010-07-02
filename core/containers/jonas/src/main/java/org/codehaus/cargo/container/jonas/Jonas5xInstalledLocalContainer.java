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
    @Override
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
    @Override
    public void doStop(final Java java)
    {
        doAction(java);
        doServerAndDomainNameArgs(java);
        java.createArg().setValue("-stop");

        // Call java.execute directly since ClientAdmin.stop is synchronous
        int returnCode = java.executeJava();
        if (returnCode != 0 && returnCode != 2)
        {
            throw new ContainerException("JonasAdmin stop returned " + returnCode
                    + ", the only values allowed are 0 and 2");
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see AbstractLocalContainer#waitForCompletion(boolean)
     */
    @Override
    protected void waitForCompletion(boolean waitForStarting) throws InterruptedException
    {
        if (waitForStarting)
        {
            // Wait for JOnAS to start by pinging
            // (to ensure all modules are deployed and ready)
            this.ping(0);
        }
        else
        {
            // Wait for JOnAS to stop by listing JNDI
            this.ping(1);
        }
    }

    /**
     * Wait for the JOnAS server to reach a given state.
     *
     * @param expectedReturnCode expected return code.
     */
    protected void ping(int expectedReturnCode)
    {
        int returnCode = -1;

        // Wait for JOnAS to start by pinging (to ensure all modules are deployed and ready)
        long timeout = System.currentTimeMillis() + this.getTimeout();
        while (System.currentTimeMillis() < timeout)
        {
            Java ping = (Java) new AntUtils().createAntTask("java");
            ping.setFork(true);

            doAction(ping);
            doServerAndDomainNameArgs(ping);
            ping.createArg().setValue("-ping");
            // IMPORTANT: impose timeout since default is 120 seconds
            //            the argument is in milliseconds in JOnAS 5
            ping.createArg().setValue("-timeout");
            ping.createArg().setValue("2000");
            ping.reconfigure();
            ping.setTimeout(new Long(10000));
            try
            {
                Thread.sleep(1000);
            }
            catch (InterruptedException e)
            {
                throw new ContainerException("Thread.sleep failed", e);
            }

            returnCode = ping.executeJava();
            if (returnCode != -1 && returnCode != 0 && returnCode != 1 && returnCode != 2)
            {
                throw new ContainerException("JonasAdmin ping returned " + returnCode
                        + ", the only values allowed are -1, 0, 1 and 2");
            }
            if (returnCode == expectedReturnCode)
            {
                return;
            }
        }

        throw new ContainerException("Server did not reach wanted state after "
                + Long.toString(this.getTimeout()) + " milliseconds (code " + returnCode + ")");
    }

    /**
     * {@inheritDoc}
     *
     * @see AbstractJonasInstalledLocalContainer#setupExtraSysProps(Java, Map)
     */
    @Override
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

        Path classpath = java.createClasspath();
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
