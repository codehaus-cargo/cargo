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
package org.codehaus.cargo.daemon.properties;

import java.util.HashMap;

import org.codehaus.cargo.daemon.CargoDaemonException;

/**
 * Properties container.
 *
 * @version $Id$
 */
public class PropertyTable extends HashMap<String, String>
{
    /**
     *
     */
    private static final long serialVersionUID = 363523668224756869L;

    /**
     * Gets a boolean value of a property with key {@code name}.
     *
     * @param name The key name
     * @return the boolean value
     */
    public boolean getBoolean(String name)
    {
        Object object = this.get(name);

        if (object != null)
        {
            return Boolean.valueOf(object.toString());
        }
        else
        {
            return false;
        }
    }

    /**
     * Gets the value of a property with key {@code name}.
     * @param name The key name
     * @param required required is @{code true} if this property is required,
     * {@code false} otherwise.
     * @return the value
     */
    public String get(String name, boolean required)
    {
        String value = get(name);

        if (value == null || value.length() == 0)
        {
            if (required)
            {
                throw new CargoDaemonException("Parameter " + name + " is required.");
            }
        }

        return value;
    }
}
