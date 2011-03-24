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

import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.tomcat.TomcatWAR;

/**
 * Utility methods for Tomcat.
 * 
 * @version $Id$
 */
public final class TomcatUtils
{

    /**
     * Hide constructor.
     */
    private TomcatUtils()
    {
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

}
