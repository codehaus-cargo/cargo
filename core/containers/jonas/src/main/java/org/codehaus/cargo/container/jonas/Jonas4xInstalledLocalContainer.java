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

import java.util.Map;

import org.apache.tools.ant.taskdefs.Java;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.internal.AntContainerExecutorThread;
import org.codehaus.cargo.container.jonas.internal.AbstractJonasInstalledLocalContainer;

/**
 * Support for the JOnAS JEE container.
 * 
 * @version $Id$
 */
public class Jonas4xInstalledLocalContainer extends AbstractJonasInstalledLocalContainer
{
    /**
     * Unique container id.
     */
    public static final String ID = "jonas4x";

    /**
     * {@inheritDoc}
     * 
     * @see AbstractJonasInstalledLocalContainer#AbstractJonasInstalledLocalContainer(org.codehaus.cargo.container.configuration.LocalConfiguration)
     */
    public Jonas4xInstalledLocalContainer(LocalConfiguration configuration)
    {
        super(configuration);
    }

    /**
     * {@inheritDoc}
     * 
     * @see AbstractJonasInstalledLocalContainer#doStart(Java)
     */
    public void doStart(Java java)
    {
        doAction(java);
        java.createArg().setValue("org.objectweb.jonas.server.Server");
        java.createArg().setValue("-fg");

        AntContainerExecutorThread jonasRunner = new AntContainerExecutorThread(java);
        jonasRunner.start();
    }

    /**
     * {@inheritDoc}
     * 
     * @see AbstractJonasInstalledLocalContainer#doStop(Java)
     */
    public void doStop(Java java)
    {
        doAction(java);
        java.createArg().setValue("org.objectweb.jonas.adm.JonasAdmin");
        doServerNameParam(java);
        java.createArg().setValue("-s");

        AntContainerExecutorThread jonasRunner = new AntContainerExecutorThread(java);
        jonasRunner.start();
    }

    /**
     * {@inheritDoc}
     * 
     * @see AbstractJonasInstalledLocalContainer#setupExtraSysProps(Java, Map)
     */
    protected void setupExtraSysProps(Java java, Map configuredSysProps)
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
     * {@inheritDoc}
     * 
     * @see org.codehaus.cargo.container.Container#getId()
     */
    public String getId()
    {
        return ID;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.codehaus.cargo.container.Container#getName()
     */
    public String getName()
    {
        return "JOnAS " + getVersion("4.x");
    }
}
