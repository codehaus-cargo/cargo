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
package org.codehaus.cargo.maven2.log;

import org.apache.maven.plugin.logging.Log;
import org.codehaus.cargo.util.log.LogLevel;
import org.codehaus.cargo.util.internal.log.AbstractLogger;

/**
 * Logger that sends messages to the Maven 2 logging subsystem.
 * 
 * @version $Id$
 */
public class MavenLogger extends AbstractLogger
{
    /**
     * The Maven 2 logger to use.
     */
    private Log logger;

    /**
     * @param logger the Maven 2 logger to send messages to
     */
    public MavenLogger(Log logger)
    {
        this.logger = logger;
    }

    /**
     * {@inheritDoc}
     * @see AbstractLogger#doLog(org.codehaus.cargo.util.log.LogLevel, String, String)
     */
    @Override
    protected void doLog(LogLevel level, String message, String category)
    {
        String formattedMessage = formatMessage(message, category);

        if (level == LogLevel.WARN)
        {
            this.logger.warn(formattedMessage);
        }
        else if (level == LogLevel.DEBUG)
        {
            this.logger.debug(formattedMessage);
        }
        else
        {
            this.logger.info(formattedMessage);
        }
    }

    /**
     * Format the message to display.
     *   
     * @return the formatted message
     * @param message the message to log
     * @param category the log category
     */
    private String formatMessage(String message, String category)
    {
        String formattedCategory = category.length() > 20 
            ? category.substring(category.length() - 20) : category;

        return "[" + formattedCategory + "] " + message;
    }
}
