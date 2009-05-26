/*
 * ========================================================================
 *
 * Copyright 2008 Vincent Massol.
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
import java.net.MalformedURLException;

import org.apache.tools.ant.types.FilterChain;
import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.jboss.internal.JBoss5xInstalledLocalContainer;

/**
 * 
 *@version $Id: JBoss5xStandaloneLocalConfiguration.java 1850 2008-12-11 19:30:42Z mwringe $
 */
public class JBoss51xStandaloneLocalConfiguration extends JBossStandaloneLocalConfiguration
{

    /**
     * {@inheritDoc}
     * @see AbstractStandaloneLocalConfiguration#AbstractStandaloneLocalConfiguration(String)
     */
    public JBoss51xStandaloneLocalConfiguration(String dir)
    {
        super(dir);
    }

    /**
     * Create and return a filterchain for the JBoss configuration files.
     * 
     * @param container  The container
     * @return the filterchain  The filterchain
     * @throws MalformedURLException If a MalformedURLException occurs
     */
    protected FilterChain createJBossFilterChain(JBoss51xInstalledLocalContainer container)
        throws MalformedURLException
    {
        FilterChain filterChain = super.createJBossFilterChain(container);

        // add the deployer directory needed for JBoss51x
        File deployersDir =
            new File(container.getDeployersDir(getPropertyValue(JBossPropertySet.CONFIGURATION)));
        getAntUtils().addTokenToFilterChain(filterChain, "cargo.jboss.deployers.url",
            deployersDir.toURL().toString());
        // add the deploy directory needed for JBoss51x
        File deployDir =
            new File(container.getDeployDir(getPropertyValue(JBossPropertySet.CONFIGURATION)));
        getAntUtils().addTokenToFilterChain(filterChain, "cargo.jboss.deploy.url",
            deployDir.toURL().toString());
        
        return filterChain;
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.spi.configuration.AbstractLocalConfiguration#configure(LocalContainer)
     */
    protected void doConfigure(LocalContainer container) throws Exception
    {
        getLogger().info("Configuring JBoss using the ["
            + getPropertyValue(JBossPropertySet.CONFIGURATION) + "] server configuration",
            this.getClass().getName());

        setupConfigurationDir();

        jbossContainer = (JBoss51xInstalledLocalContainer) container;

        FilterChain filterChain = createJBossFilterChain(
                (JBoss51xInstalledLocalContainer) jbossContainer);

        // Deploy with user defined deployables with the appropriate deployer
        JBossInstalledLocalDeployer deployer = new JBossInstalledLocalDeployer(jbossContainer);
        deployer.deploy(getDeployables());

        // Setup the shared class path
        if (container instanceof InstalledLocalContainer)
        {
            InstalledLocalContainer installedContainer = (InstalledLocalContainer) container;
            String[] sharedClassPath = installedContainer.getSharedClasspath();
            StringBuffer tmp = new StringBuffer();
            if (sharedClassPath != null)
            {
                for (int i = 0; i < sharedClassPath.length; i++)
                {
                    String fileName = getFileHandler().getName(sharedClassPath[i]);
                    String directoryName = getFileHandler().getParent(sharedClassPath[i]);

                    tmp.append("<classpath codebase=\"" + directoryName + "\" archives=\""
                            + fileName + "\"/>");
                    tmp.append("\n");
                }
            } 
            String sharedClassPathString = tmp.toString();
            getLogger().debug("Shared loader classpath is " + sharedClassPathString,
                getClass().getName());
            getAntUtils().addTokenToFilterChain(filterChain, "jboss.shared.classpath",
                tmp.toString());
        }

        String deployDir = getFileHandler().createDirectory(getHome(), "/deploy");
        String deployersDir = getFileHandler().createDirectory(getHome(), "/deployers");
        String libDir = getFileHandler().createDirectory(getHome(), "/lib");
        String confDir = getFileHandler().createDirectory(getHome(), "/conf");

        String clustered = jbossContainer.getConfiguration().
            getPropertyValue(JBossPropertySet.CLUSTERED);
        
        if (Boolean.valueOf(jbossContainer.getConfiguration().
                getPropertyValue(JBossPropertySet.CLUSTERED)).booleanValue())
        {
            String farmDir = getFileHandler().createDirectory(getHome(), "/farm");        
        }
       
        String[] configFiles = new String[]
        {"jboss-log4j.xml", "jboss-service.xml"};
        
        // Copy configuration files from cargo resources directory with token replacement
        String[] cargoConfigFiles = new String[] {"jboss-log4j.xml", "jboss-service.xml"};
        for (int i = 0; i < cargoConfigFiles.length; i++)
        {
            getResourceUtils().copyResource(
                RESOURCE_PATH + jbossContainer.getId() + "/" + cargoConfigFiles[i],
                new File(confDir, cargoConfigFiles[i]), filterChain);
        }
        
        // Copy resources from jboss installation folder and exclude files
        // that already copied from cargo resources folder
        copyExternalResources(new File(jbossContainer
                .getConfDir(getPropertyValue(JBossPropertySet.CONFIGURATION))), new File(confDir),
                configFiles);
        
        // Copy the files within the JBoss Deploy directory to the cargo deploy directory
        copyExternalResources(
                 new File(jbossContainer
                 .getDeployDir(getPropertyValue(JBossPropertySet.CONFIGURATION))), new File(
                     deployDir), new String[0]);
       
        getResourceUtils().copyResource(
               RESOURCE_PATH + jbossContainer.getId() + "/" + "server.xml",
               new File(deployDir + "/jbossweb.sar/", "server.xml"), filterChain);
        
        getResourceUtils().copyResource(
                RESOURCE_PATH + jbossContainer.getId() + "/" + "bindings-jboss-beans.xml",
                new File(confDir + "/bindingservice.beans/META-INF", 
                        "bindings-jboss-beans.xml"), filterChain);
 
        // Copy the files within the JBoss Deployers directory to the cargo deployers directory
        copyExternalResources(
                 new File(((JBoss5xInstalledLocalContainer) jbossContainer)
                 .getDeployersDir(getPropertyValue(JBossPropertySet.CONFIGURATION))), new File(
                     deployersDir), new String[0]);
        
        // Deploy the CPC (Cargo Ping Component) to the webapps directory
        getResourceUtils().copyResource(RESOURCE_PATH + "cargocpc.war",
            new File(getHome(), "/deploy/cargocpc.war"));
    }

}
