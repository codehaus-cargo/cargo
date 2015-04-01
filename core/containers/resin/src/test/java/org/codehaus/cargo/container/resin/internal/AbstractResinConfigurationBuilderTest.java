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
package org.codehaus.cargo.container.resin.internal;

import org.codehaus.cargo.container.spi.configuration.builder.AbstractConfigurationBuilderTest;

/**
 * Unit tests for Resin servers' configuration builder.
 * 
 */
public abstract class AbstractResinConfigurationBuilderTest extends
    AbstractConfigurationBuilderTest
{

    /**
     * Checks that creating datasource configuration entries with local transaction support
     * throws an exception with message
     * {@link Resin2xConfigurationBuilder#TRANSACTIONS_WITH_XA_OR_JCA_ONLY}. {@inheritDoc}
     * @throws Exception If anything goes wrong.
     */
    @Override
    public void testBuildConfigurationEntryForDriverConfiguredDSWithLocalTransactionSupport()
        throws Exception
    {
        try
        {
            super
                .testBuildConfigurationEntryForDriverConfiguredDSWithLocalTransactionSupport();
        }
        catch (UnsupportedOperationException e)
        {
            assertEquals(Resin2xConfigurationBuilder.TRANSACTIONS_WITH_XA_OR_JCA_ONLY,
                e.getMessage());
        }
    }

    /**
     * Checks that creating datasource configuration entries with driver-configured XA transaction
     * support throws an exception with message
     * {@link Resin2xConfigurationBuilder#TRANSACTIONS_WITH_XA_OR_JCA_ONLY}. {@inheritDoc}
     * @throws Exception If anything goes wrong.
     */
    @Override
    public void testBuildConfigurationEntryForDriverConfiguredDataSourceWithXaTransactionSupport()
        throws Exception
    {
        try
        {
            super
                .testBuildConfigurationEntryForDriverConfiguredDataSourceWithXaTransactionSupport();
        }
        catch (UnsupportedOperationException e)
        {
            assertEquals(Resin2xConfigurationBuilder.TRANSACTIONS_WITH_XA_OR_JCA_ONLY,
                e.getMessage());
        }
    }

}
