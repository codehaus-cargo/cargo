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
package org.codehaus.cargo.container.deployable;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.cargo.util.log.LogLevel;
import org.codehaus.cargo.util.log.Logger;

/**
 * Mock {@link Logger} implementation, that logs everything in {@link ArrayList}s.
 * 
 */
public class MockLogger implements Logger
{
    /**
     * Severities.
     */
    public List<String> severities = new ArrayList<String>();

    /**
     * Messages.
     */
    public List<String> messages = new ArrayList<String>();

    /**
     * Categories.
     */
    public List<String> categories = new ArrayList<String>();

    /**
     * Doesn't do anything. {@inheritDoc}
     * @param level Ignored.
     */
    public void setLevel(LogLevel level)
    {
        // Do nothing
    }

    /**
     * Throws a RuntimeException. {@inheritDoc}
     * @return Nothing.
     */
    public LogLevel getLevel()
    {
        throw new RuntimeException("Not implemented for testing");
    }

    /**
     * Logs a debug message. {@inheritDoc}
     * @param message Message.
     * @param category Category.
     */
    public void debug(String message, String category)
    {
        this.severities.add("debug");
        this.messages.add(message);
        this.categories.add(category);
    }

    /**
     * Logs an info message. {@inheritDoc}
     * @param message Message.
     * @param category Category.
     */
    public void info(String message, String category)
    {
        this.severities.add("info");
        this.messages.add(message);
        this.categories.add(category);
    }

    /**
     * Logs a warning message. {@inheritDoc}
     * @param message Message.
     * @param category Category.
     */
    public void warn(String message, String category)
    {
        this.severities.add("warn");
        this.messages.add(message);
        this.categories.add(category);
    }
}
