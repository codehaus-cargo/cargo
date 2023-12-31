/*
 * ========================================================================
 *
 * Copyright 2007-2008 OW2. Code from this file
 * was originally imported from the OW2 JOnAS project.
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

/**
 * Support for the JOnAS JEE container.
 */
public class Jonas5xInstalledLocalContainer extends AbstractJonasInstalledLocalContainer
{

    /**
     * Container capability instance.
     */
    private static final ContainerCapability CAPABILITY = new Jonas5xContainerCapability();

    /**
     * {@inheritDoc}
     * @see AbstractJonasInstalledLocalContainer#AbstractJonasInstalledLocalContainer(org.codehaus.cargo.container.configuration.LocalConfiguration)
     */
    public Jonas5xInstalledLocalContainer(LocalConfiguration configuration)
    {
        super(configuration);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doStart(JvmLauncher java)
    {
        doAction(java);
        doServerAndDomainNameArgs(java);
        java.addAppArguments("-start");
        doUsernameAndPasswordArgs(java);

        java.start();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doStop(JvmLauncher java)
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
     */
    @Override
    protected void waitForCompletion(boolean waitForStarting) throws InterruptedException
    {
        if (waitForStarting)
        {
            waitForStarting(new Jonas5xContainerMonitor(this));
        }
        else
        {
            super.waitForCompletion(waitForStarting);
        }
    }

    /**
     * Ping the JOnAS server.
     * 
     * @return Return code from the JOnAS server.
     */
    public int ping()
    {
        JvmLauncher ping = createJvmLauncher(false);

        doAction(ping);
        doServerAndDomainNameArgs(ping);
        ping.addAppArguments("-ping");
        // IMPORTANT: impose timeout since default is 120 seconds,
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

        int returnCode = ping.execute();
        getLogger().debug("JonasAdmin ping returned " + returnCode, this.getClass().getName());
        if (returnCode != -1 && returnCode != 0 && returnCode != 1 && returnCode != 2)
        {
            throw new ContainerException("JonasAdmin ping returned " + returnCode
                + ", the only values allowed are -1, 0, 1 and 2");
        }
        return returnCode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setupExtraSysProps(JvmLauncher java, Map<String, String> configuredSysProps)
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
    protected void doUsernameAndPasswordArgs(JvmLauncher java)
    {
        String username = getConfiguration().getPropertyValue(RemotePropertySet.USERNAME);
        String password = getConfiguration().getPropertyValue(RemotePropertySet.PASSWORD);

        if (username != null && !username.trim().isEmpty()
            && password != null && !password.trim().isEmpty())
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
    public void doAction(JvmLauncher java)
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
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ContainerCapability getCapability()
    {
        return CAPABILITY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getId()
    {
        return "jonas5x";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName()
    {
        return "JOnAS 5.x";
    }
}
