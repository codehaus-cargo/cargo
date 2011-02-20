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
package org.codehaus.cargo.container;

import org.codehaus.cargo.util.log.Loggable;

/**
 * Common container API to wrap a physical container. There can be specialized types of containers
 * such as {@link org.codehaus.cargo.container.LocalContainer} or
 * {@link org.codehaus.cargo.container.RemoteContainer}.
 * 
 * @version $Id$
 */
public interface Container extends Loggable
{
    /**
     * @return the short name of the container. Note: this is not a unique id. It is simply the name
     * in a computer-usable format.
     */
    String getId();

    /**
     * @return the human readable name of the Container (ex: "Resin 3.x", "JBoss 3.0.8", etc).
     */
    String getName();

    /**
     * @return the {@link ContainerCapability} of the container in term of ability to deploy such
     * and such type of {@link org.codehaus.cargo.container.deployable.Deployable}s (eg WAR, EAR,
     * etc).
     */
    ContainerCapability getCapability();

    /**
     * @return the container state (Valid states are Container.STOPPED, Container.STARTED,
     * Container.STARTING and Container.STOPPING)
     */
    State getState();

    /**
     * @return the container's type (local , remote, etc)
     */
    ContainerType getType();
}
