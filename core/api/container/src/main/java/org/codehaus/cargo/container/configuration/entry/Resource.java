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
package org.codehaus.cargo.container.configuration.entry;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Represents a resource, such as a datasource.
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
     * Resource Implementation class.
     */
    private String className;

    /**
     * Resource parameters.
     */
    private Map<String, String> parameters;

    /**
     * 
     * @param name Name of resource (e.g. jdbc/myConnection).
     * @param type Type of resource (e.g. javax.sql.DataSource).
     */
    public Resource(String name, String type)
    {
        this.name = name;
        this.type = type;
        parameters = new LinkedHashMap<String, String>();
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
     * Set a resource parameter. I.e. this relates in tomcat to &lt;ResourceParams...&gt;
     * &lt;parameter&gt; &lt;name&gt;key&lt;/name&gt; &lt;value&gt;value&lt;/name&gt;
     * &lt;/parameter&gt; ... &lt;/ResourceParams&gt;
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
        return parameters.get(name);
    }

    /**
     * @return A <code>Set</code> with all parameters.
     * 
     */
    public Set<String> getParameterNames()
    {
        return parameters.keySet();

    }

    /**
     * Set parameters.
     * 
     * @param parameters The parameters to set.
     */
    public void setParameters(Map<String, String> parameters)
    {
        this.parameters = parameters;
    }

    /**
     * Get parameters.
     * 
     * @return parameters The parameters for this resource.
     */
    public Map<String, String> getParameters()
    {
        return parameters;
    }

    /**
     * @param className the className to set
     */
    public void setClassName(String className)
    {
        this.className = className;
    }

    /**
     * @return the className
     */
    public String getClassName()
    {
        return className;
    }

}
