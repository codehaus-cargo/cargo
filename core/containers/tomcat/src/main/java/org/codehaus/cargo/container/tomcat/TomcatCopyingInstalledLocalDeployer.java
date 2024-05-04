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
package org.codehaus.cargo.container.tomcat;

import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.deployable.DeployableVersion;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.spi.deployer.AbstractCopyingInstalledLocalDeployer;
import org.codehaus.cargo.container.tomcat.internal.TomcatUtils;
import org.codehaus.cargo.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Static deployer that deploys WARs to the Tomcat <code>webapps</code> directory.
 */
public class TomcatCopyingInstalledLocalDeployer extends AbstractCopyingInstalledLocalDeployer
{
    /**
     * @see #setShouldCopyWars(boolean)
     */
    private boolean shouldCopyWars = true;

    /**
     * {@inheritDoc}
     * @see AbstractCopyingInstalledLocalDeployer#AbstractCopyingInstalledLocalDeployer(org.codehaus.cargo.container.LocalContainer)
     */
    public TomcatCopyingInstalledLocalDeployer(LocalContainer container)
    {
        super(container);
    }

    /**
     * {@inheritDoc}. For Tomcat this is the <code>webapps</code> directory.
     */
    @Override
    public String getDeployableDir(Deployable deployable)
    {
        String propertyName = TomcatPropertySet.WEBAPPS_DIRECTORY;
        if (getContainer() instanceof Tomcat10xInstalledLocalContainer)
        {
            if (deployable.getVersion() == DeployableVersion.J2EE
                || deployable.getVersion() == DeployableVersion.JAVA_EE)
            {
                propertyName = TomcatPropertySet.WEBAPPS_LEGACY_DIRECTORY;
            }
        }
        return getFileHandler().append(getContainer().getConfiguration().getHome(),
            getContainer().getConfiguration().getPropertyValue(propertyName));
    }

    /**
     * Whether the local deployer should copy the wars to the Tomcat webapps directory. This is
     * because Tomcat standalone configuration may not want to copy wars and instead configure
     * server.xml to point to where the wars are located instead of copying them.
     * 
     * @param shouldCopyWars true if the wars should be copied
     */
    public void setShouldCopyWars(boolean shouldCopyWars)
    {
        this.shouldCopyWars = shouldCopyWars;
    }

    /**
     * {@inheritDoc}. We override the base implementation in order to handle the special Tomcat
     * scenarios: if the deployable is a {@link TomcatWAR} instance and it contains a
     * <code>context.xml</code> file that we need to manually copy.
     */
    @Override
    protected void doDeploy(String deployableDir, Deployable deployable)
    {
        if (DeployableType.WAR.equals(deployable.getType()))
        {
            // CARGO-1450: Add a warning if the a WAR path cannot be set in Tomcat due to a
            // context.xml file
            if (deployable instanceof TomcatWAR)
            {
                TomcatWAR tomcatWar = (TomcatWAR) deployable;
                String tomcatContextXml = tomcatWar.parseTomcatContextXml();
                if (tomcatContextXml != null)
                {
                    getLogger().warn("The WAR contains a context.xml file which sets the path to ["
                        + tomcatContextXml + "], which means path set it the Cargo configuration "
                            + "will be ignored by Tomcat", getClass().getName());
                }
            }

            WAR war = (WAR) deployable;
            if (deployable.isExpanded())
            {
                if (TomcatUtils.containsContextFile(war))
                {
                    // If the WAR contains a META-INF/context.xml then it means the user is
                    // defining how to deploy it.
                    String contextDir = getFileHandler().createDirectory(
                        getContainer().getConfiguration().getHome(),
                            "conf/Catalina/" + getContainer().getConfiguration().getPropertyValue(
                                GeneralPropertySet.HOSTNAME));

                    getLogger().info("Deploying WAR by creating Tomcat context XML file in ["
                        + contextDir + "]...", getClass().getName());

                    // Copy only the context.xml to <config>/Catalina/<hostname>/<context-path>.xml
                    // and set docBase to point at the expanded WAR
                    XmlUtils xmlUtil = new XmlUtils(getFileHandler());
                    Document doc =
                        xmlUtil.loadXmlFromFile(getFileHandler().append(war.getFile(),
                            "META-INF/context.xml"));
                    Element context = (Element) doc.getDocumentElement();
                    context.setAttribute("docBase", war.getFile());
                    configureExtraClasspath(war, context);
                    xmlUtil.saveXml(doc,
                        getFileHandler().append(contextDir, war.getContext() + ".xml"));
                }
                else if (this.shouldCopyWars)
                {
                    super.doDeploy(deployableDir, war);
                }
                else
                {
                    // Else, do nothing since the context.xml will reference the existing file
                }
            }
            else
            {
                if (TomcatUtils.containsContextFile(war))
                {
                    // Drop WAR file into the webapps dir, Tomcat will read META-INF/context.xml
                    super.doDeploy(deployableDir, war);
                }
                else if (this.shouldCopyWars)
                {
                    super.doDeploy(deployableDir, war);
                }
                else
                {
                    // Else, do nothing since the context.xml will reference the existing file
                }
            }
        }
        else
        {
            super.doDeploy(deployableDir, deployable);
        }
    }

    /**
     * Configures the specified context element with the extra classpath (if any) of the given WAR.
     * 
     * @param war The WAR whose extra classpath should be configured, must not be {@code null}.
     * @param context The context element to configure, must not be {@code null}.
     */
    private void configureExtraClasspath(WAR war, Element context)
    {
        if (war.getExtraClasspath() != null)
        {
            // if extraClasspath is not null here, we are on tomcat >=5x
            ((Tomcat5xStandaloneLocalConfiguration) getContainer().getConfiguration())
                .configureExtraClasspathToken(war, context);
        }
    }

    /**
     * Undeploy WAR deployables by deleting the local file from the Tomcat webapps directory.
     * 
     * {@inheritDoc}
     */
    @Override
    public void undeploy(Deployable deployable)
    {
        // Check that the container supports the deployable type to undeploy
        if (!getContainer().getCapability().supportsDeployableType(deployable.getType()))
        {
            throw new ContainerException(getContainer().getName() + " doesn't support ["
                + deployable.getType().getType().toUpperCase() + "] archives. Got ["
                + deployable.getFile() + "]");
        }

        String deployableDir = getDeployableDir(deployable);
        try
        {
            if (deployable.getType() == DeployableType.WAR)
            {
                WAR war = (WAR) deployable;
                getLogger().info("Undeploying context [" + war.getContext() + "] from ["
                    + deployableDir + "]...", this.getClass().getName());

                // Delete both the WAR file or the expanded WAR directory.
                String warLocation =
                    getFileHandler().append(deployableDir, war.getBaseFilename() + ".war");
                String expandedwarLocation =
                    getFileHandler().append(deployableDir, war.getBaseFilename());

                if (!getFileHandler().exists(warLocation)
                    && !getFileHandler().exists(expandedwarLocation))
                {
                    throw new ContainerException("Failed to undeploy as there is no WAR at ["
                        + warLocation + "] nor [" + expandedwarLocation + "]");
                }

                if (getFileHandler().exists(warLocation))
                {
                    getLogger().info("Trying to delete WAR from [" + warLocation + "]...",
                        this.getClass().getName());
                    getFileHandler().delete(warLocation);
                }
                if (getFileHandler().exists(expandedwarLocation))
                {
                    getLogger().info("Trying to delete WAR from [" + expandedwarLocation + "]...",
                        this.getClass().getName());
                    getFileHandler().delete(expandedwarLocation);
                }
            }
            else
            {
                throw new ContainerException("Only WAR undeployment is currently supported");
            }
        }
        catch (Exception e)
        {
            throw new ContainerException("Failed to undeploy [" + deployable.getFile()
                + "] from [" + deployableDir + "]", e);
        }
    }

    /**
     * Replace the slashes with <code>#</code> in the deployable name (see: CARGO-1041).
     * {@inheritDoc}
     */
    @Override
    protected String getDeployableName(Deployable deployable)
    {
        return super.getDeployableName(deployable).replace('/', '#');
    }
}
