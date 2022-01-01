/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2022 Ali Tokmen.
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
import org.codehaus.cargo.generic.ContainerFactory;
import org.codehaus.cargo.generic.DefaultContainerFactory;

/**
 * Abstract validator, that instanciates the {@link ContainerFactory}.
 */
public abstract class AbstractContainerFactoryValidator implements Validator
{
    /**
     * Container type.
     */
    private ContainerType type;

    /**
     * Container factory.
     */
    private final ContainerFactory factory = new DefaultContainerFactory();

    /**
     * Saves the type to check for.
     * @param type type to check for.
     */
    public AbstractContainerFactoryValidator(ContainerType type)
    {
        this.type = type;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean validate(String containerId, ContainerType type)
    {
        return this.factory.isContainerRegistered(containerId, this.type);
    }
}
