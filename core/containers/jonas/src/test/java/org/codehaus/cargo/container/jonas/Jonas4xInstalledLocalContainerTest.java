/*
 * ========================================================================
 *
 * Copyright 2007-2008 OW2. Code from this file
 * was originally imported from the OW2 JOnAS project.
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
package org.codehaus.cargo.container.jonas;

import java.util.Properties;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.apache.commons.vfs2.impl.StandardFileSystemManager;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.stub.JvmLauncherStub;
import org.codehaus.cargo.util.FileHandler;
import org.codehaus.cargo.util.VFSFileHandler;

/**
 * Unit tests for {@link Jonas4xInstalledLocalContainer}.
 */
public class Jonas4xInstalledLocalContainerTest
{
    /**
     * JONAS_ROOT folder for tests.
     */
    private static final String JONAS_ROOT = "ram:///jonasroot";

    /**
     * JONAS_BASE folder for tests.
     */
    private static final String JONAS_BASE = "ram:///jonasbase";

    /**
     * Container.
     */
    private Jonas4xInstalledLocalContainer container;

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

        this.fileHandler.createDirectory(null, JONAS_ROOT);
        this.fileHandler.createDirectory(null, JONAS_BASE);

        LocalConfiguration configuration = new Jonas4xStandaloneLocalConfiguration(JONAS_BASE);

        this.container = new Jonas4xInstalledLocalContainer(configuration);
        this.container.setFileHandler(this.fileHandler);
        this.container.setHome(JONAS_ROOT);
    }

    /**
     * Closes the test file system manager.
     */
    @AfterEach
    protected void tearDown()
    {
        if (fsManager != null)
        {
            fsManager.close();
        }
    }

    /**
     * Test system properties.
     */
    @Test
    public void testSetupSysProps()
    {
        JvmLauncherStub java = new JvmLauncherStub();
        container.setupSysProps(java);

        Properties props = java.getSystemProperties();
        Assertions.assertEquals(16, props.size());
        Assertions.assertTrue(props.getProperty("install.root").endsWith("ram:/jonasroot"));
        Assertions.assertTrue(props.getProperty("jonas.base").endsWith("ram:/jonasbase"));
        Assertions.assertTrue(props.getProperty("java.endorsed.dirs").endsWith(
            fileHandler.append("ram:/jonasroot", "lib/endorsed")));
        Assertions.assertTrue(props.getProperty("java.security.policy").endsWith(
            fileHandler.append("ram:/jonasbase", "conf/java.policy")));
        Assertions.assertTrue(props.getProperty("java.security.auth.login.config").endsWith(
            fileHandler.append("ram:/jonasbase", "conf/jaas.config")));
    }

}
