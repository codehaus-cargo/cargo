/* 
 * ========================================================================
 * 
 * Copyright 2005-2008 Vincent Massol.
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
package org.codehaus.cargo.generic.deployer;

import org.codehaus.cargo.container.Container;
import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.RemoteContainer;
import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.EmbeddedLocalContainer;
import org.codehaus.cargo.container.deployer.Deployer;
import org.codehaus.cargo.container.deployer.DeployerType;
import org.codehaus.cargo.generic.spi.AbstractIntrospectionGenericHintFactory;
import org.codehaus.cargo.generic.internal.util.RegistrationKey;
import org.codehaus.cargo.generic.internal.util.SimpleContainerIdentity;
import org.codehaus.cargo.generic.AbstractFactoryRegistry;

import java.lang.reflect.Constructor;

/**
 * Default {@link DeployerFactory} implementation that has all the known container
 * deployers registered against their containers. It also supports registering new
 * deployers against any container.
 *
 * @version $Id$
 */
public class DefaultDeployerFactory extends AbstractIntrospectionGenericHintFactory
    implements DeployerFactory
{
    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.generic.spi.AbstractGenericHintFactory.GenericParameters
     */
    private static class DeployerFactoryParameters implements GenericParameters
    {
        /**
         * The container to deploy to.
         */
        public Container container;
    }

    /**
     * Register default deployers.
     */
    public DefaultDeployerFactory()
    {
        this(null);
    }

    /**
     * Register deployer name mappings.
     *
     * @param classLoader
     *      ClassLoader to discover implementations from. See
     *      {@link AbstractFactoryRegistry#register(ClassLoader, DeployerFactory)}
     *      for the details of what this value means.
     */
    public DefaultDeployerFactory(ClassLoader classLoader)
    {
        super();

        // Note: We register containers using introspection so that we don't have to depend on
        // those containers at build time nor at runtime. More specifically this allows a user
        // to use the generic API and choose what container implementation jar he wants to use
        // without having to add all container implementations jars in the classpath.

        // Note: Sorted by container id alphabetical order
        registerGeronimo();

        registerJBoss();

        registerJetty();

        registerJO();

        registerJOnAS();
        
        registerJRun();

        registerOrion();

        registerResin();

        registerTomcat();

        registerWeblogic();

        AbstractFactoryRegistry.register(classLoader, this);
    }

    /**
     * Register Apache Geronimo
     */
    public void registerGeronimo()
    {
        registerDeployer("geronimo1x", DeployerType.INSTALLED,
            "org.codehaus.cargo.container.geronimo.GeronimoInstalledLocalDeployer");
    }

    /**
     * Register JBoss
     */
    public void registerJBoss()
    {
        registerDeployer("jboss3x", DeployerType.INSTALLED,
            "org.codehaus.cargo.container.jboss.JBossInstalledLocalDeployer");

        registerDeployer("jboss4x", DeployerType.INSTALLED,
            "org.codehaus.cargo.container.jboss.JBossInstalledLocalDeployer");
        registerDeployer("jboss4x", DeployerType.REMOTE,
            "org.codehaus.cargo.container.jboss.JBossRemoteDeployer");

        registerDeployer("jboss42x", DeployerType.INSTALLED,
            "org.codehaus.cargo.container.jboss.JBossInstalledLocalDeployer");
        registerDeployer("jboss42x", DeployerType.REMOTE,
            "org.codehaus.cargo.container.jboss.JBossRemoteDeployer");

        registerDeployer("jboss5x", DeployerType.INSTALLED,
            "org.codehaus.cargo.container.jboss.JBossInstalledLocalDeployer");
        registerDeployer("jboss5x", DeployerType.REMOTE,
            "org.codehaus.cargo.container.jboss.JBossRemoteDeployer");
    }

    /**
     * Register Jetty
     */
    public void registerJetty()
    {
        registerDeployer("jetty4x", DeployerType.EMBEDDED,
            "org.codehaus.cargo.container.jetty.Jetty4xEmbeddedLocalDeployer");

        registerDeployer("jetty5x", DeployerType.EMBEDDED,
            "org.codehaus.cargo.container.jetty.Jetty5xEmbeddedLocalDeployer");

        registerDeployer("jetty6x", DeployerType.EMBEDDED,
            "org.codehaus.cargo.container.jetty.Jetty6xEmbeddedLocalDeployer");
        registerDeployer("jetty6x", DeployerType.INSTALLED,
            "org.codehaus.cargo.container.jetty.Jetty6xInstalledLocalDeployer");
        registerDeployer("jetty6x", DeployerType.REMOTE,
            "org.codehaus.cargo.container.jetty.JettyRemoteDeployer");

        registerDeployer("jetty7x", DeployerType.EMBEDDED,
            "org.codehaus.cargo.container.jetty.Jetty6xEmbeddedLocalDeployer");
        registerDeployer("jetty7x", DeployerType.INSTALLED,
            "org.codehaus.cargo.container.jetty.Jetty6xInstalledLocalDeployer");
        registerDeployer("jetty7x", DeployerType.REMOTE,
            "org.codehaus.cargo.container.jetty.JettyRemoteDeployer");
    }

    /**
     * Register JO!
     */
    public void registerJO()
    {
        registerDeployer("jo1x", DeployerType.INSTALLED,
            "org.codehaus.cargo.container.jo.Jo1xInstalledLocalDeployer");
    }
    
    /**
     * Register OW2 JOnAS
     */
    public void registerJOnAS()
    {
        registerDeployer("jonas4x", DeployerType.REMOTE,
            "org.codehaus.cargo.container.jonas.Jonas4xJsr160RemoteDeployer");
        registerDeployer("jonas4x", DeployerType.INSTALLED,
            "org.codehaus.cargo.container.jonas.Jonas4xInstalledLocalDeployer");

        registerDeployer("jonas5x", DeployerType.REMOTE,
            "org.codehaus.cargo.container.jonas.Jonas5xJsr160RemoteDeployer");
        registerDeployer("jonas5x", DeployerType.INSTALLED,
            "org.codehaus.cargo.container.jonas.Jonas5xInstalledLocalDeployer");
    }
    
    /**
     * Register JRun
     */
    public void registerJRun()
    {
        registerDeployer("jrun4x", DeployerType.INSTALLED,
            "org.codehaus.cargo.container.jrun.JRun4xInstalledLocalDeployer");
    }

    /**
     * Register Orion
     */
    public void registerOrion()
    {
        // TODO: this is empty...
    }

    /**
     * Register Resin
     */
    public void registerResin()
    {
        registerDeployer("resin2x", DeployerType.INSTALLED,
            "org.codehaus.cargo.container.resin.ResinInstalledLocalDeployer");

        registerDeployer("resin3x", DeployerType.INSTALLED,
            "org.codehaus.cargo.container.resin.ResinInstalledLocalDeployer");
    }

    /**
     * Register Tomcat
     */
    public void registerTomcat()
    {
        registerDeployer("tomcat4x", DeployerType.INSTALLED,
            "org.codehaus.cargo.container.tomcat.TomcatCopyingInstalledLocalDeployer");

        registerDeployer("tomcat4x", DeployerType.REMOTE,
            "org.codehaus.cargo.container.tomcat.Tomcat4xRemoteDeployer");

        registerDeployer("tomcat5x", DeployerType.INSTALLED,
            "org.codehaus.cargo.container.tomcat.TomcatCopyingInstalledLocalDeployer");
        registerDeployer("tomcat5x", DeployerType.REMOTE,
            "org.codehaus.cargo.container.tomcat.Tomcat5xRemoteDeployer");
        registerDeployer("tomcat5x", DeployerType.EMBEDDED,
            "org.codehaus.cargo.container.tomcat.Tomcat5xEmbeddedLocalDeployer");

        registerDeployer("tomcat6x", DeployerType.INSTALLED,
             "org.codehaus.cargo.container.tomcat.TomcatCopyingInstalledLocalDeployer");
        registerDeployer("tomcat6x", DeployerType.REMOTE,
            "org.codehaus.cargo.container.tomcat.Tomcat6xRemoteDeployer");
    }

    /**
     * Register BEA/Oracle Weblogic
     */
    public void registerWeblogic()
    {
        registerDeployer("weblogic8x", DeployerType.INSTALLED,
            "org.codehaus.cargo.container.weblogic.WebLogic8xSwitchableLocalDeployer");
        registerDeployer("weblogic9x", DeployerType.INSTALLED,
            "org.codehaus.cargo.container.weblogic.WebLogicCopyingInstalledLocalDeployer");
        registerDeployer("weblogic10x", DeployerType.INSTALLED,
            "org.codehaus.cargo.container.weblogic.WebLogicCopyingInstalledLocalDeployer");
        registerDeployer("weblogic103x", DeployerType.INSTALLED,
            "org.codehaus.cargo.container.weblogic.WebLogicCopyingInstalledLocalDeployer");

    }

    /**
     * {@inheritDoc}
     * @see DeployerFactory#registerDeployer(String, DeployerType, Class)
     */
    public void registerDeployer(String containerId, DeployerType deployerType, Class deployerClass)
    {
        registerImplementation(new RegistrationKey(new SimpleContainerIdentity(containerId),
            deployerType.getType()), deployerClass);
    }

    /**
     * Registers a deployer using a class specified as a String.
     *
     * @param containerId {@inheritDoc}
     * @param deployerType {@inheritDoc}
     * @param deployerClassName the deployer implementation class to register as a String
     * @see #registerDeployer(String, DeployerType, Class)
     */
    public void registerDeployer(String containerId, DeployerType deployerType,
        String deployerClassName)
    {
        registerImplementation(new RegistrationKey(new SimpleContainerIdentity(containerId),
            deployerType.getType()), deployerClassName);
    }

    /**
     * {@inheritDoc}
     * @see DeployerFactory#isDeployerRegistered(String, DeployerType)
     */
    public boolean isDeployerRegistered(String containerId, DeployerType deployerType)
    {
        return hasMapping(new RegistrationKey(new SimpleContainerIdentity(containerId),
            deployerType.getType()));
    }

    /**
     * {@inheritDoc}
     * @see DeployerFactory#getDeployerClass(String, org.codehaus.cargo.container.deployer.DeployerType)
     */
    public Class getDeployerClass(String containerId, DeployerType deployerType)
    {
        return getMapping(new RegistrationKey(new SimpleContainerIdentity(containerId),
            deployerType.getType()));
    }

    /**
     * {@inheritDoc}
     * @see DeployerFactory#createDeployer(Container, DeployerType)
     */
    public Deployer createDeployer(Container container, DeployerType deployerType)
    {
        DeployerFactoryParameters parameters = new DeployerFactoryParameters();
        parameters.container = container;

        return (Deployer) createImplementation(new RegistrationKey(new SimpleContainerIdentity(
            container.getId()), deployerType.getType()), parameters, "deployer");
    }

    /**
     * {@inheritDoc}
     * @see DeployerFactory#createDeployer(Container)
     */
    public Deployer createDeployer(Container container)
    {
        Deployer deployer;

        DeployerType type = DeployerType.toType(container.getType());

        if (isDeployerRegistered(container.getId(), type))
        {
            getLogger().debug(
                "Creating a default [" + type + "] deployer", this.getClass().getName());
            deployer = createDeployer(container, type);
        }
        else
        {
            throw new ContainerException("There's no registered deployer matching your "
                + "container's type of [" + container.getType().getType() + "]");
        }

        return deployer;
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.generic.spi.AbstractGenericHintFactory#getConstructor(Class, String, GenericParameters)
     */
    protected Constructor getConstructor(Class deployerClass, String hint,
        GenericParameters parameters) throws NoSuchMethodException
    {
        Constructor constructor;

        DeployerType type = DeployerType.toType(hint);

        if (type == DeployerType.INSTALLED)
        {
            constructor = deployerClass.getConstructor(new Class[] {InstalledLocalContainer.class});
        }
        else if (type == DeployerType.EMBEDDED)
        {
            constructor = deployerClass.getConstructor(new Class[]{EmbeddedLocalContainer.class});
        }
        else if (type == DeployerType.REMOTE)
        {
            constructor = deployerClass.getConstructor(new Class[] {RemoteContainer.class});
        }
        else
        {
            throw new ContainerException("Unknown deployer type [" + type.getType() + "]");
        }

        return constructor;
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.generic.spi.AbstractGenericHintFactory#createInstance
     */
    protected Object createInstance(Constructor constructor, String hint,
        GenericParameters parameters) throws Exception
    {
        Container container = ((DeployerFactoryParameters) parameters).container;

        return constructor.newInstance(new Object[] {container});
    }
}
