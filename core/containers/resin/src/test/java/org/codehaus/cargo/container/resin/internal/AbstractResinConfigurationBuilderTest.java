package org.codehaus.cargo.container.resin.internal;

import org.codehaus.cargo.container.spi.configuration.builder.AbstractConfigurationBuilderTest;

public abstract class AbstractResinConfigurationBuilderTest extends
    AbstractConfigurationBuilderTest
{

    public AbstractResinConfigurationBuilderTest()
    {
        super();
    }

    public void testBuildConfigurationEntryForDriverConfiguredDataSourceWithLocalTransactionSupport()
        throws Exception
    {
        try
        {
            super
                .testBuildConfigurationEntryForDriverConfiguredDataSourceWithLocalTransactionSupport();
        }
        catch (UnsupportedOperationException e)
        {
            assertEquals(Resin2xConfigurationBuilder.TRANSACTIONS_WITH_XA_OR_JCA_ONLY,
                e.getMessage());
        }
    }

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
