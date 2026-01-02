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
package org.codehaus.cargo.container.deployable;

import org.codehaus.cargo.util.log.Loggable;

/**
 * A deployable is a file archive to be deployed in a container (eg WAR, EAR, etc).
 */
public interface Deployable extends Loggable
{
    /**
     * @return the file representing the archive to deploy
     */
    String getFile();

    /**
     * @return the deployable's type (war, ear, etc)
     */
    DeployableType getType();

    /**
     * @return the deployable's version (j2ee, javaee, jakartaee, etc)
     */
    DeployableVersion getVersion();

    /**
     * @return If the deployable is a directory or not
     */
    boolean isExpanded();

    /**
     * @return The name of this deployable.
     * See CARGO-1352.
     */
    String getName();

    /**
     * Deployable file name, taking into account the {@link #getName()}, including any
     * deployable-specific aspects of it, and the escaping in order to avoid unwanted file system
     * actions (for example, ensuring the file name contains no slashes).
     * @return The file or directory name for this deployable.
     */
    String getFilename();
}
