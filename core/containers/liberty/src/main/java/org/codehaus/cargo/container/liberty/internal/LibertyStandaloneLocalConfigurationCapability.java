package org.codehaus.cargo.container.liberty.internal;

import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.spi.configuration.AbstractStandaloneLocalConfigurationCapability;

/**
 * Capabilities of Liberty's standalone local configuration.
 */
public class LibertyStandaloneLocalConfigurationCapability
        extends AbstractStandaloneLocalConfigurationCapability
{

    /**
     * Create the capability for local configuration
     */
    public LibertyStandaloneLocalConfigurationCapability()
    {
        super();
//        propertySupportMap.put(DatasourcePropertySet.DATASOURCE, Boolean.TRUE);
        this.propertySupportMap.put(GeneralPropertySet.RUNTIME_ARGS, Boolean.FALSE);
    }

}
