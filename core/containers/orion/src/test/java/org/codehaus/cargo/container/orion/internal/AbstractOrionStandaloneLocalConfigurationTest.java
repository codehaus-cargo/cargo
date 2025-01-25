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
package org.codehaus.cargo.container.orion.internal;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.configuration.builder.ConfigurationChecker;
import org.codehaus.cargo.container.configuration.entry.DataSourceFixture;
import org.codehaus.cargo.container.configuration.entry.ResourceFixture;
import org.codehaus.cargo.container.orion.Oc4j9xStandaloneLocalConfiguration;
import org.codehaus.cargo.container.property.User;
import org.codehaus.cargo.container.spi.configuration.builder.AbstractLocalConfigurationWithConfigurationBuilderTest;

/**
 * Abstract Orion standalone configuration implementation test.
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
    @Test
    public void testGetRoleToken()
    {
        List<User> users = User.parseUsers("u1:p1:r1,r2|u2:p2:r2,r3");
        configuration.getUsers().addAll(users);

        String token = ((AbstractOrionStandaloneLocalConfiguration) configuration).getRoleToken();
        Assertions.assertTrue(token.contains("<security-role-mapping name=\"r1\">"
            + "<user name=\"u1\"/></security-role-mapping>"));
        Assertions.assertTrue(token.contains("<security-role-mapping name=\"r2\">"
            + "<user name=\"u1\"/><user name=\"u2\"/></security-role-mapping>"));
        Assertions.assertTrue(token.contains("<security-role-mapping name=\"r3\">"
            + "<user name=\"u2\"/></security-role-mapping>"));
    }

    /**
     * Test the user token getter.
     */
    @Test
    public void testGetUserToken()
    {
        List<User> users = User.parseUsers("u1:p1:r1,r2|u2:p2:r2,r3");
        configuration.getUsers().addAll(users);

        String token = ((AbstractOrionStandaloneLocalConfiguration) configuration).getUserToken();
        Assertions.assertEquals("<user deactivated=\"false\" username=\"u1\" password=\"p1\"/>"
            + "<user deactivated=\"false\" username=\"u2\" password=\"p2\"/>", token);
    }

    /**
     * Test {@link
     * Oc4j9xStandaloneLocalConfiguration#configure(org.codehaus.cargo.container.LocalContainer)}
     * @throws Exception If anything goes wrong.
     */
    @Test
    public void testConfigure() throws Exception
    {
        configuration.configure(container);
        Assertions.assertTrue(configuration.getFileHandler().exists(
            configuration.getHome() + "/conf/server.xml"));
        Assertions.assertTrue(configuration.getFileHandler().exists(
            configuration.getHome() + "/conf/application.xml"));
        Assertions.assertTrue(configuration.getFileHandler().exists(
            configuration.getHome() + "/conf/default-web-site.xml"));
        Assertions.assertTrue(configuration.getFileHandler().exists(
            configuration.getHome() + "/conf/mime.types"));
        Assertions.assertTrue(configuration.getFileHandler().exists(
            configuration.getHome() + "/conf/principals.xml"));
        Assertions.assertTrue(configuration.getFileHandler().exists(
            configuration.getHome() + "/conf/rmi.xml"));
        Assertions.assertTrue(configuration.getFileHandler().exists(
            configuration.getHome() + "/default-web-app/WEB-INF/web.xml"));
        Assertions.assertTrue(configuration.getFileHandler()
            .exists(configuration.getHome() + "/persistence"));
        Assertions.assertTrue(configuration.getFileHandler().exists(
            configuration.getHome() + "/application-deployments"));
        Assertions.assertTrue(
            configuration.getFileHandler().exists(configuration.getHome() + "/log"));
        Assertions.assertTrue(configuration.getFileHandler().exists(
            configuration.getHome() + "/applications/cargocpc.war"));
    }

    /**
     * Test datasource setup.
     * @throws Exception If anything goes wrong.
     */
    protected void setUpDataSourceFile() throws Exception
    {
        String file = configuration.getHome() + "/conf/data-sources.xml";
        getFileHandler().writeTextFile(file, "<data-sources/>", StandardCharsets.UTF_8);
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
    @Test
    @Override
    public void testConfigureCreatesResource() throws Exception
    {
        // Nothing
    }

    /**
     * Orion does not currently support Resources. {@inheritDoc}
     * @throws Exception If anything goes wrong.
     */
    @Test
    @Override
    public void testConfigureCreatesTwoResourcesViaProperties() throws Exception
    {
        // Nothing
    }
}
