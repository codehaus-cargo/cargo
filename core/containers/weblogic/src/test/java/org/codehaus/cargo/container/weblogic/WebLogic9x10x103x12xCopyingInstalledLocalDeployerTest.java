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
package org.codehaus.cargo.container.weblogic;

import junit.framework.TestCase;

import org.apache.commons.vfs.impl.StandardFileSystemManager;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.util.FileHandler;
import org.codehaus.cargo.util.VFSFileHandler;

/**
 * Unit tests for {@link WebLogic9x10x103x12xCopyingInstalledLocalDeployer}.
 * 
 * <p>
 * Note: These tests are using <a href="http://jakarta.apache.org/commons/vfs/">VFS</a> with a <a
 * href="http://jakarta.apache.org/commons/vfs/filesystems.html#ram">RAM file system</a> so that
 * files are only created in memory. This makes is easy to test file-based operations without having
 * to resort to creating files in the file system and deleting them afterwards.
 * </p>
 * 
 */
public class WebLogic9x10x103x12xCopyingInstalledLocalDeployerTest extends TestCase
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
     * Container.
     */
    private WebLogic9xInstalledLocalContainer container;

    /**
     * Deployer.
     */
    private WebLogic9x10x103x12xCopyingInstalledLocalDeployer deployer;

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
        this.fileHandler.createDirectory(DOMAIN_HOME, "");

        LocalConfiguration configuration = new WebLogic9xStandaloneLocalConfiguration(
                DOMAIN_HOME);
        this.container = new WebLogic9xInstalledLocalContainer(configuration);
        this.container.setHome(WL_HOME);
        this.container.setFileHandler(this.fileHandler);
        this.deployer = new WebLogic9x10x103x12xCopyingInstalledLocalDeployer(container);
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
     * This tests that the DeployableDir is DOMAIN_HOME/autodeploy, which should be the case, as
     * this test uses WebLogic 9.
     */
    public void testDeployableDirIsAutoDeploy()
    {
        // convert so that file paths match in windows os
        String name = this.deployer.getDeployableDir(null).replaceAll("\\\\", "/");
        assertEquals(DOMAIN_HOME + "/autodeploy", name);
    }
}
