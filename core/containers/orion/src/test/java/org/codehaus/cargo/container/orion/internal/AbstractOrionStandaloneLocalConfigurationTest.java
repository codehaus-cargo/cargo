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
package org.codehaus.cargo.container.orion.internal;

import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.configuration.builder.ConfigurationChecker;
import org.codehaus.cargo.container.configuration.entry.DataSourceFixture;
import org.codehaus.cargo.container.configuration.entry.ResourceFixture;
import org.codehaus.cargo.container.orion.Oc4j9xStandaloneLocalConfiguration;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.container.spi.configuration.builder.AbstractLocalConfigurationWithConfigurationBuilderTest;
import org.codehaus.cargo.util.Dom4JUtil;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;

/**
 * Abstract Orion standalone configuration implementation test.
 * 
 * @version $Id$
 */
public abstract class AbstractOrionStandaloneLocalConfigurationTest extends
    AbstractLocalConfigurationWithConfigurationBuilderTest
{

    /**
     * Creates a {@link Oc4j9xStandaloneLocalConfiguration}. {@inheritDoc}
     * @param home Configuration home.
     * @return Local configuration for <code>home</code>.
     */
    @Override
    protected LocalConfiguration createLocalConfiguration(String home)
    {
        return new Oc4j9xStandaloneLocalConfiguration(home);
    }

    /**
     * {@inheritDoc}
     * @return {@link OrionConfigurationChecker}.
     */
    @Override
    protected ConfigurationChecker createConfigurationChecker()
    {
        return new OrionConfigurationChecker();
    }

    /**
     * {@inheritDoc}
     * @param fixture Ignored.
     * @return Directory <code>conf/data-sources.xml</code> in the configuration home.
     */
    @Override
    protected String getDataSourceConfigurationFile(DataSourceFixture fixture)
    {
        return configuration.getHome() + "/conf/data-sources.xml";
    }

    /**
     * Test the role token getter.
     */
    public void testGetRoleToken()
    {
        configuration.setProperty(ServletPropertySet.USERS, "u1:p1:r1,r2|u2:p2:r2,r3");

        String token = ((AbstractOrionStandaloneLocalConfiguration) configuration).getRoleToken();
        assertTrue(token.contains("<security-role-mapping name=\"r1\">"
            + "<user name=\"u1\"/></security-role-mapping>"));
        assertTrue(token.contains("<security-role-mapping name=\"r2\">"
            + "<user name=\"u1\"/><user name=\"u2\"/></security-role-mapping>"));
        assertTrue(token.contains("<security-role-mapping name=\"r3\">"
            + "<user name=\"u2\"/></security-role-mapping>"));
    }

    /**
     * Test the user token getter.
     */
    public void testGetUserToken()
    {
        configuration.setProperty(ServletPropertySet.USERS, "u1:p1:r1,r2|u2:p2:r2,r3");

        String token = ((AbstractOrionStandaloneLocalConfiguration) configuration).getUserToken();
        assertEquals(" " + "<user deactivated=\"false\" username=\"u1\" password=\"p1\"/>"
            + "<user deactivated=\"false\" username=\"u2\" password=\"p2\"/>", token);
    }

    /**
     * Test {@link
     * Oc4j9xStandaloneLocalConfiguration#configure(org.codehaus.cargo.container.LocalContainer)}
     * @throws Exception If anything goes wrong.
     */
    public void testConfigure() throws Exception
    {
        configuration.configure(container);
        assertTrue(configuration.getFileHandler().exists(
            configuration.getHome() + "/conf/server.xml"));
        assertTrue(configuration.getFileHandler().exists(
            configuration.getHome() + "/conf/application.xml"));
        assertTrue(configuration.getFileHandler().exists(
            configuration.getHome() + "/conf/default-web-site.xml"));
        assertTrue(configuration.getFileHandler().exists(
            configuration.getHome() + "/conf/mime.types"));
        assertTrue(configuration.getFileHandler().exists(
            configuration.getHome() + "/conf/principals.xml"));
        assertTrue(configuration.getFileHandler().exists(
            configuration.getHome() + "/conf/rmi.xml"));
        assertTrue(configuration.getFileHandler().exists(
            configuration.getHome() + "/default-web-app/WEB-INF/web.xml"));
        assertTrue(configuration.getFileHandler()
            .exists(configuration.getHome() + "/persistence"));
        assertTrue(configuration.getFileHandler().exists(
            configuration.getHome() + "/application-deployments"));
        assertTrue(configuration.getFileHandler().exists(configuration.getHome() + "/log"));
        assertTrue(configuration.getFileHandler().exists(
            configuration.getHome() + "/applications/cargocpc.war"));
    }

    /**
     * Test datasource setup.
     * @throws Exception If anything goes wrong.
     */
    protected void setUpDataSourceFile() throws Exception
    {
        Dom4JUtil xmlUtil = new Dom4JUtil(getFileHandler());
        String file = configuration.getHome() + "/conf/data-sources.xml";
        Document document = DocumentHelper.createDocument();
        document.addElement("data-sources");
        xmlUtil.saveXml(document, file);
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
     * Setup datasource file and call parent. {@inheritDoc}
     * @param fixture Datasource fixture.
     * @return Configured datasource file.
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
     * Orion does not currently support Resources. {@inheritDoc}
     * @param fixture Ignored.
     * @return <code>null</code>.
     */
    @Override
    protected String getResourceConfigurationFile(ResourceFixture fixture)
    {
        return null;
    }

    /**
     * Orion does not currently support Resources. {@inheritDoc}
     * @throws Exception If anything goes wrong.
     */
    public void testConfigureCreatesResourceForXADataSource() throws Exception
    {
        // Nothing
    }

    /**
     * Orion does not currently support Resources. {@inheritDoc}
     * @throws Exception If anything goes wrong.
     */
    @Override
    public void testConfigureCreatesResource() throws Exception
    {
        // Nothing
    }

    /**
     * Orion does not currently support Resources. {@inheritDoc}
     * @throws Exception If anything goes wrong.
     */
    @Override
    public void testConfigureCreatesTwoResourcesViaProperties() throws Exception
    {
        // Nothing
    }

}
