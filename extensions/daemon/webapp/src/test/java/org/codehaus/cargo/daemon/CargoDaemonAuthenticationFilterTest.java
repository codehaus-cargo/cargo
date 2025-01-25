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
package org.codehaus.cargo.daemon;

import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link CargoDaemonAuthenticationFilter}.
 */
public class CargoDaemonAuthenticationFilterTest
{
    /**
     * Tests the {@link CargoDaemonAuthenticationFilter#parsePasswordFile(java.io.InputStream)}
     * method.
     * @throws Exception If anything goes wrong.
     */
    @Test
    public void testParsePasswordFile() throws Exception
    {
        Map<String, PasswordWithHash> usernamePasswords =
            CargoDaemonAuthenticationFilter.parsePasswordFile(
                this.getClass().getResourceAsStream("/cargo-daemon-passwords-sample.properties"));

        Assertions.assertTrue(usernamePasswords.containsKey("cargo-daemon-user_"));
        Assertions.assertNull(usernamePasswords.get("cargo-daemon-user_"));

        Assertions.assertTrue(
            usernamePasswords.get("cargo-daemon-user1").matches("cargo-password"));
        Assertions.assertFalse(
            usernamePasswords.get("cargo-daemon-user1").matches("some-other-password"));

        Assertions.assertTrue(
            usernamePasswords.get("cargo-daemon-user0").matches("{cargo-password"));
        Assertions.assertFalse(
            usernamePasswords.get("cargo-daemon-user0").matches("some-other-password"));

        Assertions.assertTrue(
            usernamePasswords.get("cargo-daemon-user2").matches("cargo-password"));
        Assertions.assertFalse(
            usernamePasswords.get("cargo-daemon-user2").matches("some-other-password"));

        Assertions.assertTrue(
            usernamePasswords.get("cargo-daemon-user3").matches("cargo-password"));
        Assertions.assertFalse(
            usernamePasswords.get("cargo-daemon-user3").matches("some-other-password"));

        Assertions.assertFalse(usernamePasswords.containsKey("some-other-user"));
    }
}
