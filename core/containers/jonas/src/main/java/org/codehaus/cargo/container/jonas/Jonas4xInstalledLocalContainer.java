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
import org.codehaus.cargo.container.jonas.internal.Jonas4xAdmin;
import org.codehaus.cargo.container.jonas.internal.Jonas4xAdminImpl;

/**
 * Support for the JOnAS JEE container.
 *
 * @version $Id$
 */
public class Jonas4xInstalledLocalContainer extends AbstractJonasInstalledLocalContainer
{
    /**
     * The jonas admin.
     */
    private Jonas4xAdmin jonasAdmin;

    /**
     * {@inheritDoc}
     *
     * @see AbstractJonasInstalledLocalContainer#AbstractJonasInstalledLocalContainer(org.codehaus.cargo.container.configuration.LocalConfiguration)
     */
    public Jonas4xInstalledLocalContainer(final LocalConfiguration configuration)
    {
        super(configuration);
        jonasAdmin = new Jonas4xAdminImpl(this);
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
        java.createArg().setValue("org.objectweb.jonas.server.Server");

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
        // Wait until JonasAdmin stop succeeds, throw exception if anything bad occurs
        java.setFork(true);

        doAction(java);
        java.createArg().setValue("org.objectweb.jonas.adm.JonasAdmin");
        doServerAndDomainNameParam(java);
        java.createArg().setValue("-s");

        java.reconfigure();

        int returnCode = java.executeJava();
        if (returnCode != 0 && returnCode != 2)
        {
            throw new IllegalStateException("JonasAdmin stop returned " + returnCode
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
        long timeout = System.currentTimeMillis() + this.getTimeout();
        while (System.currentTimeMillis() < timeout)
        {
            try
            {
                Thread.sleep(1000);
            }
            catch (InterruptedException e)
            {
                throw new IllegalStateException("Thread.sleep failed");
            }

            if (waitForStarting)
            {
                // Wait for JOnAS to start by pinging
                // (to ensure all modules are deployed and ready)
                if (jonasAdmin.isServerRunning("ping", 0))
                {
                    return;
                }
            }
            else
            {
                // Wait for JOnAS to stop by listing JNDI
                if (jonasAdmin.isServerRunning("j", 2))
                {
                    return;
                }
            }
        }

        throw new ContainerException("Server.waitForCompletion not finished after "
                + Long.toString(timeout) + " milliseconds!");

    }

    /**
     * {@inheritDoc}
     *
     * @see AbstractJonasInstalledLocalContainer#setupExtraSysProps(Java, Map)
     */
    protected void setupExtraSysProps(final Java java, final Map configuredSysProps)
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
     * Configuring the target java ant task to launch a JOnAS command.
     *
     * @param java the target java ant task to setup
     */
    public void doAction(final Java java)
    {
        setupSysProps(java);

        java.setClassname("org.objectweb.jonas.server.Bootstrap");

        Path classpath = java.createClasspath();
        classpath.createPathElement().setLocation(
            new File(getHome(), "lib/common/ow_jonas_bootstrap.jar"));
        classpath.createPathElement().setLocation(
            new File(getHome(), "lib/commons/jonas/jakarta-commons/commons-logging-api.jar"));
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
    }

    /**
     * {@inheritDoc}
     *
     * @see org.codehaus.cargo.container.Container#getId()
     */
    public String getId()
    {
        return "jonas4x";
    }

    /**
     * {@inheritDoc}
     *
     * @see org.codehaus.cargo.container.Container#getName()
     */
    public String getName()
    {
        return "JOnAS 4.x";
    }
}
