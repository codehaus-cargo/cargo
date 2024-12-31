/*
 * ========================================================================
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
package org.codehaus.cargo.container.wildfly.internal;

import junit.framework.TestCase;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.WAR;

/**
 * Unit tests for {@link WildFlyRemoteDeploymentJsonMarshaller}.
 */
public class WildFlyRemoteDeploymentJsonMarshallerTest extends TestCase
{

    /**
     * Test that marshallDeployRequest doesn't escape slash in hash value.
     * @throws Exception If anything goes wrong.
     */
    public void testMarshallDeployRequestEscaping() throws Exception
    {
        String hash = "K47rAt/kgPKImk/K2wqCUFniIOI=";
        WildFlyRemoteDeploymentJsonMarshaller marshaller =
            new WildFlyRemoteDeploymentJsonMarshaller(null);

        Deployable deployable = new WAR("/test/path");
        String deployRequest = marshaller.marshallDeployRequest(deployable, hash);

        assertTrue("Deploy request doesn't contain correct hash!", deployRequest.contains(hash));
    }
}
