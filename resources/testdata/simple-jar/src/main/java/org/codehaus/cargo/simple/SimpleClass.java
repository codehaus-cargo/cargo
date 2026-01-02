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
package org.codehaus.cargo.simple;

/**
 * Simple class which holds a String message. We use this in our test Servlet used to verify that
 * extra / shared classpath is present.
 */
public class SimpleClass
{
    /**
     * Message.
     */
    protected String message;

    /**
     * @param message message to set
     */
    public void setMessage(String message)
    {
        this.message = message;
    }

    /**
     * @return message previously set
     */
    public String getMessage()
    {
        return message;
    }
}
