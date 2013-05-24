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
package org.codehaus.cargo.tools.daemon;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents the daemon parameters to be sent to the listening daemon.
 *
 * @version $Id$
 */
public class DaemonParameters
{
    /**
     * Map files to send.
     */
    private final Map<String, String> files = new HashMap<String, String>();

    /**
     * Map parameters to send.
     */
    private final Map<String, String> parameters = new HashMap<String, String>();

    /**
     * Sets a daemon parameter.
     *
     * @param key The key name
     * @param value The value string for the specified key
     */
    public void setParameter(String key, String value)
    {
        parameters.put(key, value);
    }

    /**
     * Sets a daemon file.
     *
     * @param key The key name
     * @param file The path to a file
     */
    public void setFile(String key, String file)
    {
        files.put(key, file);
    }

    /**
     * @return if this is a multipart form or not
     */
    public boolean isMultipartForm()
    {
        return files.size() > 0 || parameters.size() > 1;
    }

    /**
     * @return the parameter map
     */
    public Map<String, String> getParameters()
    {
        return parameters;
    }

    /**
     * @return the files map
     */
    public Map<String, String> getFiles()
    {
        return files;
    }
}
