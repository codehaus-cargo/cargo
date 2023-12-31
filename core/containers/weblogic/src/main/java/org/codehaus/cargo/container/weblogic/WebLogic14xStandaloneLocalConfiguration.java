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
package org.codehaus.cargo.container.weblogic;

import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.weblogic.internal.WebLogic14xStandaloneLocalConfigurationCapability;

/**
 * WebLogic 14.x standalone
 * {@link org.codehaus.cargo.container.spi.configuration.ContainerConfiguration} implementation.
 * WebLogic 14.x uses WLST for container configuration.
 */
public class WebLogic14xStandaloneLocalConfiguration extends
    WebLogic122xStandaloneLocalConfiguration
{

    /**
     * {@inheritDoc}
     * @see WebLogic122xStandaloneLocalConfiguration#WebLogic122xStandaloneLocalConfiguration(String)
     */
    public WebLogic14xStandaloneLocalConfiguration(String dir)
    {
        super(dir);

        getProperties().remove(WebLogicPropertySet.PASSWORD_LENGTH_MIN);
        getProperties().remove(WebLogicPropertySet.PASSWORD_SPNUM_MIN);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ConfigurationCapability getCapability()
    {
        return new WebLogic14xStandaloneLocalConfigurationCapability();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return "WebLogic 14.x Standalone Configuration";
    }
}
