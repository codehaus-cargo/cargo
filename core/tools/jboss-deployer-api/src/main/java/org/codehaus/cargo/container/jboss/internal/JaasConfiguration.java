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
package org.codehaus.cargo.container.jboss.internal;

import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;

/**
 * JAAS configuration implementation with only one configuration entry.
 * 
 */
public class JaasConfiguration extends Configuration
{

    /**
     * Only configuration entry.
     */
    private AppConfigurationEntry[] entry;

    /**
     * Saves the configuration entry.
     * @param entry Only configuration entry.
     */
    public JaasConfiguration(AppConfigurationEntry entry)
    {
        this.entry = new AppConfigurationEntry[] {entry};
    }

    /**
     * {@inheritDoc}
     * @see Configuration#getAppConfigurationEntry(java.lang.String)
     */
    @Override
    public AppConfigurationEntry[] getAppConfigurationEntry(String string)
    {
        return this.entry;
    }

    /**
     * {@inheritDoc}. Does nothing.
     * @see Configuration#refresh()
     */
    @Override
    public void refresh()
    {
        // Nothing
    }

}
