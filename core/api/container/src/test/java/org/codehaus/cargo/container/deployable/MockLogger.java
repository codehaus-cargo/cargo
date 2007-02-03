/* 
 * ========================================================================
 * 
 * Copyright 2004-2006 Vincent Massol.
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

import org.codehaus.cargo.util.log.Logger;
import org.codehaus.cargo.util.log.LogLevel;

public class MockLogger implements Logger
{
    public List severities = new ArrayList();
    public List messages = new ArrayList();
    public List categories = new ArrayList();

    public void setLevel(LogLevel level)
    {
        // Do nothing
    }

    public LogLevel getLevel()
    {
        throw new RuntimeException("Not implemented for testing");
    }

    public void debug(String message, String category)
    {
        this.severities.add("debug");
        this.messages.add(message);
        this.categories.add(category);
    }

    public void info(String message, String category)
    {
        this.severities.add("info");
        this.messages.add(message);
        this.categories.add(category);
    }

    public void warn(String message, String category)
    {
        this.severities.add("warn");
        this.messages.add(message);
        this.categories.add(category);
    }
}
