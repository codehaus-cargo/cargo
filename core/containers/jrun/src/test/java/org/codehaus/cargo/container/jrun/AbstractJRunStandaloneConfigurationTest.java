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
package org.codehaus.cargo.container.jrun;

import org.codehaus.cargo.container.configuration.entry.DataSourceFixture;
import org.codehaus.cargo.container.configuration.entry.ResourceFixture;
import org.codehaus.cargo.container.spi.configuration.builder.AbstractLocalConfigurationWithConfigurationBuilderTest;

/**
 * {@link AbstractLocalConfigurationWithConfigurationBuilderTest} for JRun.
 * 
 * @version $Id$
 */
public abstract class AbstractJRunStandaloneConfigurationTest extends
    AbstractLocalConfigurationWithConfigurationBuilderTest
{

    /**
     * Empty constructor.
     */
    public AbstractJRunStandaloneConfigurationTest()
    {
        super();
    }

    /**
     * Constructor with container name.
     * @param name Container name.
     */
    public AbstractJRunStandaloneConfigurationTest(String name)
    {
        super(name);
    }

    /**
     * Setup the datasource file and call parent. {@inheritDoc}
     * @param fixture Datasource fixture.
     * @return Configuration file for <code>fixture</code>.
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
     * Setup the datasource file.
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
     * {@inheritDoc}
     * @param fixture Ignored.
     * @return <code>null</code>.
     */
    @Override
    protected String getResourceConfigurationFile(ResourceFixture fixture)
    {
        return null;
    }

    /**
     * JRun does not currently support Resources. {@inheritDoc}
     * @throws Exception If anything goes wrong.
     */
    public void testConfigureCreatesResourceForXADataSource() throws Exception
    {
        // Nothing
    }

    /**
     * JRun does not currently support Resources. {@inheritDoc}
     * @throws Exception If anything goes wrong.
     */
    @Override
    public void testConfigureCreatesResource() throws Exception
    {
        // Nothing
    }

    /**
     * JRun does not currently support Resources. {@inheritDoc}
     * @throws Exception If anything goes wrong.
     */
    @Override
    public void testConfigureCreatesTwoResourcesViaProperties() throws Exception
    {
        // Nothing
    }

}
