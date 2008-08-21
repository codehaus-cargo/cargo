/*
 * ========================================================================
 *
 * Copyright 2006-2008 Vincent Massol.
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

import org.codehaus.cargo.generic.spi.AbstractIntrospectionGenericHintFactory;
import org.codehaus.cargo.generic.internal.util.RegistrationKey;
import org.codehaus.cargo.generic.internal.util.SimpleContainerIdentity;
import org.codehaus.cargo.container.ContainerCapability;

import java.lang.reflect.Constructor;

/**
 * Default implementation of {@link org.codehaus.cargo.generic.ContainerCapabilityFactory}.
 * Registers all known container capabilities.
 *
 * @version $Id$
 */
public class DefaultContainerCapabilityFactory extends AbstractIntrospectionGenericHintFactory
    implements ContainerCapabilityFactory
{
    /**
     * Initialize container capability name mappings with container ids.
     */
    public DefaultContainerCapabilityFactory()
    {
        this(null);
    }

    /**
     * Register container capability name mappings.
     *
     * @param classLoader
     *      ClassLoader to discover implementations from. See
     *      {@link AbstractFactoryRegistry#register(ClassLoader, ContainerCapabilityFactory)}
     *      for the details of what this value means.
     */
    public DefaultContainerCapabilityFactory(ClassLoader classLoader)
    {
        super();

        // Note: We register container capabilities using introspection so that we don't have to
        // depend on those classes at build time nor at runtime. More specifically this allows a
        // user to use the generic API and choose what container implementation jar he wants to use
        // without having to add all container implementations jars in the classpath.

        // Note: Sorted by container id alphabetical order

        registerContainerCapability("geronimo1x",
            "org.codehaus.cargo.container.internal.J2EEContainerCapability");

        registerContainerCapability("jboss3x",
            "org.codehaus.cargo.container.jboss.internal.JBossContainerCapability");
        registerContainerCapability("jboss4x",
            "org.codehaus.cargo.container.jboss.internal.JBossContainerCapability");
        registerContainerCapability("jboss42x",
            "org.codehaus.cargo.container.jboss.internal.JBossContainerCapability");
        registerContainerCapability("jboss5x",
            "org.codehaus.cargo.container.jboss.internal.JBossContainerCapability");

        registerContainerCapability("jetty4x",
            "org.codehaus.cargo.container.internal.ServletContainerCapability");
        registerContainerCapability("jetty5x",
            "org.codehaus.cargo.container.internal.ServletContainerCapability");
        registerContainerCapability("jetty6x",
            "org.codehaus.cargo.container.internal.ServletContainerCapability");

        registerContainerCapability("jo1x",
            "org.codehaus.cargo.container.internal.ServletContainerCapability");

        registerContainerCapability("jonas4x",
            "org.codehaus.cargo.container.jonas.internal.JonasContainerCapability");

        registerContainerCapability("oc4j9x",
            "org.codehaus.cargo.container.internal.J2EEContainerCapability");
        registerContainerCapability("oc4j10x",
            "org.codehaus.cargo.container.internal.J2EEContainerCapability");

        registerContainerCapability("orion1x",
            "org.codehaus.cargo.container.internal.J2EEContainerCapability");
        registerContainerCapability("orion2x",
            "org.codehaus.cargo.container.internal.J2EEContainerCapability");

        registerContainerCapability("resin2x",
            "org.codehaus.cargo.container.internal.ServletContainerCapability");
        registerContainerCapability("resin3x",
            "org.codehaus.cargo.container.internal.ServletContainerCapability");

        registerContainerCapability("tomcat3x",
            "org.codehaus.cargo.container.internal.ServletContainerCapability");
        registerContainerCapability("tomcat4x",
            "org.codehaus.cargo.container.internal.ServletContainerCapability");
        registerContainerCapability("tomcat5x",
            "org.codehaus.cargo.container.internal.ServletContainerCapability");
        registerContainerCapability("tomcat6x",
            "org.codehaus.cargo.container.internal.ServletContainerCapability");


        registerContainerCapability("weblogic8x",
            "org.codehaus.cargo.container.internal.J2EEContainerCapability");
        registerContainerCapability("weblogic9x",
            "org.codehaus.cargo.container.internal.J2EEContainerCapability");

        AbstractFactoryRegistry.register(classLoader, this);
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.generic.ContainerCapabilityFactory#registerContainerCapability(String, Class)
     */
    public void registerContainerCapability(String containerId, Class containerCapabilityClass)
    {
        registerImplementation(new RegistrationKey(new SimpleContainerIdentity(containerId),
            "default"), containerCapabilityClass);
    }

    /**
     * Registers a container capability using a class specified as a String.
     *
     * @param containerId {@inheritDoc}
     * @param containerCapabilityClassName the container capability implementation class to
     *        register as a String
     * @see #registerContainerCapability(String, Class)
     */
    public void registerContainerCapability(String containerId, String containerCapabilityClassName)
    {
        registerImplementation(new RegistrationKey(new SimpleContainerIdentity(containerId),
            "default"), containerCapabilityClassName);
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.generic.ContainerCapabilityFactory#createContainerCapability(String)
     */
    public ContainerCapability createContainerCapability(String containerId)
    {
        return (ContainerCapability) createImplementation(new RegistrationKey(
            new SimpleContainerIdentity(containerId), "default"), null, "container capability");
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.generic.spi.AbstractGenericHintFactory#getConstructor
     */
    protected Constructor getConstructor(Class containerCapabilityClass, String hint,
        GenericParameters parameters) throws NoSuchMethodException
    {
        return containerCapabilityClass.getConstructor(null);
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.generic.spi.AbstractGenericHintFactory#createInstance
     */
    protected Object createInstance(Constructor constructor, String hint,
        GenericParameters parameters) throws Exception
    {
        return constructor.newInstance(null);
    }
}
