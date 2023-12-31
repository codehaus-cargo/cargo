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
package org.codehaus.cargo.daemon;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Password holder, with support for hashing.
 */
public class PasswordWithHash
{
    /**
     * Password hash, if <code>null</code> then it is plain.
     */
    private MessageDigest digest;

    /**
     * Password (could be hashed), if <code>null</code> then it is a user with an empty password.
     */
    private String password;

    /**
     * Save password, which can use hashes, for example
     * <code>{MD5}9addb63b65b01292700094b0ef056036</code> or
     * <code>{SHA-1}2681c738294805939045be2a4af53b687c25bf4d</code>. The hashing algorithms can be
     * any algorithm supported by {@link MessageDigest}.
     * 
     * @param passwordWithDigest Password (could be hashed).
     * @throws NoSuchAlgorithmException If the digest algorithm isn't known to the JVM.
     */
    public PasswordWithHash(String passwordWithDigest) throws NoSuchAlgorithmException
    {
        if (passwordWithDigest.startsWith("{"))
        {
            int digestEnd = passwordWithDigest.indexOf("}");
            if (digestEnd > 0)
            {
                digest = MessageDigest.getInstance(passwordWithDigest.substring(1, digestEnd));
                password = passwordWithDigest.substring(digestEnd + 1);
            }
        }

        if (digest == null)
        {
            // Even if the password has no hashing algorithm, hash it to avoid leaks (for example,
            // in case someone has introspection enabled on the JVM).
            digest = MessageDigest.getInstance("SHA-256");
            password = hash(passwordWithDigest);
        }
    }

    /**
     * Checks if the given password matches the stored one (or its hash).
     * 
     * @param password Password to check.
     * @return <code>true</code> if the given password matches the stored one (or its hash),
     * <code>false</code> otherwise.
     */
    public boolean matches(String password)
    {
        return this.password.equals(this.hash(password));
    }

    /**
     * Hash the given text into a string.
     * 
     * @param text Text to hash.
     * @return Hashed string.
     */
    private String hash(String text)
    {
        byte[] digest;
        synchronized (this.digest)
        {
            digest = this.digest.digest(text.getBytes(StandardCharsets.UTF_8));
        }
        BigInteger no = new BigInteger(1, digest);
        String hashtext = no.toString(16);
        while (hashtext.length() < 32)
        {
            hashtext = "0" + hashtext;
        }
        return hashtext;
    }
}
