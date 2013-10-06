/*
 * ========================================================================
 *
 * Copyright 2007-2008 OW2. Code from this file
 * was originally imported from the OW2 JOnAS project.
 *
 * Codehaus CARGO, copyright 2004-2011 Vincent Massol.
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
package org.codehaus.cargo.container.jonas.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.cargo.container.ContainerException;

import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.configuration.entry.DataSource;
import org.codehaus.cargo.container.jonas.JonasPropertySet;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.container.spi.configuration.AbstractStandaloneLocalConfiguration;
import org.ow2.jonas.tools.configurator.Jonas;
import org.ow2.jonas.tools.configurator.api.JDBCConfiguration;
import org.ow2.jonas.tools.configurator.api.JdbcXMLConfiguration;
import org.ow2.jonas.tools.configurator.api.JonasConfigurator;

/**
 * Implementation of a standalone {@link org.codehaus.cargo.container.configuration.Configuration}
 * for JOnAS.
 * 
 * @version $Id$
 */
public class AbstractJonasStandaloneLocalConfiguration extends AbstractStandaloneLocalConfiguration
{

    /**
     * JOnAS container capability.
     */
    private static final ConfigurationCapability CONTAINER_CAPABILITY =
        new JonasStandaloneLocalConfigurationCapability();

    /**
     * JOnAS installed container.
     */
    protected InstalledLocalContainer installedContainer;

    /**
     * {@inheritDoc}
     * 
     * @see AbstractStandaloneLocalConfiguration#AbstractStandaloneLocalConfiguration(String)
     */
    public AbstractJonasStandaloneLocalConfiguration(String dir)
    {
        super(dir);

        setProperty(GeneralPropertySet.RMI_PORT, "1099");
        setProperty(GeneralPropertySet.HOSTNAME, "localhost");
        setProperty(ServletPropertySet.PORT, "9000");
        setProperty(JonasPropertySet.JONAS_SERVER_NAME, "jonas");
        setProperty(JonasPropertySet.JONAS_DOMAIN_NAME, "jonas");
        setProperty(JonasPropertySet.JONAS_JMS_PORT, "16010");
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.codehaus.cargo.container.configuration.Configuration#getCapability()
     */
    public ConfigurationCapability getCapability()
    {
        return CONTAINER_CAPABILITY;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.codehaus.cargo.container.spi.configuration.AbstractLocalConfiguration#configure(LocalContainer)
     */
    @Override
    protected void doConfigure(LocalContainer container) throws Exception
    {
        this.installedContainer = (InstalledLocalContainer) container;
        setupConfigurationDir();

        String services = getPropertyValue(JonasPropertySet.JONAS_SERVICES_LIST);
        if (services == null || services.trim().length() == 0)
        {
            Properties jonasProperties = new Properties();
            File jonasPropertiesFile = new File(
                installedContainer.getHome(), "conf/jonas.properties");
            if (!jonasPropertiesFile.isFile())
            {
                throw new ContainerException("File [" + jonasPropertiesFile + "] does not exist");
            }
            InputStream jonasPropertiesStream = new FileInputStream(jonasPropertiesFile);
            try
            {
                jonasProperties.load(jonasPropertiesStream);
            }
            finally
            {
                jonasPropertiesStream.close();
                jonasPropertiesStream = null;
                System.gc();
            }
            services = jonasProperties.getProperty("jonas.services");
        }

        // Create the JOnAS configurator (version-independent)
        Jonas jonas = new Jonas(installedContainer.getHome());
        JonasConfigurator configurator = jonas.getJonasConfigurator();

        Logger configuratorNotApplicableLogger = Logger.getLogger(
            "org.ow2.jonas.tools.configurator.impl.NotApplicableHelper");
        Level oldLevel = configuratorNotApplicableLogger.getLevel();
        try
        {
            configuratorNotApplicableLogger.setLevel(Level.SEVERE);

            // Set the configuration
            configurator.setJonasBase(getHome());
            configurator.setJonasName(getPropertyValue(JonasPropertySet.JONAS_SERVER_NAME));
            configurator.setJonasName(getPropertyValue(JonasPropertySet.JONAS_DOMAIN_NAME));
            configurator.setServices(services);
            configurator.setHost(getPropertyValue(GeneralPropertySet.HOSTNAME));
            configurator.setProtocolsJrmpPort(getPropertyValue(GeneralPropertySet.RMI_PORT));
            configurator.setHttpConnectorActivation(true);
            configurator.setHttpPort(getPropertyValue(ServletPropertySet.PORT));
            configurator.setJmsPort(getPropertyValue(JonasPropertySet.JONAS_JMS_PORT));

            // Add datasources
            for (DataSource ds : getDataSources())
            {
                if (jonas.getMajorVersion() == 4 || jonas.getMajorVersion() == 5
                    || jonas.getMajorVersion() == 52)
                {
                    JDBCConfiguration dsConfiguration = new JDBCConfiguration();
                    dsConfiguration.datasourceClass = ds.getDriverClass();
                    dsConfiguration.driverName = ds.getDriverClass();
                    dsConfiguration.url = ds.getUrl();
                    dsConfiguration.mappername = "rdb";
                    dsConfiguration.user = ds.getUsername();
                    dsConfiguration.password = ds.getPassword();
                    dsConfiguration.jndiName = ds.getJndiLocation();
                    configurator.addJdbcRA(ds.getId(), dsConfiguration);
                }
                else
                {
                    JdbcXMLConfiguration xmlDsConfiguration = new JdbcXMLConfiguration();
                    xmlDsConfiguration.classname = ds.getDriverClass();
                    xmlDsConfiguration.url = ds.getUrl();
                    xmlDsConfiguration.mapper = "rdb";
                    xmlDsConfiguration.username = ds.getUsername();
                    xmlDsConfiguration.password = ds.getPassword();
                    xmlDsConfiguration.name = ds.getJndiLocation();
                    configurator.addJdbcResource(xmlDsConfiguration);
                }
            }

            for (Map.Entry<String, String> property : getProperties().entrySet())
            {
                if (property.getKey() != null
                    && property.getKey().startsWith(JonasPropertySet.CONFIGURATOR_PREFIX))
                {
                    String setterName = getConfiguratorSetterName(property.getKey());

                    for (Method method : configurator.getClass().getMethods())
                    {
                        if (method.getName().equals(setterName)
                            && method.getParameterTypes().length == 1)
                        {
                            Class<?> parameterType = method.getParameterTypes()[0];

                            if (parameterType.equals(String.class))
                            {
                                method.invoke(configurator, property.getValue());
                                break;
                            }
                            else if (parameterType.equals(Boolean.class)
                                || parameterType.equals(boolean.class))
                            {
                                boolean parsedProperty = Boolean.parseBoolean(property.getValue());
                                method.invoke(configurator, parsedProperty);
                                break;
                            }
                            else if (parameterType.equals(Integer.class)
                                || parameterType.equals(int.class))
                            {
                                int parsedProperty = Integer.parseInt(property.getValue());
                                method.invoke(configurator, parsedProperty);
                                break;
                            }
                            else if (parameterType.equals(Long.class)
                                || parameterType.equals(long.class))
                            {
                                long parsedProperty = Long.parseLong(property.getValue());
                                method.invoke(configurator, parsedProperty);
                                break;
                            }
                            else
                            {
                                getLogger().warn("Property " + property.getKey()
                                    + " ignored since no appropriate setter could be found on "
                                    + "the JOnAS configurator", this.getClass().getName());
                            }
                        }
                    }
                }
            }
        }
        finally
        {
            configuratorNotApplicableLogger.setLevel(oldLevel);
        }

        // Run
        configurator.execute();

        String libExt = getHome() + "/lib/ext";
        for (String extraClasspath : installedContainer.getExtraClasspath())
        {
            String destinationFile = libExt + "/" + getFileHandler().getName(extraClasspath);
            getFileHandler().copyFile(extraClasspath, destinationFile);
        }

        // Create timestamp file (as some versions of the Configurator empty the directory)
        getFileHandler().createFile(getFileHandler().append(getHome(), ".cargo"));
    }

    /**
     * Returns the configurator setter name based on the property name.
     *
     * @param propertyName Property name.
     * @return Configurator setter name for <code>propertyName</code>.
     */
    public static String getConfiguratorSetterName(String propertyName)
    {
        if (!propertyName.startsWith(JonasPropertySet.CONFIGURATOR_PREFIX))
        {
            throw new IllegalArgumentException("Property name does not start with "
                + JonasPropertySet.CONFIGURATOR_PREFIX);
        }

        String propertyNameWithoutPrefix = propertyName.substring(
            JonasPropertySet.CONFIGURATOR_PREFIX.length());
        return "set" + propertyNameWithoutPrefix.substring(0, 1).toUpperCase(Locale.ENGLISH)
            + propertyNameWithoutPrefix.substring(1);
    }
}
