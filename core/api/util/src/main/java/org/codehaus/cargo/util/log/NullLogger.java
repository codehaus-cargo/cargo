/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2023 Ali Tokmen.
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
import org.codehaus.cargo.util.internal.log.AbstractLogger;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Null implementation which does nothing with log messages.
 */
public class NullLogger extends AbstractLogger
{
    /**
     * {@inheritDoc}
     */
    private final DateFormat format = new SimpleDateFormat("HH:mm:ss.SSS");

    private OutputStream output;
    @Override
    protected void doLog(LogLevel level, String message, String category)
    {
        {
            final String formattedCategory = category.length() > 20
                    ? category.substring(category.length() - 20) : category;

            final String msg = "[" + this.format.format(new Date()) + "]"
                    + "[" + level.getLevel() + "][" + formattedCategory + "] " + message + "\n";
            try
            {
                this.output.write(msg.getBytes(StandardCharsets.UTF_8));
                this.output.flush();
            }
            catch (IOException e)
            {
                throw new CargoException("Failed to write log message ["
                        + msg + "]", e);
            }
        }
    }
}
