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
package org.codehaus.cargo.container.resin;

import org.apache.tools.ant.types.FilterChain;
import org.codehaus.cargo.container.Container;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.DatasourcePropertySet;
import org.codehaus.cargo.container.property.DataSource;
import org.codehaus.cargo.container.resin.internal.AbstractResinStandaloneLocalConfiguration;

import java.io.File;
import java.io.IOException;

/**
 * Resin 3.x standalone 
 * {@link org.codehaus.cargo.container.spi.configuration.ContainerConfiguration} implementation.
 *  
 * @version $Id$
 */
public class Resin3xStandaloneLocalConfiguration extends AbstractResinStandaloneLocalConfiguration
{
    /**
     * {@inheritDoc}
     * @see AbstractResinStandaloneLocalConfiguration#AbstractResinStandaloneLocalConfiguration(String)
     */
    public Resin3xStandaloneLocalConfiguration(String dir)
    {
        super(dir);
    }

    /**
     * {@inheritDoc}
     * @see AbstractResinStandaloneLocalConfiguration#prepareAdditions(Container, FilterChain)
     */
    protected void prepareAdditions(Container container, FilterChain theFilterChain)
        throws IOException
    {
        getResourceUtils().copyResource(RESOURCE_PATH + container.getId() + "/app-default.xml",
            new File(getHome(), "app-default.xml"), theFilterChain);
    }

    /**
     * @param cargoLoggingLevel the cargo logging level (ie "low", "medium" or "high")
     * @return the Resin logging level corresponding to the cargo logging level
     */
    private String getResinLoggingLevel(String cargoLoggingLevel)
    {
        String level;

        if (cargoLoggingLevel.equalsIgnoreCase("low"))
        {
            level = "severe";
        }
        else if (cargoLoggingLevel.equalsIgnoreCase("medium"))
        {
            level = "warning";
        }
        else
        {
            level = "config";
        }

        return level;
    }

    /**
     * {@inheritDoc}
     * @see AbstractResinStandaloneLocalConfiguration#createResinFilterChain()
     */
    protected FilterChain createResinFilterChain()
    {
        FilterChain filterChain = createFilterChain();

        // Add expanded WAR support
        getAntUtils().addTokenToFilterChain(filterChain, "resin.expanded.webapps",
            createExpandedWarTokenValue("document-directory"));

        // Add datasource support
        getAntUtils().addTokenToFilterChain(filterChain, DatasourcePropertySet.DATASOURCE,
            createDatasourceTokenValue());

        // Add token filters for authenticated users
        getAntUtils().addTokenToFilterChain(filterChain, "resin3x.users",
            getSecurityToken("<user>", "</user>"));

        // Add logging support
        getAntUtils().addTokenToFilterChain(filterChain, "resin3x.logging.level",
            getResinLoggingLevel(getPropertyValue(GeneralPropertySet.LOGGING)));

        return filterChain;
    }

    /**
     * @return   A datasource xml fragment that can be embedded directly into the resin.conf file
     */
    protected String createDatasourceTokenValue()
    {
        final String dataSourceProperty = getPropertyValue(DatasourcePropertySet.DATASOURCE);
        getLogger().info("Datasource property value [" + dataSourceProperty + "]",
            this.getClass().getName());

        if (dataSourceProperty == null)
        {
            // have to return a non-empty string, as Ant's token stuff doesn't work otherwise
            return " ";
        }
        else
        {
            DataSource ds = new DataSource(dataSourceProperty);
            return
                "<database jndi-name='" + ds.getJndiLocation() + "'>\n"
                + "  <driver type=\"" + ds.getDriverClass() + "\">\n"
                + "    <url>" + ds.getUrl() + "</url>\n"
                + "    <user>" + ds.getUsername() + "</user>\n"
                + "    <password>" + ds.getPassword() + "</password>\n"
                + "  </driver>\n"
                + "</database>";
        }
    }
}
