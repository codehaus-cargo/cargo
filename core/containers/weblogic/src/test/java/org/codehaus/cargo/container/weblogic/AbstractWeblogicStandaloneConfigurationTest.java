/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2026 Ali Tokmen.
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

import java.io.StringReader;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xmlunit.xpath.JAXPXPathEngine;
import org.xmlunit.xpath.XPathEngine;

import org.codehaus.cargo.container.configuration.entry.DataSourceFixture;
import org.codehaus.cargo.container.configuration.entry.ResourceFixture;
import org.codehaus.cargo.container.spi.configuration.builder.AbstractLocalConfigurationWithConfigurationBuilderTest;

/**
 * Integration tests for WebLogic standalone configurations.
 */
public abstract class AbstractWeblogicStandaloneConfigurationTest extends
    AbstractLocalConfigurationWithConfigurationBuilderTest
{

    /**
     * XPath engine.
     */
    protected XPathEngine xpathEngine;

    /**
     * Return given XML as Source. We need this as Source objects are single use.
     * @param xml XML String.
     * @return Source object.
     */
    protected static Source toSource(String xml)
    {
        return new StreamSource(new StringReader(xml));
    }

    /**
     * Creates the XPathEngine.
     * @throws Exception If anything goes wrong.
     */
    @BeforeEach
    protected void setUp() throws Exception
    {
        super.setUp();
        this.xpathEngine = new JAXPXPathEngine();
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
    @Test
    @Override
    public void testConfigureCreatesResource() throws Exception
    {
        Assumptions.abort("WebLogic does not currently support Resources");
    }

    /**
     * WebLogic does not currently support Resources. {@inheritDoc}
     * @throws Exception If anything goes wrong.
     */
    @Test
    @Override
    public void testConfigureCreatesTwoResourcesViaProperties() throws Exception
    {
        Assumptions.abort("WebLogic does not currently support Resources");
    }

}
