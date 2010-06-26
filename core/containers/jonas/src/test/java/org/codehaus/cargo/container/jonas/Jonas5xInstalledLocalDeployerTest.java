/* 
 * ========================================================================
 * 
 * Copyright 2007-2008 OW2.
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
import org.codehaus.cargo.generic.deployable.DefaultDeployableFactory;
import org.codehaus.cargo.generic.deployable.DeployableFactory;
import org.codehaus.cargo.util.FileHandler;
import org.codehaus.cargo.util.VFSFileHandler;
import org.jmock.cglib.MockObjectTestCase;

/**
 * Unit tests for {@link Jonas5xInstalledLocalDeployer}.
 */
public class Jonas5xInstalledLocalDeployerTest extends MockObjectTestCase
{

    private static final String JONAS_ROOT = "ram:///jonasroot";

    private static final String JONAS_BASE = "ram:///jonasbase";

    private Jonas5xInstalledLocalDeployer deployer;

    private StandardFileSystemManager fsManager;

    private FileHandler fileHandler;

    private DeployableFactory factory;

    /**
     * {@inheritDoc}
     * 
     * @see junit.framework.TestCase#setUp()
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

        Jonas5xInstalledLocalContainer container = new Jonas5xInstalledLocalContainer(configuration);
        container.setFileHandler(this.fileHandler);
        container.setHome(JONAS_ROOT);

        deployer = new Jonas5xInstalledLocalDeployer(container, this.fileHandler);

        factory = new DefaultDeployableFactory();
    }

    public void testGetDeployableDir()
    {
        assertEquals(JONAS_BASE + "/deploy", deployer.getDeployableDir());
    }

    public void testDeployEJBJar()
    {
        this.fileHandler.createFile("ram:///test.jar");
        EJB ejb = (EJB) factory.createDeployable("jonas5x", "ram:///test.jar", DeployableType.EJB);

        deployer.deployEjb(deployer.getDeployableDir(), ejb);
        assertTrue(fileHandler.exists(deployer.getDeployableDir() + "/test.jar"));
    }

    public void testDeployEar()
    {
        this.fileHandler.createFile("ram:///test.ear");
        EAR ear = (EAR) factory.createDeployable("jonas5x", "ram:///test.ear", DeployableType.EAR);

        deployer.deployEar(deployer.getDeployableDir(), ear);
        assertTrue(fileHandler.exists(deployer.getDeployableDir() + "/test.ear"));
    }

    public void testDeployWar()
    {
        this.fileHandler.createFile("ram:///test.war");
        WAR war = (WAR) factory.createDeployable("jonas5x", "ram:///test.war", DeployableType.WAR);

        deployer.deployWar(deployer.getDeployableDir(), war);
        assertTrue(fileHandler.exists(deployer.getDeployableDir() + "/test.war"));
    }

    public void testDeployRar()
    {
        this.fileHandler.createFile("ram:///test.rar");
        RAR rar = (RAR) factory.createDeployable("jonas5x", "ram:///test.rar", DeployableType.RAR);

        deployer.deployRar(deployer.getDeployableDir(), rar);
        assertTrue(fileHandler.exists(deployer.getDeployableDir() + "/test.rar"));
    }
}
