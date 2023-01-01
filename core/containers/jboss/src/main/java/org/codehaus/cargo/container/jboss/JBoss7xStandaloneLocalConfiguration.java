/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2023 Ali Tokmen.
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
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.ZipFile;

import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.configuration.entry.DataSource;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.jboss.internal.JBoss7xStandaloneLocalConfigurationCapability;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.LoggingLevel;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.container.property.TransactionSupport;
import org.codehaus.cargo.container.property.User;
import org.codehaus.cargo.container.spi.configuration.AbstractStandaloneLocalConfiguration;
import org.codehaus.cargo.util.CargoException;

/**
 * JBoss 7.x standalone local configuration.
 */
public class JBoss7xStandaloneLocalConfiguration extends AbstractStandaloneLocalConfiguration
{

    /**
     * JBoss configuration used as base.
     */
    public static final String CONFIGURATION = "standalone";

    /**
     * JBoss container capability.
     */
    private static final ConfigurationCapability CAPABILITY =
        new JBoss7xStandaloneLocalConfigurationCapability();

    /**
     * MD5 message digest.
     */
    private MessageDigest md5;

    /**
     * {@inheritDoc}
     * @see AbstractStandaloneLocalConfiguration#AbstractStandaloneLocalConfiguration(String)
     */
    public JBoss7xStandaloneLocalConfiguration(String dir)
    {
        super(dir);

        setProperty(GeneralPropertySet.RMI_PORT, "1099");
        setProperty(JBossPropertySet.JBOSS_HTTPS_PORT, "8443");
        setProperty(JBossPropertySet.JBOSS_JRMP_PORT, "1090");
        setProperty(JBossPropertySet.JBOSS_JMX_PORT, "1091");
        setProperty(JBossPropertySet.JBOSS_MANAGEMENT_NATIVE_PORT, "9999");
        setProperty(JBossPropertySet.JBOSS_MANAGEMENT_HTTP_PORT, "9990");
        setProperty(JBossPropertySet.JBOSS_OSGI_HTTP_PORT, "8090");
        setProperty(JBossPropertySet.JBOSS_REMOTING_TRANSPORT_PORT, "4447");
        setProperty(JBossPropertySet.CONFIGURATION, CONFIGURATION);
        setProperty(JBossPropertySet.DEPLOYER_KEEP_ORIGINAL_WAR_FILENAME, "false");
        setProperty(JBossPropertySet.ALTERNATIVE_MODULES_DIR, "modules");

        try
        {
            md5 = MessageDigest.getInstance("md5");
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new CargoException(
                "Cannot get the MD5 digest for generating the JBoss user properties files", e);
        }
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
     * {@inheritDoc}
     */
    @Override
    protected void performXmlReplacements(LocalContainer container)
    {
        // JBoss 7.x actually supports port offset
        this.revertPortOffset();
        super.performXmlReplacements(container);
        this.applyPortOffset();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(LocalContainer container)
    {
        for (Deployable deployable : getDeployables())
        {
            if (deployable instanceof WAR)
            {
                WAR war = (WAR) deployable;
                if (war.getContext() == null || war.getContext().isEmpty()
                    || war.getContext().equals("/") || war.getContext().equalsIgnoreCase("ROOT"))
                {
                    disableWelcomeRoot();
                    break;
                }
            }
        }

        super.configure(container);

        // Add token filters for authenticated users
        if (!getUsers().isEmpty())
        {
            StringBuilder managementToken = new StringBuilder(
                "# JBoss mgmt-users.properties file generated by CARGO\n");

            for (User user : getUsers())
            {
                managementToken.append(generateUserPasswordLine(user, "ManagementRealm"));
            }

            getFileHandler().writeTextFile(
                getFileHandler().append(getHome(), "/configuration/mgmt-users.properties"),
                    managementToken.toString(), StandardCharsets.ISO_8859_1);
        }
    }

    /**
     * CARGO-1090: Disable the welcome root application.
     */
    protected void disableWelcomeRoot()
    {
        String configurationXmlFile = "configuration/"
            + getPropertyValue(JBossPropertySet.CONFIGURATION) + ".xml";

        addXmlReplacement(
            configurationXmlFile,
            "//server/profile/subsystem/virtual-server",
            "enable-welcome-root", "false");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doConfigure(LocalContainer c) throws Exception
    {
        if (!(c instanceof InstalledLocalContainer))
        {
            throw new CargoException("Only InstalledLocalContainers are supported, got "
                + c.getClass().getName());
        }

        InstalledLocalContainer container = (InstalledLocalContainer) c;

        getLogger().info("Configuring JBoss using the ["
            + getPropertyValue(JBossPropertySet.CONFIGURATION) + "] server configuration",
                this.getClass().getName());

        setProperty("cargo.jboss.logging",
            getJBossLogLevel(getPropertyValue(GeneralPropertySet.LOGGING)));

        String configurationXmlFile = "configuration/"
            + getPropertyValue(JBossPropertySet.CONFIGURATION) + ".xml";

        addXmlReplacement(
            configurationXmlFile,
            "//server/socket-binding-group/socket-binding[@name='http']",
            "port", ServletPropertySet.PORT);
        addXmlReplacement(
            configurationXmlFile,
            "//server/socket-binding-group/socket-binding[@name='https']",
            "port", JBossPropertySet.JBOSS_HTTPS_PORT);
        addXmlReplacement(
            configurationXmlFile,
            "//server/socket-binding-group/socket-binding[@name='jndi']",
            "port", GeneralPropertySet.RMI_PORT);
        addXmlReplacement(
            configurationXmlFile,
            "//server/socket-binding-group/socket-binding[@name='jmx-connector-registry']",
            "port", JBossPropertySet.JBOSS_JRMP_PORT);
        addXmlReplacement(
            configurationXmlFile,
            "//server/socket-binding-group/socket-binding[@name='jmx-connector-server']",
            "port", JBossPropertySet.JBOSS_JMX_PORT);
        addXmlReplacement(
            configurationXmlFile,
            "//server/management/management-interfaces/native-interface[@interface='management']",
            "port", JBossPropertySet.JBOSS_MANAGEMENT_NATIVE_PORT);
        addXmlReplacement(
            configurationXmlFile,
            "//server/management/management-interfaces/http-interface[@interface='management']",
            "port", JBossPropertySet.JBOSS_MANAGEMENT_HTTP_PORT);
        addXmlReplacement(
            configurationXmlFile,
            "//server/socket-binding-group/socket-binding[@name='osgi-http']",
            "port", JBossPropertySet.JBOSS_OSGI_HTTP_PORT);
        addXmlReplacement(
            configurationXmlFile,
            "//server/socket-binding-group/socket-binding[@name='remoting']",
            "port", JBossPropertySet.JBOSS_REMOTING_TRANSPORT_PORT);
        addXmlReplacement(
            configurationXmlFile,
            "//server/profile/subsystem/console-handler/level",
            "name", "cargo.jboss.logging");
        addXmlReplacement(
            configurationXmlFile,
            "//server/profile/subsystem/periodic-rotating-file-handler/level",
            "name", "cargo.jboss.logging");
        addXmlReplacement(
            configurationXmlFile,
            "//server/profile/subsystem/root-logger/level",
            "name", "cargo.jboss.logging");
        addXmlReplacement(
                configurationXmlFile,
                "//server/socket-binding-group",
                "port-offset", GeneralPropertySet.PORT_OFFSET);

        setupConfigurationDir();

        // Copy initial configuration
        String initialConfiguration = getFileHandler().append(container.getHome(), CONFIGURATION);
        getFileHandler().copyDirectory(initialConfiguration, getHome());

        String configurationXML = getFileHandler().append(getHome(), configurationXmlFile);
        if (!getFileHandler().exists(configurationXML))
        {
            throw new CargoException("Missing configuration XML file: " + configurationXML);
        }

        // Create JARs for modules
        Set<String> classpath = new TreeSet<String>();
        if (container.getExtraClasspath() != null && container.getExtraClasspath().length != 0)
        {
            for (String classpathElement : container.getExtraClasspath())
            {
                classpath.add(classpathElement);
            }
        }
        if (container.getSharedClasspath() != null && container.getSharedClasspath().length != 0)
        {
            for (String classpathElement : container.getSharedClasspath())
            {
                classpath.add(classpathElement);
            }
        }

        String tmpDir = getFileHandler().createUniqueTmpDirectory();
        try
        {
            List<String> driversList = new ArrayList<String>();

            StringBuilder datasources = new StringBuilder();
            StringBuilder drivers = new StringBuilder();

            for (DataSource dataSource : getDataSources())
            {
                String dataSourceClass = dataSource.getDriverClass().replace('.', '/') + ".class";
                String dataSourceFile = null;

                for (String classpathElement : classpath)
                {
                    try (ZipFile zip = new ZipFile(classpathElement))
                    {
                        if (zip.getEntry(dataSourceClass) != null)
                        {
                            dataSourceFile = classpathElement;
                        }
                    }
                }

                if (dataSourceFile == null)
                {
                    throw new CargoException("Datasource class " + dataSource.getDriverClass()
                        + " not found in the classpath");
                }

                String moduleName = getFileHandler().getName(dataSourceFile);
                // Strip extension from JAR file to get module name
                moduleName = moduleName.substring(0, moduleName.lastIndexOf('.'));
                // CARGO-1091: JBoss expects subdirectories when the module name contains dots.
                //             Replace all dots with minus to keep a version separator.
                moduleName = moduleName.replace('.', '-');

                String jndiName = dataSource.getJndiLocation();
                if (!jndiName.startsWith("java:/"))
                {
                    jndiName = "java:/" + jndiName;
                    getLogger().warn("JBoss 7 requires datasource JNDI names to start with "
                        + "java:/, hence changing the given JNDI name to: " + jndiName,
                        this.getClass().getName());
                }

                Map<String, String> replacements = new HashMap<String, String>(6);
                replacements.put("moduleName", moduleName);
                replacements.put("driverClass", dataSource.getDriverClass());
                replacements.put("jndiName", jndiName);
                replacements.put("url", dataSource.getUrl());
                replacements.put("username", dataSource.getUsername());
                replacements.put("password", dataSource.getPassword());

                String xa = "";
                if (TransactionSupport.XA_TRANSACTION.equals(dataSource.getTransactionSupport()))
                {
                    xa = "-xa";
                }

                if (!driversList.contains(dataSource.getDriverClass()))
                {
                    driversList.add(dataSource.getDriverClass());

                    String temporaryDriver = getFileHandler().append(tmpDir, "driver.xml");
                    getResourceUtils().copyResource(
                        RESOURCE_PATH + "jboss-ds/jboss-driver" + xa + ".xml",
                            temporaryDriver, getFileHandler(), replacements,
                                StandardCharsets.UTF_8);
                    drivers.append("\n");
                    temporaryDriver =
                        getFileHandler().readTextFile(temporaryDriver, StandardCharsets.UTF_8);
                    int stripTemporaryDriver =
                        temporaryDriver.indexOf("<!-- Appended section starts here -->");
                    if (stripTemporaryDriver > 0)
                    {
                        stripTemporaryDriver =
                            temporaryDriver.indexOf('\n', stripTemporaryDriver);
                        temporaryDriver = temporaryDriver.substring(stripTemporaryDriver + 1);
                    }
                    drivers.append(temporaryDriver);
                }

                String temporaryDatasource = getFileHandler().append(tmpDir, "datasource.xml");
                getResourceUtils().copyResource(
                    RESOURCE_PATH + "jboss-ds/jboss-datasource.xml",
                        temporaryDatasource, getFileHandler(), replacements,
                            StandardCharsets.UTF_8);
                datasources.append("\n");
                temporaryDatasource =
                    getFileHandler().readTextFile(temporaryDatasource, StandardCharsets.UTF_8);
                int stripTemporaryDatasource =
                    temporaryDatasource.indexOf("<!-- Appended section starts here -->");
                if (stripTemporaryDatasource > 0)
                {
                    stripTemporaryDatasource =
                        temporaryDatasource.indexOf('\n', stripTemporaryDatasource);
                    temporaryDatasource =
                        temporaryDatasource.substring(stripTemporaryDatasource + 1);
                }
                datasources.append(temporaryDatasource);
            }

            Map<String, String> replacements = new HashMap<String, String>(1);
            replacements.put("<drivers>", datasources + "\n                <drivers>" + drivers);
            getFileHandler().replaceInFile(configurationXML, replacements, StandardCharsets.UTF_8);
        }
        finally
        {
            getFileHandler().delete(tmpDir);
        }

        // Deploy the deployables into the deployments directory
        String deployments;
        String altDeployDir = container.getConfiguration().
            getPropertyValue(JBossPropertySet.ALTERNATIVE_DEPLOYMENT_DIR);
        if (altDeployDir != null && !altDeployDir.isEmpty())
        {
            container.getLogger().info("Using non-default deployment target directory "
                + altDeployDir, this.getClass().getName());
            deployments = getFileHandler().append(getHome(), altDeployDir);
        }
        else
        {
            deployments = getFileHandler().append(getHome(), "deployments");
        }

        getResourceUtils().copyResource(RESOURCE_PATH + "cargocpc.war",
            new File(deployments, "cargocpc.war"));
        JBoss7xInstalledLocalDeployer deployer = new JBoss7xInstalledLocalDeployer(container);
        deployer.deploy(getDeployables());
    }

    /**
     * Generate the user and password line for th JBoss users properties file.
     * @param user User object.
     * @param realm Real (for example, <code>ApplicationRealm</code>)
     * @return User and password line for th JBoss users properties file.
     */
    protected String generateUserPasswordLine(User user, String realm)
    {
        String toHash = user.getName() + ":" + realm + ":" + user.getPassword();
        byte[] hash = md5.digest(toHash.getBytes(StandardCharsets.UTF_8));

        StringBuilder sb = new StringBuilder();
        sb.append(user.getName());
        sb.append("=");
        for (byte hashByte : hash)
        {
            sb.append(String.format("%02x", hashByte));
        }
        sb.append('\n');
        return sb.toString();
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
            level = "INFO";
        }
        else
        {
            level = "DEBUG";
        }

        return level;
    }
}
