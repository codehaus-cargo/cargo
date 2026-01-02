/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2026 Ali Tokmen.
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
package org.codehaus.cargo.container.wildfly.internal.configuration.commands;

import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.cargo.container.configuration.Configuration;
import org.codehaus.cargo.container.configuration.entry.DataSource;
import org.codehaus.cargo.container.configuration.entry.Resource;
import org.codehaus.cargo.container.configuration.script.AbstractResourceScriptCommand;

/**
 * Contains common logic used by WildFly commands.
 */
public abstract class AbstractWildFlyScriptCommand extends AbstractResourceScriptCommand
{

    /**
     * Sets configuration containing all needed information for building configuration scripts.
     * 
     * @param configuration Container configuration.
     * @param resourcePath Path to configuration script resources.
     */
    public AbstractWildFlyScriptCommand(Configuration configuration, String resourcePath)
    {
        super(configuration, resourcePath);
    }

    /**
     * @param resource Resource.
     * @return JNDI name corresponding to this Resource.
     */
    protected String getResourceJndi(Resource resource)
    {
        String jndiName = resource.getName();
        return sanitizeJndiName(jndiName);
    }

    /**
     * @param ds DataSource.
     * @return JNDI name corresponding to this DataSource.
     */
    protected String getDataSourceJndi(DataSource ds)
    {
        String jndiName = ds.getJndiLocation();
        return sanitizeJndiName(jndiName);
    }

    /**
     * @param jndiName JNDI name to be sanitized.
     * @return Sanitized JNDI name.
     */
    private String sanitizeJndiName(String jndiName)
    {
        String response = jndiName;
        if (!jndiName.startsWith("java:/"))
        {
            response = "java:/" + jndiName;
            getConfiguration().getLogger().warn("WildFly requires resource JNDI names "
                + "to start with java:/, hence changing the given JNDI name to: " + response,
                this.getClass().getName());
        }
        return response;
    }

    /**
     * Create WildFly CLI parameter string from properties map.
     * WildFly CLI properties usually have format " --name=value"
     * 
     * @param resourceProperties Properties for WildFly Resource.
     * @return Mapped properties.
     */
    protected String mapResourceProperties(Map<String, String> resourceProperties)
    {
        StringBuilder sb = new StringBuilder();
        for (Entry<String, String> element : resourceProperties.entrySet())
        {
            if (element.getValue() != null && !element.getValue().isEmpty())
            {
                sb.append(" --");
                sb.append(element.getKey());
                sb.append("=");
                sb.append(element.getValue());
            }
        }
        return sb.toString();
    }
}
