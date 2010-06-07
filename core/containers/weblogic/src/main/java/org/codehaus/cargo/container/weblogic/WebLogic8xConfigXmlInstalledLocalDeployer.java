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
package org.codehaus.cargo.container.weblogic;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.List;

import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.deployable.EAR;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.spi.deployer.AbstractInstalledLocalDeployer;
import org.codehaus.cargo.util.FileHandler;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.XPath;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

/**
 * Static deployer that manages deployment configuration by manipulating the WebLogic config.xml
 * file.
 * 
 * @version $Id$
 */
public class WebLogic8xConfigXmlInstalledLocalDeployer extends AbstractInstalledLocalDeployer
{
    /**
     * {@inheritDoc}
     * 
     * @param container container to configure
     */
    public WebLogic8xConfigXmlInstalledLocalDeployer(LocalContainer container)
    {
        super(container);
        // using the same filehandler as the container will help pass unit tests
        FileHandler handler = container.getFileHandler();
        setFileHandler(handler);
    }

    /**
     * read the domain's config.xml file into a Document.
     * 
     * @return Document corresponding with config.xml
     */
    public Document readConfigXml()
    {
        Document configXml;
        try
        {
            configXml =
                new SAXReader().read(getFileHandler().getInputStream(
                    getFileHandler().append(getDomainHome(), "config.xml")));
        }
        catch (DocumentException e)
        {
            throw new ContainerException("Error parsing config.xml for " + this.getServerName(),
                e);
        }
        return configXml;
    }

    /**
     * write the domain's config.xml to disk.
     * 
     * @param configXml document to write to disk
     */
    public void writeConfigXml(Document configXml)
    {
        OutputFormat outformat = OutputFormat.createPrettyPrint();
        XMLWriter writer;
        try
        {
            writer =
                new XMLWriter(getFileHandler().getOutputStream(
                    getFileHandler().append(getDomainHome(), "config.xml")), outformat);
            writer.write(configXml);
            writer.flush();
        }
        catch (UnsupportedEncodingException e)
        {
            throw new ContainerException("Error encoding config.xml for " + this.getServerName(),
                e);
        }
        catch (IOException e)
        {
            throw new ContainerException("Error writing config.xml for " + this.getServerName(),
                e);
        }

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
    public void deploy(Deployable deployable)
    {
        Document configXml = readConfigXml();
        XPath xpathSelector = DocumentHelper.createXPath("//Domain");
        List results = xpathSelector.selectNodes(configXml);
        Element domain = (Element) results.get(0);

        if (deployable.getType() == DeployableType.WAR)
        {
            addWarToDomain((WAR) deployable, domain);
        }
        else if (deployable.getType() == DeployableType.EAR)
        {
            addEarToDomain((EAR) deployable, domain);
        }
        else
        {
            throw new ContainerException("Not supported");
        }

        this.writeConfigXml(configXml);
    }

    /**
     * {@inheritDoc} undeploys files by removing their configuration to the config.xml file of the
     * WebLogic server.
     * 
     * @see org.codehaus.cargo.container.spi.deployer.AbstractDeployer#undeploy(org.codehaus.cargo.container.deployable.Deployable)
     */
    public void undeploy(Deployable deployable)
    {
        Document configXml = readConfigXml();
        XPath xpathSelector =
            DocumentHelper.createXPath("//Application[@Path='"
                + getFileHandler().getParent(getAbsolutePath(deployable)) + "']");
        List results = xpathSelector.selectNodes(configXml);
        for (Iterator iter = results.iterator(); iter.hasNext();)
        {
            Element element = (Element) iter.next();
            configXml.remove(element);
        }
        this.writeConfigXml(configXml);

    }

    /**
     * Insert the corresponding web app element into the domain of the WebLogic server.
     * 
     * @param war - web application component to configure
     * @param domain - Domain element of the WebLogic server
     */
    protected void addWarToDomain(WAR war, Element domain)
    {
        Element application = domain.addElement("Application");
        application.addAttribute("Name", "_" + war.getContext() + "_app");
        application.addAttribute("Path", getFileHandler().getParent(getAbsolutePath(war)));
        application.addAttribute("StagingMode", "nostage");
        application.addAttribute("TwoPhase", "false");
        Element webAppComponent = application.addElement("WebAppComponent");
        webAppComponent.addAttribute("Name", war.getContext());
        webAppComponent.addAttribute("Targets", getServerName());
        webAppComponent.addAttribute("URI", getURI(war));
    }

    /**
     * Insert the corresponding ear element into the domain of the WebLogic server.
     * 
     * @param ear - ear to configure
     * @param domain - Domain element of the WebLogic server
     */
    protected void addEarToDomain(EAR ear, Element domain)
    {
        Element application = domain.addElement("Application");
        application.addAttribute("Name", "_" + ear.getName() + "_app");
        application.addAttribute("Path", getAbsolutePath(ear));
        application.addAttribute("StagingMode", "nostage");
        application.addAttribute("TwoPhase", "false");
        Iterator contexts = ear.getWebContexts();
        while (contexts.hasNext())
        {
            String context = (String) contexts.next();
            Element webAppComponent = application.addElement("WebAppComponent");
            webAppComponent.addAttribute("Name", context);
            webAppComponent.addAttribute("Targets", getServerName());
            webAppComponent.addAttribute("URI", ear.getWebUri(context));
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
