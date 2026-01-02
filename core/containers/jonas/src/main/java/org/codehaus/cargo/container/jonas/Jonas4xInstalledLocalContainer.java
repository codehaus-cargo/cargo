/*
 * ========================================================================
 *
 * Copyright 2007-2008 OW2. Code from this file
 * was originally imported from the OW2 JOnAS project.
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2026 Ali Tokmen.
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
import org.codehaus.cargo.container.jonas.internal.Jonas4xAdmin;
import org.codehaus.cargo.container.jonas.internal.Jonas4xAdminImpl;
import org.codehaus.cargo.container.jonas.internal.Jonas4xContainerCapability;
import org.codehaus.cargo.container.spi.jvm.JvmLauncher;

/**
 * Support for the JOnAS JEE container.
 */
public class Jonas4xInstalledLocalContainer extends AbstractJonasInstalledLocalContainer
{

    /**
     * Container capability instance.
     */
    private static final ContainerCapability CAPABILITY = new Jonas4xContainerCapability();

    /**
     * The JOnAS admin.
     */
    private Jonas4xAdmin jonasAdmin;

    /**
     * {@inheritDoc}
     * @see AbstractJonasInstalledLocalContainer#AbstractJonasInstalledLocalContainer(org.codehaus.cargo.container.configuration.LocalConfiguration)
     */
    public Jonas4xInstalledLocalContainer(LocalConfiguration configuration)
    {
        super(configuration);
        jonasAdmin = new Jonas4xAdminImpl(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doStart(JvmLauncher java)
    {
        doAction(java);
        doServerAndDomainNameArgs(java);
        java.addAppArguments("org.objectweb.jonas.server.Server");

        java.start();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doStop(JvmLauncher java)
    {
        // Wait until JonasAdmin stop succeeds, throw exception if anything bad occurs

        doAction(java);
        java.addAppArguments("org.objectweb.jonas.adm.JonasAdmin");
        doServerAndDomainNameParam(java);
        java.addAppArguments("-s");

        int returnCode = java.execute();
        if (returnCode != 0 && returnCode != 2)
        {
            throw new IllegalStateException("JonasAdmin stop returned " + returnCode
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
            waitForStarting(new Jonas4xContainerMonitor(this, jonasAdmin, waitForStarting));
        }
        else
        {
            super.waitForCompletion(waitForStarting);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setupExtraSysProps(JvmLauncher java, Map<String, String> configuredSysProps)
    {
        addSysProp(java, configuredSysProps, "jonas.default.classloader", "true");
        addSysProp(java, configuredSysProps, "org.omg.CORBA.ORBClass", "org.jacorb.orb.ORB");
        addSysProp(java, configuredSysProps, "org.omg.CORBA.ORBSingletonClass",
            "org.jacorb.orb.ORBSingleton");
        addSysProp(java, configuredSysProps,
            "org.omg.PortableInterceptor.ORBInitializerClass.standard_init",
            "org.jacorb.orb.standardInterceptors.IORInterceptorInitializer");
        addSysProp(java, configuredSysProps, "javax.rmi.CORBA.PortableRemoteObjectClass",
            "org.objectweb.carol.rmi.multi.MultiPRODelegate");
        addSysProp(java, configuredSysProps, "java.naming.factory.initial",
            "org.objectweb.carol.jndi.spi.MultiOrbInitialContextFactory");
        addSysProp(java, configuredSysProps, "javax.rmi.CORBA.UtilClass",
            "org.objectweb.carol.util.delegate.UtilDelegateImpl");
        addSysProp(java, configuredSysProps, "java.rmi.server.RMIClassLoaderSpi",
            "org.objectweb.jonas.server.RemoteClassLoaderSpi");
    }

    /**
     * Configuring the target java to launch a JOnAS command.
     * 
     * @param java the target JVM launcher to setup
     */
    public void doAction(JvmLauncher java)
    {
        setJvmToLaunchContainerIn(java);
        setupSysProps(java);

        java.setMainClass("org.objectweb.jonas.server.Bootstrap");

        java.addClasspathEntries(
            new File(getHome(), "lib/common/ow_jonas_bootstrap.jar"));
        java.addClasspathEntries(
            new File(getHome(), "lib/commons/jonas/jakarta-commons/commons-logging-api.jar"));
        java.addClasspathEntries(new File(getConfiguration().getHome(), "conf"));

        try
        {
            addToolsJarToClasspath(java);
        }
        catch (IOException ex)
        {
            throw new ContainerException("IOException occured during java command line setup", ex);
        }
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
        return "jonas4x";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName()
    {
        return "JOnAS 4.x";
    }
}
