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

        AbstractFactoryRegistry.register(classLoader, this);
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
    @Override
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
    @Override
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
