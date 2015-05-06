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
package org.codehaus.cargo.container.weblogic.internal.configuration;

import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.configuration.builder.ConfigurationBuilder;
import org.codehaus.cargo.container.configuration.entry.DataSource;
import org.codehaus.cargo.container.configuration.entry.Resource;
import org.codehaus.cargo.container.weblogic.WebLogicPropertySet;
import org.codehaus.cargo.util.CargoException;

/**
 * Class holding common properties for JMS resource builders.
 */
public abstract class AbstractWebLogicJmsConfigurationBuilder implements ConfigurationBuilder
{
    /**
     * New line constant.
     */
    protected static final String NEW_LINE = System.getProperty("line.separator");

    /**
     * Configuration containing all needed informations.
     */
    private LocalConfiguration configuration;

    /**
     * Sets configuration containing all needed information for building JMS resources.
     *
     * @param configuration containing all needed informations.
     */
    public AbstractWebLogicJmsConfigurationBuilder(LocalConfiguration configuration)
    {
        this.configuration = configuration;
    }

    /**
     * @return Jms server name.
     */
    protected String getJmsServerName()
    {
        for (Resource resource : configuration.getResources())
        {
            if (WeblogicConfigurationEntryType.JMS_SERVER.equals(resource.getType()))
            {
                return resource.getId();
            }
        }
        throw new CargoException("No JMS server resource found.");
    }

    /**
     * @return Jms subdeployment name.
     */
    protected String getJmsSubdeploymentName()
    {
        for (Resource resource : configuration.getResources())
        {
            if (WeblogicConfigurationEntryType.JMS_SUBDEPLOYMENT.equals(resource.getType()))
            {
                return resource.getId();
            }
        }
        throw new CargoException("No JMS subdeployment resource found.");
    }

    /**
     * @return Jms module name.
     */
    protected String getJmsModuleName()
    {
        for (Resource resource : configuration.getResources())
        {
            if (WeblogicConfigurationEntryType.JMS_MODULE.equals(resource.getType()))
            {
                return resource.getId();
            }
        }
        throw new CargoException("No JMS subdeployment resource found.");
    }

    /**
     * @return Server name.
     */
    protected String getServerName()
    {
        return configuration.getPropertyValue(WebLogicPropertySet.SERVER);
    }

    /**
     * {@inheritDoc}
     */
    public String toConfigurationEntry(DataSource ds)
    {
        throw new UnsupportedOperationException("Datasource configuration unsupported in this "
            + "configuration builder.");
    }
}
