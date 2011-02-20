/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2010 Vincent Massol.
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
package org.codehaus.cargo.container.spi.configuration;

import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.ServletPropertySet;

/**
 * Base implementation of {@link org.codehaus.cargo.container.configuration.ConfigurationCapability}
 * that needs to be extended by each existing configuration's implementation.
 * 
 * @version $Id$
 */
public abstract class AbstractExistingLocalConfigurationCapability
    extends AbstractConfigurationCapability
{
    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.spi.configuration.AbstractConfigurationCapability#AbstractConfigurationCapability()
     */
    public AbstractExistingLocalConfigurationCapability()
    {
        super();

        this.defaultSupportsMap.put(GeneralPropertySet.PROTOCOL, Boolean.TRUE);
        this.defaultSupportsMap.put(GeneralPropertySet.HOSTNAME, Boolean.TRUE);
        this.defaultSupportsMap.put(ServletPropertySet.PORT, Boolean.TRUE);
        this.defaultSupportsMap.put(GeneralPropertySet.JAVA_HOME, Boolean.TRUE);
    }
}
