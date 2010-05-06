package org.codehaus.cargo.container.tomcat.internal;

import org.codehaus.cargo.container.configuration.entry.DataSourceFixture;
import org.codehaus.cargo.container.configuration.entry.ResourceFixture;
import org.codehaus.cargo.container.deployable.EAR;
import org.codehaus.cargo.container.spi.configuration.builder.AbstractLocalConfigurationWithConfigurationBuilderTest;
import org.codehaus.cargo.container.tomcat.Tomcat4xStandaloneLocalConfiguration;
import org.codehaus.cargo.util.CargoException;

public abstract class AbstractCatalinaStandaloneLocalConfigurationTest extends
    AbstractLocalConfigurationWithConfigurationBuilderTest
{

    protected void setUp() throws Exception
    {
        super.setUp();
        setUpManager();
    }

    abstract protected void setUpManager();

    protected String getDataSourceConfigurationFile(DataSourceFixture fixture)
    {
        return getResourceConfigurationFile(null);
    }

    private void testEscapePath(String path, String expectedEscapedPath)
    {
        AbstractCatalinaStandaloneLocalConfiguration configuration =
                (AbstractCatalinaStandaloneLocalConfiguration) this.configuration;

        String escapedPath = configuration.escapePath(path);

        assertEquals(path, expectedEscapedPath, escapedPath);
    }

    public void testEscapePathWithFullUNIXPath()
    {
        testEscapePath("/usr/bin/java", "/usr/bin/java");
    }

    public void testEscapePathWithPartialUNIXPath()
    {
        testEscapePath("Documents/java", "Documents/java");
    }

    public void testEscapePathWithFullWindowsPath()
    {
        testEscapePath("C:\\Windows\\SYSTEM32\\java.exe", "/C:/Windows/SYSTEM32/java.exe");
    }

    public void testEscapePathWithPartialWindowsPath()
    {
        testEscapePath("Documents\\java", "Documents/java");
    }

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

    protected String configureDataSourceViaPropertyAndRetrieveConfigurationFile(
        DataSourceFixture fixture) throws Exception
    {
        setUpResourceFile();
        return super.configureDataSourceViaPropertyAndRetrieveConfigurationFile(fixture);
    }

    protected String configureDataSourceAndRetrieveConfigurationFile(DataSourceFixture fixture)
        throws Exception
    {
        setUpResourceFile();
        return super.configureDataSourceAndRetrieveConfigurationFile(fixture);
    }

    protected String configureResourceViaPropertyAndRetrieveConfigurationFile(
        ResourceFixture fixture) throws Exception
    {
        setUpResourceFile();
        return super.configureResourceViaPropertyAndRetrieveConfigurationFile(fixture);
    }

    abstract protected void setUpResourceFile() throws Exception;

    protected String configureResourceAndRetrieveConfigurationFile(ResourceFixture fixture)
        throws Exception
    {
        setUpResourceFile();
        return super.configureResourceAndRetrieveConfigurationFile(fixture);
    }

    public void testConfigureCreatesDataSourceForDriverConfiguredDataSourceWithXaTransactionSupport()
        throws Exception
    {
        try
        {
            super
                .testConfigureCreatesDataSourceForDriverConfiguredDataSourceWithXaTransactionSupport();
            fail("should have received an exception");
        }
        catch (UnsupportedOperationException e)
        {
            assertEquals(
                "Tomcat does not support XA_TRANSACTION for DataSource implementations.", e
                    .getMessage());
        }
    }

    public void testConfigureCreatesDataSourceForDriverConfiguredDataSourceWithLocalTransactionSupport()
        throws Exception
    {
        try
        {
            super
                .testConfigureCreatesDataSourceForDriverConfiguredDataSourceWithLocalTransactionSupport();
            fail("should have received an exception");
        }
        catch (UnsupportedOperationException e)
        {
            assertEquals(
                "Tomcat does not support LOCAL_TRANSACTION for DataSource implementations.", e
                    .getMessage());
        }
    }

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
