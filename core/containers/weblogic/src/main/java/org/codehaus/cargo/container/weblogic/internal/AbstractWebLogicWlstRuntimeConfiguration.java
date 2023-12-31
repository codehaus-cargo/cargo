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

import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.RemotePropertySet;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.container.spi.configuration.AbstractRuntimeConfiguration;
import org.codehaus.cargo.container.weblogic.WebLogicPropertySet;
import org.codehaus.cargo.container.weblogic.WebLogicWlstConfiguration;
import org.codehaus.cargo.container.weblogic.internal.configuration.WebLogicWlstConfigurationFactory;

/**
 * Contains common WebLogic configuration functionality for WLST.
 */
public abstract class AbstractWebLogicWlstRuntimeConfiguration extends
    AbstractRuntimeConfiguration implements WebLogicWlstConfiguration
{

    /**
     * Configuration factory for creating WLST configuration scripts.
     */
    private WebLogicWlstConfigurationFactory factory;

    /**
     * {@inheritDoc}
     * @see AbstractRuntimeConfiguration#AbstractRuntimeConfiguration()
     */
    public AbstractWebLogicWlstRuntimeConfiguration()
    {
        factory = new WebLogicWlstConfigurationFactory(this);

        setProperty(RemotePropertySet.USERNAME, "weblogic");
        setProperty(RemotePropertySet.PASSWORD, "weblogic1");
        setProperty(WebLogicPropertySet.SERVER, "server");
        setProperty(ServletPropertySet.PORT, "7001");
        setProperty(GeneralPropertySet.HOSTNAME, "localhost");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ConfigurationCapability getCapability()
    {
        return new WebLogicWlstRuntimeConfigurationCapability();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WebLogicWlstConfigurationFactory getConfigurationFactory()
    {
        return factory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDomainHome()
    {
        throw new UnsupportedOperationException("Domain home doesn't exist for "
                + "runtime configuration.");
    }
}
