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
package org.codehaus.cargo.container.tomcat.internal;

import org.codehaus.cargo.container.configuration.entry.DataSourceFixture;
import org.codehaus.cargo.container.configuration.entry.ResourceFixture;
import org.codehaus.cargo.container.deployable.EAR;
import org.codehaus.cargo.container.spi.configuration.builder.AbstractLocalConfigurationWithConfigurationBuilderTest;
import org.codehaus.cargo.container.tomcat.Tomcat4xStandaloneLocalConfiguration;
import org.codehaus.cargo.util.CargoException;

/**
 * Unit tests for Tomcat standalone local configurations.
 * 
 * @version $Id$
 */
public abstract class AbstractCatalinaStandaloneLocalConfigurationTest extends
    AbstractLocalConfigurationWithConfigurationBuilderTest
{

    /**
     * Creates the Tomcat manager. {@inheritdoc}
     * @throws Exception If anything goes wrong.
     */
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        setUpManager();
    }

    /**
     * Set up the Tomcat manager.
     */
    protected abstract void setUpManager();

    /**
     * {@inheritdoc}. This implementation simply calls
     * <code>getResourceConfigurationFile(null)</code>.
     * @param fixture Datasource fixture.
     * @return Resource file name for <code>fixture</code>.
     * @see AbstractCatalinaStandaloneLocalConfigurationTest#getResourceConfigurationFile(ResourceFixture)
     */
    @Override
    protected String getDataSourceConfigurationFile(DataSourceFixture fixture)
    {
        return getResourceConfigurationFile(null);
    }

    /**
     * Test {@link AbstractCatalinaStandaloneLocalConfiguration#escapePath(String)}.
     * @param path Path to test.
     * @param expectedEscapedPath Expected escaped path.
     */
    private void testEscapePath(String path, String expectedEscapedPath)
    {
        AbstractCatalinaStandaloneLocalConfiguration configuration =
                (AbstractCatalinaStandaloneLocalConfiguration) this.configuration;

        String escapedPath = configuration.escapePath(path);

        assertEquals(path, expectedEscapedPath, escapedPath);
    }

    /**
     * Test if full UNIX paths get escaped correctly.
     */
    public void testEscapePathWithFullUNIXPath()
    {
        testEscapePath("/usr/bin/java", "/usr/bin/java");
    }

    /**
     * Test if partial UNIX paths get escaped correctly.
     */
    public void testEscapePathWithPartialUNIXPath()
    {
        testEscapePath("Documents/java", "Documents/java");
    }

    /**
     * Test if full Windows paths get escaped correctly.
     */
    public void testEscapePathWithFullWindowsPath()
    {
        testEscapePath("C:\\Windows\\SYSTEM32\\java.exe", "/C:/Windows/SYSTEM32/java.exe");
    }

    /**
     * Test if partial Windows paths get escaped correctly.
     */
    public void testEscapePathWithPartialWindowsPath()
    {
        testEscapePath("Documents\\java", "Documents/java");
    }

    /**
     * Test that EARs cannot be deployed.
     */
    public void testCreateTomcatFilterChainWhenTryingToDeployAnEar()
    {
        Tomcat4xStandaloneLocalConfiguration configuration =
            new Tomcat4xStandaloneLocalConfiguration("somewhere");
        configuration.addDeployable(new EAR("some.ear"));

        try
        {
            configuration.createTomcatFilterChain();
            fail("An exception should have been raised here!");
        }
        catch (CargoException expected)
        {
            assertEquals(
                "Only WAR archives are supported for deployment in Tomcat. Got [some.ear]",
                expected.getMessage());
        }
    }

    /**
     * Setup datasource file and call parent. {@inheritdoc}
     * @param fixture Datasource fixture.
     * @return Configured datasource file.
     * @throws Exception If anything goes wrong.
     */
    @Override
    protected String configureDataSourceViaPropertyAndRetrieveConfigurationFile(
        DataSourceFixture fixture) throws Exception
    {
        setUpResourceFile();
        return super.configureDataSourceViaPropertyAndRetrieveConfigurationFile(fixture);
    }

    /**
     * Setup datasource file and call parent. {@inheritdoc}
     * @param fixture Datasource fixture.
     * @return Configured datasource file.
     * @throws Exception If anything goes wrong.
     */
    @Override
    protected String configureDataSourceAndRetrieveConfigurationFile(DataSourceFixture fixture)
        throws Exception
    {
        setUpResourceFile();
        return super.configureDataSourceAndRetrieveConfigurationFile(fixture);
    }

    /**
     * Setup resource file and call parent. {@inheritdoc}
     * @param fixture Resource fixture.
     * @return Configured resource file.
     * @throws Exception If anything goes wrong.
     */
    @Override
    protected String configureResourceViaPropertyAndRetrieveConfigurationFile(
        ResourceFixture fixture) throws Exception
    {
        setUpResourceFile();
        return super.configureResourceViaPropertyAndRetrieveConfigurationFile(fixture);
    }

    /**
     * Setup resource file and call parent. {@inheritdoc}
     * @param fixture Resource fixture.
     * @return Configured resource file.
     * @throws Exception If anything goes wrong.
     */
    @Override
    protected String configureResourceAndRetrieveConfigurationFile(ResourceFixture fixture)
        throws Exception
    {
        setUpResourceFile();
        return super.configureResourceAndRetrieveConfigurationFile(fixture);
    }

    /**
     * Setup resource file.
     * @throws Exception If anything goes wrong.
     */
    protected abstract void setUpResourceFile() throws Exception;

    /**
     * Checks that creating datasource configuration entries with driver-configured XA transaction
     * support throws an exception with a good message. {@inheritdoc}
     * @throws Exception If anything goes wrong.
     */
    @Override
    public void testConfigureCreatesDataSourceForDriverConfiguredDSWithXaTransactionSupport()
        throws Exception
    {
        try
        {
            super
                .testConfigureCreatesDataSourceForDriverConfiguredDSWithXaTransactionSupport();
            fail("should have received an exception");
        }
        catch (UnsupportedOperationException e)
        {
            assertEquals(
                "Tomcat does not support XA_TRANSACTION for DataSource implementations.", e
                    .getMessage());
        }
    }

    /**
     * Checks that creating datasource configuration entries with driver-configured local
     * transaction support throws an exception with a good message. {@inheritdoc}
     * @throws Exception If anything goes wrong.
     */
    @Override
    public void testConfigureCreatesDataSourceForDriverConfiguredDSWithLocalTransactionSupport()
        throws Exception
    {
        try
        {
            super
                .testConfigureCreatesDataSourceForDriverConfiguredDSWithLocalTransactionSupport();
            fail("should have received an exception");
        }
        catch (UnsupportedOperationException e)
        {
            assertEquals(
                "Tomcat does not support LOCAL_TRANSACTION for DataSource implementations.", e
                    .getMessage());
        }
    }

    /**
     * Checks that creating XA datasource configuration entries throws an exception with a good
     * message. {@inheritdoc}
     * @throws Exception If anything goes wrong.
     */
    @Override
    public void testConfigureCreatesDataSourceForXADataSourceConfiguredDataSource()
        throws Exception
    {
        try
        {
            super.testConfigureCreatesDataSourceForXADataSourceConfiguredDataSource();
            fail("should have received an exception");
        }
        catch (UnsupportedOperationException e)
        {
            assertEquals(
                "Tomcat does not support XADataSource configured DataSource implementations.", e
                    .getMessage());
        }
    }

}
