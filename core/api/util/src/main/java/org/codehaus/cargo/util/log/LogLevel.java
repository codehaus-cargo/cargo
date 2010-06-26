/*
 * ========================================================================
 *
 * Copyright 2006 Vincent Massol.
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

import org.codehaus.cargo.util.CargoException;

/**
 * Definition of logging levels for Cargo's logs. A WARN level means only warnings are logged.
 * An INFO level means warnings and infos are logged. A DEBUG level means warnings, info and debug
 * messages are logged.
 *
 * <p>Note: There's no ERROR log level as all errors result in an exception being raised.</p>
 *
 * @version $Id$
 */
public final class LogLevel implements Comparable
{
    /**
     * Represents a warning logging level.
     */
    public static final LogLevel WARN = new LogLevel("warn", 0);

    /**
     * Represents an info logging level.
     */
    public static final LogLevel INFO = new LogLevel("info", 1);

    /**
     * Represents a  debug logging level.
     */
    public static final LogLevel DEBUG = new LogLevel("debug", 2);

    /**
     * {@inheritDoc}
     * @see #LogLevel(String, int)
     */
    private String level;

    /**
     * {@inheritDoc}
     * @see #LogLevel(String, int)
     */
    private int logVolume;

    /**
     * @param level the internal representation of the logging level.
     *        For example: "warn", "info" or "debug".
     * @param logVolume the volume of logs that will be output. High numbers have higher volumes.
     *        This is an internal feature that allows us to have a simple
     *        {@link #compareTo(Object)} algorithm.
     */
    private LogLevel(String level, int logVolume)
    {
        this.level = level;
        this.logVolume = logVolume;
    }

    /**
     * Transform a log level represented as a string into a {@link LogLevel} object.
     *
     * @param levelAsString the string to transform
     * @return the {@link LogLevel} object
     */
    public static LogLevel toLevel(String levelAsString)
    {
        LogLevel level;
        if (levelAsString.equalsIgnoreCase(INFO.getLevel()))
        {
            level = INFO;
        }
        else if (levelAsString.equalsIgnoreCase(WARN.getLevel()))
        {
            level = WARN;
        }
        else if (levelAsString.equalsIgnoreCase(DEBUG.getLevel()))
        {
            level = DEBUG;
        }
        else
        {
            throw new CargoException("Invalid log level [" + levelAsString
                + "]. Valid values are \"debug\", \"info\" and \"warn\".");
        }

        return level;
    }

    /**
     * {@inheritDoc}
     * @see Object#equals(Object)
     */
    @Override
    public boolean equals(Object object)
    {
        boolean result = false;
        if ((object != null) && (object instanceof LogLevel))
        {
            LogLevel level = (LogLevel) object;
            if (level.getLevel().equals(getLevel()))
            {
                result = true;
            }
        }

        return result;
    }

    /**
     * {@inheritDoc}
     * @see Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        return this.level.hashCode();
    }

    /**
     * {@inheritDoc}
     * @see Comparable#compareTo(Object)
     */
    public int compareTo(Object object)
    {
        int result;

        if (!object.getClass().isAssignableFrom(LogLevel.class))
        {
            throw new CargoException("Invalid object type [" + object.getClass().getName()
                + "]. Cannot compare a log level to it.");
        }

        LogLevel level = (LogLevel) object;

        if (this.logVolume == level.logVolume)
        {
            result = 0;
        }
        else if (this.logVolume > level.logVolume)
        {
            result = 1;
        }
        else
        {
            result = -1;
        }

        return result;
    }

    /**
     * @return the log level
     */
    public String getLevel()
    {
        return this.level;
    }

    /**
     * {@inheritDoc}
     * @see Object#toString()
     */
    @Override
    public String toString()
    {
        return getLevel();
    }
}
