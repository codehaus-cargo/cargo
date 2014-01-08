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
package org.codehaus.cargo.container.jboss;

import java.io.File;

import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.jboss.internal.AbstractJBoss5xInstalledLocalContainer;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.RemotePropertySet;
import org.codehaus.cargo.container.spi.jvm.JvmLauncher;

/**
 * JBoss 6.x series container implementation.
 * 
 * @version $Id$
 */
public class JBoss6xInstalledLocalContainer extends AbstractJBoss5xInstalledLocalContainer
{
    /**
     * JBoss 6.x series unique id.
     */
    public static final String ID = "jboss6x";

    /**
     * {@inheritDoc}
     * @see AbstractJBoss5xInstalledLocalContainer#AbstractJBoss5xInstalledLocalContainer(org.codehaus.cargo.container.configuration.LocalConfiguration)
     */
    public JBoss6xInstalledLocalContainer(LocalConfiguration configuration)
    {
        super(configuration);
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.Container#getId()
     */
    public String getId()
    {
        return ID;
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.Container#getName()
     */
    public String getName()
    {
        return "JBoss " + getVersion("6.x");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doStop(JvmLauncher java) throws Exception
    {
        java.addClasspathEntries(new File(getHome(), "bin/shutdown.jar"));
        java.setMainClass("org.jboss.Shutdown");

        java.addAppArguments(
            "--server=service:jmx:rmi:///jndi/rmi://"
                + getConfiguration().getPropertyValue(GeneralPropertySet.HOSTNAME) + ":"
                + getConfiguration().getPropertyValue(JBossPropertySet.JBOSS_JRMP_PORT)
                + "/jmxrmi");

        String username = getConfiguration().getPropertyValue(RemotePropertySet.USERNAME);
        String password = getConfiguration().getPropertyValue(RemotePropertySet.PASSWORD);
        if (username != null)
        {
            java.addAppArguments("--user=" + username);
            if (password != null)
            {
                java.addAppArguments("--password=" + password);
            }
        }

        java.execute();
    }
}
