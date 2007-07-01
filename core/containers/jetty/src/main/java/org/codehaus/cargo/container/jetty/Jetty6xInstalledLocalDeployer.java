/*
 * ========================================================================
 *
 * Copyright 2007 Vincent Massol.
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

import org.codehaus.cargo.container.spi.deployer.AbstractCopyingInstalledLocalDeployer;
import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.deployable.WAR;

import java.io.OutputStream;
import java.io.IOException;

/**
 * A deployer for webapps that deploys to a Jetty 6.x installed instance.
 *
 * @version $Id: Jetty6xEmbeddedLocalDeployer.java 1268 2007-01-11 15:46:46Z janb $
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
     * Specifies the directory {@link org.codehaus.cargo.container.deployable.Deployable}s should
     * be copied to. For Jetty this is the <code>webapps</code> directory.
     *
     * @return Deployable the directory to deploy to
     */
    public String getDeployableDir()
    {
        return getFileHandler().append(getContainer().getConfiguration().getHome(), "webapps");
    }

    /**
     * {@inheritDoc}
     *
     * <p>We override the base implementation because Jetty requires a context XML file deployed
     * in its context dir to perform hot deployment. Thus we need to create that context file</p>
     *
     * @see AbstractCopyingInstalledLocalDeployer#deployWar(String, org.codehaus.cargo.container.deployable.WAR) 
     */
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
                + "  <Set name=\"contextPath\">/" + war.getContext() + "</Set>\n"
                + "  <Set name=\"war\"><SystemProperty name=\"config.home\" "
                    + "default=\".\"/>/webapps/" + war.getContext() + ".war</Set>\n"
                + "  <Set name=\"extractWAR\">true</Set>\n"
                + "</Configure>").getBytes());
            out.close();
        }
        catch (IOException e)
        {
            throw new ContainerException("Failed to create Jetty Context file for ["
                + war.getFile() + "]");
        }
    }
}
