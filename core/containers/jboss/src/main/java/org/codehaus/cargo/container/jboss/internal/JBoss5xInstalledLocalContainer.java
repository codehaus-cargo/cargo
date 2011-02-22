/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2010 Vincent Massol.
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

/**
 * The JBoss5x container implementation must implement this interface which provides JBoss5x
 * specifics elements.
 * 
 * @version $Id$
 */
public interface JBoss5xInstalledLocalContainer extends JBossInstalledLocalContainer
{

    /**
     * @param configurationName the JBoss server configuration name for which to return the deployer
     * dir.
     * @return The deployer directory located under the container's home installation directory
     */
    String getDeployersDir(String configurationName);

    /**
     * Return the location of the common lib directory.
     * @return The common lib directory located under the container's home installation directory
     */
    String getCommonLibDir();

}
