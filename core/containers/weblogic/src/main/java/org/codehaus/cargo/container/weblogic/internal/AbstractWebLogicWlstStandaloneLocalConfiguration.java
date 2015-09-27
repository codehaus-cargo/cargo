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
package org.codehaus.cargo.container.weblogic.internal;

import java.util.Collections;
import java.util.List;

import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.builder.ConfigurationBuilder;
import org.codehaus.cargo.container.configuration.entry.DataSource;
import org.codehaus.cargo.container.configuration.entry.Resource;
import org.codehaus.cargo.container.spi.configuration.AbstractStandaloneLocalConfiguration;
import org.codehaus.cargo.container.weblogic.WebLogicConfiguration;
import org.codehaus.cargo.container.weblogic.internal.configuration.rules.WebLogicResourceRules;
import org.codehaus.cargo.container.weblogic.internal.configuration.util.PriorityComparator;
import org.codehaus.cargo.generic.configuration.builder.ConfigurationBuilderFactory;
import org.codehaus.cargo.generic.configuration.builder.DefaultConfigurationBuilderFactory;

/**
 * Contains common Weblogic configuration functionality for WLST.
 */
public abstract class AbstractWebLogicWlstStandaloneLocalConfiguration extends
    AbstractStandaloneLocalConfiguration implements WebLogicConfiguration
{

    /**
     * {@inheritDoc}
     *
     * @see AbstractStandaloneLocalConfiguration#AbstractStandaloneLocalConfiguration(String)
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
     * Returns configuration script for datasource.
     *
     * @param ds Datasource to be configured.
     * @param container Container the dataSource will be configured on.
     * @return Configuration script.
     */
    protected String getDataSourceScript(DataSource ds, LocalContainer container)
    {
        ConfigurationBuilder builder = this.createConfigurationBuilder(container);
        String configurationEntry = builder.toConfigurationEntry(ds);
        return configurationEntry;
    }

    /**
     * Used for adding resources which aren't defined in Cargo properties, but needs to be created
     * in order to make all resources work.
     */
    protected void addMissingResources()
    {
        WebLogicResourceRules.addMissingJmsResources(this);
    }

    /**
     * Sort resource list because some resources needs to have another resources created first.
     */
    protected void sortResources()
    {
        PriorityComparator priorityComparator = new PriorityComparator();
        List<Resource> resources = getResources();
        Collections.sort(resources, priorityComparator);
    }

    /**
     * Returns configuration script for resource.
     *
     * @param resource Resource to be configured.
     * @param container Container the resource will be configured on.
     * @return Configuration script.
     */
    protected String getResourceScript(Resource resource, LocalContainer container)
    {
        ConfigurationBuilderFactory configurationBuilderFactory =
            new DefaultConfigurationBuilderFactory();
        ConfigurationBuilder builder =
            configurationBuilderFactory.createConfigurationBuilder(container, resource);
        String configurationEntry = builder.toConfigurationEntry(resource);
        return configurationEntry;
    }

    /**
     * {@inheritDoc}
     */
    public String getDomainHome()
    {
        return getHome();
    }
}
