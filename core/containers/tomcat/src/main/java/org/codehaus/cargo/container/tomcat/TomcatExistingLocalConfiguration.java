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
package org.codehaus.cargo.container.tomcat;

import java.io.File;
import java.io.InputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.spi.configuration.AbstractExistingLocalConfiguration;
import org.codehaus.cargo.container.tomcat.internal.TomcatExistingLocalConfigurationCapability;
import org.codehaus.cargo.util.CargoException;

/**
 * Tomcat existing {@link org.codehaus.cargo.container.configuration.Configuration} implementation.
 */
public class TomcatExistingLocalConfiguration extends AbstractExistingLocalConfiguration
{
    /**
     * Capability of the Tomcat existing configuration.
     */
    private static final ConfigurationCapability CAPABILITY =
        new TomcatExistingLocalConfigurationCapability();

    /**
     * {@inheritDoc}
     * @see AbstractExistingLocalConfiguration#AbstractExistingLocalConfiguration(String)
     */
    public TomcatExistingLocalConfiguration(String dir)
    {
        super(dir);

        String file = getFileHandler().append(dir, "conf/server.xml");

        if (!getFileHandler().exists(file))
        {
            getLogger().warn("Cannot find file " + file + ", setting default "
                + TomcatPropertySet.WEBAPPS_DIRECTORY, this.getClass().getName());
            setProperty(TomcatPropertySet.WEBAPPS_DIRECTORY, "webapps");
            return;
        }

        try
        {
            DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = domFactory.newDocumentBuilder();

            XPathFactory xPathFactory = XPathFactory.newInstance();
            XPath xPath = xPathFactory.newXPath();

            if (getFileHandler().isDirectory(file))
            {
                throw new CargoException("The destination is a directory: " + file);
            }

            Document doc;
            try (InputStream is = getFileHandler().getInputStream(file))
            {
                doc = builder.parse(is);
            }

            String expression = "//Server/Service/Engine/Host";
            String attributeName = "appBase";

            XPathExpression xPathExpr = xPath.compile(expression);
            Node node = (Node) xPathExpr.evaluate(doc, XPathConstants.NODE);

            if (node == null)
            {
                throw new CargoException("Node " + expression + " not found in file " + file);
            }

            Node attribute = node.getAttributes().getNamedItem(attributeName);

            if (attribute == null)
            {
                throw new CargoException("Attribute " + attribute + " not found on node "
                    + expression + " in file " + file);
            }

            setProperty(TomcatPropertySet.WEBAPPS_DIRECTORY, attribute.getNodeValue());

            attribute = node.getAttributes().getNamedItem("legacyAppBase");

            if (attribute != null)
            {
                setProperty(TomcatPropertySet.WEBAPPS_LEGACY_DIRECTORY, attribute.getNodeValue());
            }
        }
        catch (Exception e)
        {
            throw new CargoException("Cannot read the Tomcat server.xml file", e);
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
    protected void doConfigure(LocalContainer container) throws Exception
    {
        if (container instanceof Tomcat5xEmbeddedLocalContainer)
        {
            // embedded Tomcat doesn't need CPC
            TomcatEmbeddedLocalDeployer deployer =
                new TomcatEmbeddedLocalDeployer((Tomcat5xEmbeddedLocalContainer) container);
            deployer.redeploy(getDeployables());
        }
        else
        {
            File webappsDir = new File(getHome(),
                getPropertyValue(TomcatPropertySet.WEBAPPS_DIRECTORY));

            if (!webappsDir.exists())
            {
                throw new ContainerException("Invalid existing configuration: The ["
                    + webappsDir.getPath() + "] directory does not exist");
            }

            TomcatCopyingInstalledLocalDeployer deployer = createDeployer(container);
            deployer.setShouldCopyWars(true);
            deployer.redeploy(getDeployables());

            // Deploy the CPC (Cargo Ping Component) to the webapps directory.
            getResourceUtils().copyResource(RESOURCE_PATH + "cargocpc.war",
                new File(webappsDir, "cargocpc.war"));

            if (container instanceof Tomcat10xInstalledLocalContainer
                && getPropertyValue(TomcatPropertySet.WEBAPPS_LEGACY_DIRECTORY) == null)
            {
                setProperty(TomcatPropertySet.WEBAPPS_LEGACY_DIRECTORY, "webapps-javaee");
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void verify()
    {
        // Nothing to verify right now...
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return "Tomcat Existing Configuration";
    }

    /**
     * Creates the Tomcat deployer.
     * 
     * @param container Container to create a deployer for.
     * @return Tomcat deployer.
     */
    protected TomcatCopyingInstalledLocalDeployer createDeployer(LocalContainer container)
    {
        return new TomcatCopyingInstalledLocalDeployer(container);
    }
}
