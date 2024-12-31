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
package org.codehaus.cargo.container.tomcat.internal;

import org.codehaus.cargo.container.configuration.builder.ConfigurationBuilder;
import org.codehaus.cargo.container.configuration.builder.ConfigurationChecker;
import org.codehaus.cargo.container.spi.configuration.builder.AbstractConfigurationBuilderTest;

/**
 * Unit tests for {@link Tomcat4xConfigurationBuilder}.
 */
public class Tomcat4xConfigurationBuilderTest extends AbstractConfigurationBuilderTest
{
    /**
     * @return {@link Tomcat4xConfigurationBuilder}.
     */
    @Override
    protected ConfigurationBuilder createConfigurationBuilder()
    {
        return new Tomcat4xConfigurationBuilder();
    }

    /**
     * @return {@link Tomcat4xConfigurationChecker}.
     */
    @Override
    protected ConfigurationChecker createConfigurationChecker()
    {
        return new Tomcat4xConfigurationChecker();
    }

    /**
     * Checks that creating datasource configuration entries with driver-configured local
     * transaction support throws an exception with a good message. {@inheritDoc}
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
            fail("should have received an exception");
        }
        catch (UnsupportedOperationException e)
        {
            assertEquals(
                "Tomcat does not support LOCAL_TRANSACTION for DataSource implementations.", e
                    .getMessage());
        }
    }

    /**
     * Checks that creating datasource configuration entries with driver-configured XA transaction
     * support throws an exception with a good message. {@inheritDoc}
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
            fail("should have received an exception");
        }
        catch (UnsupportedOperationException e)
        {
            assertEquals(
                "Tomcat does not support XA_TRANSACTION for DataSource implementations.", e
                    .getMessage());
        }
    }

    /**
     * Checks that creating XA datasource configuration entries throws an exception with a good
     * message. {@inheritDoc}
     * @throws Exception If anything goes wrong.
     */
    @Override
    public void testBuildConfigurationEntryForXADataSourceConfiguredDataSource() throws Exception
    {
        try
        {
            super.testBuildConfigurationEntryForXADataSourceConfiguredDataSource();
            fail("should have received an exception");
        }
        catch (UnsupportedOperationException e)
        {
            assertEquals(
                "Tomcat does not support XADataSource configured DataSource implementations.", e
                    .getMessage());
        }
    }
}
