/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2022 Ali Tokmen.
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
package org.codehaus.cargo.container.weblogic.internal;

import junit.framework.TestCase;

import org.apache.commons.vfs2.impl.StandardFileSystemManager;
import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.weblogic.WebLogic9xInstalledLocalContainer;
import org.codehaus.cargo.container.weblogic.WebLogic9xStandaloneLocalConfiguration;
import org.codehaus.cargo.util.FileHandler;
import org.codehaus.cargo.util.VFSFileHandler;

/**
 * Unit tests for {@link AbstractWebLogicInstalledLocalContainer}.
 * <p>
 * Note: These tests are using <a href="http://jakarta.apache.org/commons/vfs/">VFS</a> with a <a
 * href="http://jakarta.apache.org/commons/vfs/filesystems.html#ram">RAM file system</a> so that
 * files are only created in memory. This makes is easy to test file-based operations without having
 * to resort to creating files in the file system and deleting them afterwards.
 * </p>
 */
public class WebLogicInstalledLocalContainerTest extends TestCase
{
    /**
     * BEA_HOME
     */
    private static final String BEA_HOME = "ram:/bea";

    /**
     * DOMAIN_HOME
     */
    private static final String DOMAIN_HOME = BEA_HOME + "/mydomain";

    /**
     * WL_HOME
     */
    private static final String WL_HOME = BEA_HOME + "/weblogic9";

    /**
     * Container to test.
     */
    private WebLogic9xInstalledLocalContainer container;

    /**
     * File system manager.
     */
    private StandardFileSystemManager fsManager;

    /**
     * File handler.
     */
    private FileHandler fileHandler;

    /**
     * Creates the test file system manager and the container. {@inheritDoc}
     * @throws Exception If anything goes wrong.
     */
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        this.fsManager = new StandardFileSystemManager();
        this.fsManager.init();
        this.fileHandler = new VFSFileHandler(this.fsManager);
        this.fileHandler.delete(BEA_HOME);

        LocalConfiguration configuration = new WebLogic9xStandaloneLocalConfiguration(DOMAIN_HOME);
        this.container = new WebLogic9xInstalledLocalContainer(configuration);
        this.container.setHome(WL_HOME);
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
     * Initialize WebLogic home.
     */
    public void testInitBeaHome()
    {
        this.container.initBeaHome();
        // convert so that file paths match in windows os
        String name = this.container.getBeaHome().replaceAll("\\\\", "/");
        assertEquals(BEA_HOME, name);
    }

    /**
     * Test WebLogic home.
     */
    public void testGetBeaHome()
    {
        this.container.setBeaHome(BEA_HOME);
        assertEquals(BEA_HOME, this.container.getBeaHome());
    }

    /**
     * Test empty WebLogic home.
     * @throws Exception If anything goes wrong.
     */
    public void testVerifyWeblogicHomeWhenEmptyDirectory() throws Exception
    {
        this.fsManager.resolveFile(WL_HOME + "/server/lib").createFolder();

        this.container.setFileHandler(this.fileHandler);

        try
        {
            this.container.verifyWeblogicHome();
            fail("Should have thrown an exception here");
        }
        catch (ContainerException expected)
        {
            assertEquals("Invalid WebLogic installation. The [" + WL_HOME
                + "/server/lib] directory is empty "
                + "and it shouldn't be. Make sure the WL_HOME directory you have specified "
                + "points to the right location (It's currently pointing to [" + WL_HOME + "])",
                expected.getMessage());
        }
    }

    /**
     * Test WebLogic home with missing registry XML.
     * @throws Exception If anything goes wrong.
     */
    public void testVerifyBeaHomeWhenMissingRegistryXml() throws Exception
    {
        this.container.setBeaHome(BEA_HOME);
        this.container.setFileHandler(this.fileHandler);

        try
        {
            this.container.verifyBeaHome();
            fail("Should have thrown an exception here");
        }
        catch (ContainerException expected)
        {
            assertEquals("Invalid WebLogic installation. The [" + BEA_HOME
                + "/registry.xml] file doesn't "
                + "exist. Make sure the BEA_HOME directory you have specified "
                + "points to the correct location (it is currently pointing to [" + BEA_HOME + "])",
                expected.getMessage());
        }
    }

    /**
     * Test WebLogic home with missing directory.
     * @throws Exception If anything goes wrong.
     */
    public void testVerifyWeblogicHomeWhenMissingDirectory() throws Exception
    {
        this.container.setFileHandler(this.fileHandler);

        try
        {
            this.container.verifyWeblogicHome();
            fail("Should have thrown an exception here");
        }
        catch (ContainerException expected)
        {
            assertEquals("Invalid WebLogic installation. The [" + WL_HOME
                + "/server/lib] directory doesn't "
                + "exist. Make sure the WL_HOME directory you have specified "
                + "points to the right location (It's currently pointing to [" + WL_HOME + "])",
                expected.getMessage());
        }
    }

    /**
     * Test WebLogic home a file instead of directory.
     * @throws Exception If anything goes wrong.
     */
    public void testVerifyWeblogicHomeWhenFileInsteadOfDirectory() throws Exception
    {
        this.fsManager.resolveFile(WL_HOME + "/server/lib").createFile();

        this.container.setFileHandler(this.fileHandler);

        try
        {
            this.container.verifyWeblogicHome();
            fail("Should have thrown an exception here");
        }
        catch (ContainerException expected)
        {
            assertEquals("Invalid WebLogic installation. The [" + WL_HOME
                + "/server/lib] path should be a "
                + "directory. Make sure the WL_HOME directory you have specified "
                + "points to the right location (It's currently pointing to [" + WL_HOME + "])",
                expected.getMessage());
        }
    }

    /**
     * Test WebLogic home with valid directory.
     * @throws Exception If anything goes wrong.
     */
    public void testVerifyBeaHomeWhenValidConfiguration() throws Exception
    {
        this.container.setBeaHome(BEA_HOME);
        this.fsManager.resolveFile(BEA_HOME + "/registry.xml").createFile();
        this.container.setFileHandler(this.fileHandler);
        this.container.verifyBeaHome();
    }

    /**
     * Test WebLogic home with valid directory.
     * @throws Exception If anything goes wrong.
     */
    public void testVerifyWeblogicHomeWhenValidConfiguration() throws Exception
    {
        this.fsManager.resolveFile(WL_HOME + "/server/lib/weblogic.jar").createFile();
        this.container.setFileHandler(this.fileHandler);
        this.container.verifyWeblogicHome();
    }

}
