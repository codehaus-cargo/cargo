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
package org.codehaus.cargo.ant;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Holds configuration data for the <code>&lt;daemon&gt;</code> tag used to configure the ANT
 * tasks.
 * 
 */
public class DaemonElement
{
    /**
     * Configuration properties.
     */
    private List<Property> properties = new ArrayList<Property>();

    /**
     * Additional classpath entries.
     * 
     * @parameter
     */
    private List<String> classpaths;

    /**
     * Add a container property.
     * 
     * @param property the container property to add
     */
    public void addConfiguredProperty(Property property)
    {
        this.properties.add(property);
    }

    /**
     * @return the list of container properties
     */
    protected List<Property> getProperties()
    {
        return this.properties;
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

        for (Iterator<Property> iterator = properties.iterator(); iterator.hasNext();)
        {
            Property property = iterator.next();
            if (property.getName().equals(name))
            {
                return property.getValue();
            }
        }

        return null;
    }
}
