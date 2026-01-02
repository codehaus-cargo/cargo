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
package org.codehaus.cargo.container.jetty.internal;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.property.User;
import org.codehaus.cargo.util.FileHandler;

/**
 * A utility class to assist the Jetty containers.
 */
public final class JettyUtils
{

    /**
     * Hide constructor.
     */
    private JettyUtils()
    {
        // Nothing
    }

    /**
     * Gets the extra classpath for the WAR as a single string suitable for use within the Jetty
     * {@code WebAppContext} configuration.
     * 
     * @param war The WAR being deployed, must not be {@code null}.
     * @param xml {@code true} to escape XML markup in the result, {@code false} to return a plain
     *            string.
     * @return The WAR's extra classpath or {@code null} if none.
     */
    public static String getExtraClasspath(WAR war, boolean xml)
    {
        String[] extraClasspath = war.getExtraClasspath();
        if (extraClasspath == null || extraClasspath.length <= 0)
        {
            return null;
        }
        String result = String.join(";", extraClasspath);
        if (xml)
        {
            result = result.replace("&", "&amp;");
        }
        return result;
    }

    /**
     * Create realm (user, password and role) file.
     * @param users Users for which to create the file.
     * @param etcDir The <code>etc</code> directory of the configuration.
     * @param fileHandler File handler for writing the file.
     */
    public static void createRealmFile(List<User> users, String etcDir, FileHandler fileHandler)
    {
        // HashLoginService syntax is as follows:
        // username: password[,rolename ...]
        StringBuilder sb = new StringBuilder();
        for (User user : users)
        {
            sb.append(user.getName());
            sb.append(": ");
            sb.append(user.getPassword().replace("\\", "\\\\"));
            for (String role : user.getRoles())
            {
                sb.append(',').append(role);
            }
            sb.append("\n");
        }
        fileHandler.writeTextFile(fileHandler.append(etcDir, "cargo-realm.properties"),
            sb.toString(), StandardCharsets.ISO_8859_1);
    }

}
