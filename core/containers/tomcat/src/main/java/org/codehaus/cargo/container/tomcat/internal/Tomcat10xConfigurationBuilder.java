/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2011 Vincent Massol, 2012-2020 Ali Tokmen.
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
package org.codehaus.cargo.container.tomcat.internal;

import org.codehaus.cargo.container.configuration.entry.Resource;

/**
 * Constructs xml elements needed to configure DataSource for Tomcat 10.x. Note that this
 * implementation converts DataSources into Resources and then uses an appropriate
 * {@link org.codehaus.cargo.container.configuration.builder.ConfigurationBuilder} to create the
 * configuration.
 */
public class Tomcat10xConfigurationBuilder extends Tomcat8x9xConfigurationBuilder
{

    /**
     * {@inheritDoc} in Tomcat 10.x, all packages moved from Java EE to Jakarta EE.
     */
    @Override
    public String toConfigurationEntry(Resource resource)
    {
        String configurationEntry = super.toConfigurationEntry(resource);
        configurationEntry = configurationEntry.replace("type='javax.", "type='jakarta.");
        configurationEntry = configurationEntry.replace("type=\"javax.", "type=\"jakarta.");
        return configurationEntry;
    }

}
