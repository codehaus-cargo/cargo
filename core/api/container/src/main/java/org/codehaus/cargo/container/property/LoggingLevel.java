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

package org.codehaus.cargo.container.property;

import java.util.Locale;

/**
 * Supported logging levels for {@link GeneralPropertySet#LOGGING}.
 * 
 * @version $Id$
 */
public enum LoggingLevel
{

    /**
     * High amount of log output, e.g. debug logging.
     */
    HIGH,

    /**
     * Medium amount of log output, e.g. info logging.
     */
    MEDIUM,

    /**
     * Low amount of log output, e.g. warn/error logging.
     */
    LOW;

    /**
     * Gets the enum constant matching the specified logging level. Note that unlike
     * {@link #valueOf(String)} this method matches the level name case-insensitively.
     * 
     * @param level The logging level.
     * @return The corrensponding enum constant, never {@code null}.
     * @throws IllegalArgumentException If the specified logging level is invalid.
     */
    public static LoggingLevel toLevel(String level)
    {
        for (LoggingLevel ll : values())
        {
            if (ll.equalsLevel(level))
            {
                return ll;
            }
        }
        throw new IllegalArgumentException("unknown logging level " + level);
    }

    /**
     * Tests whether the specified logging level equals this enum constant.
     * 
     * @param level The logging level, may be {@code null}.
     * @return {@code true} if the specified logging level equals this enum constant, {@code false}
     *         otherwise.
     */
    public boolean equalsLevel(String level)
    {
        return name().equalsIgnoreCase(level);
    }

    /**
     * Gets the string value of this logging level for use as a value of the
     * {@link GeneralPropertySet#LOGGING} property.
     * 
     * @return The string value of this logging level.
     */
    public String getLevel()
    {
        return toString();
    }

    /**
     * Gets the string value of this logging level for use as a value of the
     * {@link GeneralPropertySet#LOGGING} property.
     * 
     * @return The string value of this logging level.
     */
    @Override
    public String toString()
    {
        return name().toLowerCase(Locale.ENGLISH);
    }

}
