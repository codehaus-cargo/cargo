/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2010 Vincent Massol.
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

import org.apache.commons.vfs.impl.StandardFileSystemManager;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.util.FileHandler;
import org.codehaus.cargo.util.VFSFileHandler;

import junit.framework.TestCase;

/**
 * Unit tests for {@link WebLogicCopyingInstalledLocalDeployer}.
 * 
 * <p>
 * Note: These tests are using <a
 * href="http://jakarta.apache.org/commons/vfs/">VFS</a> with a <a
 * href="http://jakarta.apache.org/commons/vfs/filesystems.html#ram">RAM file
 * system</a> so that files are only created in memory. This makes is easy to
 * test file-based operations without having to resort to creating files in the
 * file system and deleting them afterwards.
 * </p>
 * 
 * @version $Id$
 */
public class WebLogicCopyingInstalledLocalDeployerTest extends TestCase
{
    private static final String BEA_HOME = "ram:/bea";
    private static final String DOMAIN_HOME = BEA_HOME + "/mydomain";
    private static final String WL_HOME = BEA_HOME + "/weblogic9";

    private WebLogic9xInstalledLocalContainer container;

    private WebLogicCopyingInstalledLocalDeployer deployer;

    private StandardFileSystemManager fsManager;
    private FileHandler fileHandler;

    /**
     * Creates the test file system manager and the container. {@inheritdoc}
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

        LocalConfiguration configuration = new WebLogic9xStandaloneLocalConfiguration(
                DOMAIN_HOME);
        this.container = new WebLogic9xInstalledLocalContainer(configuration);
        this.container.setHome(WL_HOME);
        this.deployer = new WebLogicCopyingInstalledLocalDeployer(container);
    }

    /**
     * Closes the test file system manager. {@inheritdoc}
     * @throws Exception If anything goes wrong.
     */
    @Override
    protected void tearDown() throws Exception
    {
        if (fsManager != null)
            fsManager.close();

        super.tearDown();
    }

    /**
     * This tests that the DeployableDir is DOMAIN_HOME/autodeploy, which should be
     * the case, as this test uses WebLogic 9.
     */
    public void testDeployableDirIsAutoDeploy()
    {
        // convert so that file paths match in windows os
        String name = this.deployer.getDeployableDir().replaceAll("\\\\", "/");
        assertEquals(DOMAIN_HOME + "/autodeploy", name);
    }
}
