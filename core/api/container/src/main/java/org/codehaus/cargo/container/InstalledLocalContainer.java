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
package org.codehaus.cargo.container;

import org.codehaus.cargo.container.internal.SpawnedContainer;

/**
 * Represents a local container that requires an installation to work. The installation is where the
 * container's runtime is located.
 * 
 * @version $Id$
 */
public interface InstalledLocalContainer extends LocalContainer, SpawnedContainer
{
    /**
     * @return the directory where the container is installed. Note that we're returning a String
     * instead of a File because we want to leave the possibility of using URIs for specifying the
     * home location.
     */
    String getHome();

    /**
     * @param home the directory where the container is installed. Note that we're passing a String
     * instead of a File because we want to leave the possibility of using URIs for specifying the
     * home location. <b>IMPORTANT</b>: While some containers can deal with this parameter being
     * set as a relative path, some others require this path to be set to an absolute directory.
     * Please refer to the documentation of the server to ensure you give the path in the
     * appropriate way. If in doubt, you can use absolute paths -that is known to work with all
     * containers.
     */
    void setHome(String home);
}
