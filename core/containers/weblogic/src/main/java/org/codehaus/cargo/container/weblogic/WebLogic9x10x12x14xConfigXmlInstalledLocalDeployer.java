/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2024 Ali Tokmen.
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
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.spi.deployer.AbstractInstalledLocalDeployer;
import org.codehaus.cargo.util.XmlUtils;
import org.codehaus.cargo.util.FileHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Static deployer that manages deployment configuration by manipulating the WebLogic config.xml
 * file.
 */
public class WebLogic9x10x12x14xConfigXmlInstalledLocalDeployer extends
    AbstractInstalledLocalDeployer
{
    /**
     * used to manipulate the config.xml document.
     */
    private XmlUtils xmlTool;

    /**
     * XML namespace to use.
     */
    private String namespace;

    /**
     * {@inheritDoc}
     * 
     * @param container container to configure
     */
    public WebLogic9x10x12x14xConfigXmlInstalledLocalDeployer(InstalledLocalContainer container)
    {
        super(container);

        xmlTool = new XmlUtils();
        if (container instanceof WebLogic12xInstalledLocalContainer
            || container instanceof WebLogic121xInstalledLocalContainer)
        {
            namespace = "http://xmlns.oracle.com/weblogic/domain";
        }
        else
        {
            namespace = "http://www.bea.com/ns/weblogic/920/domain";
        }
        xmlTool.getNamespaces().put("weblogic", namespace);

        // using the same filehandler as the container will help pass unit tests
        FileHandler handler = container.getFileHandler();
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
     */
    @Override
    public void deploy(Deployable deployable)
    {
        Document configXml = readConfigXml();
        Element domain = configXml.getDocumentElement();
        addDeployableToDomain(deployable, domain);
        writeConfigXml(configXml);
    }

    /**
     * {@inheritDoc} undeploys files by removing their configuration to the config.xml file of the
     * WebLogic server.
     */
    @Override
    public void undeploy(Deployable deployable)
    {
        Document configXml = readConfigXml();
        Element domain = configXml.getDocumentElement();
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
            domain.removeChild(element);
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
        Element appDeployment = domain.getOwnerDocument().createElement("app-deployment");
        domain.appendChild(appDeployment);
        // the name element is a unique identifier in the config.xml file. that's why this is being
        // named id as opposed to name
        String id = createIdForDeployable(deployable);
        Element appId = appDeployment.getOwnerDocument().createElement("name");
        appDeployment.appendChild(appId);
        appId.setTextContent(id);
        Element target = appDeployment.getOwnerDocument().createElement("target");
        appDeployment.appendChild(target);
        target.setTextContent(getServerName());
        Element moduleType = appDeployment.getOwnerDocument().createElement("module-type");
        appDeployment.appendChild(moduleType);
        moduleType.setTextContent(deployable.getType().getType());
        Element sourcePath = appDeployment.getOwnerDocument().createElement("source-path");
        appDeployment.appendChild(sourcePath);
        if (deployable.getType() == DeployableType.WAR
            && getFileHandler().exists(getAbsolutePath(deployable)))
        {
            // CARGO-1402: Add support for context path configuration for WebLogic
            WAR war = (WAR) deployable;
            boolean needsCopy;
            if (deployable.isExpanded())
            {
                needsCopy =
                    !getFileHandler().getName(deployable.getFile()).equals(
                        war.getContext());
            }
            else
            {
                needsCopy =
                    getFileHandler().getName(deployable.getFile()).equals(
                        war.getContext() + ".war");
            }
            if (needsCopy)
            {
                String cargodeploy = getFileHandler().createDirectory(
                    ((WebLogicConfiguration) getContainer().getConfiguration()).getDomainHome(),
                        "cargodeploy");
                String targetDirectoryname =
                    getFileHandler().append(cargodeploy, war.getContext());
                getFileHandler().delete(targetDirectoryname);
                String targetFilename =
                    getFileHandler().append(cargodeploy, war.getContext() + ".war");
                getFileHandler().delete(targetFilename);
                if (deployable.isExpanded())
                {
                    getFileHandler().copyDirectory(deployable.getFile(), targetDirectoryname);
                    sourcePath.setTextContent(targetDirectoryname);
                }
                else
                {
                    getFileHandler().copyFile(deployable.getFile(), targetFilename, true);
                    sourcePath.setTextContent(targetFilename);
                }
            }
            else
            {
                sourcePath.setTextContent(getAbsolutePath(deployable));
            }
        }
        else
        {
            sourcePath.setTextContent(getAbsolutePath(deployable));
        }
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
        List<Element> appDeployments =
            xmlTool.selectElementsMatchingXPath("weblogic:app-deployment", domain);
        for (Element appDeployment : appDeployments)
        {
            domain.removeChild(appDeployment);
        }

        Element configurationVersion =
            xmlTool.selectElementMatchingXPath("weblogic:configuration-version", domain);
        Node before = null;
        NodeList children = domain.getChildNodes();
        for (int i = 0; i < children.getLength(); i++)
        {
            if (configurationVersion.equals(children.item(i)) && i < children.getLength() - 1)
            {
                before = children.item(i + 1);
            }
        }
        if (before != null)
        {
            for (Element appDeployment : appDeployments)
            {
                domain.insertBefore(appDeployment, before);
            }
        }
        else
        {
            for (Element appDeployment : appDeployments)
            {
                domain.appendChild(appDeployment);
            }
        }
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
        if (deployable.getType() == DeployableType.WAR
            || deployable.getType() == DeployableType.EAR
            ||  deployable.getType() == DeployableType.EJB
            || deployable.getType() == DeployableType.RAR)
        {
            return deployable.getFilename();
        }
        else
        {
            throw new DeployableException("name extraction for " + deployable.getType()
                + " not currently supported");
        }
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
