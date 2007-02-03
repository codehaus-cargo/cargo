/*
 * ========================================================================
 *
 * Copyright 2004-2005 Vincent Massol.
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

import org.codehaus.cargo.container.configuration.RuntimeConfiguration;

/**
 * A remote container is a container that is already installed and started (locally or on a remote 
 * machine). It is accessed remotely without any file system access.
 *
 * @version $Id$
 */
public interface RemoteContainer extends Container
{
    /**
     * @param configuration the runtime configuration implementation to use
     */
    void setConfiguration(RuntimeConfiguration configuration);
    
    /**
     * @return the runtime configuration to use
     * @see #setConfiguration(RuntimeConfiguration)
     */
    RuntimeConfiguration getConfiguration();
}
