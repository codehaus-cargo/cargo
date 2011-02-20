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

import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.internal.RunnableContainer;
import org.codehaus.cargo.util.FileHandler;

/**
 * A local container is a container that executes on the machine where Cargo is executing. A local
 * container can be started and stopped and is thus controlled by Cargo. This is by opposition to a
 * remote container which is outside the control of Cargo and which is started and stopped
 * externally from Cargo.
 * 
 * @version $Id$
 */
public interface LocalContainer extends Container, RunnableContainer
{
    /**
     * @param configuration the local configuration implementation to use
     */
    void setConfiguration(LocalConfiguration configuration);

    /**
     * @return the local configuration to use
     * @see #setConfiguration(LocalConfiguration)
     */
    LocalConfiguration getConfiguration();

    /**
     * @param handler means by which we affect local files.
     */
    void setFileHandler(FileHandler handler);

    /**
     * @return the means by which we affect local files.
     */
    FileHandler getFileHandler();
}
