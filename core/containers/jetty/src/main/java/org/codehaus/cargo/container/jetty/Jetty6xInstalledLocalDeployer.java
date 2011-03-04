/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2010 Vincent Massol.
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
     * {@inheritDoc}
     * 
     * <p>
     * We override the base implementation because Jetty requires a context XML file deployed in its
     * context dir to perform hot deployment. Thus we need to create that context file
     * </p>
     * 
     * @see AbstractCopyingInstalledLocalDeployer#deployWar(String,
     * org.codehaus.cargo.container.deployable.WAR)
     */
    @Override
    protected void deployWar(String deployableDir, WAR war)
    {
        super.deployWar(deployableDir, war);

        // Create a Jetty context file. This is useful for 2 purposes:
        // - ability to hot deploy
        // - ability to tell Jetty to install the WAR under a given context name
        String contextDir = getFileHandler().append(getContainer().getConfiguration().getHome(),
            "contexts");
        String contextFile = getFileHandler().append(contextDir, war.getContext() + ".xml");
        getFileHandler().createFile(contextFile);

        OutputStream out = getFileHandler().getOutputStream(contextFile);
        try
        {
            out.write(("<?xml version=\"1.0\"  encoding=\"ISO-8859-1\"?>\n"
                + "<!DOCTYPE Configure PUBLIC \"-//Mort Bay Consulting//DTD Configure//EN\" "
                    + "\"http://jetty.mortbay.org/configure.dtd\">\n"
                + "<Configure class=\"org.mortbay.jetty.webapp.WebAppContext\">\n"

                + "  <Array id=\"plusConfig\" type=\"java.lang.String\">\n"
                + "    <Item>org.mortbay.jetty.webapp.WebInfConfiguration</Item>\n"
                + "    <Item>org.mortbay.jetty.plus.webapp.EnvConfiguration</Item>\n"
                + "    <Item>org.mortbay.jetty.plus.webapp.Configuration</Item>\n"
                + "    <Item>org.mortbay.jetty.webapp.JettyWebXmlConfiguration</Item>\n"
                + "    <Item>org.mortbay.jetty.webapp.TagLibConfiguration</Item>\n"
                + "  </Array>\n"

                + "  <Set name=\"contextPath\">/" + war.getContext() + "</Set>\n"
                + "  <Set name=\"war\"><SystemProperty name=\"config.home\" "
                    + "default=\".\"/>/webapps/" + war.getContext() + ".war</Set>\n"
                + "  <Set name=\"extractWAR\">true</Set>\n"
                + "  <Set name=\"defaultsDescriptor\"><SystemProperty name=\"config.home\" "
                   + "default=\".\"/>/etc/webdefault.xml</Set>\n"
                + "  <Set name=\"ConfigurationClasses\"><Ref id=\"plusConfig\"/></Set>\n"
                + "</Configure>").getBytes("ISO-8859-1"));
            out.close();
        }
        catch (IOException e)
        {
            throw new ContainerException("Failed to create Jetty Context file for ["
                + war.getFile() + "]");
        }
    }
}
