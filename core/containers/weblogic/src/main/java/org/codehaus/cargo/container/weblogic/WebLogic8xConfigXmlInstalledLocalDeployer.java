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
package org.codehaus.cargo.container.weblogic;

import java.io.File;
import java.util.List;

import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.deployable.EAR;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.spi.deployer.AbstractInstalledLocalDeployer;
import org.codehaus.cargo.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Static deployer that manages deployment configuration by manipulating the WebLogic config.xml
 * file.
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
    }

    /**
     * write the domain's config.xml to disk.
     * 
     * @param configXml document to write to disk
     */
    public void writeConfigXml(Document configXml)
    {
        XmlUtils xmlUtil = new XmlUtils(getFileHandler());
        xmlUtil.saveXml(configXml, getFileHandler().append(getDomainHome(), "config.xml"));
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
        XmlUtils xmlUtil = new XmlUtils(getFileHandler());
        Document configXml =
            xmlUtil.loadXmlFromFile(getFileHandler().append(getDomainHome(), "config.xml"));
        Element domain = xmlUtil.selectElementMatchingXPath("//Domain",
            configXml.getDocumentElement());

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
     */
    @Override
    public void undeploy(Deployable deployable)
    {
        XmlUtils xmlUtil = new XmlUtils(getFileHandler());
        Document configXml =
            xmlUtil.loadXmlFromFile(getFileHandler().append(getDomainHome(), "config.xml"));
        List<Element> results = xmlUtil.selectElementsMatchingXPath("//Application[@Path='"
            + getFileHandler().getParent(getAbsolutePath(deployable)) + "']",
                configXml.getDocumentElement());

        for (Element element : results)
        {
            configXml.removeChild(element);
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
        Element application = domain.getOwnerDocument().createElement("Application");
        domain.appendChild(application);
        application.setAttribute("Name", "_" + war.getContext() + "_app");
        application.setAttribute("Path", getFileHandler().getParent(getAbsolutePath(war)));
        application.setAttribute("StagingMode", "nostage");
        application.setAttribute("TwoPhase", "false");
        Element webAppComponent = application.getOwnerDocument().createElement("WebAppComponent");
        application.appendChild(webAppComponent);
        webAppComponent.setAttribute("Name", war.getContext());
        webAppComponent.setAttribute("Targets", getServerName());
        webAppComponent.setAttribute("URI", getURI(war));
    }

    /**
     * Insert the corresponding ear element into the domain of the WebLogic server.
     * 
     * @param ear - ear to configure
     * @param domain - Domain element of the WebLogic server
     */
    protected void addEarToDomain(EAR ear, Element domain)
    {
        Element application = domain.getOwnerDocument().createElement("Application");
        domain.appendChild(application);
        application.setAttribute("Name", "_" + ear.getName() + "_app");
        application.setAttribute("Path", getAbsolutePath(ear));
        application.setAttribute("StagingMode", "nostage");
        application.setAttribute("TwoPhase", "false");
        for (String context : ear.getWebContexts())
        {
            Element webAppComponent =
                application.getOwnerDocument().createElement("WebAppComponent");
            application.appendChild(webAppComponent);
            webAppComponent.setAttribute("Name", context);
            webAppComponent.setAttribute("Targets", getServerName());
            webAppComponent.setAttribute("URI", ear.getWebUri(context));
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
