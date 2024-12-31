/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2025 Ali Tokmen.
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

import java.lang.reflect.Constructor;

import org.codehaus.cargo.container.ContainerCapability;
import org.codehaus.cargo.generic.internal.util.RegistrationKey;
import org.codehaus.cargo.generic.internal.util.SimpleContainerIdentity;
import org.codehaus.cargo.generic.spi.AbstractIntrospectionGenericHintFactory;

/**
 * Default implementation of {@link org.codehaus.cargo.generic.ContainerCapabilityFactory}.
 * Registers all known container capabilities.
 */
public class DefaultContainerCapabilityFactory extends
    AbstractIntrospectionGenericHintFactory<ContainerCapability> implements
    ContainerCapabilityFactory
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
     * @param classLoader ClassLoader to discover implementations from. See
     *            {@link AbstractFactoryRegistry#register(ClassLoader, ContainerCapabilityFactory)}
     *            for the details of what this value means.
     */
    public DefaultContainerCapabilityFactory(ClassLoader classLoader)
    {
        super();

        AbstractFactoryRegistry.register(classLoader, this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void registerContainerCapability(String containerId,
        Class<? extends ContainerCapability> containerCapabilityClass)
    {
        registerImplementation(new RegistrationKey(new SimpleContainerIdentity(containerId),
            "default"), containerCapabilityClass);
    }

    /**
     * Registers a container capability using a class specified as a String.
     * 
     * @param containerId Container id.
     * @param containerCapabilityClassName Container capability implementation class to register
     *            as a String
     * @see #registerContainerCapability(String, Class)
     */
    public void registerContainerCapability(String containerId,
        String containerCapabilityClassName)
    {
        registerImplementation(new RegistrationKey(new SimpleContainerIdentity(containerId),
            "default"), containerCapabilityClassName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ContainerCapability createContainerCapability(String containerId)
    {
        return createImplementation(new RegistrationKey(new SimpleContainerIdentity(containerId),
            "default"), null, "container capability");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Constructor<? extends ContainerCapability> getConstructor(
        Class<? extends ContainerCapability> containerCapabilityClass, String hint,
        GenericParameters parameters) throws NoSuchMethodException
    {
        return containerCapabilityClass.getConstructor((Class[]) null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ContainerCapability createInstance(
        Constructor<? extends ContainerCapability> constructor, String hint,
        GenericParameters parameters) throws Exception
    {
        return constructor.newInstance((Object[]) null);
    }
}
