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
package org.codehaus.cargo.container.resource;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Represents a resource, such as a datasource.
 * @version $Id$
 */
public class Resource
{
    
    /**
     * Resource name.
     */
    private String name;
    
    /**
     * Resource Type.
     */
    private String type;
    
    /**
     * Resource parameters.
     */
    private Map parameters;

    /**
     * 
     * @param name Name of resource (e.g. jdbc/myConnection).
     * @param type Type of resource (e.g. javax.sql.DataSource).
     */
    public Resource(String name, String type) 
    {
        this.name = name;
        this.type = type;
        parameters = new LinkedHashMap();
    }

    /**
     * @return The resource name
     */
    public String getName()
    {
        return name;
    }

    /**
     * @return The resource Type
     */
    public String getType()
    {
        return type;
    }

    /**
     * Set a resource parameter. I.e. this relates in tomcat to
     * <ResourceParams...> <parameter> <name>key</name> <value>value</name>
     * </parameter> ... </ResourceParams>
     * 
     * @param name Name of the value to set.
     * @param value Value to set.
     */
    public void setParameter(String name, String value)
    {
        parameters.put(name, value);
    }

    /**
     * Get a stored parameter. Returns null if no parameter stored by this name.
     * 
     * @param name Name of parameter.
     * @return Value of parameter.
     */
    public String getParameter(String name)
    {
        return (String) parameters.get(name);
    }

    /**
     * @return A <code>Set</code> with all parameters.
     * 
     */
    public Set getParameterNames()
    {
        return parameters.keySet();

    }

    /**
     * Set parameters.
     * 
     * @param parameters The parameters to set.
     */
    public void setParameters(Map parameters)
    {
        this.parameters = parameters;
    }

}
