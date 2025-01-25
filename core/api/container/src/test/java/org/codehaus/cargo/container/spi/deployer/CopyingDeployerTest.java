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
package org.codehaus.cargo.container.spi.deployer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.apache.commons.vfs2.impl.StandardFileSystemManager;
import org.codehaus.cargo.container.ContainerCapability;
import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.util.CargoException;
import org.codehaus.cargo.util.FileHandler;
import org.codehaus.cargo.util.VFSFileHandler;
import org.codehaus.cargo.util.log.NullLogger;
import org.mockito.AdditionalMatchers;
import org.mockito.Mockito;

/**
 * Unit tests for {@link AbstractCopyingInstalledLocalDeployer}.
 */
public class CopyingDeployerTest
{
    /**
     * File system manager.
     */
    private StandardFileSystemManager fsManager;

    /**
     * File handler.
     */
    private FileHandler fileHandler;

    /**
     * Creates the test file system manager.
     * @throws Exception If anything goes wrong.
     */
    @BeforeEach
    protected void setUp() throws Exception
    {
        this.fsManager = new StandardFileSystemManager();
        this.fsManager.init();
        this.fileHandler = new VFSFileHandler(this.fsManager);

        this.fileHandler.createDirectory("ram:///", "webapps");
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
     * Mock {@link AbstractCopyingInstalledLocalDeployer} implementation.
     */
    private class TestableCopyingDeployer extends AbstractCopyingInstalledLocalDeployer
    {
        /**
         * {@inheritDoc}
         * @param container Local container.
         */
        public TestableCopyingDeployer(InstalledLocalContainer container)
        {
            super(container);
        }

        /**
         * {@inheritDoc}
         * @return Mock directory.
         */
        @Override
        public String getDeployableDir(Deployable deployable)
        {
            return "ram:///webapps";
        }
    }

    /**
     * Mock {@link AbstractCopyingInstalledLocalDeployer} implementation.
     */
    private class TestableCopyingDeployerWithDifferentDirectory
        extends AbstractCopyingInstalledLocalDeployer
    {
        /**
         * {@inheritDoc}
         * @param container Local container.
         */
        public TestableCopyingDeployerWithDifferentDirectory(InstalledLocalContainer container)
        {
            super(container);
        }

        /**
         * {@inheritDoc}
         * @return Mock directory.
         */
        @Override
        public String getDeployableDir(Deployable deployable)
        {
            return "ram:///webapps-nonexisting";
        }
    }

    /**
     * Test that the handling of ShouldDeployExpanded functions correctly.
     */
    @Test
    public void testShouldDeployExpanded()
    {
        AbstractCopyingInstalledLocalDeployer deployer = new TestableCopyingDeployer(
            createContainer(createContainerCapability(DeployableType.WAR), null));

        Assertions.assertTrue(deployer.shouldDeployExpanded(DeployableType.WAR));
        deployer.setShouldDeployExpanded(DeployableType.WAR, true);
        Assertions.assertTrue(deployer.shouldDeployExpanded(DeployableType.WAR));
        deployer.setShouldDeployExpanded(DeployableType.WAR, false);
        Assertions.assertFalse(deployer.shouldDeployExpanded(DeployableType.WAR));
        deployer.setShouldDeployExpanded(DeployableType.WAR, true);
        Assertions.assertTrue(deployer.shouldDeployExpanded(DeployableType.WAR));
    }

    /**
     * Test whether two WARs can be deployed to the same context.
     * @throws Exception If anything goes wrong.
     */
    @Test
    public void testCanBeDeployedWhenTwoWARsInSameWebContext() throws Exception
    {
        AbstractCopyingInstalledLocalDeployer deployer = new TestableCopyingDeployer(
            createContainer(createContainerCapability(DeployableType.WAR), null));

        // Create WARs and make sure the wrapped files exist
        WAR war1 = new WAR("ram:///path1/warfile.war");
        this.fsManager.resolveFile(war1.getFile()).createFile();
        WAR war2 = new WAR("ram:///path2/warfile.war");
        this.fsManager.resolveFile(war2.getFile()).createFile();

        // Deploy the first WAR
        deployer.deploy(war1);
        Assertions.assertTrue(this.fsManager.resolveFile("ram:///webapps/warfile.war").exists());

        try
        {
            // Deploy the second WAR using the same context as the already deployed WAR
            deployer.deploy(war2);
            Assertions.fail(
                "Expected ContainerException because we deployed two WARs with the same context "
                    + "name.");
        }
        catch (ContainerException expected)
        {
            Assertions.assertEquals(
                "Failed to deploy [ram:///path2/warfile.war] to [ram:///webapps]. The "
                    + "required web context is already in use by another application.",
                        expected.getMessage());
        }
    }

    /**
     * Test deployment when the container does not support a given deployable type.
     */
    @Test
    public void testDeployWhenContainerDoesNotSupportDeployableType()
    {
        AbstractCopyingInstalledLocalDeployer deployer = new TestableCopyingDeployer(
            createContainer(createContainerCapability(DeployableType.EAR), null));

        try
        {
            deployer.deploy(new WAR("dummy"));
            Assertions.fail("Should have thrown a ContainerException here");
        }
        catch (ContainerException expected)
        {
            Assertions.assertEquals(
                "WAR archives are not supported for deployment in [mycontainer]. "
                    + "Got [dummy]", expected.getMessage());
        }
    }

    /**
     * Test deployment of a WAR in a custom context.
     * @throws Exception If anything goes wrong.
     */
    @Test
    public void testDeployWhenWarWithCustomContext() throws Exception
    {
        WAR war = new WAR("ram:///some/warfile.war");
        this.fsManager.resolveFile("ram:///some/warfile.war").createFile();

        war.setContext("context");

        AbstractCopyingInstalledLocalDeployer deployer = new TestableCopyingDeployer(
            createContainer(createContainerCapability(DeployableType.WAR), null));

        Assertions.assertFalse(this.fsManager.resolveFile("ram:///webapps/context.war").exists());
        deployer.deploy(war);
        Assertions.assertTrue(this.fsManager.resolveFile("ram:///webapps/context.war").exists());
    }

    /**
     * Test deployment of a WAR in its default context.
     * @throws Exception If anything goes wrong.
     */
    @Test
    public void testDeployWhenWarWithDefaultContext() throws Exception
    {
        WAR war = new WAR("ram:///some/warfile.war");
        this.fsManager.resolveFile(war.getFile()).createFile();

        AbstractCopyingInstalledLocalDeployer deployer = new TestableCopyingDeployer(
            createContainer(createContainerCapability(DeployableType.WAR), null));

        Assertions.assertFalse(this.fsManager.resolveFile("ram:///webapps/warfile.war").exists());
        deployer.deploy(war);
        Assertions.assertTrue(this.fsManager.resolveFile("ram:///webapps/warfile.war").exists());
    }

    /**
     * Test deployment of an expanded WAR in a custom context.
     * @throws Exception If anything goes wrong.
     */
    @Test
    public void testDeployWhenExpandedWarWithCustomContext() throws Exception
    {
        // Create an expanded WAR
        WAR war = new WAR("ram:///some/expanded/warfile");
        war.setContext("context");
        war.setFileHandler(this.fileHandler);
        this.fsManager.resolveFile(war.getFile()).createFolder();

        AbstractCopyingInstalledLocalDeployer deployer = new TestableCopyingDeployer(
            createContainer(createContainerCapability(DeployableType.WAR), null));

        Assertions.assertFalse(this.fsManager.resolveFile("ram:///webapps/context").exists());
        deployer.deploy(war);
        Assertions.assertTrue(this.fsManager.resolveFile("ram:///webapps/context").exists());
    }

    /**
     * Test deployment of an expanded WAR in a custom context.
     * @throws Exception If anything goes wrong.
     */
    @Test
    public void testDeployToNonExistingDirectory() throws Exception
    {
        WAR war = new WAR("ram:///some/warfile.war");
        this.fsManager.resolveFile(war.getFile()).createFile();

        AbstractCopyingInstalledLocalDeployer deployer =
            new TestableCopyingDeployerWithDifferentDirectory(
                createContainer(createContainerCapability(DeployableType.WAR), null));

        try
        {
            deployer.deploy(war);
            Assertions.fail("Should have thrown a CargoException here");
        }
        catch (CargoException expected)
        {
            Assertions.assertTrue(
                expected.getMessage().contains("ram:///webapps-nonexisting"),
                    "Incorrect message: " + expected.getMessage());
        }
    }

    /**
     * Test deployment when the container configuration does not exist (see CARGO-1131).
     */
    @Test
    public void testDeployWhenContainerConfigurationDoesNotExist()
    {
        try
        {
            new TestableCopyingDeployer(createContainer(
                createContainerCapability(DeployableType.WAR), "non-existing"));
            Assertions.fail("Should have thrown a CargoException here");
        }
        catch (CargoException expected)
        {
            Assertions.assertTrue(
                expected.getMessage().contains("ram:///non-existing"),
                    "Incorrect message: " + expected.getMessage());
        }
    }

    /**
     * Create mock container capability.
     * @param type Deployable type.
     * @return Mock container capability for given deployable type.
     */
    private ContainerCapability createContainerCapability(DeployableType type)
    {
        ContainerCapability mockContainerCapability = Mockito.mock(ContainerCapability.class);
        Mockito.when(mockContainerCapability.supportsDeployableType(
            Mockito.eq(type))).thenReturn(true);
        Mockito.when(mockContainerCapability.supportsDeployableType(
            AdditionalMatchers.not(Mockito.eq(type)))).thenReturn(false);
        return mockContainerCapability;
    }

    /**
     * Create mock container.
     * @param capability Container capability.
     * @param home Container home.
     * @return Mock container for given capability.
     */
    private InstalledLocalContainer createContainer(ContainerCapability capability, String home)
    {
        String homeString;
        if (home == null)
        {
            homeString = "";
        }
        else
        {
            homeString = home;
        }

        LocalConfiguration mockConfiguration = Mockito.mock(LocalConfiguration.class);
        Mockito.when(mockConfiguration.getHome()).thenReturn("ram:///" + homeString);

        InstalledLocalContainer mockContainer = Mockito.mock(InstalledLocalContainer.class);
        Mockito.when(mockContainer.getConfiguration()).thenReturn(mockConfiguration);
        Mockito.when(mockContainer.getCapability()).thenReturn(capability);
        Mockito.when(mockContainer.getFileHandler()).thenReturn(this.fileHandler);
        Mockito.when(mockContainer.getLogger()).thenReturn(new NullLogger());
        Mockito.when(mockContainer.getId()).thenReturn("mycontainer");

        return mockContainer;
    }
}
