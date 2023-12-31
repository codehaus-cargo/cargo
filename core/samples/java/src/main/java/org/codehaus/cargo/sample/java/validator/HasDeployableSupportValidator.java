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
package org.codehaus.cargo.sample.java.validator;

import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.generic.ContainerCapabilityFactory;
import org.codehaus.cargo.generic.DefaultContainerCapabilityFactory;

/**
 * Validator, that instanciates the {@link ContainerCapabilityFactory} for {@link DeployableType}.
 */
public class HasDeployableSupportValidator implements Validator
{
    /**
     * Deployable type.
     */
    private DeployableType deployableType;

    /**
     * Container capability factory.
     */
    private final ContainerCapabilityFactory factory = new DefaultContainerCapabilityFactory();

    /**
     * Saves the type to check for.
     * @param deployableType type to check for.
     */
    public HasDeployableSupportValidator(DeployableType deployableType)
    {
        this.deployableType = deployableType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean validate(String containerId, ContainerType type)
    {
        return this.factory.createContainerCapability(containerId).supportsDeployableType(
            this.deployableType);
    }
}
