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

import org.codehaus.cargo.util.CargoException;

/**
 * Base exception class for all Cargo Container API classes.
 * 
 * @version $Id$
 */
public class ContainerException extends CargoException
{
    /**
     * @param message the exception message 
     */
    public ContainerException(String message)
    {
        super(message);
    }

    /**
     * @param message the exception message
     * @param throwable the exception to wrap
     */
    public ContainerException(String message, Throwable throwable)
    {
        super(message, throwable);
    }
}
