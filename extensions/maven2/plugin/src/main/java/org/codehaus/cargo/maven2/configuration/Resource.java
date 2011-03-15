/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2010 Vincent Massol.
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

import java.util.Map;

import org.codehaus.cargo.maven2.util.CargoProject;

/**
 * Holds configuration data for the <code>&lt;resource&gt;</code> tag used to configure the plugin
 * in the <code>pom.xml</code> file.
 * 
 * @version $Id$
 */
public class Resource
{
    /**
     * Resource name.
     */
    private String name;

    /**
     * Resource type.
     */
    private String type;

    /**
     * Resource parameters.
     */
    private Map<String, String> parameters;

    /**
     * @return Resource name.
     */
    public String getName()
    {
        return name;
    }

    /**
     * @param name Resource name.
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * @return Resource type.
     */
    public String getType()
    {
        return type;
    }

    /**
     * @param type Resource type.
     */
    public void setType(String type)
    {
        this.type = type;
    }

    /**
     * @return Resource parameters.
     */
    public Map<String, String> getParameters()
    {
        return parameters;
    }

    /**
     * @param parameters Resource parameters.
     */
    public void setParameters(Map<String, String> parameters)
    {
        this.parameters = parameters;
    }

    /**
     * Create the resource object.
     * @param containerId Container identifier.
     * @param project Cargo project.
     * @return Cargo resource object.
     */
    public org.codehaus.cargo.container.configuration.entry.Resource createResource(
        String containerId, CargoProject project)
    {
        org.codehaus.cargo.container.configuration.entry.Resource resource =
            new org.codehaus.cargo.container.configuration.entry.Resource(getName(), getType());
        resource.setParameters(getParameters());
        return resource;
    }
}
