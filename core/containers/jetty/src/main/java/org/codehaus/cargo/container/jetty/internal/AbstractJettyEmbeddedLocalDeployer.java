/* 
 * ========================================================================
 * 
 * Copyright 2006 Vincent Massol.
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
package org.codehaus.cargo.container.jetty.internal;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.cargo.container.EmbeddedLocalContainer;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.deployer.DeployerType;
import org.codehaus.cargo.container.spi.deployer.AbstractEmbeddedLocalDeployer;

/**
 * Base class for all Jetty deployers.
 *
 * @version $Id$
 */
public abstract class AbstractJettyEmbeddedLocalDeployer extends AbstractEmbeddedLocalDeployer
{
    /**
     * Map of context paths to webapps. For the Jetty API, we need the deployed webapp object in
     * order to call the API to remove it.
     */
    private static Map deployedWebAppMap = new HashMap();

    /**
     * List of virtual hostnames with which to associate webapps deployed with this deployer.
     */
    private String[] hosts;

    /**
     * Whether or not to extract packed wars deployed with this deployer eg if running jsps.
     */
    private Boolean extract;

    /**
     * Whether or not to copy the webapp in a non-packed war to allow hot replacement of jars.
     */
    private Boolean copyWebApp;

    /**
     * If true use java2 class loading (defer to parent first) otherwise use servlet spec (defer
     * to webapp first) for webapps deployed via this deployer.
     */
    private Boolean parentLoaderPriority;



    /**
     * {@inheritDoc}
     * @see AbstractEmbeddedLocalDeployer#AbstractEmbeddedLocalDeployer(org.codehaus.cargo.container.EmbeddedLocalContainer)
     */
    public AbstractJettyEmbeddedLocalDeployer(EmbeddedLocalContainer container)
    {
        super(container);
    }

    /**
     * Implement to perform the work of the deploy.
     * @param deployable the deployable
     * @return the webapp object that was deployed
     */
    public abstract Object deployWebApp(Deployable deployable);

    /**
     * Implement to perform the work of the undeploy.
     * @param deployable the webapp to undeploy
     */
    public abstract void undeployWebApp(Deployable deployable);

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.deployer.Deployer#deploy(org.codehaus.cargo.container.deployable.Deployable)
     */
    public void deploy(Deployable deployable)
    {
        Object o = deployWebApp(deployable);
        addDeployedWebAppContext(getContext(deployable), o);
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.deployer.Deployer#undeploy(org.codehaus.cargo.container.deployable.Deployable)
     */
    public void undeploy(Deployable deployable)
    {
        undeployWebApp(deployable);
        removeDeployedWebAppContext(getContext(deployable));
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.deployer.Deployer#getType()
     */
    public DeployerType getType()
    {
        return DeployerType.EMBEDDED;
    }

    /**
     * Set a list of virtual hosts corresponding to the webapps deployed via this deployer.
     * @param hosts list of virtual host names
     */
    public void setVirtualHosts(String[] hosts)
    {
        this.hosts = (hosts == null ? null : new String[hosts.length]);
        System.arraycopy(hosts, 0, this.hosts, 0, hosts.length);
    }

    /**
     * @return the virtual hosts
     */
    public String[] getVirtualHosts()
    {
        return this.hosts;
    }

    /**
     * If true, all wars deployed by this deployer will be extracted before being deployed.
     * If false, they won't be extracted first. If null, jetty will do the default.
     *
     * @param extract true=extract; false=don't extract, null=do container default
     */
    public void setExtractWar(Boolean extract)
    {
        this.extract = extract;
    }

    /**
     * @return whether to extract or not
     */
    public Boolean getExtractWar()
    {
        return this.extract;
    }

    /**
     * copy webapp.
     * 
     * If true, unpacked wars will be copied to a tmp location so
     * their jars can be replaced at runtime
     * @param copy if true, webapps are copied to tmp dir
     */
    public void setCopyWebApp(Boolean copy)
    {
        this.copyWebApp = copy;
    }

    /**
     * @return wether to copy unpacked war or not
     */
    public Boolean getCopyWebApp()
    {
        return this.copyWebApp;
    }

     
    /**
     * This is called java2classloadercompliance setting in jetty5 and the parentloaderpriority
     * in jetty6. If true, the webapp classloader will try the parent.
     * classloader first.
     *
     * @param java2compliant true=inverted loading, false=servlet spec, null=do the container
     *        default
     */
    public void setParentLoaderPriority(Boolean java2compliant)
    {
        this.parentLoaderPriority = java2compliant;
    }

    /**
     * @return the class loader priority
     */
    public Boolean getParentLoaderPriority()
    {
        return this.parentLoaderPriority;
    }

    /**
     * Get the deployable that matches the context path.
     *
     * @param deployable the deployable object
     * @return the webapp object
     */
    protected static Object getDeployedWebAppContext(Deployable deployable)
    {
        synchronized (deployedWebAppMap)
        {
            return deployedWebAppMap.get(getContext(deployable));
        }
    }

    /**
     * Add a new entry to the context path:deployable map.
     * @param context the contextpath for the webapp
     * @param deployedWebApp the jetty webapp object
     */
    protected static void addDeployedWebAppContext(String context, Object deployedWebApp)
    {
        synchronized (deployedWebAppMap)
        {
            deployedWebAppMap.put(context, deployedWebApp);
        }
    }

    /**
     * Take a map entry away using the key.
     *
     * @param context the context path
     */
    protected static void removeDeployedWebAppContext(String context)
    {
        synchronized (deployedWebAppMap)
        {
            deployedWebAppMap.remove(context);
        }
    }

    /**
     * Get the context path for the webapp.
     *
     * @param deployable the deployable
     * @return the context path
     */
    public static String getContext(Deployable deployable)
    {
        return "/" + ((WAR) deployable).getContext();
    }
}
