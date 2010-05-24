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
package org.codehaus.cargo.generic.configuration;

import org.codehaus.cargo.generic.spi.AbstractIntrospectionGenericHintFactory;
import org.codehaus.cargo.generic.internal.util.RegistrationKey;
import org.codehaus.cargo.generic.internal.util.FullContainerIdentity;
import org.codehaus.cargo.generic.AbstractFactoryRegistry;
import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.ContainerType;

import java.lang.reflect.Constructor;

/**
 * Default implementation of {@link ConfigurationCapabilityFactory}.
 * Registers all known configuration capabilities.
 *
 * @version $Id$
 */
public class DefaultConfigurationCapabilityFactory extends AbstractIntrospectionGenericHintFactory
    implements ConfigurationCapabilityFactory
{
    /**
     * Initialize configuration capability name mappings with container ids and configuration types.
     */
    public DefaultConfigurationCapabilityFactory()
    {
        this(null);
    }

    /**
     * Register configuration capability name mappings.
     *
     * @param classLoader
     *      ClassLoader to discover implementations from. See
     *      {@link AbstractFactoryRegistry#register(ClassLoader, ConfigurationCapabilityFactory)}
     *      for the details of what this value means.
     */
    public DefaultConfigurationCapabilityFactory(ClassLoader classLoader)
    {
        super();

        AbstractFactoryRegistry.register(classLoader, this);
    }

    /**
     * Register BEA/Oracle Weblogic
     */
    public void registerWeblogic()
    {
        registerConfigurationCapability("weblogic8x", ContainerType.INSTALLED,
            ConfigurationType.STANDALONE, "org.codehaus.cargo.container.weblogic.internal."
                + "WebLogicStandaloneLocalConfigurationCapability");
        registerConfigurationCapability("weblogic8x", ContainerType.INSTALLED,
            ConfigurationType.EXISTING, "org.codehaus.cargo.container.weblogic.internal."
                + "WebLogicExistingLocalConfigurationCapability");

        registerConfigurationCapability("weblogic9x", ContainerType.INSTALLED,
            ConfigurationType.STANDALONE, "org.codehaus.cargo.container.weblogic.internal."
                + "WebLogic9x10xAnd103xStandaloneLocalConfigurationCapability");
        registerConfigurationCapability("weblogic9x", ContainerType.INSTALLED,
            ConfigurationType.EXISTING, "org.codehaus.cargo.container.weblogic.internal."
                + "WebLogicExistingLocalConfigurationCapability");
        
        registerConfigurationCapability("weblogic10x", ContainerType.INSTALLED,
            ConfigurationType.STANDALONE, "org.codehaus.cargo.container.weblogic.internal."
                + "WebLogic9x10xAnd103xStandaloneLocalConfigurationCapability");
        registerConfigurationCapability("weblogic10x", ContainerType.INSTALLED,
            ConfigurationType.EXISTING, "org.codehaus.cargo.container.weblogic.internal."
                + "WebLogicExistingLocalConfigurationCapability");

        registerConfigurationCapability("weblogic103x", ContainerType.INSTALLED,
            ConfigurationType.STANDALONE, "org.codehaus.cargo.container.weblogic.internal."
                + "WebLogic9x10xAnd103xStandaloneLocalConfigurationCapability");
        registerConfigurationCapability("weblogic103x", ContainerType.INSTALLED,
            ConfigurationType.EXISTING, "org.codehaus.cargo.container.weblogic.internal."
                + "WebLogicExistingLocalConfigurationCapability");
        
    }

    /**
     * {@inheritDoc}
     * @see ConfigurationCapabilityFactory#registerConfigurationCapability
     */
    public void registerConfigurationCapability(String containerId, ContainerType containerType,
        ConfigurationType configurationType, Class configurationCapabilityClass)
    {
        registerImplementation(new RegistrationKey(
            new FullContainerIdentity(containerId, containerType), configurationType.getType()),
            configurationCapabilityClass);
    }

    /**
     * Registers a configuration capability using a class specified as a String.
     *
     * @param containerId {@inheritDoc}
     * @param containerType {@inheritDoc}
     * @param configurationType {@inheritDoc}
     * @param configurationCapabilityClass the configuration capability implementation class to
     *        register as a String
     * @see #registerConfigurationCapability(String, org.codehaus.cargo.container.ContainerType, org.codehaus.cargo.container.configuration.ConfigurationType, String)
     */
    public void registerConfigurationCapability(String containerId, ContainerType containerType,
        ConfigurationType configurationType, String configurationCapabilityClass)
    {
        registerImplementation(new RegistrationKey(
            new FullContainerIdentity(containerId, containerType), configurationType.getType()),
            configurationCapabilityClass);
    }

    /**
     * {@inheritDoc}
     * @see ConfigurationCapabilityFactory#createConfigurationCapability
     */
    public ConfigurationCapability createConfigurationCapability(String containerId,
        ContainerType containerType, ConfigurationType configurationType)
    {
        return (ConfigurationCapability) createImplementation(new RegistrationKey(
            new FullContainerIdentity(containerId, containerType), configurationType.getType()),
            null, "configuration capability");
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.generic.spi.AbstractGenericHintFactory#getConstructor
     */
    protected Constructor getConstructor(Class configurationCapabilityClass, String hint,
        GenericParameters parameters) throws NoSuchMethodException
    {
        return configurationCapabilityClass.getConstructor(null);
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
