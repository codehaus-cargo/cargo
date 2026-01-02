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
package org.codehaus.cargo.daemon;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link PasswordWithHash}.
 */
public class PasswordWithHashTest
{
    /**
     * Test a plain password.
     * @throws Exception If anything goes wrong.
     */
    @Test
    public void testPlainPassword() throws Exception
    {
        PasswordWithHash password = new PasswordWithHash("cargo-password");
        Assertions.assertTrue(password.matches("cargo-password"));
        Assertions.assertFalse(password.matches("some-other-password"));
    }

    /**
     * Test a plain password with a curly bracket in it.
     * @throws Exception If anything goes wrong.
     */
    @Test
    public void testPlainPasswordWithCurlyBrackets() throws Exception
    {
        PasswordWithHash password = new PasswordWithHash("{cargo-password");
        Assertions.assertTrue(password.matches("{cargo-password"));
        Assertions.assertFalse(password.matches("some-other-password"));
    }

    /**
     * Test a md5-hashed password.
     * @throws Exception If anything goes wrong.
     */
    @Test
    public void testMd5Password() throws Exception
    {
        PasswordWithHash password = new PasswordWithHash("{MD5}9addb63b65b01292700094b0ef056036");
        Assertions.assertTrue(password.matches("cargo-password"));
        Assertions.assertFalse(password.matches("some-other-password"));
    }

    /**
     * Test a sha1-hashed password.
     * @throws Exception If anything goes wrong.
     */
    @Test
    public void testSha1Password() throws Exception
    {
        PasswordWithHash password =
            new PasswordWithHash("{SHA-1}2681c738294805939045be2a4af53b687c25bf4d");
        Assertions.assertTrue(password.matches("cargo-password"));
        Assertions.assertFalse(password.matches("some-other-password"));
    }
}
