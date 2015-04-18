/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2011 Vincent Massol, 2011-2015 Ali Tokmen.
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
package org.codehaus.cargo.container.weblogic;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.builder.ConfigurationBuilder;
import org.codehaus.cargo.container.configuration.entry.DataSource;
import org.codehaus.cargo.container.configuration.entry.Resource;
import org.codehaus.cargo.container.spi.configuration.AbstractStandaloneLocalConfiguration;
import org.codehaus.cargo.container.weblogic.internal.WebLogic8xConfigurationBuilder;

/**
 * Contains common Weblogic configuration functionality for WLST.
 */
public abstract class AbstractWebLogicWlstStandaloneLocalConfiguration extends
    AbstractStandaloneLocalConfiguration implements WebLogicConfiguration
{

    /**
     * {@inheritDoc}
     *
     * @see AbstractLocalConfiguration#AbstractLocalConfiguration(String)
     */
    public AbstractWebLogicWlstStandaloneLocalConfiguration(String dir)
    {
        super(dir);
    }

    /**
     * @param container Container the dataSource will be configured on.
     * @return Configuration builder that produces WLST script for DataSource creation.
     */
    protected abstract ConfigurationBuilder createConfigurationBuilder(LocalContainer container);

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(LocalContainer container)
    {
        super.configure(container);
        configureDataSources(container);
        configureResources(container);
    }

    /**
     * Configure datasources.
     *
     * @param container Container the datasource will be configured on.
     */
    protected void configureDataSources(LocalContainer container)
    {
        WebLogic121xWlstInstalledLocalContainer weblogicContainer =
            (WebLogic121xWlstInstalledLocalContainer) container;
        List<String> configurationScript = new ArrayList<String>();

        for (DataSource dataSource : getDataSources())
        {
            configurationScript.add("cd('/')");
            configurationScript.add(configure(dataSource, container));
        }

        weblogicContainer.modifyDomainConfigurationWithWlst(configurationScript);
    }

    /**
     * Returns configuration script for datasource.
     *
     * @param ds Datasource to be configured.
     * @param container Container the dataSource will be configured on.
     * @return Configuration script.
     */
    protected String configure(DataSource ds, LocalContainer container)
    {
        ConfigurationBuilder builder = this.createConfigurationBuilder(container);
        String configurationEntry = builder.toConfigurationEntry(ds);
        return configurationEntry;
    }

    /**
     * Configure resources.
     *
     * @param container Container the datasource will be configured on.
     */
    protected void configureResources(LocalContainer container)
    {
        WebLogic121xWlstInstalledLocalContainer weblogicContainer =
            (WebLogic121xWlstInstalledLocalContainer) container;
        List<String> configurationScript = new ArrayList<String>();

        for (Resource resource : getResources())
        {
            configurationScript.add("cd('/')");
            configurationScript.add(configure(resource, container));
        }

        weblogicContainer.modifyDomainConfigurationWithWlst(configurationScript);
    }

    /**
     * Returns configuration script for datasource.
     *
     * @param resource Resource to be configured.
     * @param container Container the dataSource will be configured on.
     * @return Configuration script.
     */
    protected String configure(Resource resource, LocalContainer container)
    {
        throw new UnsupportedOperationException(
            WebLogic8xConfigurationBuilder.RESOURCE_CONFIGURATION_UNSUPPORTED);
    }

    /**
     * {@inheritDoc}
     */
    public String getDomainHome()
    {
        return getHome();
    }
}
