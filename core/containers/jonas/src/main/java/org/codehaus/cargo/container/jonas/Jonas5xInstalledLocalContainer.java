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

import org.codehaus.cargo.container.ContainerCapability;
import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.jonas.internal.AbstractJonasInstalledLocalContainer;
import org.codehaus.cargo.container.jonas.internal.Jonas5xContainerCapability;
import org.codehaus.cargo.container.property.RemotePropertySet;
import org.codehaus.cargo.container.spi.jvm.JvmLauncher;
import org.codehaus.cargo.container.spi.jvm.JvmLauncherRequest;

/**
 * Support for the JOnAS JEE container.
 * 
 * @version $Id$
 */
public class Jonas5xInstalledLocalContainer extends AbstractJonasInstalledLocalContainer
{

    /**
     * Container capability instance.
     */
    private static final ContainerCapability CAPABILITY = new Jonas5xContainerCapability();

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
     * @see AbstractJonasInstalledLocalContainer#doStart(JvmLauncher)
     */
    @Override
    public void doStart(final JvmLauncher java)
    {
        doAction(java);
        doServerAndDomainNameArgs(java);
        java.addAppArguments("-start");
        doUsernameAndPasswordArgs(java);

        java.start();
    }

    /**
     * {@inheritDoc}
     * 
     * @see AbstractJonasInstalledLocalContainer#doStop(JvmLauncher)
     */
    @Override
    public void doStop(final JvmLauncher java)
    {
        doAction(java);
        doServerAndDomainNameArgs(java);
        java.addAppArguments("-stop");
        doUsernameAndPasswordArgs(java);

        // Call java.execute directly since ClientAdmin.stop is synchronous
        int returnCode = java.execute();
        if (returnCode != 0 && returnCode != 2)
        {
            throw new ContainerException("JonasAdmin stop returned " + returnCode
                    + ", the only values allowed are 0 and 2");
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.codehaus.cargo.container.spi.AbstractLocalContainer#waitForCompletion(boolean)
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
            JvmLauncherRequest request = new JvmLauncherRequest(false, this);
            JvmLauncher ping = getJvmLauncherFactory().createJvmLauncher(request);

            doAction(ping);
            doServerAndDomainNameArgs(ping);
            ping.addAppArguments("-ping");
            // IMPORTANT: impose timeout since default is 120 seconds
            // the argument is in milliseconds in JOnAS 5
            ping.addAppArguments("-timeout");
            ping.addAppArguments("2000");
            doUsernameAndPasswordArgs(ping);
            ping.setTimeout(10000);
            try
            {
                Thread.sleep(1000);
            }
            catch (InterruptedException e)
            {
                throw new ContainerException("Thread.sleep failed", e);
            }

            returnCode = ping.execute();
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
     * @see AbstractJonasInstalledLocalContainer#setupExtraSysProps(JvmLauncher, Map)
     */
    @Override
    protected void setupExtraSysProps(final JvmLauncher java,
        final Map<String, String> configuredSysProps)
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
     * Setup of the username and password for the JOnAS admin command call.
     * 
     * @param java the target JVM launcher to setup
     */
    protected void doUsernameAndPasswordArgs(final JvmLauncher java)
    {
        String username = getConfiguration().getPropertyValue(RemotePropertySet.USERNAME);
        String password = getConfiguration().getPropertyValue(RemotePropertySet.PASSWORD);

        if (username != null && username.trim().length() != 0
            && password != null && password.trim().length() != 0)
        {
            java.addAppArguments("-username");
            java.addAppArguments(username);
            java.addAppArguments("-password");
            java.addAppArguments(password);
        }
    }

    /**
     * Configuring the target java ant task to launch a JOnAS command.
     * 
     * @param java the target java ant task to setup
     */
    public void doAction(final JvmLauncher java)
    {
        setupSysProps(java);

        java.addClasspathEntries(new File(getHome(), "lib/bootstrap/felix-launcher.jar"));
        java.addClasspathEntries(new File(getHome(), "lib/bootstrap/jonas-commands.jar"));
        java.addClasspathEntries(new File(getHome(), "lib/bootstrap/jonas-version.jar"));
        java.addClasspathEntries(new File(getConfiguration().getHome(), "conf"));

        try
        {
            addToolsJarToClasspath(java);
        }
        catch (IOException ex)
        {
            throw new ContainerException("IOException occured during java command line setup", ex);
        }

        java.setMainClass("org.ow2.jonas.commands.admin.ClientAdmin");

        java.setWorkingDirectory(new File(getConfiguration().getHome()));
    }

    /**
     * {@inheritDoc}
     */
    public ContainerCapability getCapability()
    {
        return CAPABILITY;
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
