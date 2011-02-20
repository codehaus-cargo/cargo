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
package org.codehaus.cargo.container.jboss.internal;

import junit.framework.TestCase;

import org.apache.commons.vfs.impl.StandardFileSystemManager;
import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.jboss.JBoss4xInstalledLocalContainer;
import org.codehaus.cargo.container.jboss.JBossInstalledLocalDeployer;
import org.codehaus.cargo.container.jboss.JBossPropertySet;
import org.codehaus.cargo.container.jboss.JBossStandaloneLocalConfiguration;
import org.codehaus.cargo.util.FileHandler;
import org.codehaus.cargo.util.VFSFileHandler;

/**
 * Unit tests for {@link AbstractJBossInstalledLocalContainer}.
 *
 * <p>Note: These tests are using
 * <a href="http://jakarta.apache.org/commons/vfs/">VFS</a> with a
 * <a href="http://jakarta.apache.org/commons/vfs/filesystems.html#ram">RAM file system</a> so
 * that files are only created in memory. This makes is easy to test file-based operations without
 * having to resort to creating files in the file system and deleting them afterwards.</p>
 *
 * @version $Id$
 */
public class JBossInstalledLocalContainerTest extends TestCase
{
    private static final String CONTAINER_HOME = "ram:///jboss";
    private static final String SERVER_CONFIG = "custom";
    private static final String CONFIGURATION_HOME = CONTAINER_HOME + "/server/" + SERVER_CONFIG;

    private JBoss4xInstalledLocalContainer container;

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
        
        LocalConfiguration configuration =
            new JBossStandaloneLocalConfiguration(CONFIGURATION_HOME);
        configuration.setProperty(JBossPropertySet.CONFIGURATION, SERVER_CONFIG);

        this.container = new JBoss4xInstalledLocalContainer(configuration);
        this.container.setHome(CONTAINER_HOME);
        
    }

    public void testGetConfDir()
    {
        String expected = this.fileHandler.append(CONFIGURATION_HOME, "conf");
        assertEquals(expected, this.container.getConfDir(
            this.container.getConfiguration().getPropertyValue(JBossPropertySet.CONFIGURATION)));
    }

    public void testGetLibDir()
    {
        String expected = this.fileHandler.append(CONFIGURATION_HOME, "lib");
        assertEquals(expected, this.container.getLibDir(
            this.container.getConfiguration().getPropertyValue(JBossPropertySet.CONFIGURATION)));
    }

    public void testGetDeployDir()
    {
        String expected = this.fileHandler.append(CONFIGURATION_HOME, "deploy");
        assertEquals(expected, this.container.getDeployDir(
            this.container.getConfiguration().getPropertyValue(JBossPropertySet.CONFIGURATION)));
    }

    public void testGetFarmDir()
    {
        this.container.getConfiguration().setProperty(JBossPropertySet.CLUSTERED, "true");
        String expected = this.fileHandler.append(CONFIGURATION_HOME, "farm");
        assertEquals(expected, this.container.getDeployDir(
            this.container.getConfiguration().getPropertyValue(JBossPropertySet.CONFIGURATION)));
    }
    
    public void testGetFarmDeployDir()
    {
        this.container.getConfiguration().setProperty(JBossPropertySet.CLUSTERED, "true");
        String expected = this.fileHandler.append(CONFIGURATION_HOME, "farm");
        
        JBossInstalledLocalDeployer deployer = new JBossInstalledLocalDeployer(this.container);
        assertEquals(expected, deployer.getDeployableDir());
    }

    public void testVerifyJBossHomeWhenEmptyDirectory() throws Exception
    {
        this.fsManager.resolveFile("ram:///jboss/bin").createFolder();

        this.container.setFileHandler(this.fileHandler);

        try
        {
            this.container.verifyJBossHome();
            fail("Should have thrown an exception here");
        }
        catch (ContainerException expected)
        {
            assertEquals("Invalid JBoss installation. The [ram:///jboss/bin] directory is empty "
                + "and it shouldn't be. Make sure the JBoss container home directory you have "
                + "specified points to the right location (It's currently pointing to "
                + "[ram:///jboss])", expected.getMessage());
        }
    }

    public void testVerifyJBossHomeWhenMissingRunJar() throws Exception
    {
        this.fsManager.resolveFile("ram:///jboss/bin/shutdown.jar").createFile();
        this.fsManager.resolveFile("ram:///jboss/client/something").createFile();
        this.fsManager.resolveFile("ram:///jboss/lib/something").createFile();
        this.fsManager.resolveFile("ram:///jboss/lib/endorsed/something").createFile();
        this.fsManager.resolveFile("ram:///jboss/server/something").createFile();

        this.container.setFileHandler(this.fileHandler);

        try
        {
            this.container.verifyJBossHome();
            fail("Should have thrown an exception here");
        }
        catch (ContainerException expected)
        {
            assertEquals("Invalid JBoss installation. The [ram:///jboss/bin/run.jar] JAR doesn't "
                + "exist. Make sure the JBoss container home directory you have specified points "
                + "to the right location (It's currently pointing to [ram:///jboss])",
                expected.getMessage());
        }
    }

    public void testVerifyJBossHomeWhenMissingDirectory() throws Exception
    {
        this.fsManager.resolveFile("ram:///jboss/bin/run.jar").createFile();
        this.fsManager.resolveFile("ram:///jboss/bin/shutdown.jar").createFile();
        this.fsManager.resolveFile("ram:///jboss/lib/something").createFile();
        this.fsManager.resolveFile("ram:///jboss/lib/endorsed/something").createFile();
        this.fsManager.resolveFile("ram:///jboss/server/something").createFile();

        this.container.setFileHandler(this.fileHandler);

        try
        {
            this.container.verifyJBossHome();
            fail("Should have thrown an exception here");
        }
        catch (ContainerException expected)
        {
            assertEquals("Invalid JBoss installation. The [ram:///jboss/client] directory doesn't "
                + "exist. Make sure the JBoss container home directory you have specified points "
                + "to the right location (It's currently pointing to [ram:///jboss])",
                expected.getMessage());
        }
    }

    public void testVerifyJBossHomeWhenFileInsteadOfDirectory() throws Exception
    {
        this.fsManager.resolveFile("ram:///jboss/bin").createFile();

        this.container.setFileHandler(this.fileHandler);

        try
        {
            this.container.verifyJBossHome();
            fail("Should have thrown an exception here");
        }
        catch (ContainerException expected)
        {
            assertEquals("Invalid JBoss installation. The [ram:///jboss/bin] path should be a "
                + "directory. Make sure the JBoss container home directory you have specified "
                + "points to the right location (It's currently pointing to [ram:///jboss])",
                expected.getMessage());
        }
    }

    public void testVerifyJBossHomeWhenValidConfiguration() throws Exception
    {
        // Create a valid JBoss server configuration directory structure
        this.fsManager.resolveFile("ram:///jboss/bin/run.jar").createFile();
        this.fsManager.resolveFile("ram:///jboss/bin/shutdown.jar").createFile();
        this.fsManager.resolveFile("ram:///jboss/client/something").createFile();
        this.fsManager.resolveFile("ram:///jboss/lib/something").createFile();
        this.fsManager.resolveFile("ram:///jboss/lib/endorsed/something").createFile();
        this.fsManager.resolveFile("ram:///jboss/server/something").createFile();

        this.container.setFileHandler(this.fileHandler);
        this.container.verifyJBossHome();
    }
}
