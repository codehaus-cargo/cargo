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
package org.codehaus.cargo.container.tomcat.internal;

import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.tomcat.TomcatWAR;

/**
 * Utility methods for Tomcat.
 */
public final class TomcatUtils
{

    /**
     * Private constructor to prevent getting an instance.
     */
    private TomcatUtils()
    {
        // Utility classes have no public constructors
    }

    /**
     * Tests whether the specified deployable is a Tomcat WAR and contains a
     * {@code META-INF/context.xml} file.
     * 
     * @param deployable The deployable to test, may be {@code null}.
     * @return {@code true} if the deployable is a Tomcat WAR with a context file, {@code false}
     *         otherwise.
     */
    public static boolean containsContextFile(Deployable deployable)
    {
        if (deployable instanceof TomcatWAR)
        {
            return ((TomcatWAR) deployable).containsContextFile();
        }
        return false;
    }

    /**
     * Gets the extra classpath for the WAR as a single string suitable for use within the
     * {@code context.xml}.
     * 
     * @param war The WAR being deployed, must not be {@code null}.
     * @param xml {@code true} to escape XML markup in the result, {@code false} to return a plain
     *            string.
     * @return The WAR's extra classpath or {@code null} if none.
     */
    public static String getExtraClasspath(WAR war, boolean xml)
    {
        String[] extraClasspath = getExtraClasspath(war);
        if (extraClasspath == null)
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
     * Gets the extra classpath for the WAR as a string array
     * 
     * @param war The WAR being deployed, must not be {@code null}.
     * @return The WAR's extra classpath or {@code null} if none.
     */
    public static String[] getExtraClasspath(WAR war)
    {
        String[] extraClasspath = war.getExtraClasspath();
        if (extraClasspath == null || extraClasspath.length <= 0)
        {
            return null;
        }
        return extraClasspath;
    }
}
