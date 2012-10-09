/*
 * ========================================================================
 *
 * Copyright 2003-2004 The Apache Software Foundation. Code from this file 
 * was originally imported from the Jakarta Cactus project.
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
package org.codehaus.cargo.daemon;

/**
 * Cargo servlet exception.
 * 
 * @version $Id: $
 */
public class CargoDaemonException extends RuntimeException
{
    /**
     * 
     */
    private static final long serialVersionUID = 6416322683984202099L;

    /**
     * Constructor
     */
    public CargoDaemonException()
    {
        super();
    }

    /**
     * Constructor
     * 
     * @param message the message
     */
    public CargoDaemonException(String message)
    {
        super(message);
    }

    /**
     * Constructor.
     * 
     * @param message the message
     * @param cause the cause
     */
    public CargoDaemonException(String message, Throwable cause)
    {
        super(message, cause);
    }

    /**
     * Constructor.
     * 
     * @param cause the cause
     */
    public CargoDaemonException(Throwable cause)
    {
        super(cause);
    }

}
