/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2025 Ali Tokmen.
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
package org.codehaus.cargo.container.glassfish.internal;

import org.codehaus.cargo.container.deployable.DeployableType;

/**
 * GlassFish 3.x, 4.x, 5.x, 6.x and 7.x container capability.
 */
public class GlassFish3x4x5x6x7xContainerCapability extends GlassFish2xContainerCapability
{
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean supportsDeployableType(DeployableType type)
    {
        return super.supportsDeployableType(type) || DeployableType.BUNDLE.equals(type);
    }

}
