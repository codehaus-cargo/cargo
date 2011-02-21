/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2010 Vincent Massol.
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
package org.codehaus.cargo.container.spi.configuration;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.configuration.entry.ConfigurationFixtureFactory;
import org.codehaus.cargo.container.property.DatasourcePropertySet;
import org.codehaus.cargo.container.property.ResourcePropertySet;
import org.codehaus.cargo.util.CargoException;

/**
 * Unit tests for {@link AbstractLocalConfiguration}.
 * 
 * @version $Id$
 */
public class LocalConfigurationTest extends TestCase
{

    /**
     * Local configuration that supports some properties.
     */
    private class LocalConfigurationThatSupportsProperty extends AbstractLocalConfiguration
    {
        /**
         * Supported properties.
         */
        private List<String> supportedProperties;

        /**
         * Constructor which sets the list of supported properties.
         * @param supportedProperties The list of supported properties.
         */
        public LocalConfigurationThatSupportsProperty(List<String> supportedProperties)
        {
            super(null);
            this.supportedProperties = supportedProperties;
        }

        /**
         * Empty method. {@inheritDoc}
         * @param container Ignored.
         */
        @Override
        protected void doConfigure(LocalContainer container) throws Exception
        {
        }

        /**
         * @return {@link ConfigurationCapability} which reuses the
         * <code>supportedProperties</code> given in the constructor.
         */
        public ConfigurationCapability getCapability()
        {
            return new ConfigurationCapability()
            {
                /**
                 * {@inheritDoc}
                 * @return <code>null</code>.
                 */
                public Map<String, Boolean> getProperties()
                {
                    return null;
                }

                /**
                 * {@inheritDoc}
                 * @param propertyName Property name to check for.
                 * @return <code>true</code> if the <code>supportedProperties</code> given in the
                 * constructor contains <code>propertyName</code>, <code>false</code> otherwise.
                 */
                public boolean supportsProperty(String propertyName)
                {
                    if (supportedProperties.contains(propertyName))
                    {
                        return true;
                    }
                    return false;
                }
            };
        }

        /**
         * {@inheritDoc}
         * @return <code>null</code>.
         */
        public ConfigurationType getType()
        {
            return null;
        }
    }

    /**
     * Test no resource support.
     */
    public void testNoResourceSupport()
    {
        AbstractLocalConfiguration configuration =
            new LocalConfigurationThatSupportsProperty(Arrays.asList(new String[] {}));

        configuration.getResources().add(
            ConfigurationFixtureFactory.createXADataSourceAsResource().buildResource());
        try
        {
            configuration.collectUnsupportedResourcesAndThrowException();
            fail("should have gotten an Exception");

        }
        catch (CargoException e)
        {
            assertEquals("This configuration does not support Resource configuration! "
                + "JndiName: resource/XADataSource", e.getMessage());
        }
    }

    /**
     * Test resource support.
     */
    public void testResourceSupport()
    {
        AbstractLocalConfiguration configuration =
            new LocalConfigurationThatSupportsProperty(Arrays
                .asList(new String[] {ResourcePropertySet.RESOURCE}));

        configuration.getResources().add(
            ConfigurationFixtureFactory.createXADataSourceAsResource().buildResource());
        configuration.collectUnsupportedResourcesAndThrowException();
        assertEquals(1, configuration.getResources().size());
    }

    /**
     * Test no datasource support.
     */
    public void testNoDataSourceSupport()
    {
        AbstractLocalConfiguration configuration =
            new LocalConfigurationThatSupportsProperty(Arrays.asList(new String[] {}));

        configuration.getDataSources().add(
            ConfigurationFixtureFactory.createDataSource().buildDataSource());
        try
        {
            configuration.collectUnsupportedDataSourcesAndThrowException();
            fail("should have gotten an Exception");

        }
        catch (CargoException e)
        {
            assertEquals("This configuration does not support DataSource configuration! "
                + "JndiName: jdbc/CargoDS", e.getMessage());
        }
    }

    /**
     * Test datasource support.
     */
    public void testDataSourceSupport()
    {
        AbstractLocalConfiguration configuration =
            new LocalConfigurationThatSupportsProperty(Arrays
                .asList(new String[] {DatasourcePropertySet.DATASOURCE}));

        configuration.getDataSources().add(
            ConfigurationFixtureFactory.createDataSource().buildDataSource());
        configuration.collectUnsupportedDataSourcesAndThrowException();
        assertEquals(1, configuration.getDataSources().size());
    }

    /**
     * Test no datasource with transaction emulation support.
     */
    public void testNoDataSourceTransactionEmulationSupport()
    {
        AbstractLocalConfiguration configuration =
            new LocalConfigurationThatSupportsProperty(Arrays
                .asList(new String[] {DatasourcePropertySet.DATASOURCE}));

        configuration.getDataSources().add(
            ConfigurationFixtureFactory.createDataSource().buildDataSource());
        configuration.getDataSources().add(
            ConfigurationFixtureFactory
                .createDriverConfiguredDataSourceWithXaTransactionSupport().buildDataSource());

        try
        {
            configuration.collectUnsupportedDataSourcesAndThrowException();
            fail("should have gotten an Exception");

        }
        catch (CargoException e)
        {
            assertEquals("This configuration does not support Transactions on Driver configured "
                + "DataSources! JndiName: jdbc/CargoDS", e.getMessage());
        }
    }

    /**
     * Test datasource with transaction emulation support.
     */
    public void testDataSourceTransactionEmulationSupport()
    {
        AbstractLocalConfiguration configuration =
            new LocalConfigurationThatSupportsProperty(Arrays.asList(new String[] {
                DatasourcePropertySet.DATASOURCE, DatasourcePropertySet.TRANSACTION_SUPPORT}));

        configuration.getDataSources().add(
            ConfigurationFixtureFactory.createDataSource().buildDataSource());
        configuration.getDataSources().add(
            ConfigurationFixtureFactory
                .createDriverConfiguredDataSourceWithXaTransactionSupport().buildDataSource());
        configuration.collectUnsupportedDataSourcesAndThrowException();
        assertEquals(2, configuration.getDataSources().size());
    }

    /**
     * Test no datasource with XA support.
     */
    public void testNoDataSourceXASupport()
    {
        AbstractLocalConfiguration configuration =
            new LocalConfigurationThatSupportsProperty(Arrays
                .asList(new String[] {DatasourcePropertySet.DATASOURCE}));

        configuration.getDataSources().add(
            ConfigurationFixtureFactory.createDataSource().buildDataSource());
        configuration.getDataSources().add(
            ConfigurationFixtureFactory.createXADataSourceConfiguredDataSource()
                .buildDataSource());
        try
        {
            configuration.collectUnsupportedDataSourcesAndThrowException();
            fail("should have gotten an Exception");

        }
        catch (CargoException e)
        {
            assertEquals("This configuration does not support XADataSource configured "
                + "DataSources! JndiName: jdbc/CargoDS", e.getMessage());
        }
    }

    /**
     * Test datasource with XA support.
     */
    public void testDataSourceXASupport()
    {
        AbstractLocalConfiguration configuration =
            new LocalConfigurationThatSupportsProperty(Arrays.asList(new String[] {
                DatasourcePropertySet.DATASOURCE, DatasourcePropertySet.CONNECTION_TYPE}));

        configuration.getDataSources().add(
            ConfigurationFixtureFactory.createDataSource().buildDataSource());
        configuration.getDataSources().add(
            ConfigurationFixtureFactory.createXADataSourceConfiguredDataSource()
                .buildDataSource());
        configuration.collectUnsupportedDataSourcesAndThrowException();
        assertEquals(2, configuration.getDataSources().size());
    }
}
