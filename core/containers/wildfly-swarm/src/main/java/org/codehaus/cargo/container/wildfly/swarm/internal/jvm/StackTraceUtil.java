/*
 * ========================================================================
 *
 *  Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2022 Ali Tokmen.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  ========================================================================
 */
package org.codehaus.cargo.container.wildfly.swarm.internal.jvm;

/**
 * Utility class for Exceptions.
 */
public final class StackTraceUtil
{
    /**
     * Line separator character.
     */
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

    /**
     * Prevent creating an instance.
     */
    private StackTraceUtil()
    {
        // no instance
    }

    /**
     * Returns error stacktrace as a String.
     * @param error error to be converted to String.
     * @return error stacktrace
     */
    public static String getStackTrace(final Throwable error)
    {
        StringBuilder output = new StringBuilder();
        for (StackTraceElement element : error.getStackTrace())
        {
            output.append(element.toString());
            output.append(StackTraceUtil.LINE_SEPARATOR);
        }
        return output.toString();
    }
}
