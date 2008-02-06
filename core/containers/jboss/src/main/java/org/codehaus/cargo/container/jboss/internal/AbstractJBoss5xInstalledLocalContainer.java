package org.codehaus.cargo.container.jboss.internal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.configuration.LocalConfiguration;

public abstract class AbstractJBoss5xInstalledLocalContainer extends
		AbstractJBossInstalledLocalContainer implements JBoss5xInstalledLocalContainer{

	public AbstractJBoss5xInstalledLocalContainer(LocalConfiguration configuration) {
		super(configuration);
	}
	
	public String getDeployersDir(String configurationName)
    {
        return getSpecificConfigurationDir("deployers", configurationName);
    }

}
