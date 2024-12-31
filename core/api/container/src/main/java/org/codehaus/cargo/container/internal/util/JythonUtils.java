/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2025 Ali Tokmen.
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
package org.codehaus.cargo.container.internal.util;

/**
 * Set of common utility methods for Jython scripts.
 */
public final class JythonUtils
{

    /**
     * Ensures that this utility class cannot be instantiated.
     */
    private JythonUtils()
    {
    }

    /**
     * Escape special characters for Jython scripts.
     * 
     * @param plainText Text to be escaped.
     * @return Escaped text.
     */
    public static String escapeStringLiteral(String plainText)
    {
        return plainText.
                replace("\\", "\\\\").
                replace("\'", "\\\'").
                replace("\"", "\\\"");
    }
}
