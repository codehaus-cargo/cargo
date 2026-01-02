/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2026 Ali Tokmen.
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
package org.codehaus.cargo.container.wildfly.internal.util;

import org.codehaus.cargo.container.property.LoggingLevel;

/**
 * Utility class providing informations about logging.
 */
public final class WildFlyLogUtils
{

    /**
     * Private constructor to prevent getting an instance.
     */
    private WildFlyLogUtils()
    {
        // Utility classes have no public constructors
    }

    /**
     * Translate Cargo logging levels into WildFly logging levels.
     * 
     * @param cargoLogLevel Cargo logging level
     * @return the corresponding WildFly logging level
     */
    public static String getWildFlyLogLevel(String cargoLogLevel)
    {
        String level;

        if (LoggingLevel.LOW.equalsLevel(cargoLogLevel))
        {
            level = "ERROR";
        }
        else if (LoggingLevel.MEDIUM.equalsLevel(cargoLogLevel))
        {
            level = "INFO";
        }
        else
        {
            level = "DEBUG";
        }

        return level;
    }
}
