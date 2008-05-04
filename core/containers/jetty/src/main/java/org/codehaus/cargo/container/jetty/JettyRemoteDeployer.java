/* 
 * ========================================================================
 * 
 * Copyright 2008 Vincent Massol.
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
import org.codehaus.cargo.container.RemoteContainer;
import org.codehaus.cargo.container.configuration.Configuration;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.spi.deployer.AbstractRemoteDeployer;
import org.codehaus.cargo.util.DefaultFileHandler;
import org.codehaus.cargo.util.FileHandler;

/**
 * A remote deployer for the Jetty container. Currently only works
 * with locally installed containers.
 *
 * @version $Id$
 */
public class JettyRemoteDeployer extends AbstractRemoteDeployer
{

    /**
     * Jetty doesn't appear to contain anything by default that would allow for
     * remote deployment. Possible option:
     *  - A servlet could be added to provide this functionality (like the
     * tomcat manager) but this would require that the servlet is already
     * deployed before the jetty server starts (which is not something we can
     * assume)
     *  - JMX could be used to provide an mbean to provide this functionality,
     * but the jetty jmx server is not started by default, nor can we assume
     * this bean will be provided when jetty starts.
     *  - Set a home value of the jetty server. This would allow for deployment
     * only to a server running on the local machine.
     * 
     * 
     * Setting the home value is probably the best option as it covers 1 of the
     * 2 options (local machine but not remote machine). The first two options
     * require that the remote server be started with special conditions which
     * can't be guaranteed.
     * 
     * TODO: figure this out more and create a more robust implementation
     */

    /**
     * The remote container to be used.
     */
    private RemoteContainer container;
    
    /**
     *  The configuration to be used.
     */
    private Configuration configuration;

    /**
     *  The file handler to copy the deployment
     */
    private FileHandler fileHandler;

    /**
     * The location where Jetty is locally installed
     */
    private String jettyHome;

    /**
     * The location of webapps directory
     */
    private String webappDirectory;

    /**
     * The location of the context directory
     */
    private String contextDirectory;

    /**
     * Remote deployer for the Jetty container.
     * @param container The container used for deployment
     */
    public JettyRemoteDeployer(RemoteContainer container)
    {

        fileHandler = new DefaultFileHandler();

        this.container = container;
        this.configuration = container.getConfiguration();

        jettyHome = configuration.getPropertyValue("cargo.jetty.remote.home");

        if (jettyHome != null)
        {
            webappDirectory = jettyHome + "/webapps";
            contextDirectory = jettyHome + "/contexts";
        } 
        else
        {
            getLogger()
                    .warn(
                            "no Jetty home has been provided, assuming the default location for "
                            + "installed containers",
                            this.getClass().getName());
            webappDirectory = fileHandler.getTmpPath("conf/webapps");
            contextDirectory = fileHandler.getTmpPath("conf/contexts");
        }

    }

    /**
     * {@inheritDoc}
     */
    public void deploy(Deployable deployable)
    {
        deployArchive(webappDirectory, (WAR) deployable);
        deployContext(contextDirectory, (WAR) deployable);
    }

    /**
     * Deploy the archive to a specified directory.
     * @param directory The directory to contain the war
     * @param war The war to deploy
     */
    private void deployArchive(String directory, WAR war)
    {
        fileHandler.copyFile(war.getFile(), fileHandler
                .append(directory, war.getContext() + ".war"));
    }

    /**
     * Jetty requires a context file to be created to hot deploy the webapp.
     * 
     * @param directory
     *            The directory to install the context
     * @param war
     *            The war to deployed
     */
    private void deployContext(String directory, WAR war)
    {
        String contextFile = fileHandler.append(directory, war.getContext() + ".xml");
        fileHandler.createFile(contextFile);

        OutputStream out = fileHandler.getOutputStream(contextFile);
        try
        {
            out.write(("<?xml version=\"1.0\"  encoding=\"ISO-8859-1\"?>\n"
                    + "<!DOCTYPE Configure PUBLIC \"-//Mort Bay Consulting//DTD Configure//EN\" "
                    + "\"http://jetty.mortbay.org/configure.dtd\">\n"
                    + "<Configure class=\"org.mortbay.jetty.webapp.WebAppContext\">\n"
                    + "  <Set name=\"contextPath\">/" + war.getContext() + "</Set>\n"
                    + "  <Set name=\"war\"><SystemProperty name=\"config.home\" "
                    + "default=\".\"/>/webapps/" + war.getContext() + ".war</Set>\n"
                    + "  <Set name=\"extractWAR\">true</Set>\n" + "</Configure>").getBytes());
            out.close();
        } 
        catch (IOException e)
        {
            throw new ContainerException("Failed to create Jetty Context file for ["
                    + war.getFile() + "]");
        }
    }

    /**
     * {@inheritDoc}
     */
    public void undeploy(Deployable deployable)
    {
        undeployContext(contextDirectory, (WAR) deployable);
        undeployArchive(webappDirectory, (WAR) deployable);
    }

    /**
     * Undeploy a War from a specific directory.
     * @param directory The specified directory
     * @param war The war to undeploy
     */
    private void undeployArchive(String directory, WAR war)
    {
        fileHandler.delete(fileHandler.append(directory, war.getContext() + ".war"));
    }

    /**
     *  Undeploy the web app context file.
     * @param directory The directory that contains the context
     * @param war The war archive to be removed
     */
    private void undeployContext(String directory, WAR war)
    {
        String contextFile = fileHandler.append(directory, war.getContext() + ".xml");
        fileHandler.delete(contextFile);
    }

    /**
     * {@inheritDoc}
     */
    public void redeploy(Deployable deployable)
    {
        undeploy(deployable);
        deploy(deployable);
    }
}
