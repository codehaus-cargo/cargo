/*
 * ========================================================================
 *
 * Copyright 2004-2008 Vincent Massol.
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
package org.codehaus.cargo.generic;

import org.codehaus.cargo.container.configuration.Configuration;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.configuration.RuntimeConfiguration;
import org.codehaus.cargo.container.Container;
import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.generic.spi.AbstractIntrospectionGenericHintFactory;
import org.codehaus.cargo.generic.internal.util.RegistrationKey;
import org.codehaus.cargo.generic.internal.util.SimpleContainerIdentity;

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;

/**
 * Default implementation of {@link ContainerFactory}. Registers all known containers.
 *
 * @version $Id$
 */
public class DefaultContainerFactory extends AbstractIntrospectionGenericHintFactory
    implements ContainerFactory
{
    /**
     * @see GenericParameters
     */
    private static class ContainerFactoryParameters implements GenericParameters
    {
        /**
         * The configuration object to associate with the container.
         */
        public Configuration configuration;
    }

    /**
     * Initialize container name mappings with container ids.
     */
    public DefaultContainerFactory()
    {
        this(null);
    }

    /**
     * Register packager name mappings.
     *
     * @param classLoader
     *      ClassLoader to discover implementations from. See
     *      {@link AbstractFactoryRegistry#register(ClassLoader, ContainerFactory)}
     *      for the details of what this value means.
     */
    public DefaultContainerFactory(ClassLoader classLoader)
    {
        super();

        // Note: We register containers using introspection so that we don't have to depend on
        // those containers at build time nor at runtime. More specifically this allows a user
        // to use the generic API and choose what container implementation jar he wants to use
        // without having to add all container implementations jars in the classpath.

        // Note: Sorted by container id alphabetical order
        registerGeronimo();

        registerJBoss();

        registerJO();

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
        registerContainer("geronimo1x", ContainerType.INSTALLED,
            "org.codehaus.cargo.container.geronimo.Geronimo1xInstalledLocalContainer");
    }

    /**
     * Register JBoss
     */
    public void registerJBoss()
    {
        registerContainer("jboss3x", ContainerType.INSTALLED,
            "org.codehaus.cargo.container.jboss.JBoss3xInstalledLocalContainer");

        registerContainer("jboss4x", ContainerType.INSTALLED,
            "org.codehaus.cargo.container.jboss.JBoss4xInstalledLocalContainer");
        registerContainer("jboss4x", ContainerType.REMOTE,
            "org.codehaus.cargo.container.jboss.JBoss4xRemoteContainer");

        registerContainer("jboss42x", ContainerType.INSTALLED,
            "org.codehaus.cargo.container.jboss.JBoss42xInstalledLocalContainer");
        registerContainer("jboss42x", ContainerType.REMOTE,
            "org.codehaus.cargo.container.jboss.JBoss42xRemoteContainer");

        registerContainer("jboss5x", ContainerType.INSTALLED,
            "org.codehaus.cargo.container.jboss.JBoss5xInstalledLocalContainer");
        registerContainer("jboss5x", ContainerType.REMOTE,
            "org.codehaus.cargo.container.jboss.JBoss5xRemoteContainer");

        registerContainer("jboss51x", ContainerType.INSTALLED,
            "org.codehaus.cargo.container.jboss.JBoss51xInstalledLocalContainer");
        registerContainer("jboss51x", ContainerType.REMOTE,
            "org.codehaus.cargo.container.jboss.JBoss51xRemoteContainer");

        registerContainer("jboss6x", ContainerType.INSTALLED,
            "org.codehaus.cargo.container.jboss.JBoss6xInstalledLocalContainer");
        registerContainer("jboss6x", ContainerType.REMOTE,
            "org.codehaus.cargo.container.jboss.JBoss6xRemoteContainer");
    }

    /**
     * Register JO!
     */
    public void registerJO()
    {
        registerContainer("jo1x", ContainerType.INSTALLED,
            "org.codehaus.cargo.container.jo.Jo1xInstalledLocalContainer");
    }
    
    /**
     * Register JRun
     */
    public void registerJRun()
    {
        registerContainer("jrun4x", ContainerType.INSTALLED,
            "org.codehaus.cargo.container.jrun.JRun4xInstalledLocalContainer");
    }    

    /**
     * Register Orion
     */
    public void registerOrion()
    {
        registerContainer("oc4j9x", ContainerType.INSTALLED,
            "org.codehaus.cargo.container.orion.Oc4j9xInstalledLocalContainer");

        registerContainer("oc4j10x", ContainerType.INSTALLED,
            "org.codehaus.cargo.container.orion.Oc4j10xInstalledLocalContainer");
    }

    /**
     * Register Resin
     */
    public void registerResin()
    {
        registerContainer("resin2x", ContainerType.INSTALLED,
            "org.codehaus.cargo.container.resin.Resin2xInstalledLocalContainer");

        registerContainer("resin3x", ContainerType.INSTALLED,
            "org.codehaus.cargo.container.resin.Resin3xInstalledLocalContainer");
    }

    /**
     * Register Tomcat
     */
    public void registerTomcat()
    {
        registerContainer("tomcat4x", ContainerType.INSTALLED,
            "org.codehaus.cargo.container.tomcat.Tomcat4xInstalledLocalContainer");
        registerContainer("tomcat4x", ContainerType.REMOTE,
            "org.codehaus.cargo.container.tomcat.Tomcat4xRemoteContainer");

        registerContainer("tomcat5x", ContainerType.INSTALLED,
            "org.codehaus.cargo.container.tomcat.Tomcat5xInstalledLocalContainer");
        registerContainer("tomcat5x", ContainerType.REMOTE,
            "org.codehaus.cargo.container.tomcat.Tomcat5xRemoteContainer");
        registerContainer("tomcat5x", ContainerType.EMBEDDED,
            "org.codehaus.cargo.container.tomcat.Tomcat5xEmbeddedLocalContainer");

        registerContainer("tomcat6x", ContainerType.INSTALLED,
            "org.codehaus.cargo.container.tomcat.Tomcat6xInstalledLocalContainer");
        registerContainer("tomcat6x", ContainerType.REMOTE,
            "org.codehaus.cargo.container.tomcat.Tomcat6xRemoteContainer");
    }

    /**
     * Register BEA/Oracle Weblogic
     */
    public void registerWeblogic()
    {
        registerContainer("weblogic8x", ContainerType.INSTALLED,
            "org.codehaus.cargo.container.weblogic.WebLogic8xInstalledLocalContainer");

        registerContainer("weblogic9x", ContainerType.INSTALLED,
            "org.codehaus.cargo.container.weblogic.WebLogic9xInstalledLocalContainer");

        registerContainer("weblogic10x", ContainerType.INSTALLED,
            "org.codehaus.cargo.container.weblogic.WebLogic10xInstalledLocalContainer");

        registerContainer("weblogic103x", ContainerType.INSTALLED,
            "org.codehaus.cargo.container.weblogic.WebLogic103xInstalledLocalContainer");
    }

    /**
     * {@inheritDoc}
     * @see ContainerFactory#isContainerRegistered(String, ContainerType)
     */
    public boolean isContainerRegistered(String containerId, ContainerType containerType)
    {
        return hasMapping(
            new RegistrationKey(new SimpleContainerIdentity(containerId), containerType.getType()));
    }

    /**
     * {@inheritDoc}
     * @see ContainerFactory#registerContainer(String, org.codehaus.cargo.container.ContainerType, Class)
     */
    public void registerContainer(String containerId, ContainerType containerType,
        Class containerClass)
    {
        registerImplementation(new RegistrationKey(new SimpleContainerIdentity(containerId),
            containerType.getType()), containerClass);
    }

    /**
     * Registers a container using a class specified as a String.
     *
     * @param containerId {@inheritDoc}
     * @param containerType {@inheritDoc}
     * @param containerClassName the container implementation class to register as a String
     * @see #registerContainer(String, ContainerType, Class)
     */
    public void registerContainer(String containerId, ContainerType containerType,
        String containerClassName)
    {
        registerImplementation(new RegistrationKey(new SimpleContainerIdentity(containerId),
            containerType.getType()), containerClassName);
    }

    /**
     * {@inheritDoc}
     * @see ContainerFactory#getContainerClass
     */
    public Class getContainerClass(String containerId, ContainerType containerType)
    {
        return getMapping(
            new RegistrationKey(new SimpleContainerIdentity(containerId), containerType.getType()));
    }

    /**
     * {@inheritDoc}
     * @see ContainerFactory#createContainer(String, ContainerType, Configuration)
     */
    public Container createContainer(String containerId, ContainerType containerType,
        Configuration configuration)
    {
        ContainerFactoryParameters parameters = new ContainerFactoryParameters();
        parameters.configuration = configuration;

        return (Container) createImplementation(new RegistrationKey(new SimpleContainerIdentity(
            containerId), containerType.getType()), parameters, "container");
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.generic.spi.AbstractGenericHintFactory#getConstructor
     */
    protected Constructor getConstructor(Class containerClass, String containerType,
        GenericParameters parameters) throws NoSuchMethodException
    {
        Constructor constructor;

        ContainerType type = ContainerType.toType(containerType);

        if (type.isLocal())
        {
            constructor = containerClass.getConstructor(new Class[] {LocalConfiguration.class});
        }
        else if (type.isRemote())
        {
            constructor = containerClass.getConstructor(new Class[] {RuntimeConfiguration.class});
        }
        else
        {
            throw new ContainerException("Unknown container type [" + type.getType() + "]");
        }

        return constructor;
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.generic.spi.AbstractGenericHintFactory#createInstance
     */
    protected Object createInstance(Constructor constructor, String containerType,
        GenericParameters parameters) throws Exception
    {
        Configuration configuration = ((ContainerFactoryParameters) parameters).configuration;

        return constructor.newInstance(new Object[] {configuration});
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.generic.ContainerFactory#getContainerIds()
     */
    public Map getContainerIds()
    {
        Map containerIds = new HashMap();

        Iterator keys = getMappings().keySet().iterator();
        while (keys.hasNext())
        {
            RegistrationKey key = (RegistrationKey) keys.next();

            SimpleContainerIdentity identity = (SimpleContainerIdentity) key.getContainerIdentity();
            if (containerIds.containsKey(identity.getId()))
            {
                Set hints = (Set) containerIds.get(identity.getId());
                hints.add(ContainerType.toType(key.getHint()));
            }
            else
            {
                Set hints = new HashSet();
                hints.add(ContainerType.toType(key.getHint()));
                containerIds.put(identity.getId(), hints);
            }
        }

        return containerIds;
    }
}
