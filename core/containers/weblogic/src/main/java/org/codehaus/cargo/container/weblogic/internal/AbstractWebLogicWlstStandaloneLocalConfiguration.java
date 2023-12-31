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
package org.codehaus.cargo.container.weblogic.internal;

import java.util.Collections;
import java.util.List;

import org.codehaus.cargo.container.configuration.entry.Resource;
import org.codehaus.cargo.container.spi.configuration.AbstractStandaloneLocalConfiguration;
import org.codehaus.cargo.container.weblogic.WebLogicWlstConfiguration;
import org.codehaus.cargo.container.weblogic.internal.configuration.WebLogicWlstConfigurationFactory;
import org.codehaus.cargo.container.weblogic.internal.configuration.rules.WebLogicResourceRules;
import org.codehaus.cargo.container.weblogic.internal.configuration.util.WebLogicResourceComparator;

/**
 * Contains common WebLogic configuration functionality for WLST.
 */
public abstract class AbstractWebLogicWlstStandaloneLocalConfiguration extends
    AbstractStandaloneLocalConfiguration implements WebLogicWlstConfiguration
{

    /**
     * Configuration factory for creating WLST configuration scripts.
     */
    private WebLogicWlstConfigurationFactory factory;

    /**
     * {@inheritDoc}
     * @see AbstractStandaloneLocalConfiguration#AbstractStandaloneLocalConfiguration(String)
     */
    public AbstractWebLogicWlstStandaloneLocalConfiguration(String dir)
    {
        super(dir);
        factory = new WebLogicWlstConfigurationFactory(this);
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
        WebLogicResourceComparator priorityComparator = new WebLogicResourceComparator();
        List<Resource> resources = getResources();
        Collections.sort(resources, priorityComparator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDomainHome()
    {
        return getHome();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WebLogicWlstConfigurationFactory getConfigurationFactory()
    {
        return factory;
    }
}
