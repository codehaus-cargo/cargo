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
package org.codehaus.cargo.container.spi.deployer;

import java.io.IOException;

import org.apache.commons.vfs.impl.StandardFileSystemManager;
import org.codehaus.cargo.container.ContainerCapability;
import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.util.FileHandler;
import org.codehaus.cargo.util.VFSFileHandler;
import org.codehaus.cargo.util.log.NullLogger;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

/**
 * Unit tests for {@link AbstractCopyingInstalledLocalDeployer}.
 * 
 * @version $Id$
 */
public class CopyingDeployerTest extends MockObjectTestCase
{
    private StandardFileSystemManager fsManager;
    private FileHandler fileHandler;

    /**
     * Creates the test file system manager. {@inheritdoc}
     * @throws Exception If anything goes wrong.
     */
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        this.fsManager = new StandardFileSystemManager();
        this.fsManager.init();
        this.fileHandler = new VFSFileHandler(this.fsManager);
    }

    /**
     * Closes the test file system manager. {@inheritdoc}
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

    private class TestableCopyingDeployer extends AbstractCopyingInstalledLocalDeployer
    {
        public TestableCopyingDeployer(InstalledLocalContainer container)
        {
            super(container);
        }

        @Override
        public String getDeployableDir()
        {
            return "ram:///webapps";
        }
    }

    public void testCanBeDeployedWhenTwoWARsInSameWebContext() throws IOException
    {
        AbstractCopyingInstalledLocalDeployer deployer = new TestableCopyingDeployer(
            createContainer(createContainerCapability(DeployableType.WAR)));

        // Create WARs and make sure the wrapped files exist
        WAR war1 = new WAR("ram:///path1/warfile.war");
        this.fsManager.resolveFile(war1.getFile()).createFile();
        WAR war2 = new WAR("ram:///path2/warfile.war");
        this.fsManager.resolveFile(war2.getFile()).createFile();

        deployer.setFileHandler(this.fileHandler);

        // Deploy the first WAR
        deployer.deploy(war1);
        assertTrue(this.fsManager.resolveFile("ram:///webapps/warfile.war").exists());

        try
        {
            // Deploy the second WAR using the same context as the already deployed WAR
            deployer.deploy(war2);
            fail("Expected ContainerException because we deployed two WARs with the same context "
                + "name.");
        }
        catch (ContainerException expected)
        {
            assertEquals("Failed to deploy [ram:///path2/warfile.war] to [ram:///webapps]. The "
                + "required web context is already in use by another application.",
                expected.getMessage());
        }
    }

    public void testDeployWhenContainerDoesNotSupportDeployableType()
    {
        AbstractCopyingInstalledLocalDeployer deployer = new TestableCopyingDeployer(
            createContainer(createContainerCapability(DeployableType.EAR)));

        try
        {
            deployer.deploy(new WAR("dummy"));
            fail("Should have thrown a ContainerException here");
        }
        catch (ContainerException expected)
        {
            assertEquals("WAR archives are not supported for deployment in [mycontainer]. "
                + "Got [dummy]", expected.getMessage());
        }
    }

    public void testDeployWhenWarWithCustomContext() throws Exception
    {
        WAR war = new WAR("ram:///some/warfile.war");
        this.fsManager.resolveFile("ram:///some/warfile.war").createFile();

        war.setContext("context");

        AbstractCopyingInstalledLocalDeployer deployer = new TestableCopyingDeployer(
            createContainer(createContainerCapability(DeployableType.WAR)));

        deployer.setFileHandler(this.fileHandler);

        assertFalse(this.fsManager.resolveFile("ram:///webapps/context.war").exists());
        deployer.deploy(war);
        assertTrue(this.fsManager.resolveFile("ram:///webapps/context.war").exists());
    }

    public void testDeployWhenWarWithDefaultContext() throws Exception
    {
        WAR war = new WAR("ram:///some/warfile.war");
        this.fsManager.resolveFile(war.getFile()).createFile();

        AbstractCopyingInstalledLocalDeployer deployer = new TestableCopyingDeployer(
            createContainer(createContainerCapability(DeployableType.WAR)));

        deployer.setFileHandler(this.fileHandler);

        assertFalse(this.fsManager.resolveFile("ram:///webapps/warfile.war").exists());
        deployer.deploy(war);
        assertTrue(this.fsManager.resolveFile("ram:///webapps/warfile.war").exists());
    }

    public void testDeployWhenExpandedWarWithCustomContext() throws Exception
    {
        // Create an expanded WAR
        WAR war = new WAR("ram:///some/expanded/warfile");
        war.setContext("context");
        war.setFileHandler(this.fileHandler);
        this.fsManager.resolveFile(war.getFile()).createFolder();

        AbstractCopyingInstalledLocalDeployer deployer = new TestableCopyingDeployer(
            createContainer(createContainerCapability(DeployableType.WAR)));

        deployer.setFileHandler(this.fileHandler);

        assertFalse(this.fsManager.resolveFile("ram:///webapps/context").exists());
        deployer.deploy(war);
        assertTrue(this.fsManager.resolveFile("ram:///webapps/context").exists());
    }

    private ContainerCapability createContainerCapability(DeployableType type)
    {
        Mock mockContainerCapability = mock(ContainerCapability.class);
        mockContainerCapability.stubs().method("supportsDeployableType")
            .with(eq(type)).will(returnValue(true));
        mockContainerCapability.stubs().method("supportsDeployableType")
            .with(not(eq(type))).will(returnValue(false));
        return (ContainerCapability) mockContainerCapability.proxy();
    }

    private InstalledLocalContainer createContainer(ContainerCapability capability)
    {
        Mock mockContainer = mock(InstalledLocalContainer.class);
        mockContainer.stubs().method("getCapability").will(returnValue(capability));
        mockContainer.stubs().method("getLogger").will(returnValue(new NullLogger()));
        mockContainer.stubs().method("getId").will(returnValue("mycontainer"));
        return (InstalledLocalContainer) mockContainer.proxy();
    }
}
