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

import java.lang.reflect.Method;
import java.net.MalformedURLException;

import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.configuration.Configuration;
import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.configuration.entry.ConfigurationFixtureFactory;
import org.codehaus.cargo.container.configuration.entry.ResourceFixture;
import org.codehaus.cargo.container.property.ResourcePropertySet;
import org.codehaus.cargo.sample.java.validator.HasResourceSupportValidator;

/**
 * Test for XA datasource deployed as resource resource capabilities.
 */
public class XADatasourceResourceOnStandaloneConfigurationTest extends
    AbstractDataSourceWarCapabilityContainerTestCase
{
    /**
     * Add the required validators.
     * @see #addValidator(org.codehaus.cargo.sample.java.validator.Validator)
     */
    public XADatasourceResourceOnStandaloneConfigurationTest()
    {
        this.addValidator(new HasResourceSupportValidator(ConfigurationType.STANDALONE));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSupported(String containerId, ContainerType containerType, Method testMethod)
    {
        if (!super.isSupported(containerId, containerType, testMethod))
        {
            return false;
        }

        // JBoss 7.5.x, GlassFish 3.x, 4.x, 5.x, 6.x, 7.x and 8.x, Payara, the WebLogic WSLT
        // deployer and WildFly cannot deploy XA datasources as resource
        return this.isNotContained(containerId,
            "glassfish3x", "glassfish4x", "glassfish5x", "glassfish6x", "glassfish7x",
                "glassfish8x",
            "jboss75x",
            "payara",
            "weblogic121x", "weblogic122x", "weblogic14x",
            "wildfly8x", "wildfly9x", "wildfly10x", "wildfly11x", "wildfly12x", "wildfly13x",
                "wildfly14x", "wildfly15x", "wildfly16x", "wildfly17x", "wildfly18x", "wildfly19x",
                "wildfly20x", "wildfly21x", "wildfly22x", "wildfly23x", "wildfly24x", "wildfly25x",
                "wildfly26x", "wildfly27x", "wildfly28x", "wildfly29x", "wildfly30x", "wildfly31x",
                "wildfly32x", "wildfly33x", "wildfly34x", "wildfly35x", "wildfly36x",
                "wildfly37x", "wildfly38x");
    }

    /**
     * User configures javax.sql.XADataSource -&gt; container provides that same
     * javax.sql.XADataSource
     * @throws MalformedURLException If URL for the test WAR cannot be built.
     */
    @CargoTestCase
    public void testUserConfiguresXADataSourceAsResource() throws MalformedURLException
    {
        ResourceFixture fixture = ConfigurationFixtureFactory.createXADataSourceAsResource();

        addResourceToConfigurationViaProperty(fixture);

        testWar("xadatasource", "Got XADataSource connection!");
    }

    /**
     * Add resource to configuration using properties.
     * @param fixture Container.
     */
    protected void addResourceToConfigurationViaProperty(ResourceFixture fixture)
    {
        Configuration config = getLocalContainer().getConfiguration();
        config.setProperty(ResourcePropertySet.RESOURCE, fixture.buildResourcePropertyString());
    }
}
