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
package org.codehaus.cargo.generic.configuration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.configuration.Configuration;
import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.stub.ExistingLocalConfigurationStub;
import org.codehaus.cargo.container.stub.RuntimeConfigurationStub;
import org.codehaus.cargo.container.stub.StandaloneLocalConfigurationStub;
import org.codehaus.cargo.util.DefaultFileHandler;

/**
 * Unit tests for {@link DefaultConfigurationFactory}.
 */
public class DefaultConfigurationFactoryTest
{
    /**
     * Configuration factory.
     */
    private ConfigurationFactory factory;

    /**
     * Creates the configuration factory.
     */
    @BeforeEach
    public void setUp()
    {
        this.factory = new DefaultConfigurationFactory();
    }

    /**
     * Test configuration creation with invalid hint.
     */
    @Test
    public void testCreateConfigurationWhenInvalidHint()
    {
        try
        {
            this.factory.createConfiguration("testableContainerId", ContainerType.INSTALLED,
                new ConfigurationType("invalidhint"));
        }
        catch (ContainerException expected)
        {
            Assertions.assertEquals(
                "Cannot create configuration. There's no registered configuration for "
                    + "the parameters (container [id = [testableContainerId], type = "
                    + "[installed]], configuration type [invalidhint]). Actually there are no "
                    + "valid types registered for this configuration. Maybe you've made a mistake "
                    + "spelling it?",
                expected.getMessage());
        }
    }

    /**
     * Test custom configuration registration on an existing container.
     * @throws Exception If anything goes wrong.
     */
    @Test
    public void testRegisterCustomConfigurationOnExistingContainer() throws Exception
    {
        this.factory.registerConfiguration("testableContainerId", ContainerType.INSTALLED,
            ConfigurationType.STANDALONE, StandaloneLocalConfigurationStub.class);

        Configuration configuration = this.factory.createConfiguration("testableContainerId",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE, "/some/path");
        Assertions.assertEquals(StandaloneLocalConfigurationStub.class.getName(),
            configuration.getClass().getName());
    }

    /**
     * Test runtime configuration creation.
     */
    @Test
    public void testCreateRuntimeConfiguration()
    {
        this.factory.registerConfiguration("testableContainerId", ContainerType.REMOTE,
            ConfigurationType.RUNTIME, RuntimeConfigurationStub.class);

        Configuration configuration = this.factory.createConfiguration("testableContainerId",
            ContainerType.REMOTE, ConfigurationType.RUNTIME);
        Assertions.assertEquals(
            RuntimeConfigurationStub.class.getName(), configuration.getClass().getName());
    }

    /**
     * Test standalone local configuration creation when no home directory specified.
     */
    @Test
    public void testCreateStandaloneLocalConfigurationWhenNoHomeDirectorySpecified()
    {
        this.factory.registerConfiguration("testableContainerId", ContainerType.INSTALLED,
            ConfigurationType.STANDALONE, StandaloneLocalConfigurationStub.class);

        LocalConfiguration configuration = (LocalConfiguration) this.factory.createConfiguration(
            "testableContainerId", ContainerType.INSTALLED, ConfigurationType.STANDALONE);
        Assertions.assertEquals(
            new DefaultFileHandler().getTmpPath("conf"), configuration.getHome());
    }

    /**
     * Test existing local configuration creation when no home directory specified.
     */
    @Test
    public void testCreateExistingLocalConfigurationWhenNoHomeDirectorySpecified()
    {
        this.factory.registerConfiguration("testableContainerId", ContainerType.INSTALLED,
            ConfigurationType.EXISTING, ExistingLocalConfigurationStub.class);

        try
        {
            this.factory.createConfiguration("testableContainerId", ContainerType.INSTALLED,
                ConfigurationType.EXISTING);
            Assertions.fail("An exception should have been raised");
        }
        catch (ContainerException expected)
        {
            Assertions.assertEquals(
                "The configuration home parameter must be specified for existing "
                    + "configurations", expected.getOriginalThrowable().getMessage());
        }
    }
}
