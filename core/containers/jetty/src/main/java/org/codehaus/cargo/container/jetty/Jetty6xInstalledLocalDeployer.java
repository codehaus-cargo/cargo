/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2022 Ali Tokmen.
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
package org.codehaus.cargo.container.jetty;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.jetty.internal.JettyUtils;
import org.codehaus.cargo.container.spi.deployer.AbstractCopyingInstalledLocalDeployer;

/**
 * A deployer for webapps that deploys to a Jetty 6.x installed instance.
 */
public class Jetty6xInstalledLocalDeployer extends AbstractCopyingInstalledLocalDeployer
{
    /**
     * {@inheritDoc}
     * @see AbstractCopyingInstalledLocalDeployer#AbstractCopyingInstalledLocalDeployer(org.codehaus.cargo.container.LocalContainer)
     */
    public Jetty6xInstalledLocalDeployer(LocalContainer container)
    {
        super(container);
    }

    /**
     * {@inheritDoc}. For Jetty this is the <code>webapps</code> directory.
     */
    @Override
    public String getDeployableDir(Deployable deployable)
    {
        return getFileHandler().append(getContainer().getConfiguration().getHome(), "webapps");
    }

    /**
     * Specifies the directory for which the <code>context.xml</code> for the
     * {@link org.codehaus.cargo.container.deployable.Deployable}s should be copied to. For Jetty
     * this is the <code>webapps</code> directory.
     * 
     * @return Deployable the directory to deploy the <code>context.xml</code> file to
     */
    public String getContextsDir()
    {
        return getFileHandler().append(getContainer().getConfiguration().getHome(), "contexts");
    }

    /**
     * {@inheritDoc}. We override the base implementation because Jetty requires a context XML file
     * deployed in its context dir to perform hot deployment. Thus we need to create that context
     * file.
     */
    @Override
    protected void doDeploy(String deployableDir, Deployable deployable)
    {
        String createContextXml = getContainer().getConfiguration().getPropertyValue(
            JettyPropertySet.CREATE_CONTEXT_XML);

        if (DeployableType.WAR.equals(deployable.getType())
            && Boolean.parseBoolean(createContextXml))
        {
            WAR war = (WAR) deployable;
            // Create a Jetty context file. This is useful for various purposes:
            // - ability to hot deploy
            // - ability to tell Jetty to install the WAR under a given context name
            // - ability to accelerate deployment by avoiding an actual copy of the WAR
            String contextDir = getContextsDir();
            String contextFile = war.getFilename();
            contextFile = getFileHandler().append(contextDir,
                contextFile.substring(0, contextFile.length() - 3) + "xml");
            getFileHandler().createFile(contextFile);

            getLogger().info("Deploying WAR by creating Jetty context XML file in [" + contextFile
                + "]...", getClass().getName());

            try (OutputStream out = getFileHandler().getOutputStream(contextFile))
            {
                out.write(createContextXml(war).getBytes(StandardCharsets.UTF_8));
            }
            catch (IOException e)
            {
                throw new ContainerException("Failed to create Jetty Context file for ["
                    + war.getFile() + "]", e);
            }
        }
        else
        {
            super.doDeploy(deployableDir, deployable);
        }
    }

    /**
     * Creates the contents of the context file.
     * 
     * @param war The WAR being deployed, must not be {@code null}.
     * @return The contents of the context file, never {@code null}.
     */
    protected String createContextXml(WAR war)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<!DOCTYPE Configure PUBLIC \"-//Mort Bay Consulting//DTD Configure//EN\" "
            + "\"http://jetty.mortbay.org/configure.dtd\">\n");
        sb.append("<Configure class=\"org.mortbay.jetty.webapp.WebAppContext\">\n");
        sb.append("  <Array id=\"plusConfig\" type=\"java.lang.String\">\n");
        sb.append("    <Item>org.mortbay.jetty.webapp.WebInfConfiguration</Item>\n");
        sb.append("    <Item>org.mortbay.jetty.plus.webapp.EnvConfiguration</Item>\n");
        sb.append("    <Item>org.mortbay.jetty.plus.webapp.Configuration</Item>\n");
        sb.append("    <Item>org.mortbay.jetty.webapp.JettyWebXmlConfiguration</Item>\n");
        sb.append("    <Item>org.mortbay.jetty.webapp.TagLibConfiguration</Item>\n");
        sb.append("  </Array>\n");
        sb.append("  <Set name=\"contextPath\">/" + war.getContext() + "</Set>\n");
        sb.append("  <Set name=\"war\">" + war.getFile() + "</Set>\n");
        sb.append("  <Set name=\"extractWAR\">true</Set>\n");
        sb.append("  <Set name=\"defaultsDescriptor\"><SystemProperty name=\"config.home\" "
            + "default=\".\"/>/etc/webdefault.xml</Set>\n");
        sb.append("  <Set name=\"ConfigurationClasses\"><Ref id=\"plusConfig\"/></Set>\n");
        sb.append(getExtraClasspathXmlFragment(war));
        sb.append(getSharedClasspathXmlFragment());
        sb.append("</Configure>\n");
        return sb.toString();
    }

    /**
     * @return The XML fragment for shared classpath.
     */
    protected String getSharedClasspathXmlFragment()
    {
        StringBuilder sb = new StringBuilder();

        if (getContainer() instanceof InstalledLocalContainer)
        {
            InstalledLocalContainer installedLocalContainer =
                (InstalledLocalContainer) getContainer();

            String[] sharedClasspath = installedLocalContainer.getSharedClasspath();
            if (sharedClasspath != null && sharedClasspath.length > 0)
            {
                sb.append("  <Set name=\"extraClasspath\">\n");
                for (String sharedClasspathElement : sharedClasspath)
                {
                    sb.append("    ").append(sharedClasspathElement).append(";\n");
                }
                sb.append("  </Set>\n");
            }
        }

        return sb.toString();
    }

    /**
     * @param war The WAR being deployed, must not be {@code null}.
     * @return The XML fragment for WAR extra classpath
     */
    protected String getExtraClasspathXmlFragment(WAR war)
    {
        StringBuilder sb = new StringBuilder();
        String extraClasspath = JettyUtils.getExtraClasspath(war, true);
        if (extraClasspath != null)
        {
            sb.append("  <Set name=\"extraClasspath\">" + extraClasspath + "</Set>\n");
        }
        return sb.toString();
    }
}
