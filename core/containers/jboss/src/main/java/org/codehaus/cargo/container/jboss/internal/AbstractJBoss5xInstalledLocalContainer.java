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
package org.codehaus.cargo.container.jboss.internal;

import java.io.File;

import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.spi.jvm.JvmLauncher;

/**
 * Abstract class for JBoss 5x container family.
 * 
 * @version $Id$
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
    @Override
    protected void doStart(JvmLauncher java) throws Exception
    {
        java.setSystemProperty("jboss.common.lib.url",
            new File(getCommonLibDir()).toURI().toURL().toString());
        super.doStart(java);
    }

    /**
     * {@inheritDoc}
     */
    public String getDeployersDir(String configurationName)
    {
        return getSpecificConfigurationDir("deployers", configurationName);
    }

    /**
     * {@inheritDoc}
     */
    public String getCommonLibDir()
    {
        return getFileHandler().append(getHome(), "common/lib");
    }
}
