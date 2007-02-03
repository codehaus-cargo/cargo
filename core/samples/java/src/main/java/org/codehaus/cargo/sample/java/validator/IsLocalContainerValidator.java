/* 
 * ========================================================================
 * 
 * Copyright 2005-2006 Vincent Massol.
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
package org.codehaus.cargo.sample.java.validator;

import org.codehaus.cargo.container.ContainerType;

/**
 * Validate that a container is a local container.
 * 
 * @version $Id$
 */
public class IsLocalContainerValidator implements Validator
{
    /**
     * @return true if the container has a local container implementation available, false
     *              otherwise
     */
    public boolean validate(String containerId, ContainerType type)
    {
        return type.isLocal();
    }
}
