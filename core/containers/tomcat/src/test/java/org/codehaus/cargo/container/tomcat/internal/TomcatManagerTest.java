/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2011 Vincent Massol.
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
package org.codehaus.cargo.container.tomcat.internal;

import org.codehaus.cargo.util.AbstractResourceTest;

/**
 * Unit tests for {@link TomcatManager}.
 * 
 */
public class TomcatManagerTest extends AbstractResourceTest
{
    /**
     * Test header component extraction.
     * @throws Exception If anything goes wrong.
     */
    public void testExtractHeaderComponent() throws Exception
    {
        String header = "WWW-Authenticate: Digest realm=\"testrealm@host.com\",\n"
            + "                        qop=\"auth,auth-int\",\n"
            + "                        nonce=\"dcd98b7102dd2f0e8b11d0f600bfb0c093\",\n"
            + "                        opaque=\"5ccc069c403ebaf9f0171e9517f40e41\"";

        assertEquals("testrealm@host.com", TomcatManager.extractHeaderComponent(header, "realm"));
        assertEquals("auth,auth-int", TomcatManager.extractHeaderComponent(header, "qop"));
        assertEquals("dcd98b7102dd2f0e8b11d0f600bfb0c093",
            TomcatManager.extractHeaderComponent(header, "nonce"));
        assertEquals("5ccc069c403ebaf9f0171e9517f40e41",
            TomcatManager.extractHeaderComponent(header, "opaque"));
        assertNull(TomcatManager.extractHeaderComponent(header, "nothing"));
    }
}
