/* 
 * ========================================================================
 * 
 * Copyright 2005 Vincent Massol.
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
package org.codehaus.cargo.container.configuration;

/**
 * Configuration type of {@link org.codehaus.cargo.container.configuration.Configuration}s. There
 * are currently 3 types: Standalone local, Existing local and runtime configurations.
 *
 * @version $Id: $
 */
public class ConfigurationType
{
    /**
     * Represents a standalone configuration type.
     */
    public static final ConfigurationType STANDALONE = new ConfigurationType("standalone");

    /**
     * Represents an existing configuration type.
     */
    public static final ConfigurationType EXISTING = new ConfigurationType("existing");

    /**
     * Represents a runtime configuration type.
     */
    public static final ConfigurationType RUNTIME = new ConfigurationType("runtime");
    
    /**
     * {@inheritDoc}
     * @see #ConfigurationType(String)
     */
    private String type;

    /**
     * @param type the internal string representation of the configuration type.
     *        For example: "standalone", "existing" or "runtime".
     */
    public ConfigurationType(String type)
    {
        this.type = type;
    }

    /**
     * Transform a type represented as a string into a {@link ConfigurationType} object.
     *
     * @param typeAsString the string to transform
     * @return the {@link ConfigurationType} object
     */
    public static ConfigurationType toType(String typeAsString)
    {
        ConfigurationType type;
        if (typeAsString.equalsIgnoreCase(STANDALONE.getType()))
        {
            type = STANDALONE;
        }
        else if (typeAsString.equalsIgnoreCase(EXISTING.getType()))
        {
            type = EXISTING;
        }
        else if (typeAsString.equalsIgnoreCase(RUNTIME.getType()))
        {
            type = RUNTIME;
        }
        else
        {
            type = new ConfigurationType(typeAsString);
        }

        return type;
    }

    /**
     * {@inheritDoc}
     * @see Object#equals(Object)
     */
    public boolean equals(Object object)
    {
        boolean result = false;
        if ((object != null) && (object instanceof ConfigurationType))
        {
            ConfigurationType type = (ConfigurationType) object;
            if (type.getType().equals(getType()))
            {
                result = true;
            }
        }

        return result;
    }

    /**
     * {@inheritDoc}
     * @see Object#hashCode()
     */
    public int hashCode()
    {
        return this.type.hashCode();
    }

    /**
     * @return the configuration type
     */
    public String getType()
    {
        return this.type;
    }

    /**
     * {@inheritDoc}
     * @see Object#toString()
     */
    public String toString()
    {
        return getType();
    }
}
