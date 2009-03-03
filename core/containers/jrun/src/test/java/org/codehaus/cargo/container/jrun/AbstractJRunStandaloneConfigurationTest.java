package org.codehaus.cargo.container.jrun;

import org.codehaus.cargo.container.configuration.entry.DataSourceFixture;
import org.codehaus.cargo.container.configuration.entry.ResourceFixture;
import org.codehaus.cargo.container.spi.configuration.builder.AbstractLocalConfigurationWithConfigurationBuilderTest;

public abstract class AbstractJRunStandaloneConfigurationTest extends
    AbstractLocalConfigurationWithConfigurationBuilderTest
{

    public AbstractJRunStandaloneConfigurationTest()
    {
        super();
    }

    public AbstractJRunStandaloneConfigurationTest(String name)
    {
        super(name);
    }

    protected String configureDataSourceViaPropertyAndRetrieveConfigurationFile(
        DataSourceFixture fixture) throws Exception
    {
        setUpDataSourceFile();
        return super.configureDataSourceViaPropertyAndRetrieveConfigurationFile(fixture);
    }

    abstract protected void setUpDataSourceFile() throws Exception;

    protected String configureDataSourceAndRetrieveConfigurationFile(DataSourceFixture fixture)
        throws Exception
    {
        setUpDataSourceFile();
        return super.configureDataSourceAndRetrieveConfigurationFile(fixture);
    }

    protected String getResourceConfigurationFile(ResourceFixture fixture)
    {
        return null;
    }

    public void testConfigureCreatesResourceForXADataSource() throws Exception
    {
        // JRun does not currently support Resources
    }

    public void testConfigureCreatesResource() throws Exception
    {
        // JRun does not currently support Resources

    }

    public void testConfigureCreatesTwoResourcesViaProperties() throws Exception
    {
    	// JRun does not currently support Resources
    }

}
