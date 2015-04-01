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

import org.codehaus.cargo.container.jboss.JBossPropertySet;

/**
 * Capabilities of the JBoss's
 * {@link org.codehaus.cargo.container.jboss.JBossStandaloneLocalConfiguration} configuration.
 * 
 */
public class JBoss72xStandaloneLocalConfigurationCapability
    extends JBoss71xStandaloneLocalConfigurationCapability
{

    /**
     * Initialize JBoss-specific configuration Map.
     */
    public JBoss72xStandaloneLocalConfigurationCapability()
    {
        super();

        this.propertySupportMap.remove(JBossPropertySet.JBOSS_OSGI_HTTP_PORT);
    }

}
