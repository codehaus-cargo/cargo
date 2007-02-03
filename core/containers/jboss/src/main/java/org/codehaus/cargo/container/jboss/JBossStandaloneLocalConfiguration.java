/*
 * ========================================================================
 *
 * Copyright 2005-2006 Vincent Massol.
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

import org.apache.tools.ant.types.FilterChain;
import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.jboss.internal.JBossStandaloneLocalConfigurationCapability;
import org.codehaus.cargo.container.jboss.internal.JBossInstalledLocalContainer;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.container.spi.configuration.AbstractStandaloneLocalConfiguration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;

/**
 * Implementation of a standalone {@link org.codehaus.cargo.container.configuration.Configuration}
 * for JBoss 3.x series and JBoss 4.x series.
 *
 * @version $Id: $
 */
public class JBossStandaloneLocalConfiguration extends AbstractStandaloneLocalConfiguration
{
    /**
     * JBoss container capability.
     */
    private static ConfigurationCapability capability =
        new JBossStandaloneLocalConfigurationCapability();
    
    /**
     * JBoss container instance.
     */
    private JBossInstalledLocalContainer jbossContainer;

    /**
     * {@inheritDoc}
     * @see AbstractStandaloneLocalConfiguration#AbstractStandaloneLocalConfiguration(String) 
     */
    public JBossStandaloneLocalConfiguration(String dir)
    {
        super(dir);

        setProperty(GeneralPropertySet.RMI_PORT, "1299");
        setProperty(JBossPropertySet.CONFIGURATION, "default");
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.configuration.Configuration#getCapability()
     */
    public ConfigurationCapability getCapability()
    {
        return capability;
    }

    /**
     * @return Returns the jbossContainer.
     */
    public JBossInstalledLocalContainer getJbossContainer()
    {
        return jbossContainer;
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

        jbossContainer = (JBossInstalledLocalContainer) container;

        FilterChain filterChain = createJBossFilterChain(jbossContainer);

        getFileHandler().createDirectory(getHome(), "/deploy");
        getFileHandler().createDirectory(getHome(), "/lib");

        String confDir = getFileHandler().createDirectory(getHome(), "/conf");

        // Copy configuration files from cargo resources directory with token replacement
        String[] cargoFiles = new String[] {"cargo-binding.xml", "log4j.xml",
            "jboss-service.xml"};
        for (int i = 0; i < cargoFiles.length; i++)
        {
            getResourceUtils().copyResource(
                RESOURCE_PATH + jbossContainer.getId() + "/" + cargoFiles[i],
                new File(confDir, cargoFiles[i]), filterChain);
        }

        // Copy resources from jboss installation folder and exclude files
        // that already copied from cargo resources folder
        copyExternalResources(
            new File(jbossContainer.getConfDir(getPropertyValue(JBossPropertySet.CONFIGURATION))),
            new File(confDir), cargoFiles);

        // Deploy the CPC (Cargo Ping Component) to the webapps directory
        getResourceUtils().copyResource(RESOURCE_PATH + "cargocpc.war",
            new File(getHome(), "/deploy/cargocpc.war"));
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.spi.configuration.AbstractStandaloneLocalConfiguration#verify()
     */
    public void verify()
    {
        super.verify();

        String configurationName = getPropertyValue(JBossPropertySet.CONFIGURATION);
        if ((configurationName == null) || (configurationName.length() == 0))
        {
            throw new ContainerException("Invalid JBoss configuration: ["
                + JBossPropertySet.CONFIGURATION + "] doesn't exist.");
        }
    }

    /**
     * Copy external resources to cargo configuration directory. This method will copy entire
     * resources in the sourceDir (recursive), if it's a directory.
     *
     * @param sourceDir resource file / directory to be copied
     * @param destDir cargo configuration directory
     * @param cargoFiles list of cargo resources file that will excluded
     * @throws IOException If an error occurs during the copy.
     */
    private void copyExternalResources(File sourceDir, File destDir, String[] cargoFiles)
        throws IOException
    {
        File[] sourceFiles = sourceDir.listFiles();
        if (sourceFiles != null)
        {
            for (int i = 0; i < sourceFiles.length; i++)
            {
                if (!isExcluded(cargoFiles, sourceFiles[i].getName()))
                {
                    if (sourceFiles[i].isDirectory())
                    {
                        getFileHandler().createDirectory(destDir.getPath(),
                            sourceFiles[i].getName());
                        copyExternalResources(sourceFiles[i], new File(destDir, sourceFiles[i]
                            .getName()), cargoFiles);
                    }
                    else
                    {
                        getFileHandler().copy(new FileInputStream(sourceFiles[i]),
                            new FileOutputStream(new File(destDir, sourceFiles[i].getName())));
                    }

                }
            }
        }
    }

    /**
     * Check if file with name <code>filename</code> is one of cargo resources file.
     *
     * @param cargoFiles list of cargo resources files
     * @param filename filename of the file
     * @return true if <code>filename</code> is one of cargo resources file
     */
    private boolean isExcluded(String[] cargoFiles, String filename)
    {
        for (int i = 0; i < cargoFiles.length; i++)
        {
            if (cargoFiles[i].equals(filename))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Create filter to replace token in configuration file with user defined token.
     *
     * @param container the JBoss contaiber instance from which we'll find the JBoss installed files
     *        to reference
     * @return token with all the user-defined token value
     * @throws MalformedURLException If an URL is malformed.
     * @link MalformedURLException
     */
    protected FilterChain createJBossFilterChain(JBossInstalledLocalContainer container)
        throws MalformedURLException
    {
        FilterChain filterChain = new FilterChain();
        
        String[] version = jbossContainer.getName().split(" ");
        String mayorVersion = version[1].substring(0, 1);
        String minorVersion = version[1].substring(2, 3);
        String revisionVersion = version[1].substring(4, 5);
        
        if (!((Integer.valueOf(mayorVersion).intValue() <= 3)
            && (Integer.valueOf(minorVersion).intValue() <= 2)
            && (Integer.valueOf(revisionVersion).intValue() <= 7)))
        {
            getAntUtils().addTokenToFilterChain(filterChain, "cargo.jboss.server.mode.attr",
                "<attribute name=\"ServerMode\">true</attribute>");
        }

        String bindingXmlFile = getFileHandler().append(getHome(), "conf/cargo-binding.xml");
        getAntUtils().addTokenToFilterChain(filterChain, "cargo.jboss.binding.url",
            getFileHandler().getURL(bindingXmlFile));

        getAntUtils().addTokenToFilterChain(filterChain, GeneralPropertySet.RMI_PORT,
            getPropertyValue(GeneralPropertySet.RMI_PORT));

        getAntUtils().addTokenToFilterChain(filterChain, ServletPropertySet.PORT,
            getPropertyValue(ServletPropertySet.PORT));

        getAntUtils().addTokenToFilterChain(filterChain, GeneralPropertySet.LOGGING,
            getJBossLogLevel(getPropertyValue(GeneralPropertySet.LOGGING)));

        File libDir =
            new File(container.getLibDir(getPropertyValue(JBossPropertySet.CONFIGURATION)));
        getAntUtils().addTokenToFilterChain(filterChain, "cargo.server.lib.url",
            libDir.toURL().toString());

        // String representation of scanned folder and archive
        StringBuffer buffer = new StringBuffer();

        // Initiate the value of scanned folder or archive with cargo deploy
        // directory and existing jboss deploy directory
        File deployDir =
            new File(container.getDeployDir(getPropertyValue(JBossPropertySet.CONFIGURATION)));
        buffer.append("deploy/, ").append(deployDir.toURL().toString());

        // Deploy with user defined deployables with the appropriate deployer
        JBossInstalledLocalDeployer deployer = new JBossInstalledLocalDeployer(jbossContainer);
        deployer.deploy(getDeployables());

        getAntUtils().addTokenToFilterChain(filterChain, "cargo.server.deploy.url",
            buffer.toString());

        return filterChain;
    }

    /**
     * Translate Cargo logging levels into JBoss logging levels.
     *
     * @param cargoLogLevel Cargo logging level
     * @return the corresponding JBoss logging level
     */
    private String getJBossLogLevel(String cargoLogLevel)
    {
        String level;

        if (cargoLogLevel.equalsIgnoreCase("low"))
        {
            level = "ERROR";
        }
        else if (cargoLogLevel.equalsIgnoreCase("medium"))
        {
            level = "WARN";
        }
        else
        {
            level = "INFO";
        }

        return level;
    }

    /**
     * {@inheritDoc}
     * @see Object#toString()
     */
    public String toString()
    {
        return "JBoss Standalone Configuration";
    }
}
