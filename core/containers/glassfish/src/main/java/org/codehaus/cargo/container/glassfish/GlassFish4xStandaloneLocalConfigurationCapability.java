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
package org.codehaus.cargo.container.glassfish;

import org.codehaus.cargo.container.property.ServletPropertySet;

/**
 * GlassFish 4.x standalone local configuration capability.
 * 
 * @version $Id$
 */
public class GlassFish4xStandaloneLocalConfigurationCapability extends
    GlassFish3xStandaloneLocalConfigurationCapability
{
    /**
     * CARGO-1246.  GlassFish 4.x is the only one supported for {@link ServletPropertySet#USERS}.
     */
    public GlassFish4xStandaloneLocalConfigurationCapability()
    {
        super();

        this.defaultSupportsMap.put(ServletPropertySet.USERS, Boolean.TRUE);
    }
}
