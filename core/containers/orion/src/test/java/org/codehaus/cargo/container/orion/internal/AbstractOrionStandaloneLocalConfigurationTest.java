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

public abstract class AbstractOrionStandaloneLocalConfigurationTest extends
    AbstractLocalConfigurationWithConfigurationBuilderTest
{

    public LocalConfiguration createLocalConfiguration(String home)
    {
        return new Oc4j9xStandaloneLocalConfiguration(home);
    }

    protected ConfigurationChecker createConfigurationChecker()
    {
        return new OrionConfigurationChecker();
    }

    protected String getDataSourceConfigurationFile(DataSourceFixture fixture)
    {
        return configuration.getHome() + "/conf/data-sources.xml";
    }

    public void testGetRoleToken()
    {
        configuration.setProperty(ServletPropertySet.USERS, "u1:p1:r1,r2|u2:p2:r2,r3");

        String token = ((AbstractOrionStandaloneLocalConfiguration) configuration).getRoleToken();
        assertTrue(token.indexOf("<security-role-mapping name=\"r1\">"
            + "<user name=\"u1\"/></security-role-mapping>") > -1);
        assertTrue(token.indexOf("<security-role-mapping name=\"r2\">"
            + "<user name=\"u1\"/><user name=\"u2\"/></security-role-mapping>") > -1);
        assertTrue(token.indexOf("<security-role-mapping name=\"r3\">"
            + "<user name=\"u2\"/></security-role-mapping>") > -1);
    }

    public void testGetUserToken()
    {
        configuration.setProperty(ServletPropertySet.USERS, "u1:p1:r1,r2|u2:p2:r2,r3");

        String token = ((AbstractOrionStandaloneLocalConfiguration) configuration).getUserToken();
        assertEquals(" " + "<user deactivated=\"false\" username=\"u1\" password=\"p1\"/>"
            + "<user deactivated=\"false\" username=\"u2\" password=\"p2\"/>", token);
    }

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

    protected void setUpDataSourceFile() throws Exception
    {
        Dom4JUtil xmlUtil = new Dom4JUtil(getFileHandler());
        String file = configuration.getHome() + "/conf/data-sources.xml";
        Document document = DocumentHelper.createDocument();
        document.addElement("data-sources");
        xmlUtil.saveXml(document, file);
    }

    protected String configureDataSourceViaPropertyAndRetrieveConfigurationFile(
        DataSourceFixture fixture) throws Exception
    {
        setUpDataSourceFile();
        return super.configureDataSourceViaPropertyAndRetrieveConfigurationFile(fixture);
    }

    protected String configureDataSourceAndRetrieveConfigurationFile(DataSourceFixture fixture)
        throws Exception
    {
        setUpDataSourceFile();
        return super.configureDataSourceAndRetrieveConfigurationFile(fixture);
    }

    protected String getResourceConfigurationFile(ResourceFixture fixture)
    {
        // Orion does not currently support Resources
        return null;
    }

    public void testConfigureCreatesResourceForXADataSource() throws Exception
    {
        // Orion does not currently support Resources
    }

    public void testConfigureCreatesResource() throws Exception
    {
        // Orion does not currently support Resources

    }

    public void testConfigureCreatesTwoResourcesViaProperties() throws Exception
    {
        // Orion does not currently support Resources

    }

}
