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
package org.codehaus.cargo.maven2.configuration;

import java.util.List;
import java.util.Map;

/**
 * Holds configuration data for the <code>&lt;daemon&gt;</code> tag used to configure the plugin in
 * the <code>pom.xml</code> file.
 * 
 */
public class Daemon
{
    /**
     * Daemon properties.
     * 
     * @parameter
     */
    private Map<String, String> properties;

    /**
     * Additional classpath entries.
     * 
     * @parameter
     */
    private List<String> classpaths;

    /**
     * Constructs a daemon configuration with default settings.
     */
    public Daemon()
    {
    }

    /**
     * Constructs a daemon configuration with properties.
     * 
     * @param properties The properties
     */
    public Daemon(Map<String, String> properties)
    {
        this.properties = properties;
    }

    /**
     * @return The daemon properties.
     */
    public Map<String, String> getProperties()
    {
        return properties;
    }

    /**
     * @param properties The daemon properties to set.
     */
    public void setProperties(Map<String, String> properties)
    {
        this.properties = properties;
    }

    /**
     * @return The additional classpath entries to set for a container.
     */
    public List<String> getClasspaths()
    {
        return classpaths;
    }

    /**
     * @param classpaths The additional classpath entries for a container.
     */
    public void setClasspaths(List<String> classpaths)
    {
        this.classpaths = classpaths;
    }

    /**
     * @param name The daemon property key
     * @return the value of a daemon property
     */
    public String getProperty(String name)
    {
        if (properties == null)
        {
            return null;
        }

        return properties.get(name);
    }
}
