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
package org.codehaus.cargo.container.spi.configuration;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.configuration.entry.ConfigurationFixtureFactory;
import org.codehaus.cargo.container.property.DatasourcePropertySet;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.ResourcePropertySet;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.util.CargoException;

/**
 * Unit tests for {@link AbstractLocalConfiguration}.
 */
public class LocalConfigurationTest
{

    /**
     * CARGO-1438: Only update numbers for properties prefixed with "cargo."
     */
    private static final String NON_CARGO_PORT = "spring.mail.port";

    /**
     * CARGO-1438: Only update numbers for properties prefixed with "cargo."
     */
    private static final String CUSTOM_CARGO_PORT = "cargo.custom.port";

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
        @Override
        public ConfigurationCapability getCapability()
        {
            return new ConfigurationCapability()
            {
                /**
                 * {@inheritDoc}
                 * @return <code>null</code>.
                 */
                @Override
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
                @Override
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
        @Override
        public ConfigurationType getType()
        {
            return null;
        }
    }

    /**
     * Test no resource support.
     */
    @Test
    public void testNoResourceSupport()
    {
        AbstractLocalConfiguration configuration =
            new LocalConfigurationThatSupportsProperty(Arrays.asList(new String[] {}));

        configuration.getResources().add(
            ConfigurationFixtureFactory.createXADataSourceAsResource().buildResource());
        try
        {
            configuration.collectUnsupportedResourcesAndThrowException();
            Assertions.fail("should have gotten an Exception");
        }
        catch (CargoException e)
        {
            Assertions.assertEquals("This configuration does not support Resource configuration! "
                + "JndiName: resource/XADataSource", e.getMessage());
        }
    }

    /**
     * Test resource support.
     */
    @Test
    public void testResourceSupport()
    {
        AbstractLocalConfiguration configuration =
            new LocalConfigurationThatSupportsProperty(Arrays
                .asList(new String[] {ResourcePropertySet.RESOURCE}));

        configuration.getResources().add(
            ConfigurationFixtureFactory.createXADataSourceAsResource().buildResource());
        configuration.collectUnsupportedResourcesAndThrowException();
        Assertions.assertEquals(1, configuration.getResources().size());
    }

    /**
     * Test no datasource support.
     */
    @Test
    public void testNoDataSourceSupport()
    {
        AbstractLocalConfiguration configuration =
            new LocalConfigurationThatSupportsProperty(Arrays.asList(new String[] {}));

        configuration.getDataSources().add(
            ConfigurationFixtureFactory.createDataSource().buildDataSource());
        try
        {
            configuration.collectUnsupportedDataSourcesAndThrowException();
            Assertions.fail("should have gotten an Exception");
        }
        catch (CargoException e)
        {
            Assertions.assertEquals("This configuration does not support DataSource configuration! "
                + "JndiName: jdbc/CargoDS", e.getMessage());
        }
    }

    /**
     * Test datasource support.
     */
    @Test
    public void testDataSourceSupport()
    {
        AbstractLocalConfiguration configuration =
            new LocalConfigurationThatSupportsProperty(Arrays
                .asList(new String[] {DatasourcePropertySet.DATASOURCE}));

        configuration.getDataSources().add(
            ConfigurationFixtureFactory.createDataSource().buildDataSource());
        configuration.collectUnsupportedDataSourcesAndThrowException();
        Assertions.assertEquals(1, configuration.getDataSources().size());
    }

    /**
     * Test no datasource with transaction emulation support.
     */
    @Test
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
            Assertions.fail("should have gotten an Exception");
        }
        catch (CargoException e)
        {
            Assertions.assertEquals(
                "This configuration does not support Transactions on Driver configured "
                    + "DataSources! JndiName: jdbc/CargoDS", e.getMessage());
        }
    }

    /**
     * Test datasource with transaction emulation support.
     */
    @Test
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
        Assertions.assertEquals(2, configuration.getDataSources().size());
    }

    /**
     * Test no datasource with XA support.
     */
    @Test
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
            Assertions.fail("should have gotten an Exception");
        }
        catch (CargoException e)
        {
            Assertions.assertEquals("This configuration does not support XADataSource configured "
                + "DataSources! JndiName: jdbc/CargoDS", e.getMessage());
        }
    }

    /**
     * Test datasource with XA support.
     */
    @Test
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
        Assertions.assertEquals(2, configuration.getDataSources().size());
    }

    /**
     * Test the apply port offset.
     */
    @Test
    public void testApplyPortOffset()
    {
        AbstractLocalConfiguration configuration =
            new LocalConfigurationThatSupportsProperty(Arrays.asList(new String[] {
                GeneralPropertySet.PORT_OFFSET,
                GeneralPropertySet.RMI_PORT,
                ServletPropertySet.PORT}));

        configuration.setProperty(GeneralPropertySet.PORT_OFFSET, "100");
        configuration.setProperty(GeneralPropertySet.RMI_PORT, "1099");
        configuration.setProperty(ServletPropertySet.PORT, "8080");
        configuration.setProperty(LocalConfigurationTest.NON_CARGO_PORT, "15025");
        configuration.setProperty(LocalConfigurationTest.CUSTOM_CARGO_PORT, "17025");

        // apply port offset one first time
        configuration.applyPortOffset();

        Assertions.assertTrue(configuration.isOffsetApplied());
        Assertions.assertEquals(
            "1199", configuration.getPropertyValue(GeneralPropertySet.RMI_PORT));
        Assertions.assertEquals("8180", configuration.getPropertyValue(ServletPropertySet.PORT));
        Assertions.assertEquals("15025",
            configuration.getPropertyValue(LocalConfigurationTest.NON_CARGO_PORT));
        Assertions.assertEquals("17125",
            configuration.getPropertyValue(LocalConfigurationTest.CUSTOM_CARGO_PORT));

        // re-apply port offset, which should have no impact
        configuration.applyPortOffset();

        Assertions.assertTrue(configuration.isOffsetApplied());
        Assertions.assertEquals(
            "1199", configuration.getPropertyValue(GeneralPropertySet.RMI_PORT));
        Assertions.assertEquals("8180", configuration.getPropertyValue(ServletPropertySet.PORT));
        Assertions.assertEquals("15025",
            configuration.getPropertyValue(LocalConfigurationTest.NON_CARGO_PORT));
        Assertions.assertEquals("17125",
            configuration.getPropertyValue(LocalConfigurationTest.CUSTOM_CARGO_PORT));
    }

    /**
     * Test the revert port offset.
     */
    @Test
    public void testRevertPortOffset()
    {
        AbstractLocalConfiguration configuration =
                new LocalConfigurationThatSupportsProperty(Arrays.asList(new String[] {
                    GeneralPropertySet.PORT_OFFSET,
                    GeneralPropertySet.RMI_PORT,
                    ServletPropertySet.PORT}));

        configuration.setProperty(GeneralPropertySet.PORT_OFFSET, "100");
        configuration.setProperty(GeneralPropertySet.RMI_PORT, "1199");
        configuration.setProperty(ServletPropertySet.PORT, "8180");

        configuration.flagOffsetApplied(GeneralPropertySet.RMI_PORT, true);
        configuration.flagOffsetApplied(ServletPropertySet.PORT, true);

        // revert port offset one first time
        configuration.revertPortOffset();

        Assertions.assertFalse(configuration.isOffsetApplied());
        Assertions.assertEquals(
            "1099", configuration.getPropertyValue(GeneralPropertySet.RMI_PORT));
        Assertions.assertEquals("8080", configuration.getPropertyValue(ServletPropertySet.PORT));

        // re-revert port offset, which should have no impact
        configuration.revertPortOffset();

        Assertions.assertFalse(configuration.isOffsetApplied());
        Assertions.assertEquals(
            "1099", configuration.getPropertyValue(GeneralPropertySet.RMI_PORT));
        Assertions.assertEquals("8080", configuration.getPropertyValue(ServletPropertySet.PORT));
    }

    /**
     * Test port offset with system properties.
     */
    @Test
    public void testPortOffsetWithSystemProperties()
    {
        AbstractLocalConfiguration configuration =
                new LocalConfigurationThatSupportsProperty(Arrays.asList(new String[] {
                    GeneralPropertySet.PORT_OFFSET,
                    ServletPropertySet.PORT}));

        configuration.setProperty(ServletPropertySet.PORT, "8080");
        Assertions.assertEquals("8080", configuration.getPropertyValue(ServletPropertySet.PORT));

        try
        {
            System.setProperty(ServletPropertySet.PORT, "8091");
            Assertions.assertEquals(
                "8091", configuration.getPropertyValue(ServletPropertySet.PORT));

            configuration.setProperty(LocalConfigurationTest.NON_CARGO_PORT, "15025");
            Assertions.assertEquals("15025",
                configuration.getPropertyValue(LocalConfigurationTest.NON_CARGO_PORT));

            configuration.setProperty(LocalConfigurationTest.CUSTOM_CARGO_PORT, "17025");
            Assertions.assertEquals("17025",
                configuration.getPropertyValue(LocalConfigurationTest.CUSTOM_CARGO_PORT));

            configuration.setProperty(GeneralPropertySet.PORT_OFFSET, "20");
            Assertions.assertEquals(
                "8091", configuration.getPropertyValue(ServletPropertySet.PORT));
            Assertions.assertEquals("15025",
                configuration.getPropertyValue(LocalConfigurationTest.NON_CARGO_PORT));
            Assertions.assertEquals("17025",
                configuration.getPropertyValue(LocalConfigurationTest.CUSTOM_CARGO_PORT));

            configuration.applyPortOffset();
            Assertions.assertEquals(
                "8111", configuration.getPropertyValue(ServletPropertySet.PORT));
            Assertions.assertEquals("15025",
                configuration.getPropertyValue(LocalConfigurationTest.NON_CARGO_PORT));
            Assertions.assertEquals("17045",
                configuration.getPropertyValue(LocalConfigurationTest.CUSTOM_CARGO_PORT));

            configuration.revertPortOffset();
            Assertions.assertEquals(
                "8091", configuration.getPropertyValue(ServletPropertySet.PORT));
            Assertions.assertEquals("15025",
                configuration.getPropertyValue(LocalConfigurationTest.NON_CARGO_PORT));
            Assertions.assertEquals("17025",
                configuration.getPropertyValue(LocalConfigurationTest.CUSTOM_CARGO_PORT));
        }
        finally
        {
            System.clearProperty(ServletPropertySet.PORT);
        }
    }
}
