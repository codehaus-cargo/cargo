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
package org.codehaus.cargo.container.jboss.internal;

import org.codehaus.cargo.container.InstalledLocalContainer;

/**
 * All JBoss container implementation must implement this interface which provides method to find
 * out the location of configuration files located in the JBoss installation source tree.
 * 
 * @version $Id$
 */
public interface JBossInstalledLocalContainer extends InstalledLocalContainer
{
    /**
     * @return The conf directory located under the container's configuration directory
     */
    String getConfDir();

    /**
     * @return The lib directory located under the container's configuration directory
     */
    String getLibDir();

    /**
     * @return The deploy directory located under the container's configuration directory
     */
    String getDeployDir();
}
