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
package org.codehaus.cargo.container.stub;

import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.configuration.RuntimeConfiguration;

/**
 * Mock for {@link RuntimeConfiguration}. We need a static mock rather than a dynamic mock (which
 * we could get using JMock for example) because we're testing factory classes which create an
 * object out of a class name.
 * 
 */
public class RuntimeConfigurationStub extends AbstractConfigurationStub
    implements RuntimeConfiguration
{
    /**
     * {@inheritDoc}
     * @return {@link ConfigurationType#RUNTIME}
     */
    public ConfigurationType getType()
    {
        return ConfigurationType.RUNTIME;
    }
}
