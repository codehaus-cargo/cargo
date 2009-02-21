/* 
 * ========================================================================
 * 
 * Copyright 2004-2006 Vincent Massol.
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
import java.util.Collections;

import junit.framework.Test;

import org.codehaus.cargo.container.Container;
import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.configuration.Configuration;
import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.configuration.entry.ConfigurationFixtureFactory;
import org.codehaus.cargo.container.configuration.entry.ResourceFixture;
import org.codehaus.cargo.container.property.ResourcePropertySet;
import org.codehaus.cargo.sample.java.validator.HasResourceSupportValidator;
import org.codehaus.cargo.sample.java.validator.HasStandaloneConfigurationValidator;
import org.codehaus.cargo.sample.java.validator.HasWarSupportValidator;
import org.codehaus.cargo.sample.java.validator.IsInstalledLocalContainerValidator;
import org.codehaus.cargo.sample.java.validator.Validator;

public class ResourceOnStandaloneConfigurationTest extends
    AbstractDataSourceWarCapabilityContainerTestCase
{
    public ResourceOnStandaloneConfigurationTest(String testName, EnvironmentTestData testData)
        throws Exception
    {
        super(testName, testData);
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        setContainer(createContainer(createConfiguration(ConfigurationType.STANDALONE)));
    }

    public Container createContainer(ContainerType type, Configuration configuration)
    {
        InstalledLocalContainer container =
            (InstalledLocalContainer) super.createContainer(type, configuration);
        addMailJarsToExtraClasspath(container);
        return container;

    }

    private void addMailJarsToExtraClasspath(InstalledLocalContainer container)
    {
        String mail = System.getProperty("cargo.testdata.mail-jars");
        if (mail != null)
        {
            String[] jars = container.getFileHandler().getChildren(mail);
            for (int i = 0; i < jars.length; i++)
            {
                container.addExtraClasspath(jars[i]);
            }
        }
    }

    public static Test suite() throws Exception
    {
        CargoTestSuite suite =
            new CargoTestSuite("Tests that run on local containers supporting Resource and WAR deployments");

        // Note: We exclude geronimo1x container as it doesn't support static deployments yet.
        suite.addTestSuite(ResourceOnStandaloneConfigurationTest.class, new Validator[] {
        new IsInstalledLocalContainerValidator(), new HasStandaloneConfigurationValidator(),
        new HasWarSupportValidator(),
        new HasResourceSupportValidator(ConfigurationType.STANDALONE)}, Collections
            .singleton("geronimo1x"));
        return suite;
    }

    /**
     * User configures javax.sql.XADataSource -> container provides that same javax.sql.XADataSource
     */
    public void testUserConfiguresXADataSourceAsResource() throws MalformedURLException
    {
        ResourceFixture fixture = ConfigurationFixtureFactory.createXADataSourceAsResource();

        addResourceToConfigurationViaProperty(fixture);

        testWar("xadatasource");
    }

    /**
     * User configures javax.mail.Session -> container provides that same javax.mail.Session
     */
    public void testUserConfiguresMailSessionAsResource() throws MalformedURLException
    {
        ResourceFixture fixture = ConfigurationFixtureFactory.createMailSessionAsResource();

        addResourceToConfigurationViaProperty(fixture);

        testWar("mailsession");
    }

    private void addResourceToConfigurationViaProperty(ResourceFixture fixture)
    {
        Configuration config = getLocalContainer().getConfiguration();
        config.setProperty(ResourcePropertySet.RESOURCE, fixture.buildResourcePropertyString());
    }
}
