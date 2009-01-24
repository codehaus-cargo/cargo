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
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.DatasourcePropertySet;
import org.codehaus.cargo.container.property.DataSource;
import org.codehaus.cargo.container.resin.internal.AbstractResinStandaloneLocalConfiguration;
import org.codehaus.cargo.container.Container;
import java.io.IOException;

/**
 * Resin 2.x standalone 
 * {@link org.codehaus.cargo.container.spi.configuration.ContainerConfiguration} implementation.
 *  
 * @version $Id$
 */
public class Resin2xStandaloneLocalConfiguration extends AbstractResinStandaloneLocalConfiguration
{
    /**
     * {@inheritDoc}
     * @see AbstractResinStandaloneLocalConfiguration#AbstractResinStandaloneLocalConfiguration(String)
     */
    public Resin2xStandaloneLocalConfiguration(String dir)
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
        // Nothing additional required
    }

    /**
     * {@inheritDoc}
     * @see AbstractResinStandaloneLocalConfiguration#createResinFilterChain()
     */
    protected FilterChain createResinFilterChain()
    {
        FilterChain filterChain = getFilterChain();

        // Add expanded WAR support
        getAntUtils().addTokenToFilterChain(filterChain, "resin.expanded.webapps",
            createExpandedWarTokenValue("app-dir"));

        // Add datasource support
        getAntUtils().addTokenToFilterChain(filterChain, "resin2x.datasource.xml",
            createDatasourceTokenValue());

        // Add token filters for adding users
        getAntUtils().addTokenToFilterChain(filterChain, "resin2x.users",
            getSecurityToken("<init-param user='", "'/>"));

        // Add logging property tokens

        // Note: The tokenValue value must never be an empty string as otherwise
        // the Ant filtering code fails.
        String tokenValue = " ";
        String logLevel = getPropertyValue(GeneralPropertySet.LOGGING);
        if (logLevel.equalsIgnoreCase("high"))
        {
            tokenValue = "<log id='/' href='stdout:' timestamp='[%H:%M:%S.%s]'/>";
        }
        getAntUtils().addTokenToFilterChain(filterChain, "resin2x.debuglog", tokenValue);

        return filterChain;
    }

    /**
     * @return a datasource xml fragment that can be embedded directly into the resin.conf file
     */
    protected String createDatasourceTokenValue()
    {
        final String dataSourceProperty = getPropertyValue(DatasourcePropertySet.DATASOURCE);
        if (dataSourceProperty == null)
        {
            // have to return a non-empty string, as Ant's token stuff doesn't work otherwise
            return " ";
        }
        else
        {
            DataSource ds = new DataSource(dataSourceProperty);
            return "<resource-ref>\n"
                + "      <res-ref-name>" + ds.getJndiLocation() + "</res-ref-name>\n"
                + "      <res-type>" + ds.getDataSourceType() + "</res-type>\n"
                + "      <init-param driver-name=\"" + ds.getDriverClass() + "\"/>\n"
                + "      <init-param url=\"" + ds.getUrl() + "\"/>\n"
                + "      <init-param user=\"" + ds.getUsername() + "\"/>\n"
                + "      <init-param password=\"" + ds.getPassword() + "\"/>\n"
                + "</resource-ref>";
        }
    }
}
