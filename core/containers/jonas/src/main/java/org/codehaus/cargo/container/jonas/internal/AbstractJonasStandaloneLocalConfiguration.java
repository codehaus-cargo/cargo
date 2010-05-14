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
package org.codehaus.cargo.container.jonas.internal;

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
import org.ow2.jonas.tools.configurator.api.JonasConfigurator;

/**
 * Implementation of a standalone
 * {@link org.codehaus.cargo.container.configuration.Configuration} for JOnAS.
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
        setProperty(GeneralPropertySet.PROTOCOL, "http");
        setProperty(GeneralPropertySet.HOSTNAME, "localhost");
        setProperty(ServletPropertySet.PORT, "9000");
        setProperty(JonasPropertySet.JONAS_SERVER_NAME, "jonas");
        setProperty(JonasPropertySet.JONAS_DOMAIN_NAME, "jonas");
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
     * @see AbstractLocalConfiguration#configure(LocalContainer)
     */
    protected void doConfigure(LocalContainer container) throws Exception
    {
        if (!"http".equals(getPropertyValue(GeneralPropertySet.PROTOCOL)))
        {
            throw new IllegalArgumentException(
                "Currently, the CARGO JOnAS container only supports HTTP");
        }

        // TODO: make this list configurable
        //
        // See: http://jorm.ow2.org/doc/mappers.html
        final String rdbMappers = "rdb.hsql,rdb.mysql,rdb.oracle,rdb.postgres";

        this.installedContainer = (InstalledLocalContainer) container;
        setupConfigurationDir();

        // Create the JOnAS configurator (version-independent)
        Jonas jonas = new Jonas(installedContainer.getHome());
        JonasConfigurator configurator = jonas.getJonasConfigurator();

        // Set the configuration
        configurator.setJonasBase(getHome());
        configurator.setJonasName(getPropertyValue(JonasPropertySet.JONAS_SERVER_NAME));
        configurator.setJonasName(getPropertyValue(JonasPropertySet.JONAS_DOMAIN_NAME));
        configurator.setServices(getPropertyValue(JonasPropertySet.JONAS_SERVICES_LIST));
        configurator.setHost(getPropertyValue(GeneralPropertySet.HOSTNAME));
        configurator.setProtocolsJrmpPort(getPropertyValue(GeneralPropertySet.RMI_PORT));
        configurator.setHttpPort(getPropertyValue(ServletPropertySet.PORT));

        for (int i = 0; i < getDataSources().size(); i++)
        {
            DataSource datasource = (DataSource) getDataSources().get(i);

            JDBCConfiguration configuration = new JDBCConfiguration();

            configuration.mappername = rdbMappers;
            configuration.driverName = datasource.getDriverClass();
            // datasource.getConnectionType();
            configuration.jndiName = datasource.getJndiLocation();
            configuration.password = datasource.getPassword();
            // datasource.getTransactionSupport();
            configuration.url = datasource.getUrl();
            configuration.user = datasource.getUsername();

            configurator.addJdbcRA(datasource.getId(), configuration);
        }

        // Run
        configurator.execute();
    }

}
