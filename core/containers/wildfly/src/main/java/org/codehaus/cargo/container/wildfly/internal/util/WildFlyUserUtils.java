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
package org.codehaus.cargo.container.wildfly.internal.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.codehaus.cargo.container.property.User;
import org.codehaus.cargo.util.CargoException;

/**
 * Utility class providing informations about users.
 */
public final class WildFlyUserUtils
{

    /**
     * Cannot instantiate this class.
     */
    private WildFlyUserUtils()
    {
    }

    /**
     * Generate the user and password line for the JBoss users properties file.
     * @param user User object.
     * @param realm Real (for example, <code>ApplicationRealm</code>)
     * @return User and password line for the JBoss users properties file.
     */
    public static String generateUserPasswordLine(User user, String realm)
    {
        MessageDigest md5;
        try
        {
            md5 = MessageDigest.getInstance("md5");
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new CargoException(
                "Cannot get the MD5 digest for generating the JBoss user properties files", e);
        }

        String toHash = user.getName() + ":" + realm + ":" + user.getPassword();
        byte[] hash = md5.digest(toHash.getBytes(StandardCharsets.UTF_8));

        StringBuilder sb = new StringBuilder();
        sb.append(user.getName());
        sb.append("=");
        for (byte hashByte : hash)
        {
            sb.append(String.format("%02x", hashByte));
        }
        sb.append('\n');
        return sb.toString();
    }
}
