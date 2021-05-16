/*
 * ========================================================================
 *
 * Copyright 2007-2008 OW2. Code from this file
 * was originally imported from the OW2 JOnAS project.
 *
 * Codehaus CARGO, copyright 2004-2011 Vincent Massol, 2012-2021 Ali Tokmen.
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
import org.codehaus.cargo.container.deployable.Bundle;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.deployable.EAR;
import org.codehaus.cargo.container.deployable.EJB;
import org.codehaus.cargo.container.deployable.File;
import org.codehaus.cargo.container.deployable.RAR;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.generic.deployable.DefaultDeployableFactory;
import org.codehaus.cargo.generic.deployable.DeployableFactory;
import org.codehaus.cargo.util.FileHandler;
import org.codehaus.cargo.util.VFSFileHandler;

/**
 * Unit tests for {@link Jonas5xInstalledLocalDeployer}.
 */
public class Jonas5xInstalledLocalDeployerTest extends TestCase
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
    private Jonas5xInstalledLocalDeployer deployer;

    /**
     * File system manager.
     */
    private StandardFileSystemManager fsManager;

    /**
     * File handler.
     */
    private FileHandler fileHandler;

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

        LocalConfiguration configuration = new Jonas5xExistingLocalConfiguration(JONAS_BASE);

        Jonas5xInstalledLocalContainer container =
            new Jonas5xInstalledLocalContainer(configuration);
        container.setFileHandler(this.fileHandler);
        container.setHome(JONAS_ROOT);

        this.deployer = new Jonas5xInstalledLocalDeployer(container);

        this.factory = new DefaultDeployableFactory();

        this.fileHandler.createDirectory(JONAS_BASE, "deploy");
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
     * Test the <code>getDeployableDir</code> method.
     */
    public void testGetDeployableDir()
    {
        assertEquals(JONAS_BASE + "/deploy", deployer.getDeployableDir(null));
    }

    /**
     * Test EJB deployment.
     */
    public void testDeployEJBJar()
    {
        this.fileHandler.createFile("ram:///test.jar");
        EJB ejb = (EJB) factory.createDeployable("jonas5x", "ram:///test.jar", DeployableType.EJB);

        deployer.deploy(ejb);
        assertTrue(fileHandler.exists(deployer.getDeployableDir(ejb) + "/test.jar"));
    }

    /**
     * Test EAR deployment.
     */
    public void testDeployEar()
    {
        // EARs need to be real since they're analyzed by the deployer
        java.io.File earFile = new java.io.File("target/test-artifacts/simple-ear.ear");
        EAR ear = (EAR) factory.createDeployable("jonas5x", earFile.getAbsolutePath(),
            DeployableType.EAR);
        ear.setName("test");

        deployer.deploy(ear);
        assertTrue(fileHandler.exists(deployer.getDeployableDir(ear) + "/test.ear"));
    }

    /**
     * Test WAR deployment.
     */
    public void testDeployWar()
    {
        this.fileHandler.createFile("ram:///test.war");
        WAR war = (WAR) factory.createDeployable("jonas5x", "ram:///test.war", DeployableType.WAR);

        deployer.deploy(war);
        assertTrue(fileHandler.exists(deployer.getDeployableDir(war) + "/test.war"));
    }

    /**
     * Test RAR deployment.
     */
    public void testDeployRar()
    {
        this.fileHandler.createFile("ram:///test.rar");
        RAR rar = (RAR) factory.createDeployable("jonas5x", "ram:///test.rar", DeployableType.RAR);

        deployer.deploy(rar);
        assertTrue(fileHandler.exists(deployer.getDeployableDir(rar) + "/test.rar"));
    }

    /**
     * Test file deployment.
     */
    public void testDeployFile()
    {
        this.fileHandler.createFile("ram:///test.extension");
        File file = (File) factory.createDeployable("jonas5x", "ram:///test.extension",
            DeployableType.FILE);

        deployer.deploy(file);
        assertTrue(fileHandler.exists(deployer.getDeployableDir(file) + "/test.extension"));
    }

    /**
     * Test OSGi bundle deployment.
     */
    public void testDeployBundle()
    {
        this.fileHandler.createFile("ram:///test.jar");
        Bundle bundle = (Bundle) factory.createDeployable("jonas5x", "ram:///test.jar",
            DeployableType.BUNDLE);

        deployer.deploy(bundle);
        assertTrue(fileHandler.exists(deployer.getDeployableDir(bundle) + "/test.jar"));
    }
}
