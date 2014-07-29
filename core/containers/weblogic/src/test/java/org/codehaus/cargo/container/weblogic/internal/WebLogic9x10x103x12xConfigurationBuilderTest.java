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
package org.codehaus.cargo.container.weblogic.internal;

import org.codehaus.cargo.container.configuration.builder.ConfigurationBuilder;
import org.codehaus.cargo.container.configuration.builder.ConfigurationChecker;

/**
 * Integration tests for {@link WebLogic9x10x103x12xConfigurationBuilder}.
 * 
 * @version $Id$
 */
public class WebLogic9x10x103x12xConfigurationBuilderTest extends
    WebLogic8xConfigurationBuilderTest
{
    /**
     * {@inheritDoc}
     * @return {@link WebLogic9x10x103x12xConfigurationBuilder}.
     */
    @Override
    protected ConfigurationBuilder createConfigurationBuilder()
    {
        return new WebLogic9x10x103x12xConfigurationBuilder("server");
    }

    /**
     * {@inheritDoc}
     * @return {@link WebLogic9x10x103x12xConfigurationChecker}.
     */
    @Override
    protected ConfigurationChecker createConfigurationChecker()
    {
        return new WebLogic9x10x103x12xConfigurationChecker("server");
    }
}
