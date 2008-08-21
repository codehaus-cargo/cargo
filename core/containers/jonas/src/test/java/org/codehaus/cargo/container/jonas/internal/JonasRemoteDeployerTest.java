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
package org.codehaus.cargo.container.jonas.internal;

import javax.management.MalformedObjectNameException;

import org.codehaus.cargo.container.RemoteContainer;
import org.codehaus.cargo.container.configuration.RuntimeConfiguration;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.jonas.Jonas4xRemoteContainer;
import org.codehaus.cargo.container.jonas.JonasPropertySet;
import org.codehaus.cargo.container.jonas.JonasRuntimeConfiguration;
import org.codehaus.cargo.container.jonas.internal.AbstractJonasRemoteDeployer.RemoteDeployerConfig;
import org.codehaus.cargo.container.spi.AbstractRemoteContainer;
import org.codehaus.cargo.generic.deployable.DefaultDeployableFactory;
import org.codehaus.cargo.generic.deployable.DeployableFactory;
import org.jmock.MockObjectTestCase;

/**
 * Unit tests for {@link AbstractJonasRemoteDeployer}.
 */
public class JonasRemoteDeployerTest extends MockObjectTestCase
{
    private RuntimeConfiguration runtime;

    private RemoteContainer container;

    private AbstractJonasRemoteDeployer deployer;

    protected void setUp() throws Exception
    {
        super.setUp();

        runtime = new JonasRuntimeConfiguration();
        container = new Jonas4xRemoteContainer(runtime);
        deployer = new TestDeployer(container);
    }

    public void testGetRemoteFileName()
    {
        DeployableFactory factory = new DefaultDeployableFactory();

        Deployable deployable = factory.createDeployable("jonas4x", "/foo/bar.war",
            DeployableType.WAR);

        assertEquals("foo.war", deployer.getRemoteFileName(deployable, "foo.pipo"));
        assertEquals("foo.war", deployer.getRemoteFileName(deployable, "foo"));
        assertEquals("bar.war", deployer.getRemoteFileName(deployable, null));

        deployable = factory.createDeployable("jonas4x", "/foo/bar.war", DeployableType.WAR);

        ((WAR) deployable).setContext("/testContext");
        assertEquals("testContext.war", deployer.getRemoteFileName(deployable, null));

        ((WAR) deployable).setContext("/");
        assertEquals("rootContext.war", deployer.getRemoteFileName(deployable, null));
    }

    public void testGetServerMBeanName()
    {
        try
        {
            String objectName = deployer.getServerMBeanName("foo", "bar").toString();
            assertEquals("foo:j2eeType=J2EEServer,name=bar", objectName);
        }
        catch (MalformedObjectNameException e)
        {
            fail("No error should be thrown");
        }
        try
        {
            deployer.getServerMBeanName(null, "bar").toString();
            fail("error should be thrown");
        }
        catch (MalformedObjectNameException e)
        {
        }
        try
        {
            deployer.getServerMBeanName("", "bar").toString();
            fail("error should be thrown");
        }
        catch (MalformedObjectNameException e)
        {
        }
    }

    public void testGetDomainMBeanName()
    {
        try
        {
            String objectName = deployer.getDomainMBeanName("foo").toString();
            assertEquals("foo:j2eeType=J2EEDomain,name=foo", objectName);
        }
        catch (MalformedObjectNameException e)
        {
            fail("No error should be thrown");
        }
        try
        {
            deployer.getDomainMBeanName(null).toString();
            fail("error should be thrown");
        }
        catch (MalformedObjectNameException e)
        {
        }
        try
        {
            deployer.getDomainMBeanName("").toString();
            fail("error should be thrown");
        }
        catch (MalformedObjectNameException e)
        {
        }
    }

    public void testRuntimeConfigurationDefaultValues()
    {
        RuntimeConfiguration runtimeConfiguration = new JonasRuntimeConfiguration();

        AbstractRemoteContainer remoteContainer = new Jonas4xRemoteContainer(runtimeConfiguration);
        AbstractJonasRemoteDeployer remoteDeployer = new TestDeployer(remoteContainer);
        RemoteDeployerConfig deployerConfig = remoteDeployer.getConfig();

        assertEquals(AbstractJonasRemoteDeployer.DEFAULT_JONAS_SERVER_NAME, deployerConfig.getServerName());
        assertEquals(AbstractJonasRemoteDeployer.DEFAULT_JONAS_DOMAIN_NAME, deployerConfig.getDomainName());
        assertNull(deployerConfig.getClusterName());
    }

    public void testRuntimeConfigurationServerName()
    {
        RuntimeConfiguration runtimeConfiguration = new JonasRuntimeConfiguration();
        runtimeConfiguration.setProperty(JonasPropertySet.JONAS_SERVER_NAME, "foo");

        AbstractRemoteContainer remoteContainer = new Jonas4xRemoteContainer(runtimeConfiguration);
        AbstractJonasRemoteDeployer remoteDeployer = new TestDeployer(remoteContainer);
        RemoteDeployerConfig deployerConfig = remoteDeployer.getConfig();

        assertEquals("foo", deployerConfig.getServerName());
        assertEquals(AbstractJonasRemoteDeployer.DEFAULT_JONAS_DOMAIN_NAME, deployerConfig.getDomainName());
        assertNull(deployerConfig.getClusterName());
    }

    public void testRuntimeConfigurationDomainName()
    {
        RuntimeConfiguration runtimeConfiguration = new JonasRuntimeConfiguration();
        runtimeConfiguration.setProperty(JonasPropertySet.JONAS_SERVER_NAME, "foo");
        runtimeConfiguration.setProperty(JonasPropertySet.JONAS_DOMAIN_NAME, "bar");

        AbstractRemoteContainer remoteContainer = new Jonas4xRemoteContainer(runtimeConfiguration);
        AbstractJonasRemoteDeployer remoteDeployer = new TestDeployer(remoteContainer);
        RemoteDeployerConfig deployerConfig = remoteDeployer.getConfig();

        assertEquals("foo", deployerConfig.getServerName());
        assertEquals("bar", deployerConfig.getDomainName());
        assertNull(deployerConfig.getClusterName());
    }

    public void testRuntimeConfigurationClusterName()
    {
        RuntimeConfiguration runtimeConfiguration = new JonasRuntimeConfiguration();
        runtimeConfiguration.setProperty(JonasPropertySet.JONAS_SERVER_NAME, "foo");
        runtimeConfiguration.setProperty(JonasPropertySet.JONAS_DOMAIN_NAME, "bar");
        runtimeConfiguration.setProperty(JonasPropertySet.JONAS_CLUSTER_NAME, "jar");

        AbstractRemoteContainer remoteContainer = new Jonas4xRemoteContainer(runtimeConfiguration);
        AbstractJonasRemoteDeployer remoteDeployer = new TestDeployer(remoteContainer);
        RemoteDeployerConfig deployerConfig = remoteDeployer.getConfig();

        assertEquals("foo", deployerConfig.getServerName());
        assertEquals("bar", deployerConfig.getDomainName());
        assertEquals("jar", deployerConfig.getClusterName());
    }

    private class TestDeployer extends AbstractJonas4xRemoteDeployer
    {
        public TestDeployer(RemoteContainer container)
        {
            super(container);
        }

        public MBeanServerConnectionFactory getMBeanServerConnectionFactory()
        {
            return null;
        }
    }
}
