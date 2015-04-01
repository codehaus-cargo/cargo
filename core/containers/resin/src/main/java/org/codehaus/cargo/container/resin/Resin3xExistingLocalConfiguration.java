/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2011 Vincent Massol.
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

import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.resin.internal.Resin3xExistingLocalConfigurationCapability;
import org.codehaus.cargo.container.resin.internal.ResinRun;

/**
 * Resin existing {@link org.codehaus.cargo.container.configuration.Configuration} implementation.
 * 
 */
public class Resin3xExistingLocalConfiguration extends Resin2xExistingLocalConfiguration
{
    /**
     * Capability of the Resin standalone configuration.
     */
    private static ConfigurationCapability capability =
        new Resin3xExistingLocalConfigurationCapability();

    /**
     * {@inheritDoc}
     * @see Resin2xExistingLocalConfiguration#Resin2xExistingLocalConfiguration(String)
     */
    public Resin3xExistingLocalConfiguration(String dir)
    {
        super(dir);

        setProperty(ResinPropertySet.SOCKETWAIT_PORT,
            Integer.toString(ResinRun.DEFAULT_KEEPALIVE_SOCKET_PORT));
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.configuration.Configuration#getCapability()
     */
    @Override
    public ConfigurationCapability getCapability()
    {
        return capability;
    } 
}
