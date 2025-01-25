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
package org.codehaus.cargo.container.wildfly.internal.configuration.commands.wildfly9.domain;

import org.apache.commons.vfs2.impl.StandardFileSystemManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.wildfly.WildFly9xStandaloneLocalConfiguration;
import org.codehaus.cargo.util.FileHandler;
import org.codehaus.cargo.util.VFSFileHandler;

/**
 * Unit tests for {@link SystemPropertyScriptCommand}.
 */
public class SystemPropertyScriptCommandTest
{

    /**
     * Container home.
     */
    private static final String CONTAINER_HOME = "ram:///wildfly";

    /**
     * Server configuration.
     */
    private static final String SERVER_CONFIG = "standalone";

    /**
     * Configuration home.
     */
    private static final String CONFIGURATION_HOME = CONTAINER_HOME + "/server/" + SERVER_CONFIG;

    /**
     * Path to configuration script resources.
     */
    private static final String RESOURCE_PATH =
            "org/codehaus/cargo/container/internal/resources/wildfly-9/cli/";

    /**
     * Container.
     */
    private LocalConfiguration configuration;

    /**
     * File system manager.
     */
    private StandardFileSystemManager fsManager;

    /**
     * File handler.
     */
    private FileHandler fileHandler;

    /**
     * Creates the test file system manager and the container.
     * @throws Exception If anything goes wrong.
     */
    @BeforeEach
    protected void setUp() throws Exception
    {
        this.fsManager = new StandardFileSystemManager();
        this.fsManager.init();
        this.fileHandler = new VFSFileHandler(this.fsManager);
        this.fileHandler.createDirectory(CONTAINER_HOME + "/server", SERVER_CONFIG);

        configuration = new WildFly9xStandaloneLocalConfiguration(CONFIGURATION_HOME);
    }

    /**
     * Test that setting system property with null value throws exception.
     * @throws Exception If anything goes wrong.
     */
    @Test
    public void testThrowExceptionOnNullSystemPropertyValue() throws Exception
    {
        try
        {
            new SystemPropertyScriptCommand(configuration, RESOURCE_PATH, "cargo.property", null);
            Assertions.fail(
                "Setting system property with null value should throw IllegalArgumentException.");
        }
        catch (IllegalArgumentException e)
        {
            // expected
        }
    }

    /**
     * Test that setting system property with null name throws exception.
     * @throws Exception If anything goes wrong.
     */
    @Test
    public void testThrowExceptionOnNullSystemPropertyName() throws Exception
    {
        try
        {
            new SystemPropertyScriptCommand(configuration, RESOURCE_PATH, null, "cargo-value");
            Assertions.fail(
                "Setting system property with null value should throw IllegalArgumentException.");
        }
        catch (IllegalArgumentException e)
        {
            // expected
        }
    }
}
