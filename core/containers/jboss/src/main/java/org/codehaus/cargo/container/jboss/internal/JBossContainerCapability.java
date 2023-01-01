/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2023 Ali Tokmen.
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

import java.util.Arrays;
import java.util.List;

import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.internal.J2EEContainerCapability;

/**
 * Capabilities of the JBoss container.
 */
public class JBossContainerCapability extends J2EEContainerCapability
{
    /**
     * The deployable types supported by the JBoss container, in addition to those specified in
     * <code>J2EEContainerCapability</code>.
     */
    private static final List<DeployableType> ADDITIONAL_SUPPORTED_DEPLOYABLE_TYPES = Arrays
        .asList(DeployableType.EJB, DeployableType.RAR, DeployableType.SAR);

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean supportsDeployableType(DeployableType type)
    {
        return super.supportsDeployableType(type)
            || ADDITIONAL_SUPPORTED_DEPLOYABLE_TYPES.contains(type);
    }

}
