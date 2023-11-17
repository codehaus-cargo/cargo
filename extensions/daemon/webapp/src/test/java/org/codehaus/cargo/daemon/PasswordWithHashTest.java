/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2023 Ali Tokmen.
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

import junit.framework.TestCase;

/**
 * Unit tests for {@link PasswordWithHash}.
 */
public class PasswordWithHashTest extends TestCase
{
    /**
     * Test a plain password.
     * @throws Exception If anything goes wrong.
     */
    public void testPlainPassword() throws Exception
    {
        PasswordWithHash password = new PasswordWithHash("cargo-password");
        assertTrue(password.matches("cargo-password"));
        assertFalse(password.matches("some-other-password"));
    }

    /**
     * Test a plain password with a curly bracket in it.
     * @throws Exception If anything goes wrong.
     */
    public void testPlainPasswordWithCurlyBrackets() throws Exception
    {
        PasswordWithHash password = new PasswordWithHash("{cargo-password");
        assertTrue(password.matches("{cargo-password"));
        assertFalse(password.matches("some-other-password"));
    }

    /**
     * Test a md5-hashed password.
     * @throws Exception If anything goes wrong.
     */
    public void testMd5Password() throws Exception
    {
        PasswordWithHash password = new PasswordWithHash("{MD5}9addb63b65b01292700094b0ef056036");
        assertTrue(password.matches("cargo-password"));
        assertFalse(password.matches("some-other-password"));
    }

    /**
     * Test a sha1-hashed password.
     * @throws Exception If anything goes wrong.
     */
    public void testSha1Password() throws Exception
    {
        PasswordWithHash password =
            new PasswordWithHash("{SHA-1}2681c738294805939045be2a4af53b687c25bf4d");
        assertTrue(password.matches("cargo-password"));
        assertFalse(password.matches("some-other-password"));
    }
}
