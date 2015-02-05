/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2015 Vincent Massol.
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
package org.codehaus.cargo.container.websphere;

/**
 * Enumeration of possible values for configuration property
 * {@link WebSpherePropertySet#OVERWRITE_EXISTING_CONFIGURATION}.
 *
 * @version $Id$
 */
public enum WebSphereExistingConfigurationSetting
{
    /**
     * Both JVM args and system properties will be overwritten.
     */
    ALL("ALL"),

    /**
     * Only JVM values (initialHeapSize, maximumHeapSize, genericJvmArguments) get changed.
     */
    JVM("JVM"),

    /**
     * Original system properties get removed, and replaced by those defined within the container.
     */
    SystemProperties("SystemProperties"),

    /**
     * Existing profile stays unchanged. Provided system properties and JVM arguments get ignored.
     */
    NONE("NONE");

    /**
     * Name of this setting.
     */
    private final String name;

    /**
     * Hidden constructor
     * @param name The name of this setting.
     */
    private WebSphereExistingConfigurationSetting(String name)
    {
        this.name = name;
    }

    /**
     * Name of this setting.
     * @return The name of this settings.
     */
    public String getName()
    {
        return name;
    }

    /**
     * @param name case insensitive name.
     * @return the value or null if not found.
     */
    public static WebSphereExistingConfigurationSetting getByName(String name)
    {
        if (name == null)
        {
            return null;
        }

        for (WebSphereExistingConfigurationSetting type
                : WebSphereExistingConfigurationSetting.values())
        {
            if (type.name.equalsIgnoreCase(name))
            {
                return type;
            }
        }

        return null;
    }
}
