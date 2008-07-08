package org.codehaus.cargo.container.jboss.internal;

import org.codehaus.cargo.container.configuration.LocalConfiguration;

/**
 * Abstract class for JBoss 5x container family.
 * 
 *@version $Id:$
 */
public abstract class AbstractJBoss5xInstalledLocalContainer extends
        AbstractJBossInstalledLocalContainer implements JBoss5xInstalledLocalContainer
{

    /**
     * {@inheritDoc}
     * @see AbstractJBossInstalledLocalContainer#AbstractJBossInstalledLocalContainer(org.codehaus.cargo.container.configuration.LocalConfiguration)
     */
    public AbstractJBoss5xInstalledLocalContainer(LocalConfiguration configuration)
    {
        super(configuration);
    }

    /**
     * {@inheritDoc}
     */
    public String getDeployersDir(String configurationName)
    {
        return getSpecificConfigurationDir("deployers", configurationName);
    }

}
