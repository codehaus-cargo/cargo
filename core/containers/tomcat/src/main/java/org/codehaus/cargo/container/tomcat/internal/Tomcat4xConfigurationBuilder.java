/*
 * ========================================================================
 *
 * Copyright 2004-2006 Vincent Massol.
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

import java.util.Iterator;

import org.codehaus.cargo.container.configuration.builder.ConfigurationEntryType;
import org.codehaus.cargo.container.configuration.entry.Resource;

/**
 * Constructs xml elements needed to configure a DataSource for Tomcat4x. Note that this
 * implementation converts DataSources into Resources and then uses an appropriate
 * {@link ConfigurationBuilder} to create the configuration.
 * 
 * @version $Id$
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
    public String toConfigurationEntry(Resource resource)
    {
        StringBuffer buff = new StringBuffer();
        buff.append("<Resource ").append("name=\"").append(resource.getName()).append("\"\n");
        if (resource.getClassName() != null)
        {
            buff.append("          ").append("type=\"").append(resource.getClassName()).append(
                "\"\n");

        }
        else
        {
            buff.append("          ").append("type=\"").append(resource.getType()).append("\"\n");

        }
        buff.append("          ").append("auth=\"").append("Container").append("\"\n");
        buff.append("          ").append("/>\n");
        if (resource.getParameter("factory") == null)
        {
            resource.setParameter("factory", getFactoryClassFor(resource.getType()));
        }
        Iterator parameterNames = resource.getParameterNames().iterator();
        if (parameterNames.hasNext())
        {
            buff.append("<ResourceParams ").append("name=\"").append(resource.getName()).append(
                "\">\n");
            while (parameterNames.hasNext())
            {
                String parameterName = (String) parameterNames.next();
                buff.append("  <parameter>\n  <name>");
                buff.append(parameterName);
                buff.append("</name>\n    <value>");
                buff.append(resource.getParameter(parameterName));
                buff.append("</value>\n  </parameter>\n");
            }
            buff.append("</ResourceParams>\n");
        }
        return buff.toString();
    }

    /**
     * {@inheritDoc}
     */
    protected String getDataSourceFactoryClass()
    {
        return "org.apache.commons.dbcp.BasicDataSourceFactory";
    }

}
