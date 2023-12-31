/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2024 Ali Tokmen.
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

import org.codehaus.cargo.util.internal.log.AbstractLogger;

/**
 * Simple implementation which sends log messages to the console (stdout).
 */
public class SimpleLogger extends AbstractLogger
{
    /**
     * {@inheritDoc}
     */
    @Override
    protected void doLog(LogLevel level, String message, String category)
    {
        System.out.println("[" + level.getLevel() + "][" + category + "] " + message);
    }
}
