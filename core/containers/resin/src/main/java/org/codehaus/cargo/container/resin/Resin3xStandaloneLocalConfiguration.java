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
package org.codehaus.cargo.container.resin;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.tools.ant.types.FilterChain;
import org.codehaus.cargo.container.Container;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.builder.ConfigurationBuilder;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.resin.internal.AbstractResinStandaloneLocalConfiguration;
import org.codehaus.cargo.container.resin.internal.Resin3xConfigurationBuilder;

/**
 * Resin 3.x standalone
 * {@link org.codehaus.cargo.container.spi.configuration.ContainerConfiguration} implementation.
 * 
 * @version $Id$
 */
public class Resin3xStandaloneLocalConfiguration extends
    AbstractResinStandaloneLocalConfiguration
{
    /**
     * Where elements for resources will be inserted. This expression evaluates to: {@value
     * XML_PARENT_OF_RESOURCES}
     */
    public static final String XML_PARENT_OF_RESOURCES = "//resin:resin";

    /**
     * {@inheritDoc}
     * 
     * @see AbstractResinStandaloneLocalConfiguration#AbstractResinStandaloneLocalConfiguration(String)
     */
    public Resin3xStandaloneLocalConfiguration(String dir)
    {
        super(dir);
    }

    /**
     * {@inheritDoc}
     * 
     * @see Resin3xConfigurationBuilder
     */
    @Override
    protected ConfigurationBuilder createConfigurationBuilder(
        LocalContainer container)
    {
        return new Resin3xConfigurationBuilder();
    }

    /**
     * This expression evaluates to: {@value XML_PARENT_OF_RESOURCES} {@inheritDoc}
     */
    @Override
    protected String getXpathForResourcesParent()
    {
        return "//resin:resin";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Map getNamespaces()
    {
        Map namespaces = new HashMap();
        namespaces.put("resin", "http://caucho.com/ns/resin");
        return namespaces;
    }

    /**
     * {@inheritDoc}
     * 
     * @see AbstractResinStandaloneLocalConfiguration#prepareAdditions(Container, FilterChain)
     */
    @Override
    protected void prepareAdditions(Container container, FilterChain theFilterChain)
        throws IOException
    {
        getResourceUtils().copyResource(RESOURCE_PATH + container.getId() + "/app-default.xml",
            getFileHandler().append(getHome(), "app-default.xml"), getFileHandler(),
            getFilterChain());
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
     * 
     * @see AbstractResinStandaloneLocalConfiguration#createResinFilterChain()
     */
    @Override
    protected FilterChain createResinFilterChain()
    {
        FilterChain filterChain = getFilterChain();

        // Add expanded WAR support
        getAntUtils().addTokenToFilterChain(filterChain, "resin.expanded.webapps",
            createExpandedWarTokenValue("document-directory"));

        // Add token filters for authenticated users
        getAntUtils().addTokenToFilterChain(filterChain, "resin3x.users",
            getSecurityToken("<user>", "</user>"));

        // Add logging support
        getAntUtils().addTokenToFilterChain(filterChain, "resin3x.logging.level",
            getResinLoggingLevel(getPropertyValue(GeneralPropertySet.LOGGING)));

        return filterChain;
    }

}
