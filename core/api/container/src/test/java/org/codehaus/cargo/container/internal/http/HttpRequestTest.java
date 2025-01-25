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
package org.codehaus.cargo.container.internal.http;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link HttpRequest}.
 */
public class HttpRequestTest
{
    /**
     * Test character set extraction.
     * @throws Exception If anything goes wrong.
     */
    @Test
    public void testExtractCharset() throws Exception
    {
        Assertions.assertEquals(StandardCharsets.UTF_8, HttpRequest.extractCharset(null));
        Assertions.assertEquals(StandardCharsets.UTF_8,
            HttpRequest.extractCharset("text/html; charset=utf-8"));
        Assertions.assertEquals(StandardCharsets.UTF_8,
            HttpRequest.extractCharset("application/json; charset=utf-8"));
        Assertions.assertEquals(StandardCharsets.US_ASCII,
            HttpRequest.extractCharset("text/plain; charset=us-ascii"));
    }

    /**
     * Test header component extraction.
     * @throws Exception If anything goes wrong.
     */
    @Test
    public void testExtractHeaderComponent() throws Exception
    {
        String header = "WWW-Authenticate: Digest realm=\"testrealm@host.com\",\n"
            + "                        qop=\"auth,auth-int\",\n"
            + "                        nonce=\"dcd98b7102dd2f0e8b11d0f600bfb0c093\",\n"
            + "                        opaque=\"5ccc069c403ebaf9f0171e9517f40e41\"";

        Assertions.assertEquals(
            "testrealm@host.com", HttpRequest.extractHeaderComponent(header, "realm"));
        Assertions.assertEquals("auth,auth-int", HttpRequest.extractHeaderComponent(header, "qop"));
        Assertions.assertEquals("dcd98b7102dd2f0e8b11d0f600bfb0c093",
            HttpRequest.extractHeaderComponent(header, "nonce"));
        Assertions.assertEquals("5ccc069c403ebaf9f0171e9517f40e41",
            HttpRequest.extractHeaderComponent(header, "opaque"));
        Assertions.assertNull(HttpRequest.extractHeaderComponent(header, "nothing"));
    }
}
