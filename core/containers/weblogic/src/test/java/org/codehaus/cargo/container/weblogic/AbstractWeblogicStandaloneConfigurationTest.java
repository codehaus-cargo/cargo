/*
 * ========================================================================
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
package org.codehaus.cargo.container.weblogic;

import org.codehaus.cargo.container.configuration.entry.DataSourceFixture;
import org.codehaus.cargo.container.configuration.entry.ResourceFixture;
import org.codehaus.cargo.container.spi.configuration.builder.AbstractLocalConfigurationWithConfigurationBuilderTest;

/**
 * Integration tests for WebLogic standalone configurations.
 * 
 * @version $Id$
 */
public abstract class AbstractWeblogicStandaloneConfigurationTest extends
    AbstractLocalConfigurationWithConfigurationBuilderTest
{

    /**
     * Empty constructor.
     */
    public AbstractWeblogicStandaloneConfigurationTest()
    {
        super();
    }

    /**
     * {@inheritDoc}
     * @param name Container name.
     */
    public AbstractWeblogicStandaloneConfigurationTest(String name)
    {
        super(name);
    }

    /**
     * Setup datasource file and call parent. {@inheritDoc}
     * @param fixture Datasource fixture.
     * @return Configured datasource.
     * @throws Exception If anything goes wrong.
     */
    @Override
    protected String configureDataSourceViaPropertyAndRetrieveConfigurationFile(
        DataSourceFixture fixture) throws Exception
    {
        setUpDataSourceFile();
        return super.configureDataSourceViaPropertyAndRetrieveConfigurationFile(fixture);
    }

    /**
     * Setup datasource file.
     * @throws Exception If anything goes wrong.
     */
    protected abstract void setUpDataSourceFile() throws Exception;

    /**
     * Setup the datasource file and call parent. {@inheritDoc}
     * @param fixture Datasource fixture.
     * @return Configuration file for <code>fixture</code>.
     * @throws Exception If anything goes wrong.
     */
    @Override
    protected String configureDataSourceAndRetrieveConfigurationFile(DataSourceFixture fixture)
        throws Exception
    {
        setUpDataSourceFile();
        return super.configureDataSourceAndRetrieveConfigurationFile(fixture);
    }

    /**
     * WebLogic does not currently support Resources. {@inheritDoc}
     * @param fixture Ignored.
     * @return <code>null</code>
     */
    @Override
    protected String getResourceConfigurationFile(ResourceFixture fixture)
    {
        return null;
    }

    /**
     * WebLogic does not currently support Resources. {@inheritDoc}
     * @throws Exception If anything goes wrong.
     */
    public void testConfigureCreatesResourceForXADataSource() throws Exception
    {
        // Nothing
    }

    /**
     * WebLogic does not currently support Resources. {@inheritDoc}
     * @throws Exception If anything goes wrong.
     */
    @Override
    public void testConfigureCreatesResource() throws Exception
    {
        // Nothing
    }

    /**
     * WebLogic does not currently support Resources. {@inheritDoc}
     * @throws Exception If anything goes wrong.
     */
    @Override
    public void testConfigureCreatesTwoResourcesViaProperties() throws Exception
    {
        // Nothins
    }

}
