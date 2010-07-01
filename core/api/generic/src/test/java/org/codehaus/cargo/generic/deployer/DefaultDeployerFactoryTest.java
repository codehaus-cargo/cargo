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
package org.codehaus.cargo.generic.deployer;

import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.stub.InstalledLocalContainerStub;
import org.codehaus.cargo.container.stub.InstalledLocalDeployerStub;
import org.codehaus.cargo.container.stub.RemoteDeployerStub;
import org.codehaus.cargo.container.stub.EmbeddedLocalContainerStub;
import org.codehaus.cargo.container.stub.RemoteContainerStub;
import org.codehaus.cargo.container.stub.EmbeddedLocalDeployerStub;
import org.codehaus.cargo.container.deployer.Deployer;
import org.codehaus.cargo.container.deployer.DeployerType;
import junit.framework.TestCase;

/**
 * Unit tests for {@link DefaultDeployerFactory}.
 *
 * @version $Id$
 */
public class DefaultDeployerFactoryTest extends TestCase
{
    private DeployerFactory factory;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        this.factory = new DefaultDeployerFactory();
    }

    public void testCreateDeployerWhenNoDeployerRegistered()
    {
        try
        {
            this.factory.createDeployer(new InstalledLocalContainerStub());
            fail("Expected ContainerException because there's no registered deployer");
        }
        catch (ContainerException expected)
        {
            assertEquals("There's no registered deployer matching your container's type of "
                + "[installed]", expected.getMessage());
        }
    }

    public void testCreateDeployerForInstalledContainers()
    {
        this.factory.registerDeployer(InstalledLocalContainerStub.ID, DeployerType.INSTALLED,
            InstalledLocalDeployerStub.class);

        Deployer deployer = this.factory.createDeployer(new InstalledLocalContainerStub());
        assertEquals(InstalledLocalDeployerStub.class.getName(), deployer.getClass().getName());
        assertEquals(DeployerType.INSTALLED, deployer.getType());
    }

    public void testCreateDeployerForEmbeddedContainers()
    {
        this.factory.registerDeployer(EmbeddedLocalContainerStub.ID, DeployerType.EMBEDDED,
            EmbeddedLocalDeployerStub.class);

        Deployer deployer = this.factory.createDeployer(new EmbeddedLocalContainerStub());
        assertEquals(EmbeddedLocalDeployerStub.class.getName(), deployer.getClass().getName());
        assertEquals(DeployerType.EMBEDDED, deployer.getType());
    }

    public void testCreateDeployerForRemoteContainers()
    {
        this.factory.registerDeployer(RemoteContainerStub.ID, DeployerType.REMOTE,
            RemoteDeployerStub.class);

        Deployer deployer = this.factory.createDeployer(new RemoteContainerStub());
        assertEquals(RemoteDeployerStub.class.getName(), deployer.getClass().getName());
        assertEquals(DeployerType.REMOTE, deployer.getType());
    }
}
