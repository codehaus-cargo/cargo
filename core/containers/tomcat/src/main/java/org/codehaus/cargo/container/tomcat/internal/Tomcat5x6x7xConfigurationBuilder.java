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
package org.codehaus.cargo.container.tomcat.internal;

import org.codehaus.cargo.container.configuration.builder.ConfigurationEntryType;
import org.codehaus.cargo.container.configuration.entry.Resource;

/**
 * Constructs xml elements needed to configure DataSource for Tomcat 5.x, 6x and 7.x. Note that
 * this implementation converts DataSources into Resources and then uses an appropriate
 * {@link org.codehaus.cargo.container.configuration.builder.ConfigurationBuilder} to create the
 * configuration.
 * 
 * @version $Id$
 */
public class Tomcat5x6x7xConfigurationBuilder extends AbstractTomcatConfigurationBuilder
{

    /**
     * generates {@link #typeToFactory}
     */
    public Tomcat5x6x7xConfigurationBuilder()
    {
        super();
        typeToFactory.put(ConfigurationEntryType.DATASOURCE,
            "org.apache.tomcat.dbcp.dbcp.BasicDataSourceFactory");
    }

    /**
     * {@inheritDoc} in Tomcat 5-6.x, Resources are elements where all configuration are attributes.
     */
    public String toConfigurationEntry(Resource resource)
    {
        StringBuilder buff = new StringBuilder();

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
        for (String parameterName : resource.getParameterNames())
        {
            buff.append("          ").append(parameterName).append("='");
            buff.append(resource.getParameter(parameterName)).append("'\n");
        }
        buff.append("/>\n");
        return buff.toString();
    }

}
