/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2011 Vincent Massol, 2011-2015 Ali Tokmen.
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
package org.codehaus.cargo.generic.configuration.builder;

import java.lang.reflect.Constructor;

import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.Configuration;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.configuration.builder.ConfigurationBuilder;
import org.codehaus.cargo.container.configuration.entry.Resource;
import org.codehaus.cargo.generic.AbstractFactoryRegistry;
import org.codehaus.cargo.generic.internal.util.FullContainerIdentity;
import org.codehaus.cargo.generic.internal.util.RegistrationKey;
import org.codehaus.cargo.generic.spi.AbstractIntrospectionGenericHintFactory;

/**
 * Default implementation of {@link ConfigurationBuilderFactory}. Registers all known containers.
 */
public class DefaultConfigurationBuilderFactory extends
    AbstractIntrospectionGenericHintFactory<ConfigurationBuilder> implements
    ConfigurationBuilderFactory
{
    /**
     * {@inheritDoc}
     *
     * @see org.codehaus.cargo.generic.spi.AbstractGenericHintFactory.GenericParameters
     */
    private static class ConfigurationBuilderFactoryParameters implements GenericParameters
    {
        /**
         * Configuration carrying informations about how/where to put configuration changes to.
         */
        public LocalConfiguration configuration;
    }

    /**
     * Register default configuration builders.
     */
    public DefaultConfigurationBuilderFactory()
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
    public DefaultConfigurationBuilderFactory(ClassLoader classLoader)
    {
        super();

        AbstractFactoryRegistry.register(classLoader, this);
    }

    @Override
    public void registerConfigurationBuilder(String containerId, ContainerType containerType,
        String configurationEntryType,
        Class< ? extends ConfigurationBuilder> configurationBuilderClass)
    {
        registerImplementation(new RegistrationKey(new FullContainerIdentity(containerId,
            containerType), configurationEntryType), configurationBuilderClass);

    }

    @Override
    public boolean isConfigurationBuilderRegistered(String containerId,
        ContainerType containerType, String configurationEntryType)
    {
        return hasMapping(new RegistrationKey(new FullContainerIdentity(containerId,
            containerType), configurationEntryType));
    }

    @Override
    public ConfigurationBuilder createConfigurationBuilder(LocalContainer container,
        Resource resource)
    {

        ConfigurationBuilder configurationBuilder;

        String resourceType = resource.getType();

        if (isConfigurationBuilderRegistered(container.getId(), container.getType(), resourceType))
        {
            getLogger().debug("Creating configuration builder for [" + resourceType + "] ",
                this.getClass().getName());

            ConfigurationBuilderFactoryParameters parameters =
                new ConfigurationBuilderFactoryParameters();
            parameters.configuration = container.getConfiguration();

            configurationBuilder =
                createImplementation(
                    new RegistrationKey(new FullContainerIdentity(container.getId(),
                        container.getType()), resourceType), parameters, "configuration builder");
        }
        else
        {
            throw new ContainerException("There's no registered conf. builder matching your "
                + "resource [" + resourceType + "]");
        }

        return configurationBuilder;
    }

    @Override
    protected Constructor< ? extends ConfigurationBuilder> getConstructor(
        Class< ? extends ConfigurationBuilder> configurationBuilderClass, String hint,
        GenericParameters parameters)
        throws NoSuchMethodException
    {

        Constructor< ? extends ConfigurationBuilder> result = null;

        Constructor< ? >[] constructors = configurationBuilderClass.getConstructors();
        for (Constructor< ? > constructor : constructors)
        {
            Class< ? >[] parameterTypes = constructor.getParameterTypes();
            if (parameterTypes != null && parameterTypes.length == 1)
            {
                Class< ? > parameter = parameterTypes[0];
                if (LocalConfiguration.class.isAssignableFrom(parameter))
                {
                    result = (Constructor< ? extends ConfigurationBuilder>) constructor;
                    break;
                }
            }
        }

        if (result == null)
        {
            throw new NoSuchMethodException("No constructor found on class "
                + configurationBuilderClass + " for configuration builder for resource type ["
                + hint + "]");
        }
        return result;
    }

    @Override
    protected ConfigurationBuilder createInstance(
        Constructor< ? extends ConfigurationBuilder> constructor, String hint,
        GenericParameters parameters) throws Exception
    {
        Configuration configuration =
            ((ConfigurationBuilderFactoryParameters) parameters).configuration;
        return constructor.newInstance(new Object[] {configuration});
    }

}
