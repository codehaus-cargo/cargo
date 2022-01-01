/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2022 Ali Tokmen.
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

import java.lang.reflect.Constructor;

import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.configuration.Configuration;
import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.generic.AbstractFactoryRegistry;
import org.codehaus.cargo.generic.internal.util.FullContainerIdentity;
import org.codehaus.cargo.generic.internal.util.RegistrationKey;
import org.codehaus.cargo.generic.spi.AbstractIntrospectionGenericHintFactory;
import org.codehaus.cargo.util.DefaultFileHandler;
import org.codehaus.cargo.util.FileHandler;

/**
 * Default {@link ConfigurationFactory} implementation that has all the known container
 * configurations registered against their containers. It also supports registering new
 * configurations against any container.
 */
public class DefaultConfigurationFactory extends
    AbstractIntrospectionGenericHintFactory<Configuration> implements ConfigurationFactory
{
    /**
     * File utility class.
     */
    private FileHandler fileHandler = new DefaultFileHandler();

    /**
     * @see GenericParameters
     */
    private static class ConfigurationFactoryParameters implements GenericParameters
    {
        /**
         * The home directory for the configuration.
         */
        public String home;
    }

    /**
     * Register default configurations.
     */
    public DefaultConfigurationFactory()
    {
        this(null);
    }

    /**
     * Register configuration name mappings.
     * 
     * @param classLoader ClassLoader to discover implementations from. See
     *            {@link AbstractFactoryRegistry#register(ClassLoader, ConfigurationFactory)} for
     *            the details of what this value means.
     */
    public DefaultConfigurationFactory(ClassLoader classLoader)
    {
        super();

        AbstractFactoryRegistry.register(classLoader, this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isConfigurationRegistered(String containerId, ContainerType containerType,
        ConfigurationType configurationType)
    {
        return hasMapping(new RegistrationKey(new FullContainerIdentity(containerId,
            containerType), configurationType.getType()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void registerConfiguration(String containerId, ContainerType containerType,
        ConfigurationType configurationType, Class<? extends Configuration> configurationClass)
    {
        registerImplementation(new RegistrationKey(new FullContainerIdentity(containerId,
            containerType), configurationType.getType()), configurationClass);
    }

    /**
     * Registers a configuration using a class specified as a String.
     * 
     * @param containerId Container id.
     * @param containerType Container type.
     * @param configurationType Configuration type.
     * @param configurationClassName Configuration implementation class to register as a String
     * @see #registerConfiguration(String, org.codehaus.cargo.container.ContainerType,
     *      org.codehaus.cargo.container.configuration.ConfigurationType, Class)
     */
    public void registerConfiguration(String containerId, ContainerType containerType,
        ConfigurationType configurationType, String configurationClassName)
    {
        registerImplementation(new RegistrationKey(new FullContainerIdentity(containerId,
            containerType), configurationType.getType()), configurationClassName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<? extends Configuration> getConfigurationClass(String containerId,
        ContainerType containerType, ConfigurationType configurationType)
    {
        return getMapping(new RegistrationKey(new FullContainerIdentity(containerId,
            containerType), configurationType.getType()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Configuration createConfiguration(String containerId, ContainerType containerType,
        ConfigurationType configurationType)
    {
        return createConfiguration(containerId, containerType, configurationType, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Configuration createConfiguration(String containerId, ContainerType containerType,
        ConfigurationType configurationType, String home)
    {
        ConfigurationFactoryParameters parameters = new ConfigurationFactoryParameters();
        parameters.home = home;

        return createImplementation(new RegistrationKey(new FullContainerIdentity(containerId,
            containerType), configurationType.getType()), parameters, "configuration");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Constructor<? extends Configuration> getConstructor(
        Class<? extends Configuration> configurationClass, String hint,
        GenericParameters parameters) throws NoSuchMethodException
    {
        Constructor<? extends Configuration> constructor;

        // Runtime configurations have constructors that do not take any parameter.
        if (ConfigurationType.toType(hint) == ConfigurationType.RUNTIME)
        {
            constructor = configurationClass.getConstructor();
        }
        else if (ConfigurationType.toType(hint) == ConfigurationType.EXISTING
            || ConfigurationType.toType(hint) == ConfigurationType.STANDALONE)
        {
            constructor = configurationClass.getConstructor(String.class);
        }
        else
        {
            throw new ContainerException("Unknown configuration type [" + hint + "]");
        }

        return constructor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Configuration createInstance(Constructor<? extends Configuration> constructor,
        String hint, GenericParameters parameters) throws Exception
    {
        Configuration instance;

        String home = ((ConfigurationFactoryParameters) parameters).home;

        // Runtime configurations have constructors that do not take any parameter.
        if (ConfigurationType.toType(hint) == ConfigurationType.RUNTIME)
        {
            if (home != null)
            {
                throw new ContainerException("The configuration home parameter should not be "
                    + "specified for runtime configurations");
            }

            instance = constructor.newInstance();
        }
        else if (ConfigurationType.toType(hint) == ConfigurationType.EXISTING
            || ConfigurationType.toType(hint) == ConfigurationType.STANDALONE)
        {
            if (home == null)
            {
                // The user has not specified a home directory for the configuration, create one
                // in the temporary directory if it's a standalone configuration
                if (ConfigurationType.toType(hint) == ConfigurationType.EXISTING)
                {
                    throw new ContainerException("The configuration home parameter must be "
                        + "specified for existing configurations");
                }
                else
                {
                    home = this.fileHandler.getTmpPath("conf");
                }
            }

            instance = constructor.newInstance(home);
        }
        else
        {
            throw new ContainerException("Unknown configuration type [" + hint + "]");
        }

        return instance;
    }
}
