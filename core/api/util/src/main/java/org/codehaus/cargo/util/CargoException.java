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
package org.codehaus.cargo.util;

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * Base exception class for all Cargo classes.
 * 
 * @version $Id$
 */
public class CargoException extends RuntimeException
{
    /**
     * Original exception which caused this exception.
     */
    private final Throwable originalThrowable;

    /**
     * @param message the exception message 
     */
    public CargoException(String message)
    {
        super(message);
        this.originalThrowable = null;
    }

    /**
     * @param message the exception message
     * @param throwable the exception to wrap
     */
    public CargoException(String message, Throwable throwable)
    {
        super(message, throwable);
        this.originalThrowable = throwable;
    }

    /**
     * @return the wrapped exception
     */
    public Throwable getOriginalThrowable()
    {
        return this.originalThrowable;
    }

    /**
     * Print the full stack trace, including the original exception.
     */
    public void printStackTrace()
    {
        printStackTrace(System.err);
    }

    /**
     * Print the full stack trace, including the original exception.
     *
     * @param ps the byte stream in which to print the stack trace
     */
    public void printStackTrace(PrintStream ps)
    {
        super.printStackTrace(ps);

        if (getOriginalThrowable() != null)
        {
            getOriginalThrowable().printStackTrace(ps);
        }
    }

    /**
     * Print the full stack trace, including the original exception.
     *
     * @param pw the character stream in which to print the stack trace
     */
    public void printStackTrace(PrintWriter pw)
    {
        super.printStackTrace(pw);

        if (getOriginalThrowable() != null)
        {
            getOriginalThrowable().printStackTrace(pw);
        }
    }
    
}
