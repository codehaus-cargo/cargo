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
package org.codehaus.cargo.container.spi.jvm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.codehaus.cargo.util.log.Logger;

/**
 * Redirects the output of a process into a {@link Logger} by periodically pumping data.
 */
public class DefaultJvmLauncherLoggerRedirector implements Runnable
{
    /**
     * The input stream of the process
     */
    private final InputStream inputStream;

    /**
     * The logger to which the output of the JVM is redirected
     */
    private Logger outputLogger;

    /**
     * The log category to use when logging the JVM's outputs
     */
    private String category;

    /**
     * Creates a new redirector.
     * 
     * @param inputStream the input stream
     * @param outputLogger The logger to which the output of the JVM is redirected
     * @param category the log category to use when logging the JVM's outputs
     */
    public DefaultJvmLauncherLoggerRedirector(
        InputStream inputStream, Logger outputLogger, String category)
    {
        if (inputStream == null)
        {
            throw new IllegalArgumentException("Input stream should not be null");
        }
        this.inputStream = inputStream;
        this.outputLogger = outputLogger;
        this.category = category;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run()
    {
        try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(this.inputStream, StandardCharsets.UTF_8)))
        {
            String line;
            while ((line = reader.readLine()) != null)
            {
                this.outputLogger.info(line, this.category);
            }
        }
        catch (Exception e)
        {
            this.outputLogger.warn("Error reading process stream: " + e, getClass().getName());
            return;
        }
        finally
        {
            try
            {
                inputStream.close();
            }
            catch (IOException e)
            {
                return;
            }
        }
    }
}
