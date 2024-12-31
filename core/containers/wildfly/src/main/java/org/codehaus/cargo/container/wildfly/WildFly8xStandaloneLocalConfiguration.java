/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2025 Ali Tokmen.
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
package org.codehaus.cargo.container.wildfly;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.configuration.entry.DataSource;
import org.codehaus.cargo.container.configuration.script.ScriptCommand;
import org.codehaus.cargo.container.jboss.JBoss7xInstalledLocalDeployer;
import org.codehaus.cargo.container.jboss.JBossPropertySet;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.container.property.TransactionSupport;
import org.codehaus.cargo.container.wildfly.internal.AbstractWildFlyInstalledLocalContainer;
import org.codehaus.cargo.container.wildfly.internal.AbstractWildFlyStandaloneLocalConfiguration;
import org.codehaus.cargo.container.wildfly.internal.WildFly8xStandaloneLocalConfigurationCapability;
import org.codehaus.cargo.container.wildfly.internal.configuration.factory.WildFly8xCliConfigurationFactory;
import org.codehaus.cargo.container.wildfly.internal.configuration.factory.WildFlyCliConfigurationFactory;
import org.codehaus.cargo.container.wildfly.internal.util.WildFlyLogUtils;
import org.codehaus.cargo.container.wildfly.internal.util.WildFlyModuleUtils;

/**
 * WildFly 8.x standalone local configuration.
 */
public class WildFly8xStandaloneLocalConfiguration
    extends AbstractWildFlyStandaloneLocalConfiguration
{

    /**
     * WildFly container capability.
     */
    private static final ConfigurationCapability CAPABILITY =
        new WildFly8xStandaloneLocalConfigurationCapability();

    /**
     * CLI configuration factory.
     */
    private WildFly8xCliConfigurationFactory factory =
            new WildFly8xCliConfigurationFactory(this);

    /**
     * {@inheritDoc}
     * @see AbstractWildFlyStandaloneLocalConfiguration#AbstractWildFlyStandaloneLocalConfiguration(String)
     */
    public WildFly8xStandaloneLocalConfiguration(String dir)
    {
        super(dir);
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
    public WildFlyCliConfigurationFactory getConfigurationFactory()
    {
        return factory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doConfigure(LocalContainer c) throws Exception
    {
        AbstractWildFlyInstalledLocalContainer container =
            (AbstractWildFlyInstalledLocalContainer) c;
        super.doConfigure(c);

        String configurationXmlFile = "configuration/"
                + getPropertyValue(JBossPropertySet.CONFIGURATION) + ".xml";
        String wildFlyLogLevel = WildFlyLogUtils.getWildFlyLogLevel(
                getPropertyValue(GeneralPropertySet.LOGGING));

        // configure ports and logging
        addXmlReplacement(
            configurationXmlFile,
            "//server/socket-binding-group/socket-binding[@name='ajp']",
            "port", JBossPropertySet.JBOSS_AJP_PORT);
        addXmlReplacement(
            configurationXmlFile,
            "//server/socket-binding-group/socket-binding[@name='https']",
            "port", JBossPropertySet.JBOSS_HTTPS_PORT);
        addXmlReplacement(
            configurationXmlFile,
            "//server/socket-binding-group/socket-binding[@name='management-http']",
            "port", JBossPropertySet.JBOSS_MANAGEMENT_HTTP_PORT);
        addXmlReplacement(
            configurationXmlFile,
            "//server/socket-binding-group/socket-binding[@name='management-https']",
            "port", JBossPropertySet.JBOSS_MANAGEMENT_HTTPS_PORT);
        addXmlReplacement(
            configurationXmlFile,
            "//server/socket-binding-group/socket-binding[@name='http']",
            "port", ServletPropertySet.PORT);
        addXmlReplacement(
            configurationXmlFile,
            "//server/socket-binding-group",
            "port-offset", GeneralPropertySet.PORT_OFFSET);
        addXmlReplacement(
            configurationXmlFile,
            "//server/profile/subsystem/root-logger/level",
            "name", wildFlyLogLevel);

        configureClassPath(container);

        configureDataSources(container, configurationXmlFile);

        // deploy deployables
        JBoss7xInstalledLocalDeployer deployer = new JBoss7xInstalledLocalDeployer(container);
        deployer.deploy(getDeployables());
    }

    /**
     * Configures classpath via WildFly modules.
     * @param container reference to a local container
     * */
    private void configureClassPath(AbstractWildFlyInstalledLocalContainer container)
    {
        if (container.getExtraClasspath().length > 0)
        {
            List<ScriptCommand> configurationScript = new ArrayList<ScriptCommand>();

            // add modules
            for (String classpathElement : container.getExtraClasspath())
            {
                addModuleScript(classpathElement, container, configurationScript);
            }
            for (String classpathElement : container.getSharedClasspath())
            {
                addModuleScript(classpathElement, container, configurationScript);
            }

            container.executeScript(configurationScript);
        }
    }

    /**
     * @param container Installed local container.
     * @param configurationXmlFile Configuration XML file (configuration/standalone....xml).
     * @throws IOException In case of IO error when editing config file.
     */
    private void configureDataSources(InstalledLocalContainer container,
        String configurationXmlFile) throws IOException
    {
        String configurationXml = getFileHandler().append(getHome(), configurationXmlFile);
        String tmpDir = getFileHandler().createUniqueTmpDirectory();
        try
        {
            List<String> driversList = new ArrayList<String>();

            StringBuilder datasources = new StringBuilder();
            StringBuilder drivers = new StringBuilder();

            for (DataSource dataSource : getDataSources())
            {
                String moduleName =
                    WildFlyModuleUtils.getDataSourceDriverModuleName(container, dataSource);

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
                        RESOURCE_PATH + "wildfly-8/datasource/jboss-driver" + xa + ".xml",
                            temporaryDriver, getFileHandler(), replacements,
                                StandardCharsets.UTF_8);
                    drivers.append("\n");
                    temporaryDriver = getFileHandler().readTextFile(
                        temporaryDriver, StandardCharsets.UTF_8);
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
                    RESOURCE_PATH + "wildfly-8/datasource/jboss-datasource.xml",
                        temporaryDatasource, getFileHandler(), replacements,
                            StandardCharsets.UTF_8);
                datasources.append("\n");
                temporaryDatasource = getFileHandler().readTextFile(
                    temporaryDatasource, StandardCharsets.UTF_8);
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
            getFileHandler().replaceInFile(configurationXml, replacements, StandardCharsets.UTF_8);
        }
        finally
        {
            getFileHandler().delete(tmpDir);
        }
    }
}
