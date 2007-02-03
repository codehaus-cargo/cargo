/*
 * ========================================================================
 *
 * Copyright 2006 Vincent Massol.
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
package org.codehaus.cargo.container.jboss;

import org.jmock.MockObjectTestCase;
import org.jmock.Mock;
import org.codehaus.cargo.container.RemoteContainer;
import org.codehaus.cargo.container.jboss.internal.HttpURLConnection;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.configuration.RuntimeConfiguration;

import java.io.File;

/**
 * Unit tests for {@link JBossRemoteDeployer}.
 *
 * @version $Id$
 */
public class JBossRemoteDeployerTest extends MockObjectTestCase
{
    public void testCreateJBossRemoteURLForDeploy()
    {
        Mock mockConfiguration = mock(RuntimeConfiguration.class);
        mockConfiguration.stubs().method("getPropertyValue").with(eq("cargo.protocol"))
            .will(returnValue("http"));
        mockConfiguration.stubs().method("getPropertyValue").with(eq("cargo.hostname"))
            .will(returnValue("localhost"));
        mockConfiguration.stubs().method("getPropertyValue").with(eq("cargo.servlet.port"))
            .will(returnValue("8888"));
        mockConfiguration.stubs().method("getPropertyValue").with(eq("cargo.remote.username"))
            .will(returnValue("john"));
        mockConfiguration.stubs().method("getPropertyValue").with(eq("cargo.remote.password"))
            .will(returnValue("doe"));

        Mock mockContainer = mock(RemoteContainer.class);
        mockContainer.stubs().method("getConfiguration")
            .will(returnValue(mockConfiguration.proxy()));

        Mock mockDeployable = mock(Deployable.class);
        mockDeployable.stubs().method("getFile").will(
            returnValue("c:/Something With Space/dummy.war"));

        Mock mockConnection = mock(HttpURLConnection.class);
        String expectedURLPortion1 = "http://localhost:8888/";
        String expectedURLPortion2 = "Something+With+Space";
        mockConnection.expects(once()).method("connect")
            .with(and(stringContains(expectedURLPortion1), stringContains(expectedURLPortion2)),
                eq("john"), eq("doe"));

        JBossRemoteDeployer deployer = new JBossRemoteDeployer((RemoteContainer) mockContainer.proxy(),
            (HttpURLConnection) mockConnection.proxy());
        deployer.deploy((Deployable) mockDeployable.proxy());
    }
}
