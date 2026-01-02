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
package org.codehaus.cargo.container.jboss;

import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import org.codehaus.cargo.container.RemoteContainer;
import org.codehaus.cargo.container.configuration.RuntimeConfiguration;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.jboss.internal.ISimpleHttpFileServer;
import org.codehaus.cargo.container.jboss.internal.JdkHttpURLConnection;
import org.codehaus.cargo.util.log.NullLogger;
import org.mockito.InOrder;
import org.mockito.Mockito;

/**
 * Unit tests for {@link JBoss4xRemoteDeployer}.
 */
public class JBoss4xRemoteDeployerTest
{
    /**
     * Test create JBoss remote URL for deploy.
     * @throws Exception If anything goes wrong.
     */
    @Test
    public void testCreateJBossRemoteURLForDeploy() throws Exception
    {
        RuntimeConfiguration mockConfiguration = Mockito.mock(RuntimeConfiguration.class);
        Mockito.when(mockConfiguration.getPropertyValue(
            Mockito.eq("cargo.protocol"))).thenReturn("http");
        Mockito.when(mockConfiguration.getPropertyValue(
            Mockito.eq("cargo.hostname"))).thenReturn("remotehost");
        Mockito.when(mockConfiguration.getPropertyValue(
            Mockito.eq("cargo.servlet.port"))).thenReturn("8888");
        Mockito.when(mockConfiguration.getPropertyValue(
            Mockito.eq("cargo.remote.username"))).thenReturn("john");
        Mockito.when(mockConfiguration.getPropertyValue(
            Mockito.eq("cargo.remote.password"))).thenReturn("doe");
        Mockito.when(mockConfiguration.getPropertyValue(
            Mockito.eq("cargo.remote.timeout"))).thenReturn("120000");
        Mockito.when(mockConfiguration.getPropertyValue(
            Mockito.eq("cargo.remotedeploy.port"))).thenReturn("9999");
        Mockito.when(mockConfiguration.getPropertyValue(
            Mockito.eq("cargo.jboss.remotedeploy.hostname"))).thenReturn("localhost");

        RemoteContainer mockContainer = Mockito.mock(RemoteContainer.class);
        Mockito.when(mockContainer.getConfiguration()).thenReturn(mockConfiguration);
        Mockito.when(mockContainer.getLogger()).thenReturn(new NullLogger());

        Deployable mockDeployable = Mockito.mock(Deployable.class);
        Mockito.when(mockDeployable.getFile()).thenReturn("c:/Something With Space/dummy.war");

        AtomicInteger count = new AtomicInteger(0);
        String mockURL = "http://localhost:9999/Something+With+Space";
        ISimpleHttpFileServer mockHttpFileServer = Mockito.mock(ISimpleHttpFileServer.class);
        Mockito.doAnswer(invocation ->
        {
            count.incrementAndGet();
            return null;
        }
        ).when(mockHttpFileServer).start();
        Mockito.when(mockHttpFileServer.getURL()).thenReturn(new URL(mockURL));
        Mockito.when(mockHttpFileServer.getCallCount()).thenAnswer(invocation ->
        {
            return count.get();
        });

        JdkHttpURLConnection mockConnection = Mockito.mock(JdkHttpURLConnection.class);

        JBoss4xRemoteDeployer deployer = new JBoss4xRemoteDeployer(
            mockContainer, mockConnection, mockHttpFileServer);
        deployer.deploy(mockDeployable);

        InOrder orderVerifier = Mockito.inOrder(mockHttpFileServer);
        orderVerifier.verify(mockHttpFileServer).setFileHandler(Mockito.any());
        orderVerifier.verify(mockHttpFileServer).setLogger(Mockito.any());
        orderVerifier.verify(mockHttpFileServer).setFile(Mockito.any(), Mockito.any());
        orderVerifier.verify(mockHttpFileServer).setListeningParameters(
            Mockito.any(), Mockito.any());
        orderVerifier.verify(mockHttpFileServer).start();
        orderVerifier.verify(mockHttpFileServer).getURL();
        orderVerifier.verify(mockHttpFileServer).stop();

        // TODO: URLEncoder.encode(String, Charset) was introduced in Java 10,
        //       simplify the below code when Codehaus Cargo is on Java 10+
        String expectedURLPortion = URLEncoder.encode(mockURL, StandardCharsets.UTF_8.name());
        Mockito.verify(mockConnection, Mockito.times(1)).connect(
            Mockito.contains(expectedURLPortion), Mockito.eq("john"), Mockito.eq("doe"),
                Mockito.anyInt(), Mockito.any());
    }
}
