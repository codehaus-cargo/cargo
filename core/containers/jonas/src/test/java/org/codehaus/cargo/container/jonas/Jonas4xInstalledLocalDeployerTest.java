/*
 * ========================================================================
 *
 * Copyright 2007-2008 OW2. Code from this file
 * was originally imported from the OW2 JOnAS project.
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2024 Ali Tokmen.
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

import junit.framework.TestCase;

import org.apache.commons.vfs2.impl.StandardFileSystemManager;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.deployable.EAR;
import org.codehaus.cargo.container.deployable.EJB;
import org.codehaus.cargo.container.deployable.RAR;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.jonas.internal.Jonas4xAdmin;
import org.codehaus.cargo.generic.deployable.DefaultDeployableFactory;
import org.codehaus.cargo.generic.deployable.DeployableFactory;
import org.codehaus.cargo.util.CargoException;
import org.codehaus.cargo.util.FileHandler;
import org.codehaus.cargo.util.VFSFileHandler;
import org.mockito.Mockito;

/**
 * Unit tests for {@link Jonas4xInstalledLocalDeployer}.
 */
public class Jonas4xInstalledLocalDeployerTest extends TestCase
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
     * Deployer.
     */
    private Jonas4xInstalledLocalDeployer deployer;

    /**
     * File system manager.
     */
    private StandardFileSystemManager fsManager;

    /**
     * File handler.
     */
    private FileHandler fileHandler;

    /**
     * {@link Jonas4xAdmin} mock.
     */
    private Jonas4xAdmin admin;

    /**
     * Deployable factory.
     */
    private DeployableFactory factory;

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

        this.fileHandler.createDirectory(null, JONAS_ROOT);
        this.fileHandler.createDirectory(null, JONAS_BASE);

        LocalConfiguration configuration = new Jonas4xExistingLocalConfiguration(JONAS_BASE);

        Jonas4xInstalledLocalContainer container =
            new Jonas4xInstalledLocalContainer(configuration);
        container.setFileHandler(this.fileHandler);
        container.setHome(JONAS_ROOT);

        this.admin = Mockito.mock(Jonas4xAdmin.class);
        this.deployer = new Jonas4xInstalledLocalDeployer(container, this.admin);

        this.factory = new DefaultDeployableFactory();

        this.fileHandler.createDirectory(JONAS_BASE, "apps/autoload");
        this.fileHandler.createDirectory(JONAS_BASE, "ejbjars/autoload");
        this.fileHandler.createDirectory(JONAS_BASE, "rars/autoload");
        this.fileHandler.createDirectory(JONAS_BASE, "webapps/autoload");
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
     * Test hot deployment.
     */
    private void setupAdminHotDeployment()
    {
        Mockito.reset(this.admin);
        Mockito.when(
            this.admin.isServerRunning(Mockito.anyString(), Mockito.anyInt())).thenReturn(true);
        Mockito.when(this.admin.deploy(Mockito.anyString())).thenReturn(true);
    }

    /**
     * Test hot deployment failure.
     */
    private void setupAdminHotDeploymentFailure()
    {
        Mockito.reset(this.admin);
        Mockito.when(
            this.admin.isServerRunning(Mockito.anyString(), Mockito.anyInt())).thenReturn(true);
        Mockito.when(this.admin.deploy(Mockito.anyString())).thenReturn(false);
    }

    /**
     * Test cold deployment.
     */
    private void setupAdminColdDeployment()
    {
        Mockito.reset(this.admin);
        Mockito.when(
            this.admin.isServerRunning(Mockito.anyString(), Mockito.anyInt())).thenReturn(false);
    }

    /**
     * Test EJB cold deployment.
     */
    public void testColdDeployEJBJar()
    {
        this.fileHandler.createFile("ram:///test.jar");
        EJB ejb = (EJB) factory.createDeployable("jonas4x", "ram:///test.jar", DeployableType.EJB);

        setupAdminColdDeployment();
        deployer.deploy(ejb);
        assertTrue(fileHandler.exists(deployer.getDeployableDir(ejb) + "/autoload/test.jar"));
    }

    /**
     * Test EJB hot deployment.
     */
    public void testHotDeployEJBJar()
    {
        this.fileHandler.createFile("ram:///test.jar");
        EJB ejb = (EJB) factory.createDeployable("jonas4x", "ram:///test.jar", DeployableType.EJB);

        setupAdminHotDeployment();
        deployer.deploy(ejb);
        assertTrue(fileHandler.exists(deployer.getDeployableDir(ejb) + "/test.jar"));
    }

    /**
     * Test EJB hot deployment failure.
     */
    public void testHotDeployFailureEJBJar()
    {
        this.fileHandler.createFile("ram:///test.jar");
        EJB ejb = (EJB) factory.createDeployable("jonas4x", "ram:///test.jar", DeployableType.EJB);

        setupAdminHotDeploymentFailure();
        try
        {
            deployer.deploy(ejb);
            fail("No CargoException raised");
        }
        catch (CargoException expected)
        {
            assertTrue(fileHandler.exists(deployer.getDeployableDir(ejb) + "/test.jar"));
        }
    }

    /**
     * Test EAR cold deployment.
     */
    public void testColdDeployEar()
    {
        // EARs need to be real since they're analyzed by the deployer
        java.io.File earFile = new java.io.File("target/test-artifacts/simple-ear.ear");
        EAR ear = (EAR) factory.createDeployable("jonas4x", earFile.getAbsolutePath(),
            DeployableType.EAR);
        ear.setName("test");

        setupAdminColdDeployment();
        deployer.deploy(ear);
        assertFalse(fileHandler.exists(deployer.getDeployableDir(ear) + "/simple-ear.ear"));
        assertTrue(fileHandler.exists(deployer.getDeployableDir(ear) + "/autoload/test.ear"));
    }

    /**
     * Test EAR hot deployment.
     */
    public void testHotDeployEar()
    {
        // EARs need to be real since they're analyzed by the deployer
        java.io.File earFile = new java.io.File("target/test-artifacts/simple-ear.ear");
        EAR ear = (EAR) factory.createDeployable("jonas4x", earFile.getAbsolutePath(),
            DeployableType.EAR);
        ear.setName("test");

        setupAdminHotDeployment();
        deployer.deploy(ear);
        assertFalse(fileHandler.exists(deployer.getDeployableDir(ear) + "/simple-ear.ear"));
        assertTrue(fileHandler.exists(deployer.getDeployableDir(ear) + "/test.ear"));
    }

    /**
     * Test EAR hot deployment failure.
     */
    public void testHotDeployFailureEar()
    {
        // EARs need to be real since they're analyzed by the deployer
        java.io.File earFile = new java.io.File("target/test-artifacts/simple-ear.ear");
        EAR ear = (EAR) factory.createDeployable("jonas4x", earFile.getAbsolutePath(),
            DeployableType.EAR);
        ear.setName("test");

        setupAdminHotDeploymentFailure();
        try
        {
            deployer.deploy(ear);
            fail("No CargoException raised");
        }
        catch (CargoException expected)
        {
            assertFalse(fileHandler.exists(deployer.getDeployableDir(ear) + "/simple-ear.ear"));
            assertTrue(fileHandler.exists(deployer.getDeployableDir(ear) + "/test.ear"));
        }
    }

    /**
     * Test WAR cold deployment.
     */
    public void testColdDeployWar()
    {
        this.fileHandler.createFile("ram:///test.war");
        WAR war = (WAR) factory.createDeployable("jonas4x", "ram:///test.war", DeployableType.WAR);
        war.setContext("testContext");

        setupAdminColdDeployment();
        deployer.deploy(war);
        assertFalse(fileHandler.exists(deployer.getDeployableDir(war) + "/autoload/test.war"));
        assertTrue(fileHandler.exists(deployer.getDeployableDir(war)
            + "/autoload/testContext.war"));
    }

    /**
     * Test WAR hot deployment.
     */
    public void testHotDeployWar()
    {
        this.fileHandler.createFile("ram:///test.war");
        WAR war = (WAR) factory.createDeployable("jonas4x", "ram:///test.war", DeployableType.WAR);
        war.setContext("testContext");

        setupAdminHotDeployment();
        deployer.deploy(war);
        assertFalse(fileHandler.exists(deployer.getDeployableDir(war) + "/test.war"));
        assertTrue(fileHandler.exists(deployer.getDeployableDir(war) + "/testContext.war"));
    }

    /**
     * Test WAR hot deployment failure.
     */
    public void testHotDeployFailureWar()
    {
        this.fileHandler.createFile("ram:///test.war");
        WAR war = (WAR) factory.createDeployable("jonas4x", "ram:///test.war", DeployableType.WAR);
        war.setContext("testContext");

        setupAdminHotDeploymentFailure();
        try
        {
            deployer.deploy(war);
            fail("No CargoException raised");
        }
        catch (CargoException expected)
        {
            assertFalse(fileHandler.exists(deployer.getDeployableDir(war) + "/test.war"));
            assertTrue(fileHandler.exists(deployer.getDeployableDir(war) + "/testContext.war"));
        }
    }

    /**
     * Test expanded WAR cold deployment.
     */
    public void testColdDeployExpandedWar()
    {
        // Expanded WARs need to be real since they're analyzed by the archive definition
        java.io.File warFile = new java.io.File("target/test-artifacts/simple-war");
        WAR war = (WAR) factory.createDeployable("jonas4x", warFile.getAbsolutePath(),
            DeployableType.WAR);
        war.setContext("testExpandedWarContext");

        setupAdminColdDeployment();
        deployer.deploy(war);
        assertFalse(fileHandler.exists(deployer.getDeployableDir(war)
            + "/autoload/testExpandedWar"));
        assertTrue(fileHandler.exists(deployer.getDeployableDir(war)
            + "/autoload/testExpandedWarContext"));
    }

    /**
     * Test expanded WAR hot deployment.
     */
    public void testHotDeployExpandedWar()
    {
        // Expanded WARs need to be real since they're analyzed by the archive definition
        java.io.File warFile = new java.io.File("target/test-artifacts/simple-war");
        WAR war = (WAR) factory.createDeployable("jonas4x", warFile.getAbsolutePath(),
            DeployableType.WAR);
        war.setContext("testExpandedWarContext");

        setupAdminHotDeployment();
        deployer.deploy(war);
        assertFalse(fileHandler.exists(deployer.getDeployableDir(war) + "/testExpandedWar"));
        assertTrue(fileHandler.exists(deployer.getDeployableDir(war)
            + "/testExpandedWarContext"));
    }

    /**
     * Test expanded WAR hot deployment.
     */
    public void testHotDeployFailureExpandedWar()
    {
        // Expanded WARs need to be real since they're analyzed by the archive definition
        java.io.File warFile = new java.io.File("target/test-artifacts/simple-war");
        WAR war = (WAR) factory.createDeployable("jonas4x", warFile.getAbsolutePath(),
            DeployableType.WAR);
        war.setContext("testExpandedWarContext");

        setupAdminHotDeploymentFailure();
        try
        {
            deployer.deploy(war);
            fail("No CargoException raised");
        }
        catch (CargoException expected)
        {
            assertFalse(fileHandler.exists(deployer.getDeployableDir(war) + "/testExpandedWar"));
            assertTrue(fileHandler.exists(deployer.getDeployableDir(war)
                + "/testExpandedWarContext"));
        }
    }

    /**
     * Test RAR cold deployment.
     */
    public void testColdDeployRar()
    {
        this.fileHandler.createFile("ram:///test.rar");
        RAR rar = (RAR) factory.createDeployable("jonas4x", "ram:///test.rar", DeployableType.RAR);

        setupAdminColdDeployment();
        deployer.deploy(rar);
        assertTrue(fileHandler.exists(deployer.getDeployableDir(rar) + "/autoload/test.rar"));
    }

    /**
     * Test RAR hot deployment.
     */
    public void testHotDeployRar()
    {
        this.fileHandler.createFile("ram:///test.rar");
        RAR rar = (RAR) factory.createDeployable("jonas4x", "ram:///test.rar", DeployableType.RAR);

        setupAdminHotDeployment();
        deployer.deploy(rar);
        assertTrue(fileHandler.exists(deployer.getDeployableDir(rar) + "/test.rar"));
    }

    /**
     * Test RAR hot deployment failure.
     */
    public void testHotDeployFailureRar()
    {
        this.fileHandler.createFile("ram:///test.rar");
        RAR rar = (RAR) factory.createDeployable("jonas4x", "ram:///test.rar", DeployableType.RAR);

        setupAdminHotDeploymentFailure();
        try
        {
            deployer.deploy(rar);
            fail("No CargoException raised");
        }
        catch (CargoException expected)
        {
            assertTrue(fileHandler.exists(deployer.getDeployableDir(rar) + "/test.rar"));
        }
    }

    /**
     * Test the <code>getDeployableDir</code> method.
     */
    public void testGetDeployableDir()
    {
        this.fileHandler.createFile("ram:///test.jar");
        EJB ejb = (EJB) factory.createDeployable("jonas4x", "ram:///test.jar", DeployableType.EJB);
        assertEquals(JONAS_BASE + "/ejbjars", deployer.getDeployableDir(ejb));

        this.fileHandler.createFile("ram:///test.ear");
        EAR ear = (EAR) factory.createDeployable("jonas4x", "ram:///test.ear", DeployableType.EAR);
        assertEquals(JONAS_BASE + "/apps", deployer.getDeployableDir(ear));

        this.fileHandler.createFile("ram:///test.war");
        WAR war = (WAR) factory.createDeployable("jonas4x", "ram:///test.war", DeployableType.WAR);
        assertEquals(JONAS_BASE + "/webapps", deployer.getDeployableDir(war));

        this.fileHandler.createFile("ram:///test.rar");
        RAR rar = (RAR) factory.createDeployable("jonas4x", "ram:///test.rar", DeployableType.RAR);
        assertEquals(JONAS_BASE + "/rars", deployer.getDeployableDir(rar));
    }

}
