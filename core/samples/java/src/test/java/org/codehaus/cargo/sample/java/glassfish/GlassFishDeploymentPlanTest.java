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
package org.codehaus.cargo.sample.java.glassfish;

import org.codehaus.cargo.container.configuration.Configuration;
import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.sample.java.CargoTestCase;
import org.codehaus.cargo.sample.java.AbstractStandaloneLocalContainerTestCase;
import org.codehaus.cargo.sample.java.validator.StartsWithContainerValidator;

/**
 * Test the deployment of GlassFish deployment plans.
 */
public class GlassFishDeploymentPlanTest extends AbstractStandaloneLocalContainerTestCase
{
    /**
     * Add the required validators.
     * @see #addValidator(org.codehaus.cargo.sample.java.validator.Validator)
     */
    public GlassFishDeploymentPlanTest()
    {
        this.addValidator(new StartsWithContainerValidator("glassfish", "payara"));
    }

    /**
     * Tests loading with a {@link javax.sql.DataSource} class. The
     * {@link javax.sql.XADataSource} is not tested because that configuration cannot
     * be tested in {@link ConfigurationType#STANDALONE}
     * 
     * @throws Exception If anything goes wrong.
     */
    @CargoTestCase
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
