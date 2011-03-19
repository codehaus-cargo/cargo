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
package org.codehaus.cargo.sample.java;

import java.net.MalformedURLException;
import java.net.URL;

import org.codehaus.cargo.container.Container;
import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.configuration.Configuration;
import org.codehaus.cargo.container.configuration.entry.DataSourceFixture;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.property.DatasourcePropertySet;
import org.codehaus.cargo.generic.deployable.DefaultDeployableFactory;
import org.codehaus.cargo.util.CargoException;

public abstract class AbstractDataSourceWarCapabilityContainerTestCase extends
    AbstractCargoTestCase
{
    public AbstractDataSourceWarCapabilityContainerTestCase(String testName,
        EnvironmentTestData testData) throws Exception
    {
        super(testName, testData);
    }

    /**
     * @param property - property looked up to build the datasource
     * @param type
     * @throws MalformedURLException
     */
    protected void _testServletThatIssuesGetConnectionFrom(DataSourceFixture fixture, String type)
        throws MalformedURLException
    {
        addDataSourceToConfigurationViaProperty(fixture);

        testWar(type);
    }

    protected void testWar(String type) throws MalformedURLException
    {
        Deployable war =
            new DefaultDeployableFactory().createDeployable(getContainer().getId(), getTestData()
                .getTestDataFileFor(type + "-war"), DeployableType.WAR);

        getLocalContainer().getConfiguration().addDeployable(war);

        URL warPingURL =
            new URL("http://localhost:" + getTestData().port + "/" + type + "-war-"
                + getTestData().version + "/test");

        startAndStop(warPingURL);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.codehaus.cargo.sample.java.AbstractCargoTestCase#createContainer(org.codehaus.cargo.container
     * .configuration.Configuration)
     */
    @Override
    public Container createContainer(Configuration configuration)
    {
        InstalledLocalContainer container =
            (InstalledLocalContainer) super.createContainer(configuration);
        setUpDerby(container);
        return container;
    }

    public void addDataSourceToConfigurationViaProperty(DataSourceFixture fixture)
    {
        Configuration config = getLocalContainer().getConfiguration();
        config.setProperty(DatasourcePropertySet.DATASOURCE, fixture
            .buildDataSourcePropertyString());
    }

    private void setUpDerby(InstalledLocalContainer container)
    {
        String jdbcdriver = System.getProperty("cargo.testdata.derby-jar");
        if (jdbcdriver != null)
        {
            container.addExtraClasspath(jdbcdriver);
        }
        else
        {
            throw new CargoException(
                "Please set property [cargo.testdata.derby-jar] to a valid location of derby.jar");
        }
        container.getSystemProperties().put("derby.system.home", getTestData().targetDir);
        container.getSystemProperties().put("derby.stream.error.logSeverityLevel", "0");
    }

    /**
     * @param warPingURL
     */
    public void startAndStop(URL warPingURL)
    {
        getLocalContainer().start();
        PingUtils.assertPingTrue(warPingURL.getPath() + " not started", warPingURL, getLogger());

        getLocalContainer().stop();
        PingUtils.assertPingFalse(warPingURL.getPath() + " not stopped", warPingURL, getLogger());
    }

    /**
     * make sure we always stop the container
     */
    @Override
    public void tearDown()
    {
        try
        {
            getLocalContainer().stop();
        }
        finally
        {
            super.tearDown();
        }
    }
}
