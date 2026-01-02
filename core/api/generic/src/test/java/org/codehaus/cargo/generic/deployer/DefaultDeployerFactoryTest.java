/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2026 Ali Tokmen.
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
package org.codehaus.cargo.generic.deployer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.deployer.Deployer;
import org.codehaus.cargo.container.deployer.DeployerType;
import org.codehaus.cargo.container.stub.EmbeddedLocalContainerStub;
import org.codehaus.cargo.container.stub.EmbeddedLocalDeployerStub;
import org.codehaus.cargo.container.stub.InstalledLocalContainerStub;
import org.codehaus.cargo.container.stub.InstalledLocalDeployerStub;
import org.codehaus.cargo.container.stub.RemoteContainerStub;
import org.codehaus.cargo.container.stub.RemoteDeployerStub;

/**
 * Unit tests for {@link DefaultDeployerFactory}.
 */
public class DefaultDeployerFactoryTest
{
    /**
     * Deployer factory.
     */
    private DeployerFactory factory;

    /**
     * Creates the test deployer.
     */
    @BeforeEach
    public void setUp()
    {
        this.factory = new DefaultDeployerFactory();
    }

    /**
     * Test deployer creation when no deployer registered.
     */
    @Test
    public void testCreateDeployerWhenNoDeployerRegistered()
    {
        try
        {
            this.factory.createDeployer(new InstalledLocalContainerStub());
            Assertions.fail("Expected ContainerException because there's no registered deployer");
        }
        catch (ContainerException expected)
        {
            Assertions.assertEquals(
                "There's no registered deployer matching your container's type of "
                    + "[installed]", expected.getMessage());
        }
    }

    /**
     * Test deployer creation for installed containers.
     */
    @Test
    public void testCreateDeployerForInstalledContainers()
    {
        this.factory.registerDeployer(InstalledLocalContainerStub.ID, DeployerType.INSTALLED,
            InstalledLocalDeployerStub.class);

        Deployer deployer = this.factory.createDeployer(new InstalledLocalContainerStub());
        Assertions.assertEquals(
            InstalledLocalDeployerStub.class.getName(), deployer.getClass().getName());
        Assertions.assertEquals(DeployerType.INSTALLED, deployer.getType());
    }

    /**
     * Test deployer creation for embedded containers.
     */
    @Test
    public void testCreateDeployerForEmbeddedContainers()
    {
        this.factory.registerDeployer(EmbeddedLocalContainerStub.ID, DeployerType.EMBEDDED,
            EmbeddedLocalDeployerStub.class);

        Deployer deployer = this.factory.createDeployer(new EmbeddedLocalContainerStub());
        Assertions.assertEquals(
            EmbeddedLocalDeployerStub.class.getName(), deployer.getClass().getName());
        Assertions.assertEquals(DeployerType.EMBEDDED, deployer.getType());
    }

    /**
     * Test deployer creation for remote containers.
     */
    @Test
    public void testCreateDeployerForRemoteContainers()
    {
        this.factory.registerDeployer(RemoteContainerStub.ID, DeployerType.REMOTE,
            RemoteDeployerStub.class);

        Deployer deployer = this.factory.createDeployer(new RemoteContainerStub());
        Assertions.assertEquals(RemoteDeployerStub.class.getName(), deployer.getClass().getName());
        Assertions.assertEquals(DeployerType.REMOTE, deployer.getType());
    }
}
