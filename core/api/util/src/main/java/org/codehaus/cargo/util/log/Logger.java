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
package org.codehaus.cargo.util.log;

/**
 * Simple interface for logging and tracing. The reason we don't use commons-logging or some other
 * logging library is because Cargo is a framework. As such we don't want to force the user to
 * include an additional library and more importantly we want to remain open so that applications
 * using Cargo will be able to adapt it to their favorite logging system, whatever that is.
 * 
 * @version $Id$
 */
public interface Logger
{
    /**
     * @param level the logging level above which the logger will log
     */
    void setLevel(LogLevel level);

    /**
     * @return the logging level above which the logger will log
     */
    LogLevel getLevel();

    /**
     * Logger informational messages.
     * 
     * @param message the message to log
     * @param category the log category (usually this is the full name
     *        of the class being logged but it can be anything)
     */
    void info(String message, String category);

    /**
     * Logger warning messages.
     * 
     * @param message the message to log
     * @param category the log category (usually this is the full name
     *        of the class being logged but it can be anything)
     */
    void warn(String message, String category);

    /**
     * Logger debug messages.
     * 
     * @param message the message to log
     * @param category the log category (usually this is the full name
     *        of the class being logged but it can be anything) 
     */
    void debug(String message, String category);
}
