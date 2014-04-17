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
package org.codehaus.cargo.sample.java.glassfish;

import java.util.Set;
import java.util.TreeSet;

import junit.framework.Test;

import org.codehaus.cargo.container.configuration.Configuration;
import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.sample.java.AbstractCargoTestCase;
import org.codehaus.cargo.sample.java.CargoTestSuite;
import org.codehaus.cargo.sample.java.EnvironmentTestData;
import org.codehaus.cargo.sample.java.validator.HasLocalDeployerValidator;
import org.codehaus.cargo.sample.java.validator.HasStandaloneConfigurationValidator;
import org.codehaus.cargo.sample.java.validator.IsInstalledLocalContainerValidator;
import org.codehaus.cargo.sample.java.validator.StartsWithContainerValidator;
import org.codehaus.cargo.sample.java.validator.Validator;

/**
 * Test the deployment of Glassfish deployment plans.
 * 
 * @version $Id$
 */
public class GlassfishDeploymentPlanTest extends AbstractCargoTestCase
{
    /**
     * Initializes the test case.
     * @param testName Test name.
     * @param testData Test environment data.
     * @throws Exception If anything goes wrong.
     */
    public GlassfishDeploymentPlanTest(String testName, EnvironmentTestData testData)
        throws Exception
    {
        super(testName, testData);
    }

    /**
     * Creates the test suite, using the {@link Validator}s.
     * @return Test suite.
     * @throws Exception If anything goes wrong.
     */
    public static Test suite() throws Exception
    {
        Set<String> excludedContainerIds = new TreeSet<String>();

        CargoTestSuite suite =
            new CargoTestSuite("Test that verifies Glassfish-specific standalone local "
                + "configuration options");

        suite.addTestSuite(GlassfishDeploymentPlanTest.class, new Validator[] {
            new StartsWithContainerValidator("glassfish"),
            new IsInstalledLocalContainerValidator(),
            new HasStandaloneConfigurationValidator(),
            new HasLocalDeployerValidator()
        }, excludedContainerIds);

        return suite;
    }

    /**
     * Tests loading with a {@link javax.sql.DataSource} class. The
     * {@link javax.sql.XADataSource} is not tested because that configuration cannot
     * be tested in {@link ConfigurationType#STANDALONE}
     * 
     * @throws Exception If anything goes wrong.
     */
    public void testDataSourceClass() throws Exception
    {
        Configuration configuration = createConfiguration(ConfigurationType.STANDALONE);
        configuration.setProperty("cargo.datasource.datasource.derby", 
            "cargo.datasource.type=javax.sql.DataSource|"
            + "cargo.datasource.driver=org.apache.derby.jdbc.EmbeddedDataSource|"
            + "cargo.datasource.jndi=jdbc/cargoembedded|"
            + "cargo.datasource.url=jdbc:derby:memory:myDB;create=true|"
            + "cargo.datasource.username=sa|"
            + "cargo.datasource.password=sa");
        setContainer(createContainer(configuration));
        getLocalContainer().start();
        getLocalContainer().stop();
    }
}
