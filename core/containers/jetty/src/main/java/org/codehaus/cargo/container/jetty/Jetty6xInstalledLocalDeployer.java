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
package org.codehaus.cargo.container.jetty;

import java.io.IOException;
import java.io.OutputStream;

import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.spi.deployer.AbstractCopyingInstalledLocalDeployer;

/**
 * A deployer for webapps that deploys to a Jetty 6.x installed instance.
 * 
 * @version $Id$
 */
public class Jetty6xInstalledLocalDeployer extends AbstractCopyingInstalledLocalDeployer
{
    /**
     * {@inheritDoc}
     * @see AbstractCopyingInstalledLocalDeployer#AbstractCopyingInstalledLocalDeployer(org.codehaus.cargo.container.InstalledLocalContainer)
     */
    public Jetty6xInstalledLocalDeployer(InstalledLocalContainer container)
    {
        super(container);
    }

    /**
     * Specifies the directory {@link org.codehaus.cargo.container.deployable.Deployable}s should be
     * copied to. For Jetty this is the <code>webapps</code> directory.
     * 
     * @return Deployable the directory to deploy to
     */
    @Override
    public String getDeployableDir()
    {
        return getFileHandler().append(getContainer().getConfiguration().getHome(), "webapps");
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
            && Boolean.valueOf(createContextXml).booleanValue())
        {
            WAR war = (WAR) deployable;
            // Create a Jetty context file. This is useful for various purposes:
            // - ability to hot deploy
            // - ability to tell Jetty to install the WAR under a given context name
            // - ability to accelerate deployment by avoiding an actual copy of the WAR
            String contextDir = getFileHandler().append(
                getContainer().getConfiguration().getHome(), "contexts");
            String contextFile = war.getContext();
            if ("/".equals(contextFile) || "".equals(contextFile))
            {
                contextFile = "root";
            }
            contextFile = contextFile.replace('/', '-');
            contextFile = getFileHandler().append(contextDir, contextFile + ".xml");
            getFileHandler().createFile(contextFile);

            getLogger().info("Deploying WAR by creating Jetty context XML file in [" + contextFile
                + "]...", getClass().getName());

            OutputStream out = getFileHandler().getOutputStream(contextFile);
            try
            {
                out.write(createContextXml(war).getBytes("UTF-8"));
                out.close();
            }
            catch (IOException e)
            {
                throw new ContainerException("Failed to create Jetty Context file for ["
                    + war.getFile() + "]", e);
            }
            finally
            {
                try
                {
                    out.close();
                }
                catch (IOException ignored)
                {
                    // Ignored
                }
                finally
                {
                    out = null;
                    System.gc();
                }
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
        StringBuilder buffer = new StringBuilder(1024);
        buffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        buffer.append("<!DOCTYPE Configure PUBLIC \"-//Mort Bay Consulting//DTD Configure//EN\" "
            + "\"http://jetty.mortbay.org/configure.dtd\">\n");
        buffer.append("<Configure class=\"org.mortbay.jetty.webapp.WebAppContext\">\n");
        buffer.append("  <Array id=\"plusConfig\" type=\"java.lang.String\">\n");
        buffer.append("    <Item>org.mortbay.jetty.webapp.WebInfConfiguration</Item>\n");
        buffer.append("    <Item>org.mortbay.jetty.plus.webapp.EnvConfiguration</Item>\n");
        buffer.append("    <Item>org.mortbay.jetty.plus.webapp.Configuration</Item>\n");
        buffer.append("    <Item>org.mortbay.jetty.webapp.JettyWebXmlConfiguration</Item>\n");
        buffer.append("    <Item>org.mortbay.jetty.webapp.TagLibConfiguration</Item>\n");
        buffer.append("  </Array>\n");
        buffer.append("  <Set name=\"contextPath\">/" + war.getContext() + "</Set>\n");
        buffer.append("  <Set name=\"war\">" + war.getFile() + "</Set>\n");
        buffer.append("  <Set name=\"extractWAR\">true</Set>\n");
        buffer.append("  <Set name=\"defaultsDescriptor\"><SystemProperty name=\"config.home\" "
            + "default=\".\"/>/etc/webdefault.xml</Set>\n");
        buffer.append("  <Set name=\"ConfigurationClasses\"><Ref id=\"plusConfig\"/></Set>\n");
        buffer.append("</Configure>\n");
        return buffer.toString();
    }

}
