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
package org.codehaus.cargo.container.resin.internal;

import org.codehaus.cargo.container.configuration.builder.ConfigurationBuilder;
import org.codehaus.cargo.container.configuration.builder.ConfigurationChecker;

public class Resin3xConfigurationBuilderTest extends Resin2xConfigurationBuilderTest
{

    /**
     * @return {@link Resin3xConfigurationBuilder}.
     */
    @Override
    protected ConfigurationBuilder createConfigurationBuilder()
    {
        return new Resin3xConfigurationBuilder();
    }

    /**
     * @return {@link Resin3xConfigurationChecker}.
     */
    @Override
    protected ConfigurationChecker createConfigurationChecker()
    {
        return new Resin3xConfigurationChecker();
    }

}
