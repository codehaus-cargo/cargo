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

import org.codehaus.cargo.container.spi.configuration.AbstractExistingLocalConfiguration;
import org.codehaus.cargo.container.weblogic.WebLogicWlstConfiguration;
import org.codehaus.cargo.container.weblogic.internal.configuration.WebLogicWlstConfigurationFactory;

/**
 * Contains common WebLogic configuration functionality for WLST.
 */
public abstract class AbstractWebLogicWlstExistingLocalConfiguration extends
    AbstractExistingLocalConfiguration implements WebLogicWlstConfiguration
{

    /**
     * Configuration factory for creating WLST configuration scripts.
     */
    private WebLogicWlstConfigurationFactory factory;

    /**
     * {@inheritDoc}
     * @see AbstractExistingLocalConfiguration#AbstractExistingLocalConfiguration(String)
     */
    public AbstractWebLogicWlstExistingLocalConfiguration(String dir)
    {
        super(dir);
        factory = new WebLogicWlstConfigurationFactory(this);
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
