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
package org.codehaus.cargo.container.weblogic;

import java.io.File;
import java.util.List;

import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.DeployableException;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.deployable.EAR;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.spi.deployer.AbstractInstalledLocalDeployer;
import org.codehaus.cargo.util.Dom4JUtil;
import org.codehaus.cargo.util.FileHandler;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;

/**
 * Static deployer that manages deployment configuration by manipulating the WebLogic config.xml
 * file.
 * 
 * @version $Id$
 */
public class WebLogic9xConfigXmlInstalledLocalDeployer extends AbstractInstalledLocalDeployer
{
    /**
     * The path under which the container resources are stored in the JAR.
     */
    protected static final String RESOURCE_PATH =
        "/org/codehaus/cargo/container/internal/resources/";

    /**
     * used to manipulate the config.xml document.
     */
    private Dom4JUtil xmlTool;

    /**
     * {@inheritDoc}
     * 
     * @param container container to configure
     */
    public WebLogic9xConfigXmlInstalledLocalDeployer(InstalledLocalContainer container)
    {
        super(container);

        xmlTool = new Dom4JUtil();
        xmlTool.getNamespaces().put("weblogic", "http://www.bea.com/ns/weblogic/920/domain");

        // using the same filehandler as the container will help pass unit tests
        FileHandler handler = container.getFileHandler();
        this.setFileHandler(handler);
        xmlTool.setFileHandler(handler);
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
     * get the DOMAIN_HOME of the server.
     * 
     * @return location to find files like config.xml
     */
    protected String getDomainHome()
    {
        return ((WebLogicConfiguration) getContainer().getConfiguration()).getDomainHome();
    }

    /**
     * {@inheritDoc} deploys files by adding their configuration to the config.xml file of the
     * WebLogic server.
     * 
     * @see org.codehaus.cargo.container.spi.deployer.AbstractDeployer#deploy(org.codehaus.cargo.container.deployable.Deployable)
     */
    @Override
    public void deploy(Deployable deployable)
    {
        Document configXml = readConfigXml();
        Element domain = configXml.getRootElement();
        addDeployableToDomain(deployable, domain);
        writeConfigXml(configXml);
    }

    /**
     * {@inheritDoc} undeploys files by removing their configuration to the config.xml file of the
     * WebLogic server.
     * 
     * @see org.codehaus.cargo.container.spi.deployer.AbstractDeployer#undeploy(org.codehaus.cargo.container.deployable.Deployable)
     */
    @Override
    public void undeploy(Deployable deployable)
    {
        Document configXml = readConfigXml();
        Element domain = configXml.getRootElement();
        removeDeployableFromDomain(deployable, domain);
        writeConfigXml(configXml);
    }

    /**
     * Remove the corresponding app-deployment element from the domain of the WebLogic server.
     * 
     * @param deployable - application component to remove
     * @param domain - Domain element of the WebLogic server
     */
    protected void removeDeployableFromDomain(Deployable deployable, Element domain)
    {
        // use contains in case there is whitespace
        List<Element> results = selectAppDeployments(deployable, domain);
        for (Element element : results)
        {
            domain.remove(element);
        }
    }

    /**
     * this will select the node(s) that match the below deployment.
     * 
     * @param deployable what to search for
     * @param domain root element to search in
     * @return list of child elements that match the deployment
     */
    protected List<Element> selectAppDeployments(Deployable deployable, Element domain)
    {
        String xpath =
            "//weblogic:app-deployment[weblogic:name/text()='"
                + createIdForDeployable(deployable) + "']";
        Element toSearch = domain;
        return xmlTool.selectElementsMatchingXPath(xpath, toSearch);
    }

    /**
     * Create and insert an app-deployment element into the domain of the WebLogic server. Ensure
     * that schema ordering is correct.
     * 
     * @param deployable - application component to configure
     * @param domain - Domain element of the WebLogic server
     */
    protected void addDeployableToDomain(Deployable deployable, Element domain)
    {
        createElementForDeployableInDomain(deployable, domain);
        reorderAppDeploymentsAfterConfigurationVersion(domain);
    }

    /**
     * create the config.xml element representing the Deployable. In WebLogic 9x, this is the
     * element app-deployment.
     * 
     * @param deployable to configure
     * @param domain root element of the config.xml file
     * @return app-deployment element
     */
    protected Element createElementForDeployableInDomain(Deployable deployable, Element domain)
    {
        QName appDeploymentQName =
            new QName("app-deployment", new Namespace("",
                "http://www.bea.com/ns/weblogic/920/domain"));
        Element appDeployment = domain.addElement(appDeploymentQName);
        String id = createIdForDeployable(deployable);
        // the name element is a unique identifier in the config.xml file. that's why this is being
        // named id as opposed to name
        Element appId = appDeployment.addElement("name");
        appId.setText(id);
        Element target = appDeployment.addElement("target");
        target.setText(getServerName());
        Element sourcePath = appDeployment.addElement("source-path");
        sourcePath.setText(getAbsolutePath(deployable));
        return appDeployment;
    }

    /**
     * Per current schema of the weblogic domain, app-deployment elements need to come directly
     * after the configuration-version element.
     * 
     * @param domain - domain to re-order
     */
    protected void reorderAppDeploymentsAfterConfigurationVersion(Element domain)
    {
        Element configurationVersion =
            xmlTool.selectElementMatchingXPath("weblogic:configuration-version", domain);

        List<Element> appDeployments =
            xmlTool.selectElementsMatchingXPath("weblogic:app-deployment", domain);

        List<Element> domainElements = domain.content();
        int indexOfConfigurationVersion = domainElements.indexOf(configurationVersion);

        domainElements.removeAll(appDeployments);
        domainElements.addAll(indexOfConfigurationVersion + 1, appDeployments);

    }

    /**
     * Get a string name for the configuration of this deployable. This should be XML friendly. For
     * example, the String returned will have no slashes or colons, and be as short as possible.
     * 
     * @param deployable used to construct the id
     * @return a string that can be used to name this configuration
     */
    protected String createIdForDeployable(Deployable deployable)
    {
        String name = null;
        // TODO this code should be moved into the deployable objects themselves, as they
        // are better responsible for their name.
        if (deployable.getType() == DeployableType.WAR)
        {
            name = ((WAR) deployable).getContext();
        }
        else if (deployable.getType() == DeployableType.EAR)
        {
            name = ((EAR) deployable).getName();
        }
        else if (deployable.getType() == DeployableType.EJB
            || deployable.getType() == DeployableType.RAR)
        {
            name = createIdFromFileName(deployable);
        }
        else
        {
            throw new DeployableException("name extraction for " + deployable.getType()
                + " not currently supported");
        }
        return name;
    }

    /**
     * Get a string name for the configuration of this deployable based on its filename.
     * 
     * @param deployable used to construct the id
     * @return a string that can be used to name this configuration
     */
    protected String createIdFromFileName(Deployable deployable)
    {
        File file = new File(deployable.getFile());
        return file.getName();
    }

    /**
     * return the running server's name.
     * 
     * @return the WebLogic server's name
     */
    protected String getServerName()
    {
        return getContainer().getConfiguration().getPropertyValue(WebLogicPropertySet.SERVER);
    }

    /**
     * gets the URI from a file. This is the basic filename. ex. web.war.
     * 
     * @param deployable - what to extract the uri from
     * @return - uri of the deployable
     */
    String getURI(Deployable deployable)
    {
        String path = deployable.getFile();
        return new File(path).getName();
    }

    /**
     * gets the absolute path from a file that may be relative to the current directory.
     * 
     * @param deployable - what to extract the file path from
     * @return - absolute path to the deployable
     */
    String getAbsolutePath(Deployable deployable)
    {
        String path = deployable.getFile();
        return getFileHandler().getAbsolutePath(path);
    }

}
