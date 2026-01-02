/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2026 Ali Tokmen.
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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.configuration.entry.DataSource;
import org.codehaus.cargo.container.jboss.internal.JBossInstalledLocalContainer;
import org.codehaus.cargo.container.jboss.internal.JBoss3x4xStandaloneLocalConfigurationCapability;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.LoggingLevel;
import org.codehaus.cargo.container.property.TransactionSupport;
import org.codehaus.cargo.container.spi.configuration.AbstractStandaloneLocalConfiguration;
import org.codehaus.cargo.util.CargoException;

/**
 * Implementation of a standalone {@link org.codehaus.cargo.container.configuration.Configuration}
 * for JBoss 3.x series and JBoss 4.x series.
 */
public class JBoss3x4xStandaloneLocalConfiguration extends AbstractStandaloneLocalConfiguration
{

    /**
     * JBoss container capability.
     */
    private static final ConfigurationCapability CAPABILITY =
        new JBoss3x4xStandaloneLocalConfigurationCapability();

    /**
     * JBoss container instance.
     */
    protected JBossInstalledLocalContainer jbossContainer;

    /**
     * Name of the JBoss log4j configuration file.
     */
    protected String log4jFileName = "log4j.xml";

    /**
     * {@inheritDoc}
     * @see AbstractStandaloneLocalConfiguration#AbstractStandaloneLocalConfiguration(String)
     */
    public JBoss3x4xStandaloneLocalConfiguration(String dir)
    {
        super(dir);

        setProperty(GeneralPropertySet.RMI_PORT, "1099");
        setProperty(JBossPropertySet.CONFIGURATION, "default");
        setProperty(JBossPropertySet.DEPLOYER_KEEP_ORIGINAL_WAR_FILENAME, "false");
        setProperty(JBossPropertySet.JBOSS_NAMING_PORT, "1098");
        setProperty(JBossPropertySet.JBOSS_CLASSLOADING_WEBSERVICE_PORT, "8083");
        setProperty(JBossPropertySet.JBOSS_JRMP_PORT, "1090");
        setProperty(JBossPropertySet.JBOSS_JRMP_INVOKER_PORT, "4444");
        setProperty(JBossPropertySet.JBOSS_INVOKER_POOL_PORT, "4445");
        setProperty(JBossPropertySet.JBOSS_REMOTING_TRANSPORT_PORT, "4446");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ConfigurationCapability getCapability()
    {
        return CAPABILITY;
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
     */
    @Override
    protected void doConfigure(LocalContainer container) throws Exception
    {
        getLogger().info("Configuring JBoss using the ["
            + getPropertyValue(JBossPropertySet.CONFIGURATION) + "] server configuration",
            this.getClass().getName());

        setupConfigurationDir();

        jbossContainer = (JBossInstalledLocalContainer) container;

        Map<String, String> replacements = createJBossReplacements(jbossContainer);

        String deployDir = getFileHandler().createDirectory(getHome(), "/deploy");
        String libDir = getFileHandler().createDirectory(getHome(), "/lib");
        String confDir = getFileHandler().createDirectory(getHome(), "/conf");

        String clustered = jbossContainer.getConfiguration().
            getPropertyValue(JBossPropertySet.CLUSTERED);

        if (Boolean.parseBoolean(clustered))
        {
            String farmDir = getFileHandler().createDirectory(getHome(), "/farm");
        }

        // Copy configuration files from cargo resources directory with token replacement
        String[] cargoFiles = new String[] {"cargo-binding.xml", "jboss-service.xml"};
        for (String cargoFile : cargoFiles)
        {
            getResourceUtils().copyResource(
                RESOURCE_PATH + jbossContainer.getId() + "/" + cargoFile,
                    new File(confDir, cargoFile), replacements, StandardCharsets.UTF_8);
        }

        // Copy resources from jboss installation folder and exclude files
        // that already copied from cargo resources folder
        copyExternalResources(
            new File(jbossContainer.getConfDir(getPropertyValue(JBossPropertySet.CONFIGURATION))),
            new File(confDir), cargoFiles);

        // CARGO-825: Configure the logging append property
        String jbossLog4jXml = getFileHandler().append(confDir, this.log4jFileName);
        String isAppend = "<param name=\"Append\" value=\""
            + Boolean.toString(jbossContainer.isAppend()) + "\"/>";
        replacements = new HashMap<String, String>(2);
        replacements.put("<param name=\"Append\" value=\"false\"/>", isAppend);
        replacements.put("<param name=\"Append\" value=\"true\"/>", isAppend);
        getFileHandler().replaceInFile(jbossLog4jXml, replacements, StandardCharsets.UTF_8, true);

        // Copy the files within the JBoss Deploy directory to the cargo deploy directory
        copyExternalResources(new File(jbossContainer.getDeployDir(getPropertyValue(
            JBossPropertySet.CONFIGURATION))), new File(deployDir));

        // Deploy the CPC (Cargo Ping Component) to the webapps directory
        getResourceUtils().copyResource(RESOURCE_PATH + "cargocpc.war",
            new File(getHome(), "/deploy/cargocpc.war"));

        // Deploy with user defined deployables with the appropriate deployer
        JBossInstalledLocalDeployer deployer = new JBossInstalledLocalDeployer(jbossContainer);
        deployer.deploy(getDeployables());

        deployDatasources(deployDir);
    }

    /**
     * Deploys the JBoss datasources.
     * 
     * @param deployDir The JBoss <code>deploy</code> directory.
     * @throws IOException If copying the datasource XMLs fail.
     */
    protected void deployDatasources(String deployDir) throws IOException
    {
        for (DataSource ds : getDataSources())
        {
            String sourceFile = RESOURCE_PATH + "jboss-ds/";

            if (TransactionSupport.NO_TRANSACTION.equals(ds.getTransactionSupport()))
            {
                sourceFile += "jboss-ds-no-transaction.xml";
            }
            else if (TransactionSupport.LOCAL_TRANSACTION.equals(ds.getTransactionSupport()))
            {
                sourceFile += "jboss-ds-local-transaction.xml";
            }
            else if (TransactionSupport.XA_TRANSACTION.equals(ds.getTransactionSupport()))
            {
                sourceFile += "jboss-ds-xa-transaction.xml";
            }
            else
            {
                throw new CargoException("Unknown transaction type " + ds.getTransactionSupport());
            }

            Map<String, String> replacements = new HashMap<String, String>(5);
            replacements.put("jndiName", ds.getJndiLocation());
            replacements.put("url", ds.getUrl());
            replacements.put("driverClass", ds.getDriverClass());
            replacements.put("username", ds.getUsername());
            replacements.put("password", ds.getPassword());

            getResourceUtils().copyResource(sourceFile, new File(
                getFileHandler().append(deployDir, "cargo-" + ds.getId() + "-ds.xml")),
                    replacements, StandardCharsets.UTF_8);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void verify()
    {
        super.verify();

        String configurationName = getPropertyValue(JBossPropertySet.CONFIGURATION);
        if (configurationName == null || configurationName.isEmpty())
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
     * @throws IOException If an error occurs during the copy.
     */
    protected void copyExternalResources(File sourceDir, File destDir) throws IOException
    {
        copyExternalResources(sourceDir, destDir, null);
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
    protected void copyExternalResources(File sourceDir, File destDir, String[] cargoFiles)
        throws IOException
    {
        File[] sourceFiles = sourceDir.listFiles();
        if (sourceFiles != null)
        {
            for (File sourceFile : sourceFiles)
            {
                if (!isExcluded(cargoFiles, sourceFile.getName()))
                {
                    if (sourceFile.isDirectory())
                    {
                        getFileHandler().createDirectory(destDir.getPath(), sourceFile.getName());
                        copyExternalResources(sourceFile,
                            new File(destDir, sourceFile.getName()), cargoFiles);
                    }
                    else
                    {
                        try (FileOutputStream fops =
                            new FileOutputStream(new File(destDir, sourceFile.getName()));
                            FileInputStream fips = new FileInputStream(sourceFile))
                        {
                            getFileHandler().copy(fips, fops);
                        }
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
        if (cargoFiles == null)
        {
            return false;
        }

        for (String cargoFile : cargoFiles)
        {
            if (cargoFile.equals(filename))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Create token replacement in configuration file with user defined token.
     * 
     * @param container the JBoss container instance from which we'll find the JBoss installed files
     * to reference
     * @return token with all the user-defined token value
     * @throws MalformedURLException If an URL is malformed.
     */
    protected Map<String, String> createJBossReplacements(JBossInstalledLocalContainer container)
        throws MalformedURLException
    {
        Map<String, String> replacements = getReplacements();

        String[] version = jbossContainer.getName().split(" ");
        if (version.length < 2)
        {
            throw new IllegalArgumentException("Cannot read JBoss version number from name "
                + jbossContainer.getName());
        }

        if (version[1].length() < 1)
        {
            throw new IllegalArgumentException("Cannot get the major version for version "
                + version[1]);
        }
        String majorVersion = version[1].substring(0, 1);

        if (version[1].length() < 3)
        {
            throw new IllegalArgumentException("Cannot get the minor version for version "
                + version[1]);
        }
        String minorVersion = version[1].substring(2, 3);

        if (version[1].length() < 5)
        {
            throw new IllegalArgumentException("Cannot get the revision for version "
                + version[1]);
        }
        String revisionVersion = version[1].substring(4, 5);

        if (!(Integer.valueOf(majorVersion) <= 3
            && Integer.parseInt(minorVersion) <= 2
            && Integer.valueOf(revisionVersion) <= 7))
        {
            replacements.put("cargo.jboss.server.mode.attr",
                "<attribute name=\"ServerMode\">true</attribute>");
        }

        String bindingXmlFile = getFileHandler().append(getHome(), "conf/cargo-binding.xml");
        replacements.put("cargo.jboss.binding.url", getFileHandler().getURL(bindingXmlFile));

        File libDir =
            new File(container.getLibDir(getPropertyValue(JBossPropertySet.CONFIGURATION)));
        replacements.put("cargo.server.lib.url", libDir.toURI().toURL().toString());

        // just use the original deploy directory and copy all the deployables from the server
        // deploy directory to the cargo one. This is due to JBoss having deployers and sars in
        // the deploy directory which contain config files used to configure the server.
        // By placing these files in the cargo home directory we will now be able to configure them
        // with cargo.
        replacements.put("cargo.server.deploy.url", "deploy/");

        // Setup the shared classpath
        replacements.put("jboss.shared.classpath", getSharedClasspathXml(container));

        return replacements;
    }

    /**
     * Construct the shared classpath XML based on the container.
     * 
     * @param container the JBoss container instance from which we'll find the JBoss installed files
     * to reference
     * @return Shared classpath XML based on the container.
     * @throws MalformedURLException If URL building fails.
     */
    protected String getSharedClasspathXml(JBossInstalledLocalContainer container)
        throws MalformedURLException
    {
        String[] sharedClassPath = container.getSharedClasspath();
        StringBuilder tmp = new StringBuilder();

        if (sharedClassPath != null)
        {
            for (String element : sharedClassPath)
            {
                String fileName = getFileHandler().getName(element);
                String directoryName = getFileHandler().getParent(element);
                URL directoryUrl = new File(directoryName).toURI().toURL();

                tmp.append("<classpath codebase=\"" + directoryUrl + "\" archives=\""
                        + fileName + "\"/>");
                tmp.append("\n");
            }
        }

        String sharedClassPathString = tmp.toString();
        getLogger().debug("Shared loader classpath is " + sharedClassPathString,
            getClass().getName());
        return sharedClassPathString;
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

        if (LoggingLevel.LOW.equalsLevel(cargoLogLevel))
        {
            level = "ERROR";
        }
        else if (LoggingLevel.MEDIUM.equalsLevel(cargoLogLevel))
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
     */
    @Override
    public String toString()
    {
        return "JBoss Standalone Configuration";
    }
}
