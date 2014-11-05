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

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;

import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.EAR;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.deployer.DeployerType;
import org.codehaus.cargo.container.spi.deployer.AbstractLocalDeployer;
import org.codehaus.cargo.container.spi.jvm.JvmLauncher;
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
            String classldrMode = container.getConfiguration().getPropertyValue(
                    WebSpherePropertySet.CLASSLOADER_MODE);
            String classldrPolicy = container.getConfiguration().getPropertyValue(
                    WebSpherePropertySet.WAR_CLASSLOADER_POLICY);

            String deployableName = getDeployableName(deployable);
            executeWsAdmin(
                "set asn [$AdminControl queryNames type=ApplicationManager,process="
                    + container.getConfiguration().getPropertyValue(WebSpherePropertySet.SERVER)
                    + ",*]",
                "$AdminApp install "
                    + deployable.getFile().replace('\\', '/').replace(" ", "\\ ")
                    + " {" + contextRoot + " "
                    + " -appname " + deployableName
                    + mapWebModToVH.toString() + "}",
                "set dep [$AdminConfig getid /Deployment:" + deployableName + "/]",
                "set depObject [$AdminConfig showAttribute $dep deployedObject]",
                "set classldr [$AdminConfig showAttribute $depObject classloader]",
                "$AdminConfig modify $classldr {{mode " + classldrMode + "}}",
                "$AdminConfig modify $depObject {{warClassLoaderPolicy " + classldrPolicy + "}}",
                "$AdminConfig save",
                "$AdminControl invoke $asn startApplication " + getDeployableName(deployable));
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
            executeWsAdmin(
                "set asn [$AdminControl queryNames type=ApplicationManager,process="
                    + container.getConfiguration().getPropertyValue(WebSpherePropertySet.SERVER)
                    + ",*]",
                "$AdminControl invoke $asn stopApplication " + getDeployableName(deployable),
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

    /**
     * Executes WS admin commands.
     * 
     * @param commands Commands to execute.
     * @throws Exception If anything goes wrong.
     */
    protected void executeWsAdmin(String... commands) throws Exception
    {
        JvmLauncher java = container.createJvmLauncher();

        java.setSystemProperty("java.util.logging.manager", "com.ibm.ws.bootstrap.WsLogManager");
        java.setSystemProperty("java.util.logging.configureByServer", "true");

        java.setSystemProperty("com.ibm.SOAP.ConfigURL",
            new File(container.getConfiguration().getHome(),
                "properties/soap.client.props").toURI().toURL().toString());
        java.setSystemProperty("com.ibm.CORBA.ConfigURL",
            new File(container.getConfiguration().getHome(),
                "properties/sas.client.props").toURI().toURL().toString());
        java.setSystemProperty("com.ibm.SSL.ConfigURL",
            new File(container.getConfiguration().getHome(),
                "properties/ssl.client.props").toURI().toURL().toString());
        java.setSystemProperty("java.security.auth.login.config",
            new File(container.getConfiguration().getHome(),
                "properties/wsjaas_client.conf").getAbsolutePath());

        java.setSystemProperty("ws.ext.dirs",
            new File(container.getJavaHome(),
                "lib").getAbsolutePath().replace(File.separatorChar, '/')
            + File.pathSeparatorChar
            + new File(container.getHome(),
                "classes").getAbsolutePath().replace(File.separatorChar, '/')
            + File.pathSeparatorChar
            + new File(container.getHome(),
                "lib").getAbsolutePath().replace(File.separatorChar, '/')
            + File.pathSeparatorChar
            + new File(container.getHome(),
                "jinstalledChannels").getAbsolutePath().replace(File.separatorChar, '/')
            + File.pathSeparatorChar
            + new File(container.getHome(),
                "lib/ext").getAbsolutePath().replace(File.separatorChar, '/')
            + File.pathSeparatorChar
            + new File(container.getHome(),
                "web/help").getAbsolutePath().replace(File.separatorChar, '/')
            + File.pathSeparatorChar
            + new File(container.getHome(),
                "deploytool/itp/plugins/com.ibm.etools.ejbdeploy/runtime")
                    .getAbsolutePath().replace(File.separatorChar, '/')
            + File.pathSeparatorChar
            + new File(container.getHome(),
                "lib/ext").getAbsolutePath().replace(File.separatorChar, '/'));

        java.setSystemProperty("was.repository.root",
            new File(container.getConfiguration().getHome(),
                "config").getAbsolutePath().replace(File.separatorChar, '/'));
        java.setSystemProperty("com.ibm.itp.location",
            new File(container.getHome(),
                    "bin").getAbsolutePath().replace(File.separatorChar, '/'));
        java.setSystemProperty("local.cell",
            container.getConfiguration().getPropertyValue(WebSpherePropertySet.CELL));
        java.setSystemProperty("local.node",
            container.getConfiguration().getPropertyValue(WebSpherePropertySet.NODE));

        java.setSystemProperty("com.ibm.ws.management.standalone", "true");

        java.setSystemProperty("com.ibm.ws.ffdc.log",
            new File(container.getConfiguration().getHome(),
                    "logs/ffdc").getAbsolutePath().replace(File.separatorChar, '/'));

        java.setMainClass("com.ibm.wsspi.bootstrap.WSPreLauncher");

        File commandFile = File.createTempFile("cargo-websphere-commandFile-", ".jacl");
        PrintWriter writer = new PrintWriter(new FileOutputStream(commandFile));
        try
        {
            for (String command : commands)
            {
                writer.println(command);
            }
        }
        finally
        {
            writer.close();
            writer = null;
            System.gc();
        }

        java.addAppArguments("-nosplash");
        java.addAppArguments("-application");
        java.addAppArguments("com.ibm.ws.bootstrap.WSLauncher");
        java.addAppArguments("com.ibm.ws.admin.services.WsAdmin");
        java.addAppArguments("-username");
        java.addAppArguments(getContainer().getConfiguration().getPropertyValue(
                WebSpherePropertySet.ADMIN_USERNAME));
        java.addAppArguments("-password");
        java.addAppArguments(getContainer().getConfiguration().getPropertyValue(
                WebSpherePropertySet.ADMIN_PASSWORD));
        java.addAppArguments("-f");
        java.addAppArgument(commandFile);

        try
        {
            int returnCode = java.execute();
            if (returnCode != 0)
            {
                throw new CargoException(
                    "WebSphere deployment failed: return code was " + returnCode);
            }
        }
        finally
        {
            commandFile.delete();
        }
    }
}
