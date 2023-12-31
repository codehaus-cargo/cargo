/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2024 Ali Tokmen.
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
package org.codehaus.cargo.container.tomcat.internal;

import java.util.Set;

import org.codehaus.cargo.container.configuration.builder.ConfigurationEntryType;
import org.codehaus.cargo.container.configuration.entry.Resource;

/**
 * Constructs xml elements needed to configure a DataSource for Tomcat4x. Note that this
 * implementation converts DataSources into Resources and then uses an appropriate
 * {@link org.codehaus.cargo.container.configuration.builder.ConfigurationBuilder} to create the
 * configuration.
 */
public class Tomcat4xConfigurationBuilder extends AbstractTomcatConfigurationBuilder
{

    /**
     * generates {@link #typeToFactory}
     */
    public Tomcat4xConfigurationBuilder()
    {
        super();
        typeToFactory.put(ConfigurationEntryType.DATASOURCE,
            "org.apache.commons.dbcp.BasicDataSourceFactory");
    }

    /**
     * @param resource what do generate XML based upon
     * @return String representing the Resource element of server.xml
     */
    @Override
    public String toConfigurationEntry(Resource resource)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("<Resource ").append("name=\"").append(resource.getName()).append("\"\n");
        if (resource.getClassName() != null)
        {
            sb.append("          ")
                .append("type=\"").append(resource.getClassName()).append("\"\n");
        }
        else
        {
            sb.append("          ").append("type=\"").append(resource.getType()).append("\"\n");
        }
        sb.append("          ").append("auth=\"").append("Container").append("\"\n")
            .append("          ").append("/>\n");
        if (resource.getParameter("factory") == null)
        {
            resource.setParameter("factory", getFactoryClassFor(resource.getType()));
        }
        Set<String> parameterNames = resource.getParameterNames();
        if (!parameterNames.isEmpty())
        {
            sb.append("<ResourceParams ").append("name=\"").append(resource.getName()).append(
                "\">\n");
            for (String parameterName : parameterNames)
            {
                sb.append("  <parameter>\n")
                    .append("    <name>").append(parameterName).append("</name>\n")
                    .append("    <value>").append(resource.getParameter(parameterName))
                    .append("</value>\n  </parameter>\n");
            }
            sb.append("</ResourceParams>\n");
        }
        return sb.toString();
    }

    /**
     * @return Datasource factory class.
     */
    protected String getDataSourceFactoryClass()
    {
        return "org.apache.commons.dbcp.BasicDataSourceFactory";
    }

}
