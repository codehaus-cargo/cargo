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
package org.codehaus.cargo.container.spi.configuration;

import junit.framework.TestCase;

import org.apache.commons.vfs.impl.StandardFileSystemManager;
import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.util.FileHandler;
import org.codehaus.cargo.util.VFSFileHandler;

/**
 * Abstract test for any {@link LocalConfiguration}.
 * 
 */
public abstract class AbstractLocalConfigurationTest extends TestCase
{

    /**
     * Container to test.
     */
    protected InstalledLocalContainer container;

    /**
     * Configuration to test.
     */
    protected LocalConfiguration configuration;

    /**
     * File system manager.
     */
    private StandardFileSystemManager fsManager;

    /**
     * File handler.
     */
    private FileHandler fileHandler;

    /**
     * Constructor without any container name.
     */
    public AbstractLocalConfigurationTest()
    {
        super();
    }

    /**
     * Constructor with a container name.
     * @param name Container name.
     */
    public AbstractLocalConfigurationTest(String name)
    {
        super(name);
    }

    /**
     * Creates the local configuration.
     * @param home Configuration home.
     * @return Local configuration for <code>home</code>.
     */
    protected abstract LocalConfiguration createLocalConfiguration(String home);

    /**
     * Creates the local container.
     * @param configuration Container's configuration.
     * @return Local container for <code>configuration</code>.
     */
    protected abstract InstalledLocalContainer createLocalContainer(
        LocalConfiguration configuration);

    /**
     * @return The file handler.
     */
    protected FileHandler getFileHandler()
    {
        return fileHandler;
    }

    /**
     * Creates the test file system manager and the container. {@inheritDoc}
     * @throws Exception If anything goes wrong.
     */
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        fsManager = new StandardFileSystemManager();
        fsManager.init();
        fileHandler = new VFSFileHandler(fsManager);

        String testHome = "ram:/" + this.getClass().getName();
        String configHome = testHome + "/config";
        String containerHome = testHome + "/container";

        fileHandler.mkdirs(configHome);
        fileHandler.mkdirs(containerHome);
        this.configuration = createLocalConfiguration(configHome);
        this.configuration.setFileHandler(fileHandler);

        this.container = createLocalContainer(configuration);
        this.container.setHome(containerHome);
        this.container.setFileHandler(fileHandler);
    }

    /**
     * Closes the test file system manager. {@inheritDoc}
     * @throws Exception If anything goes wrong.
     */
    @Override
    protected void tearDown() throws Exception
    {
        if (fsManager != null)
        {
            fsManager.close();
        }

        super.tearDown();
    }

    /**
     * Test override.
     */
    public void testOverride()
    {
        assertEquals(null, this.configuration.getPropertyValue("cargo.test"));
        try
        {
            System.setProperty("cargo.test", "somevalue");
            assertEquals("somevalue", this.configuration.getPropertyValue("cargo.test"));
        }
        finally
        {
            System.clearProperty("cargo.test");
        }
        assertEquals(null, this.configuration.getPropertyValue("cargo.test"));
    }
}
