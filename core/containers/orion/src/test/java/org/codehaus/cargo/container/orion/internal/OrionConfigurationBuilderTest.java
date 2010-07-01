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
package org.codehaus.cargo.container.orion.internal;

import org.codehaus.cargo.container.configuration.builder.ConfigurationBuilder;
import org.codehaus.cargo.container.configuration.builder.ConfigurationChecker;
import org.codehaus.cargo.container.spi.configuration.builder.AbstractConfigurationBuilderTest;

public class OrionConfigurationBuilderTest extends AbstractConfigurationBuilderTest
{
    ConfigurationBuilder builder;

    ConfigurationChecker checker;

    @Override
    protected ConfigurationBuilder createConfigurationBuilder()
    {
        return new OrionConfigurationBuilder();
    }

    @Override
    protected ConfigurationChecker createConfigurationChecker()
    {
        return new OrionConfigurationChecker();
    }

    @Override
    public void testBuildConfigurationEntryForXADataSourceConfiguredResource() throws Exception
    {
        try
        {
            super.testBuildConfigurationEntryForXADataSourceConfiguredResource();
            fail("should have received an exception");

        }
        catch (UnsupportedOperationException e)
        {
            assertEquals(OrionConfigurationBuilder.RESOURCE_CONFIGURATION_UNSUPPORTED, e
                .getMessage());
        }
    }
    
    @Override
    public void testBuildConfigurationEntryForMailSessionConfiguredResource() throws Exception
    {
        try
        {
            super.testBuildConfigurationEntryForMailSessionConfiguredResource();
            fail("should have received an exception");

        }
        catch (UnsupportedOperationException e)
        {
            assertEquals(OrionConfigurationBuilder.RESOURCE_CONFIGURATION_UNSUPPORTED, e
                .getMessage());
        }
    }
}
