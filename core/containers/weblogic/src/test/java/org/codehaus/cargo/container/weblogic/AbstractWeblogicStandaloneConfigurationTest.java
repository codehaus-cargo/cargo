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
package org.codehaus.cargo.container.weblogic;

import org.codehaus.cargo.container.configuration.entry.DataSourceFixture;
import org.codehaus.cargo.container.configuration.entry.ResourceFixture;
import org.codehaus.cargo.container.spi.configuration.builder.AbstractLocalConfigurationWithConfigurationBuilderTest;

public abstract class AbstractWeblogicStandaloneConfigurationTest extends
    AbstractLocalConfigurationWithConfigurationBuilderTest
{

    public AbstractWeblogicStandaloneConfigurationTest()
    {
        super();
    }

    public AbstractWeblogicStandaloneConfigurationTest(String name)
    {
        super(name);
    }

    @Override
    protected String configureDataSourceViaPropertyAndRetrieveConfigurationFile(
        DataSourceFixture fixture) throws Exception
    {
        setUpDataSourceFile();
        return super.configureDataSourceViaPropertyAndRetrieveConfigurationFile(fixture);
    }

    abstract protected void setUpDataSourceFile() throws Exception;

    @Override
    protected String configureDataSourceAndRetrieveConfigurationFile(DataSourceFixture fixture)
        throws Exception
    {
        setUpDataSourceFile();
        return super.configureDataSourceAndRetrieveConfigurationFile(fixture);
    }

    @Override
    protected String getResourceConfigurationFile(ResourceFixture fixture)
    {
        // WebLogic does not currently support Resources
        return null;
    }

    public void testConfigureCreatesResourceForXADataSource() throws Exception
    {
        // WebLogic does not currently support Resources
    }

    @Override
    public void testConfigureCreatesResource() throws Exception
    {
        // WebLogic does not currently support Resources

    }

    @Override
    public void testConfigureCreatesTwoResourcesViaProperties() throws Exception
    {
        // WebLogic Resources
    }

}
