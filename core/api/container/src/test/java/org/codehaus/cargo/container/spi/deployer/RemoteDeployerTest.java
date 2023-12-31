/*
 * ========================================================================
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
package org.codehaus.cargo.container.spi.deployer;

import junit.framework.TestCase;

import org.codehaus.cargo.container.RemoteContainer;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.deployer.DeployableMonitor;
import org.codehaus.cargo.container.deployer.DeployableMonitorListener;
import org.codehaus.cargo.util.log.LoggedObject;
import org.codehaus.cargo.util.log.NullLogger;

import org.mockito.Mockito;

/**
 * Unit tests for {@link AbstractRemoteDeployer}.
 */
public class RemoteDeployerTest extends TestCase
{

    /**
     * Mock {@link AbstractRemoteDeployer} implementation.
     */
    private class TestableAbstractRemoteDeployer extends AbstractRemoteDeployer
    {
        /**
         * @param container the remote container into which to perform deployment operations
         */
        public TestableAbstractRemoteDeployer(RemoteContainer container)
        {
            super(container);
        }

        /**
         * Doesn't do anything. {@inheritDoc}
         * @param deployable Ignored.
         */
        @Override
        public void deploy(Deployable deployable)
        {
            // This ensures we don't perform any real deployment - This is for testing
        }
    }

    /**
     * Mock {@link DeployableMonitor} implementation.
     */
    private class DeployableMonitorStub extends LoggedObject implements DeployableMonitor
    {
        /**
         * Deployable monitor listener.
         */
        private DeployableMonitorListener listener;

        /**
         * Deployable name.
         */
        private String deployableName;

        /**
         * Saves the deployable name.
         * @param deployableName Deployable name.
         */
        public DeployableMonitorStub(String deployableName)
        {
            this.deployableName = deployableName;
        }

        /**
         * Saves the listener.
         * @param listener Listener to save.
         */
        @Override
        public void registerListener(DeployableMonitorListener listener)
        {
            this.listener = listener;
        }

        /**
         * Calls {@link DeployableMonitorListener#deployed()}
         */
        @Override
        public void monitor()
        {
            this.listener.deployed();
        }

        /**
         * {@inheritDoc}
         * @return <code>20000</code>.
         */
        @Override
        public long getTimeout()
        {
            return 20000L;
        }

        /**
         * {@inheritDoc}
         * @return The deployable name.
         */
        @Override
        public String getDeployableName()
        {
            return this.deployableName;
        }
    }

    /**
     * Test if the <code>deploy</code> method can be called.
     */
    public void testDeployMethodWithDeployableMonitorParameterCanBeCalled()
    {
        TestableAbstractRemoteDeployer deployer = new TestableAbstractRemoteDeployer(
            createContainer());
        deployer.deploy(new WAR("some/file"), new DeployableMonitorStub("some/file"));
    }

    /**
     * Create mock container.
     * @return Mock container.
     */
    private RemoteContainer createContainer()
    {
        RemoteContainer mockContainer = Mockito.mock(RemoteContainer.class);
        Mockito.when(mockContainer.getLogger()).thenReturn(new NullLogger());
        return mockContainer;
    }
}
