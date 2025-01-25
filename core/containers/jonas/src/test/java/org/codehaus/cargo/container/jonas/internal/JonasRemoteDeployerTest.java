/*
 * ========================================================================
 *
 * Copyright 2007-2008 OW2. Code from this file
 * was originally imported from the OW2 JOnAS project.
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
package org.codehaus.cargo.container.jonas.internal;

import javax.management.MalformedObjectNameException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.codehaus.cargo.container.RemoteContainer;
import org.codehaus.cargo.container.configuration.RuntimeConfiguration;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.jonas.Jonas4xRemoteContainer;
import org.codehaus.cargo.container.jonas.Jonas4xRuntimeConfiguration;
import org.codehaus.cargo.container.jonas.JonasPropertySet;
import org.codehaus.cargo.container.jonas.internal.AbstractJonasRemoteDeployer.RemoteDeployerConfig;
import org.codehaus.cargo.container.spi.AbstractRemoteContainer;
import org.codehaus.cargo.generic.deployable.DefaultDeployableFactory;
import org.codehaus.cargo.generic.deployable.DeployableFactory;

/**
 * Unit tests for {@link AbstractJonasRemoteDeployer}.
 */
public class JonasRemoteDeployerTest
{
    /**
     * Runtime configuration.
     */
    private RuntimeConfiguration runtime;

    /**
     * Remote container.
     */
    private RemoteContainer container;

    /**
     * Deployer.
     */
    private AbstractJonasRemoteDeployer deployer;

    /**
     * Creates the test file container and deployer.
     */
    @BeforeEach
    protected void setUp()
    {
        runtime = new Jonas4xRuntimeConfiguration();
        container = new Jonas4xRemoteContainer(runtime);
        deployer = new TestDeployer(container);
    }

    /**
     * Test remote file name getter.
     */
    @Test
    public void testGetRemoteFileName()
    {
        DeployableFactory factory = new DefaultDeployableFactory();

        Deployable deployable = factory.createDeployable("jonas4x", "/foo/bar.war",
            DeployableType.WAR);

        Assertions.assertEquals(
            "foo.war", deployer.getRemoteFileName(deployable, "foo.pipo", false));
        Assertions.assertEquals("foo.war", deployer.getRemoteFileName(deployable, "foo", false));
        Assertions.assertEquals("bar.war", deployer.getRemoteFileName(deployable, null, false));

        deployable = factory.createDeployable("jonas4x", "/foo/bar.war", DeployableType.WAR);

        ((WAR) deployable).setContext("/testContext");
        Assertions.assertEquals(
            "testContext.war", deployer.getRemoteFileName(deployable, null, false));

        ((WAR) deployable).setContext("/");
        Assertions.assertEquals(
            "rootContext.war", deployer.getRemoteFileName(deployable, null, false));
    }

    /**
     * Test the server MBean getter.
     */
    @Test
    public void testGetServerMBeanName()
    {
        try
        {
            String objectName = deployer.getServerMBeanName("foo", "bar").toString();
            Assertions.assertEquals("foo:j2eeType=J2EEServer,name=bar", objectName);
        }
        catch (MalformedObjectNameException e)
        {
            Assertions.fail("No error should be thrown");
        }
        try
        {
            deployer.getServerMBeanName(null, "bar").toString();
            Assertions.fail("error should be thrown");
        }
        catch (MalformedObjectNameException expected)
        {
            // Expected
        }
        try
        {
            deployer.getServerMBeanName("", "bar").toString();
            Assertions.fail("error should be thrown");
        }
        catch (MalformedObjectNameException expected)
        {
            // Expected
        }
    }

    /**
     * Test the domain MBean getter.
     */
    @Test
    public void testGetDomainMBeanName()
    {
        try
        {
            String objectName = deployer.getDomainMBeanName("foo").toString();
            Assertions.assertEquals("foo:j2eeType=J2EEDomain,name=foo", objectName);
        }
        catch (MalformedObjectNameException e)
        {
            Assertions.fail("No error should be thrown");
        }
        try
        {
            deployer.getDomainMBeanName(null).toString();
            Assertions.fail("error should be thrown");
        }
        catch (MalformedObjectNameException expected)
        {
            // Expected
        }
        try
        {
            deployer.getDomainMBeanName("").toString();
            Assertions.fail("error should be thrown");
        }
        catch (MalformedObjectNameException expected)
        {
            // Expected
        }
    }

    /**
     * Test runtime configuration's default values.
     */
    @Test
    public void testRuntimeConfigurationDefaultValues()
    {
        RuntimeConfiguration runtimeConfiguration = new Jonas4xRuntimeConfiguration();

        AbstractRemoteContainer remoteContainer = new Jonas4xRemoteContainer(runtimeConfiguration);
        AbstractJonasRemoteDeployer remoteDeployer = new TestDeployer(remoteContainer);
        RemoteDeployerConfig deployerConfig = remoteDeployer.getConfig();

        Assertions.assertEquals("jonas", deployerConfig.getServerName());
        Assertions.assertEquals("jonas", deployerConfig.getDomainName());
        Assertions.assertNull(deployerConfig.getClusterName());
    }

    /**
     * Test runtime configuration's server name.
     */
    @Test
    public void testRuntimeConfigurationServerName()
    {
        RuntimeConfiguration runtimeConfiguration = new Jonas4xRuntimeConfiguration();
        runtimeConfiguration.setProperty(JonasPropertySet.JONAS_SERVER_NAME, "foo");

        AbstractRemoteContainer remoteContainer = new Jonas4xRemoteContainer(runtimeConfiguration);
        AbstractJonasRemoteDeployer remoteDeployer = new TestDeployer(remoteContainer);
        RemoteDeployerConfig deployerConfig = remoteDeployer.getConfig();

        Assertions.assertEquals("foo", deployerConfig.getServerName());
        Assertions.assertEquals("jonas", deployerConfig.getDomainName());
        Assertions.assertNull(deployerConfig.getClusterName());
    }

    /**
     * Test runtime configuration's domain name.
     */
    @Test
    public void testRuntimeConfigurationDomainName()
    {
        RuntimeConfiguration runtimeConfiguration = new Jonas4xRuntimeConfiguration();
        runtimeConfiguration.setProperty(JonasPropertySet.JONAS_SERVER_NAME, "foo");
        runtimeConfiguration.setProperty(JonasPropertySet.JONAS_DOMAIN_NAME, "bar");

        AbstractRemoteContainer remoteContainer = new Jonas4xRemoteContainer(runtimeConfiguration);
        AbstractJonasRemoteDeployer remoteDeployer = new TestDeployer(remoteContainer);
        RemoteDeployerConfig deployerConfig = remoteDeployer.getConfig();

        Assertions.assertEquals("foo", deployerConfig.getServerName());
        Assertions.assertEquals("bar", deployerConfig.getDomainName());
        Assertions.assertNull(deployerConfig.getClusterName());
    }

    /**
     * Test runtime configuration's cluster name.
     */
    @Test
    public void testRuntimeConfigurationClusterName()
    {
        RuntimeConfiguration runtimeConfiguration = new Jonas4xRuntimeConfiguration();
        runtimeConfiguration.setProperty(JonasPropertySet.JONAS_SERVER_NAME, "foo");
        runtimeConfiguration.setProperty(JonasPropertySet.JONAS_DOMAIN_NAME, "bar");
        runtimeConfiguration.setProperty(JonasPropertySet.JONAS_CLUSTER_NAME, "jar");

        AbstractRemoteContainer remoteContainer = new Jonas4xRemoteContainer(runtimeConfiguration);
        AbstractJonasRemoteDeployer remoteDeployer = new TestDeployer(remoteContainer);
        RemoteDeployerConfig deployerConfig = remoteDeployer.getConfig();

        Assertions.assertEquals("foo", deployerConfig.getServerName());
        Assertions.assertEquals("bar", deployerConfig.getDomainName());
        Assertions.assertEquals("jar", deployerConfig.getClusterName());
    }

    /**
     * Mock deployer for {@link AbstractJonas4xRemoteDeployer}.
     */
    private class TestDeployer extends AbstractJonas4xRemoteDeployer
    {
        /**
         * {@inheritDoc}
         * @param container Container to use.
         */
        public TestDeployer(RemoteContainer container)
        {
            super(container);
        }

        /**
         * {@inheritDoc}
         * @return <code>null</code>.
         */
        @Override
        public MBeanServerConnectionFactory getMBeanServerConnectionFactory()
        {
            return null;
        }
    }
}
