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
package org.codehaus.cargo.container.resin.internal;

import org.codehaus.cargo.container.configuration.builder.ConfigurationEntryType;
import org.codehaus.cargo.container.configuration.entry.DataSource;
import org.codehaus.cargo.container.configuration.entry.Resource;

/**
 * Constructs xml elements needed to configure a normal or XA compliant DataSource for Resin 3.x.
 * 
 * @version $Id$
 */
public class Resin3xConfigurationBuilder extends Resin2xConfigurationBuilder
{
    /**
     * In Resin 3.x DataSources are proper types
     * 
     * @param ds datasource to configure
     * @return String representing the <datasource/> entry.
     */
    @Override
    protected String toResinConfigurationEntry(DataSource ds)
    {

        StringBuilder dataSourceString = new StringBuilder();
        dataSourceString.append("<database>\n");
        dataSourceString.append("  <jndi-name>").append(ds.getJndiLocation()).append(
            "</jndi-name>\n");
        if (ds.getConnectionType().equals(ConfigurationEntryType.XA_DATASOURCE))
        {
            dataSourceString.append("  <xa>true</xa>\n");
        }
        dataSourceString.append("  <driver>").append("\n");
        dataSourceString.append("    <type>").append(ds.getDriverClass()).append("</type>\n");
        if (ds.getUrl() != null)
        {
            dataSourceString.append("    <url>" + ds.getUrl() + "</url>\n");
        }
        dataSourceString.append("    <user>" + ds.getUsername() + "</user>\n");
        dataSourceString.append("    <password>" + ds.getPassword() + "</password>\n");
        if (ds.getConnectionProperties() != null && ds.getConnectionProperties().size() != 0)
        {
            for (Object parameter : ds.getConnectionProperties().keySet())
            {
                String key = parameter.toString();
                dataSourceString.append("    <").append(key);
                dataSourceString.append(">")
                    .append(ds.getConnectionProperties().getProperty(key));
                dataSourceString.append("</").append(key).append(">\n");

            }

        }
        dataSourceString.append("  </driver>\n");
        dataSourceString.append("</database>");
        return dataSourceString.toString();

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toConfigurationEntry(Resource resource)
    {
        StringBuilder resourceString = new StringBuilder();
        resourceString.append("<resource>\n" + "      <jndi-name>" + resource.getName()
            + "</jndi-name>\n");

        if (resource.getClassName() != null)
        {
            resourceString.append("      <type>" + resource.getClassName() + "</type>\n");
        }
        else
        {
            resourceString.append("      <type>" + resource.getType() + "</type>\n");
        }

        for (String key : resource.getParameterNames())
        {
            resourceString.append("    <init ").append(key);
            resourceString.append("=\"").append(resource.getParameter(key));
            resourceString.append("\" />\n");
        }

        resourceString.append("</resource>");
        return resourceString.toString();
    }
}
