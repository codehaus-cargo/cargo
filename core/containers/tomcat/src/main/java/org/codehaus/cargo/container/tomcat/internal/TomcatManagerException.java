/* 
 * ========================================================================
 * 
 * Copyright 2005 Vincent Massol.
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

/**
 * Indicates an error received from Tomcat manager.
 * 
 * @version $Id$
 */
public class TomcatManagerException extends Exception
{
    // constants --------------------------------------------------------------

    /**
     * The Java serialization UID for this class.
     */
    private static final long serialVersionUID = 3236969317068093674L;

    // constructors -----------------------------------------------------------

    /**
     * Creates a new <code>ManagerException</code> with no message or cause.
     */
    public TomcatManagerException()
    {
        super();
    }

    /**
     * Creates a new <code>ManagerException</code> with the specified message and no cause.
     * 
     * @param message the message for this exception
     */
    public TomcatManagerException(String message)
    {
        super(message);
    }

    /**
     * Creates a new <code>ManagerException</code> with no message and the specified cause.
     * 
     * @param cause the cause of this exception
     */
    public TomcatManagerException(Throwable cause)
    {
        super(cause);
    }

    /**
     * Creates a new <code>ManagerException</code> with the specified message and cause.
     * 
     * @param message the message for this exception
     * @param cause the cause of this exception
     */
    public TomcatManagerException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
