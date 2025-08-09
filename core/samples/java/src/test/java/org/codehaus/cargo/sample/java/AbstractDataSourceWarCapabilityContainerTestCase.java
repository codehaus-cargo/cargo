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
package org.codehaus.cargo.sample.java;

import java.net.MalformedURLException;

import org.codehaus.cargo.container.Container;
import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.configuration.Configuration;
import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.configuration.entry.DataSourceFixture;
import org.codehaus.cargo.container.property.DatasourcePropertySet;
import org.codehaus.cargo.sample.java.validator.HasDataSourceSupportValidator;
import org.codehaus.cargo.sample.java.validator.IsInstalledLocalContainerValidator;
import org.codehaus.cargo.util.CargoException;

/**
 * Abstract test case for container with datasource capabilities.
 */
public abstract class AbstractDataSourceWarCapabilityContainerTestCase extends AbstractWarTestCase
{
    /**
     * Add the required validators.
     * @see #addValidator(org.codehaus.cargo.sample.java.validator.Validator)
     */
    public AbstractDataSourceWarCapabilityContainerTestCase()
    {
        this.addValidator(new IsInstalledLocalContainerValidator());
        this.addValidator(new HasDataSourceSupportValidator(ConfigurationType.STANDALONE));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Container createContainer(Configuration configuration)
    {
        InstalledLocalContainer container =
            (InstalledLocalContainer) super.createContainer(configuration);
        setUpDerby(container);
        return container;
    }

    /**
     * Add datasource.
     * @param fixture Datasource definition.
     */
    protected void addDataSourceToConfigurationViaProperty(DataSourceFixture fixture)
    {
        Configuration config = getLocalContainer().getConfiguration();
        config.setProperty(DatasourcePropertySet.DATASOURCE, fixture
            .buildDataSourcePropertyString());
    }

    /**
     * Setup a Derby datasource.
     * @param container Container to set on.
     */
    private void setUpDerby(InstalledLocalContainer container)
    {
        if ("glassfish3x".equals(container.getId()) || "glassfish4x".equals(container.getId())
            || "glassfish5x".equals(container.getId()) || "glassfish6x".equals(container.getId())
            || "glassfish7x".equals(container.getId()) || "glassfish8x".equals(container.getId())
            || "payara".equals(container.getId()))
        {
            // GlassFish 3.x, 4.x, 5.x and 6.1.x onwards (interestingly, not GlassFish 6.0.0) as
            // well as earlier versions of Payara already ship with Derby, adding the JAR twice
            // will result in a SecurityException: sealing violation: package org.apache.derby
            if (!container.getName().startsWith("GlassFish 6.0")
                && !container.getName().startsWith("Payara 5.2")
                && !container.getName().startsWith("Payara 6.")
                && !container.getName().startsWith("Payara 7."))
            {
                return;
            }
        }

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
        container.getSystemProperties().put("derby.system.home", getTestData().configurationHome);
        container.getSystemProperties().put("derby.stream.error.logSeverityLevel", "0");
    }

    /**
     * Adds datasource and Tests servlet.
     * @param fixture Datasource definition.
     * @param type WAR type.
     * @throws MalformedURLException If URL cannot be built.
     */
    protected void testServletThatIssuesGetConnectionFrom(DataSourceFixture fixture, String type)
        throws MalformedURLException
    {
        addDataSourceToConfigurationViaProperty(fixture);
        if ("datasource-cmt-local".equals(type))
        {
            testWar(type, "all good!");
        }
        else if ("datasource".equals(type))
        {
            testWar(type, "Got DataSource connection!");
        }
        else if ("xadatasource".equals(type))
        {
            testWar(type, "Got XADataSource connection!");
        }
        else
        {
            testWar(type, "Got DataSource connections!");
        }
    }
}
