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
package org.codehaus.cargo.container.tomcat.internal;

import java.util.Iterator;

import org.codehaus.cargo.container.configuration.builder.ConfigurationEntryType;
import org.codehaus.cargo.container.configuration.entry.Resource;

/**
 * Constructs xml elements needed to configure DataSource for Tomcat5 and 6x. Note that this
 * implementation converts DataSources into Resources and then uses an appropriate
 * {@link ConfigurationBuilder} to create the configuration.
 * 
 * @version $Id$
 */
public class Tomcat5And6xConfigurationBuilder extends AbstractTomcatConfigurationBuilder
{

    /**
     * generates {@link #typeToFactory}
     */
    public Tomcat5And6xConfigurationBuilder()
    {
        super();
        typeToFactory.put(ConfigurationEntryType.DATASOURCE,
            "org.apache.tomcat.dbcp.dbcp.BasicDataSourceFactory");
    }

    /**
     * {@inheritDoc} in Tomcat 5-6.x, Resources are elements where all configuration are attributes.
     * 
     * @see Tomcat4xStandaloneLocalConfiguration#buildXmlForResource(import
     *      org.codehaus.cargo.container.resource.Resource)
     */
    public String toConfigurationEntry(Resource resource)
    {
        StringBuffer buff = new StringBuffer();

        buff.append("<Resource name='").append(resource.getName()).append("'\n");
        if (resource.getClassName() != null)
        {
            buff.append("          type='").append(resource.getClassName()).append("'\n");

        }
        else
        {
            buff.append("          type='").append(resource.getType()).append("'\n");
        }
        buff.append("          auth='").append("Container").append("'\n");
        if (resource.getParameter("factory") == null)
        {
            resource.setParameter("factory", getFactoryClassFor(resource.getType()));
        }
        Iterator parameterNames = resource.getParameterNames().iterator();
        while (parameterNames.hasNext())
        {
            String parameterName = (String) parameterNames.next();
            buff.append("          ").append(parameterName).append("='");
            buff.append(resource.getParameter(parameterName)).append("'\n");
        }
        buff.append("/>\n");
        return buff.toString();
    }

}
