/*
 * ========================================================================
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
package org.codehaus.cargo.util.internal.log;

import org.codehaus.cargo.util.log.LogLevel;
import org.codehaus.cargo.util.log.Logger;

/**
 * Base clas for all Loggers.
 * 
 * @version $Id$
 */
public abstract class AbstractLogger implements Logger
{
    /**
     * The logging level. See #LogLevel
     */
    private LogLevel level = LogLevel.INFO;

    /**
     * @param level the logging level above which the logger will log
     */
    public AbstractLogger(LogLevel level)
    {
        this.level = level;
    }

    /**
     * Default logging level is WARN.
     */
    public AbstractLogger()
    {
        // Do nothing. The default logging level is then WARN.
    }

    /**
     * {@inheritDoc}
     * @see Logger#setLevel(org.codehaus.cargo.util.log.LogLevel)
     */
    public void setLevel(LogLevel level)
    {
        this.level = level;
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.util.log.Logger#getLevel()
     */
    public LogLevel getLevel()
    {
        return this.level;
    }

    /**
     * {@inheritDoc}
     * @see Logger#warn(String, String)
     */
    public void warn(String message, String category)
    {
        log(LogLevel.WARN, message, category);
    }

    /**
     * {@inheritDoc}
     * @see Logger#info(String, String)
     */
    public void info(String message, String category)
    {
        log(LogLevel.INFO, message, category);
    }

    /**
     * {@inheritDoc}
     * @see Logger#debug(String, String)
     */
    public void debug(String message, String category)
    {
        log(LogLevel.DEBUG, message, category);
    }

    /**
     * Common method for all severity levels. Verifies that the logging level is correct for logging
     * the current message.
     * 
     * @param level the log level (aka severity) of the message
     * @param message the message to log
     * @param category the log category
     */
    private void log(LogLevel level, String message, String category)
    {
        if (getLevel().compareTo(level) >= 0)
        {
            doLog(level, message, category);
        }
    }

    /**
     * Common method for all severity levels.
     * 
     * @param level the log level (aka severity) of the message
     * @param message the message to log
     * @param category the log category
     */
    protected abstract void doLog(LogLevel level, String message, String category);
}
