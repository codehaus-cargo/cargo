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

import org.apache.tools.ant.types.FilterChain;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.jboss.JBossInstalledLocalDeployer;
import org.codehaus.cargo.container.jboss.JBossPropertySet;
import org.codehaus.cargo.container.jboss.JBossStandaloneLocalConfiguration;

/**
 * Basis for all JBoss 5.x and 6.x standalone local configurations.
 * 
 * @version $Id$
 */
public abstract class AbstractJBoss5xStandaloneLocalConfiguration
    extends JBossStandaloneLocalConfiguration
{

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.spi.configuration.AbstractStandaloneLocalConfiguration#AbstractStandaloneLocalConfiguration(String)
     */
    public AbstractJBoss5xStandaloneLocalConfiguration(String dir)
    {
        super(dir);
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.spi.configuration.AbstractLocalConfiguration#configure(LocalContainer)
     */
    @Override
    protected void doConfigure(LocalContainer container) throws Exception
    {
        getLogger().info("Configuring JBoss using the ["
            + getPropertyValue(JBossPropertySet.CONFIGURATION) + "] server configuration",
            this.getClass().getName());

        setupConfigurationDir();

        jbossContainer = (AbstractJBossInstalledLocalContainer) container;

        FilterChain filterChain = createJBossFilterChain(jbossContainer);

        String deployDir = getFileHandler().createDirectory(getHome(), "/deploy");
        String deployersDir = getFileHandler().createDirectory(getHome(), "/deployers");
        String libDir = getFileHandler().createDirectory(getHome(), "/lib");
        String confDir = getFileHandler().createDirectory(getHome(), "/conf");
        String confBootstrapDir = getFileHandler().createDirectory(getHome(), "/conf/bootstrap");

        if (Boolean.valueOf(jbossContainer.getConfiguration().
                getPropertyValue(JBossPropertySet.CLUSTERED)).booleanValue())
        {
            String farmDir = getFileHandler().createDirectory(getHome(), "/farm");
        }

        String[] configFiles = new String[] {"jboss-service.xml"};

        // Copy configuration files from cargo resources directory with token replacement
        for (String configFile : configFiles)
        {
            getResourceUtils().copyResource(
                RESOURCE_PATH + jbossContainer.getId() + "/" + configFile,
                new File(confDir, configFile), filterChain, "UTF-8");
        }

        // Copy resources from jboss installation folder and exclude files
        // that already copied from cargo resources folder
        copyExternalResources(new File(jbossContainer.getConfDir(getPropertyValue(
            JBossPropertySet.CONFIGURATION))), new File(confDir), configFiles);

        // Copy the files within the JBoss Deploy directory to the cargo deploy directory
        copyExternalResources(new File(jbossContainer.getDeployDir(getPropertyValue(
            JBossPropertySet.CONFIGURATION))), new File(deployDir), new String[0]);

        // Copy the files within the JBoss Deployers directory to the cargo deployers directory
        copyExternalResources(new File(((JBoss5xInstalledLocalContainer) jbossContainer)
            .getDeployersDir(getPropertyValue(JBossPropertySet.CONFIGURATION))),
            new File(deployersDir), new String[0]);

        // Deploy the CPC (Cargo Ping Component) to the webapps directory
        getResourceUtils().copyResource(RESOURCE_PATH + "cargocpc.war",
            new File(getHome(), "/deploy/cargocpc.war"));

        // Deploy with user defined deployables with the appropriate deployer
        JBossInstalledLocalDeployer deployer = new JBossInstalledLocalDeployer(jbossContainer);
        deployer.deploy(getDeployables());
    }

}
