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
package org.codehaus.cargo.container.websphere;

import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.EAR;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.deployer.DeployerType;
import org.codehaus.cargo.container.spi.deployer.AbstractLocalDeployer;
import org.codehaus.cargo.module.application.DefaultEarArchive;
import org.codehaus.cargo.module.webapp.DefaultWarArchive;
import org.codehaus.cargo.module.webapp.WarArchive;
import org.codehaus.cargo.module.webapp.WebXmlType;
import org.codehaus.cargo.util.CargoException;
import org.jdom.Element;

/**
 * Static deployer that deploys WARs to IBM WebSphere 8.5.
 * 
 * @version $Id$
 */
public class WebSphere85xInstalledLocalDeployer extends AbstractLocalDeployer
{

    /**
     * WebSphere container.
     */
    private WebSphere85xInstalledLocalContainer container;

    /**
     * {@inheritDoc}
     * @see AbstractLocalDeployer#AbstractLocalDeployer(org.codehaus.cargo.container.LocalContainer)
     */
    public WebSphere85xInstalledLocalDeployer(InstalledLocalContainer container)
    {
        super(container);
        this.container = (WebSphere85xInstalledLocalContainer) container;
    }

    /**
     * {@inheritDoc}
     */
    public DeployerType getType()
    {
        return DeployerType.INSTALLED;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deploy(Deployable deployable)
    {
        try
        {
            String deployableFileName = getFileHandler().getName(deployable.getFile());

            String contextRoot = "";
            StringBuilder mapWebModToVH = new StringBuilder(" -MapWebModToVH {");
            if (deployable instanceof WAR)
            {
                WAR war = (WAR) deployable;
                DefaultWarArchive warArchive = new DefaultWarArchive(deployable.getFile());

                String displayName = deployableFileName;
                Element displayNameElement =
                    warArchive.getWebXml().getRootElement().getChild(WebXmlType.DISPLAY_NAME);
                if (displayNameElement != null)
                {
                    String displayNameText = displayNameElement.getText();
                    if (displayNameText != null)
                    {
                        displayNameText = displayNameText.trim();
                        if (displayNameText.length() > 0)
                        {
                            displayName = displayNameText;
                        }
                    }
                }

                contextRoot = "-contextroot " + war.getContext();
                mapWebModToVH.append("{\"" + displayName + "\" \"" + deployableFileName
                    + ",WEB-INF/web.xml\" default_host}");
            }
            else if (deployable instanceof EAR)
            {
                EAR ear = (EAR) deployable;
                DefaultEarArchive earArchive = new DefaultEarArchive(deployable.getFile());

                boolean first = true;
                for (String webUri : ear.getWebUris())
                {
                    if (first)
                    {
                        first = false;
                    }
                    else
                    {
                        mapWebModToVH.append(" ");
                    }

                    WarArchive warArchive = earArchive.getWebModule(webUri);
                    String displayName = webUri;
                    Element displayNameElement =
                        warArchive.getWebXml().getRootElement().getChild(WebXmlType.DISPLAY_NAME);
                    if (displayNameElement != null)
                    {
                        String displayNameText = displayNameElement.getText();
                        if (displayNameText != null)
                        {
                            displayNameText = displayNameText.trim();
                            if (displayNameText.length() > 0)
                            {
                                displayName = displayNameText;
                            }
                        }
                    }

                    mapWebModToVH.append("{\"" + displayName + "\" \"" + webUri
                        + ",WEB-INF/web.xml\" default_host}");
                }
            }
            else
            {
                throw new CargoException("Unknown deployable: " + deployable.getType());
            }
            mapWebModToVH.append("}");
            String serverName = container.getConfiguration().getPropertyValue(
                    WebSpherePropertySet.SERVER);
            String classldrMode = container.getConfiguration().getPropertyValue(
                    WebSpherePropertySet.CLASSLOADER_MODE);
            String classldrPolicy = container.getConfiguration().getPropertyValue(
                    WebSpherePropertySet.WAR_CLASSLOADER_POLICY);

            String deployableName = getDeployableName(deployable);
            container.executeWsAdmin(
                "$AdminApp install "
                    + deployable.getFile().replace('\\', '/').replace(" ", "\\ ")
                    + " {" + contextRoot + " "
                    + " -server " + serverName
                    + " -appname " + deployableName
                    + mapWebModToVH.toString() + "}",
                "set dep [$AdminConfig getid /Deployment:" + deployableName + "/]",
                "set depObject [$AdminConfig showAttribute $dep deployedObject]",
                "set classldr [$AdminConfig showAttribute $depObject classloader]",
                "$AdminConfig modify $classldr {{mode " + classldrMode + "}}",
                "$AdminConfig modify $depObject {{warClassLoaderPolicy " + classldrPolicy + "}}",
                "$AdminConfig save");
        }
        catch (Exception e)
        {
            throw new CargoException("Deploy failed", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void undeploy(Deployable deployable)
    {
        try
        {
            container.executeWsAdmin(
                "$AdminApp uninstall " + getDeployableName(deployable),
                "$AdminConfig save");
        }
        catch (Exception e)
        {
            throw new CargoException("Undeploy failed", e);
        }
    }

    /**
     * Get the deployable name for a deployable.
     * @param deployable Deployable to get the name for.
     * @return Deployable name.
     */
    protected String getDeployableName(Deployable deployable)
    {
        if (deployable instanceof WAR)
        {
            return "cargo-deployable-" + ((WAR) deployable).getContext();
        }
        else if (deployable instanceof EAR)
        {
            return "cargo-deployable-" + ((EAR) deployable).getName();
        }
        else
        {
            throw new CargoException("Unknown deployable: " + deployable.getType());
        }
    }
}
