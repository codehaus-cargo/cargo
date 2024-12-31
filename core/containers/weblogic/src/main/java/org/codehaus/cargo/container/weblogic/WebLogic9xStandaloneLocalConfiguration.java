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
package org.codehaus.cargo.container.weblogic;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.configuration.builder.ConfigurationBuilder;
import org.codehaus.cargo.container.configuration.entry.DataSource;
import org.codehaus.cargo.container.configuration.entry.Resource;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.LoggingLevel;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.container.spi.configuration.builder.AbstractStandaloneLocalConfigurationWithXMLConfigurationBuilder;
import org.codehaus.cargo.container.weblogic.internal.WebLogic8xConfigurationBuilder;
import org.codehaus.cargo.container.weblogic.internal.WebLogic9x10x103x12xConfigurationBuilder;
import org.codehaus.cargo.container.weblogic.internal.WebLogicLocalContainer;
import org.codehaus.cargo.container.weblogic.internal.WebLogic8xStandaloneLocalConfigurationCapability;
import org.codehaus.cargo.util.XmlUtils;
import org.codehaus.cargo.util.FileHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * WebLogic standalone {@link org.codehaus.cargo.container.spi.configuration.ContainerConfiguration}
 * implementation.
 */
public class WebLogic9xStandaloneLocalConfiguration extends
    AbstractStandaloneLocalConfigurationWithXMLConfigurationBuilder implements
    WebLogicConfiguration
{
    /**
     * Capability of the WebLogic standalone configuration.
     */
    private static final ConfigurationCapability CAPABILITY =
        new WebLogic8xStandaloneLocalConfigurationCapability();

    /**
     * used to manipulate the config.xml document.
     */
    private XmlUtils xmlTool;

    /**
     * used to generate the weblogic configuration files
     */
    private Map<String, String> namespaces;

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.spi.configuration.AbstractStandaloneLocalConfiguration#AbstractStandaloneLocalConfiguration(String)
     */
    public WebLogic9xStandaloneLocalConfiguration(String dir)
    {
        super(dir);

        setProperty(WebLogicPropertySet.ADMIN_USER, "weblogic");
        setProperty(WebLogicPropertySet.ADMIN_PWD, "weblogic");
        setProperty(WebLogicPropertySet.SERVER, "server");
        setProperty(WebLogicPropertySet.CONFIGURATION_VERSION, "9.2.3.0");
        setProperty(WebLogicPropertySet.DOMAIN_VERSION, "9.2.3.0");
        setProperty(ServletPropertySet.PORT, "7001");
        setProperty(GeneralPropertySet.HOSTNAME, "localhost");

        namespaces = new HashMap<String, String>();
        namespaces.put("weblogic", "http://www.bea.com/ns/weblogic/920/domain");
        namespaces.put("jdbc", "http://www.bea.com/ns/weblogic/90");

        xmlTool = new XmlUtils();
        xmlTool.setNamespaces(namespaces);
        xmlTool.setFileHandler(getFileHandler());
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
    protected void doConfigure(LocalContainer container) throws Exception
    {
        setupConfigurationDir();

        // in weblogic 9+ config.xml is organized under the config directory
        String configDir = getFileHandler().createDirectory(getDomainHome(), "/config");

        // in weblogic 9+ datasource files are organized under the config/jdbc directory
        getFileHandler().createDirectory(getDomainHome(), "/config/jdbc");

        // in weblogic 9+ sensitive files are organized under the security
        // directory
        String securityDir = getFileHandler().createDirectory(getDomainHome(), "/security");

        // as this is an initial install, this directory will not exist, yet
        getFileHandler().createDirectory(getDomainHome(),
            ((WebLogicLocalContainer) container).getAutoDeployDirectory());

        Map<String, String> replacements = createWebLogicReplacements();

        // make sure you use this method, as it ensures the same filehandler
        // that created the directory will be used to copy the resource.
        // This is especially important for unit testing
        getResourceUtils().copyResource(RESOURCE_PATH + container.getId() + "/config.xml",
            getFileHandler().append(configDir, "config.xml"), getFileHandler(), replacements,
                StandardCharsets.UTF_8);

        WebLogic9x10x12x14xConfigXmlInstalledLocalDeployer deployer =
            new WebLogic9x10x12x14xConfigXmlInstalledLocalDeployer(
                (InstalledLocalContainer) container);
        deployer.deploy(getDeployables());

        getResourceUtils().copyResource(
            RESOURCE_PATH + container.getId() + "/DefaultAuthenticatorInit.ldift",
              getFileHandler().append(securityDir, "DefaultAuthenticatorInit.ldift"),
                    getFileHandler(), replacements, StandardCharsets.UTF_8);

        getResourceUtils().copyResource(
            RESOURCE_PATH + container.getId() + "/SerializedSystemIni.dat",
               getFileHandler().append(securityDir, "SerializedSystemIni.dat"), getFileHandler());

        deployCargoPing((WebLogicLocalContainer) container);
    }

    /**
     * @return filter tokens used in the WebLogic configuration files.
     */
    private Map<String, String> createWebLogicReplacements()
    {
        Map<String, String> replacements = getReplacements();

        replacements.put(WebLogicPropertySet.LOGGING,
            getWebLogicLogLevel(getPropertyValue(GeneralPropertySet.LOGGING)));

        return replacements;
    }

    /**
     * Translate Cargo logging levels into WebLogic logging levels.
     * 
     * @param cargoLogLevel Cargo logging level
     * @return the corresponding WebLogic logging level
     */
    private String getWebLogicLogLevel(String cargoLogLevel)
    {
        String returnVal = "Info";

        if (LoggingLevel.LOW.equalsLevel(cargoLogLevel))
        {
            returnVal = "Warning";
        }
        else if (LoggingLevel.HIGH.equalsLevel(cargoLogLevel))
        {
            returnVal = "Debug";
        }
        else
        {
            // accept default of medium/Info
        }

        return returnVal;
    }

    /**
     * Deploy the Cargo Ping utility to the container.
     * 
     * @param container the container to configure
     * @throws IOException if the cargo ping deployment fails
     */
    protected void deployCargoPing(WebLogicLocalContainer container) throws IOException
    {
        // as this is an initial install, this directory will not exist, yet
        String deployDir =
            getFileHandler().createDirectory(getDomainHome(), container.getAutoDeployDirectory());

        // Deploy the cargocpc web-app by copying the WAR file
        getResourceUtils().copyResource(RESOURCE_PATH + "cargocpc.war",
            getFileHandler().append(deployDir, "cargocpc.war"), getFileHandler());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return "WebLogic 9x Standalone Configuration";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDomainHome()
    {
        return getHome();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getXpathForDataSourcesParent()
    {
        return "//jdbc:jdbc-data-source";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Map<String, String> getNamespaces()
    {
        return namespaces;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ConfigurationBuilder createConfigurationBuilder(LocalContainer container)
    {
        String serverName =
            container.getConfiguration().getPropertyValue(WebLogicPropertySet.SERVER);
        return new WebLogic9x10x103x12xConfigurationBuilder(serverName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getOrCreateDataSourceConfigurationFile(DataSource ds,
        LocalContainer container)
    {
        String path = buildDataSourceFileName(ds);
        createBlankDataSourceFile(path);
        linkDataSourceToConfigXml(ds);
        return path;
    }

    /**
     * This will add a reference to an externally defined datasource file into the config.xml file.
     * 
     * @param ds - datasource to reference
     */
    protected void linkDataSourceToConfigXml(DataSource ds)
    {
        Document configXml = readConfigXml();
        Element domain = configXml.getDocumentElement();
        addDataSourceToDomain(ds, domain);
        writeConfigXml(configXml);
    }

    /**
     * Insert the corresponding datasource element into the domain of the WebLogic server.
     * 
     * @param ds - datasource component to configure
     * @param domain - Domain element of the WebLogic server
     */
    protected void addDataSourceToDomain(DataSource ds, Element domain)
    {
        Element connectionPool = domain.getOwnerDocument().createElement("jdbc-system-resource");
        domain.appendChild(connectionPool);
        Element name = connectionPool.getOwnerDocument().createElement("name");
        connectionPool.appendChild(name);
        name.setTextContent(ds.getId());
        Element target = connectionPool.getOwnerDocument().createElement("target");
        connectionPool.appendChild(target);
        target.setTextContent(getServerName());
        Element descriptorFileName =
            connectionPool.getOwnerDocument().createElement("descriptor-file-name");
        connectionPool.appendChild(descriptorFileName);
        descriptorFileName.setTextContent("jdbc/" + ds.getId() + "-jdbc.xml");
    }

    /**
     * return the running server's name.
     * 
     * @return the WebLogic server's name
     */
    protected String getServerName()
    {
        return getPropertyValue(WebLogicPropertySet.SERVER);
    }

    /**
     * write the domain's config.xml to disk.
     * 
     * @param configXml document to write to disk
     */
    public void writeConfigXml(Document configXml)
    {
        String configFile = getConfigXmlPath();
        xmlTool.saveXml(configXml, configFile);
    }

    /**
     * read the domain's config.xml file into a Document.
     * 
     * @return Document corresponding with config.xml
     */
    public Document readConfigXml()
    {
        String configFile = getConfigXmlPath();
        return xmlTool.loadXmlFromFile(configFile);
    }

    /**
     * Create a blank datasource file with correct namespace.
     * 
     * @param path where to create the base file.
     */
    protected void createBlankDataSourceFile(String path)
    {
        getFileHandler().writeTextFile(path,
            "<jdbc-data-source xmlns=\"http://www.bea.com/ns/weblogic/90\"/>",
                StandardCharsets.UTF_8);
    }

    /**
     * Return the absolute path of the config.xml file.
     * 
     * @return path including config.xml
     */
    protected String getConfigXmlPath()
    {
        String configDir = getFileHandler().append(getDomainHome(), "config");
        String configFile = getFileHandler().append(configDir, "config.xml");
        return configFile;
    }

    /**
     * determines the full path to store the datasource configuration file.
     * 
     * @param ds datasource to determine the filename of
     * @return full path to the datasource configuration file
     */
    protected String buildDataSourceFileName(DataSource ds)
    {
        String configDir = getFileHandler().append(getDomainHome(), "config");
        String jdbcDir = getFileHandler().append(configDir, "jdbc");
        String file = ds.getId() + "-jdbc.xml";
        return getFileHandler().append(jdbcDir, file);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setFileHandler(FileHandler fileHandler)
    {
        super.setFileHandler(fileHandler);
        this.xmlTool.setFileHandler(fileHandler);
    }

    /**
     * {@inheritDoc} This implementation throws an UnsupportedOperationException as Resource
     * configuration is not supported in WebLogic.
     */
    @Override
    protected String getOrCreateResourceConfigurationFile(Resource resource,
        LocalContainer container)
    {
        throw new UnsupportedOperationException(
            WebLogic8xConfigurationBuilder.RESOURCE_CONFIGURATION_UNSUPPORTED);
    }

    /**
     * {@inheritDoc} This implementation throws an UnsupportedOperationException as Resource
     * configuration is not supported in WebLogic.
     */
    @Override
    protected String getXpathForResourcesParent()
    {
        throw new UnsupportedOperationException(
            WebLogic8xConfigurationBuilder.RESOURCE_CONFIGURATION_UNSUPPORTED);
    }

}
