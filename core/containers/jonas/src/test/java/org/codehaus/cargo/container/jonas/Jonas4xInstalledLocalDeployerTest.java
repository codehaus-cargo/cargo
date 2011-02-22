/*
 * ========================================================================
 *
 * Copyright 2007-2008 OW2. Code from this file
 * was originally imported from the OW2 JOnAS project.
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
package org.codehaus.cargo.container.jonas;

import org.apache.commons.vfs.impl.StandardFileSystemManager;
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
import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;

/**
 * Unit tests for {@link Jonas4xInstalledLocalDeployer}.
 */
public class Jonas4xInstalledLocalDeployerTest extends MockObjectTestCase
{

    private static final String JONAS_ROOT = "ram:///jonasroot";

    private static final String JONAS_BASE = "ram:///jonasbase";

    private Jonas4xInstalledLocalDeployer deployer;

    private StandardFileSystemManager fsManager;

    private FileHandler fileHandler;

    private Mock admin;

    private DeployableFactory factory;

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

        this.fileHandler.createDirectory(null, JONAS_ROOT);
        this.fileHandler.createDirectory(null, JONAS_BASE);

        LocalConfiguration configuration = new Jonas4xExistingLocalConfiguration(JONAS_BASE);

        Jonas4xInstalledLocalContainer container = new Jonas4xInstalledLocalContainer(configuration);
        container.setFileHandler(this.fileHandler);
        container.setHome(JONAS_ROOT);

        admin = mock(Jonas4xAdmin.class);

        deployer = new Jonas4xInstalledLocalDeployer(container, (Jonas4xAdmin) admin.proxy(),
            this.fileHandler);

        factory = new DefaultDeployableFactory();
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

    private void setupAdminHotDeployment()
    {
        admin.reset();
        admin.stubs().method("isServerRunning").will(returnValue(true));
        admin.stubs().method("deploy").withAnyArguments().will(returnValue(true));
    }

    private void setupAdminHotDeploymentFailure()
    {
        admin.reset();
        admin.stubs().method("isServerRunning").will(returnValue(true));
        admin.stubs().method("deploy").withAnyArguments().will(returnValue(false));
    }

    private void setupAdminColdDeployment()
    {
        admin.reset();
        admin.stubs().method("isServerRunning").will(returnValue(false));
    }

    public void testDeployEJBJar()
    {

        this.fileHandler.createFile("ram:///test.jar");
        EJB ejb = (EJB) factory.createDeployable("jonas4x",
            "ram:///test.jar", DeployableType.EJB);

        setupAdminColdDeployment();
        deployer.deployEjb(deployer.getDeployableDir(), ejb);
        assertTrue(fileHandler.exists(deployer.getDeployableDir() + "/ejbjars/autoload/test.jar"));

        setupAdminHotDeployment();
        deployer.deployEjb(deployer.getDeployableDir(), ejb);
        assertTrue(fileHandler.exists(deployer.getDeployableDir() + "/ejbjars/test.jar"));

        setupAdminHotDeploymentFailure();
        try
        {
            deployer.deployEjb(deployer.getDeployableDir(), ejb);
            fail("No CargoException raised");
        }
        catch (CargoException ex)
        {
        }

    }

    public void testDeployEar()
    {

        this.fileHandler.createFile("ram:///test.ear");
        EAR ear = (EAR) factory.createDeployable("jonas4x",
            "ram:///test.ear", DeployableType.EAR);

        setupAdminColdDeployment();
        deployer.deployEar(deployer.getDeployableDir(), ear);
        assertTrue(fileHandler.exists(deployer.getDeployableDir() + "/apps/autoload/test.ear"));

        setupAdminHotDeployment();
        deployer.deployEar(deployer.getDeployableDir(), ear);
        assertTrue(fileHandler.exists(deployer.getDeployableDir() + "/apps/test.ear"));

        setupAdminHotDeploymentFailure();
        try
        {
            deployer.deployEar(deployer.getDeployableDir(), ear);
            fail("No CargoException raised");
        }
        catch (CargoException ex)
        {
        }

    }

    public void testDeployWar()
    {
        this.fileHandler.createFile("ram:///test.war");
        WAR war = (WAR) factory.createDeployable("jonas4x",
            "ram:///test.war", DeployableType.WAR);
        war.setContext("testContext");

        System.gc();

        setupAdminColdDeployment();
        deployer.deployWar(deployer.getDeployableDir(), war);
        assertFalse(fileHandler.exists(deployer.getDeployableDir() + "/webapps/autoload/test.war"));
        assertTrue(fileHandler.exists(deployer.getDeployableDir()
            + "/webapps/autoload/testContext.war"));

        setupAdminHotDeployment();
        deployer.deployWar(deployer.getDeployableDir(), war);
        assertFalse(fileHandler.exists(deployer.getDeployableDir() + "/webapps/test.war"));
        assertTrue(fileHandler.exists(deployer.getDeployableDir() + "/webapps/testContext.war"));

        setupAdminHotDeploymentFailure();
        try
        {
            deployer.deployWar(deployer.getDeployableDir(), war);
            fail("No CargoException raised");
        }
        catch (CargoException ex)
        {
        }
    }

    public void testDeployExpandedWar()
    {
        this.fileHandler.createFile("ram:///testExpandedWar");
        WAR war = (WAR) factory.createDeployable("jonas4x",
            "ram:///testExpandedWar", DeployableType.WAR);
        war.setContext("testExpandedWarContext");

        setupAdminColdDeployment();
        deployer.deployExpandedWar(deployer.getDeployableDir(), war);
        assertFalse(fileHandler.exists(deployer.getDeployableDir()
            + "/webapps/autoload/testExpandedWar"));
        assertTrue(fileHandler.exists(deployer.getDeployableDir()
            + "/webapps/autoload/testExpandedWarContext"));

        setupAdminHotDeployment();
        deployer.deployExpandedWar(deployer.getDeployableDir(), war);
        assertFalse(fileHandler.exists(deployer.getDeployableDir() + "/webapps/testExpandedWar"));
        assertFalse(fileHandler.exists(deployer.getDeployableDir()
            + "/webapps/testExpandedWarContext"));

    }

    public void testDeployRar()
    {

        this.fileHandler.createFile("ram:///test.rar");
        RAR rar =
            (RAR) factory.createDeployable("jonas4x", "ram:///test.rar",
                DeployableType.RAR);

        setupAdminColdDeployment();
        deployer.deployRar(deployer.getDeployableDir(), rar);
        assertTrue(fileHandler.exists(deployer.getDeployableDir() + "/rars/autoload/test.rar"));

        setupAdminHotDeployment();
        deployer.deployRar(deployer.getDeployableDir(), rar);
        assertTrue(fileHandler.exists(deployer.getDeployableDir() + "/rars/test.rar"));

        setupAdminHotDeploymentFailure();
        try
        {
            deployer.deployRar(deployer.getDeployableDir(), rar);
            fail("No CargoException raised");
        }
        catch (CargoException ex)
        {
        }

    }

    public void testGetDeployableDir()
    {
        assertEquals(JONAS_BASE, deployer.getDeployableDir());
    }

}
