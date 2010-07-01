/* 
 * ========================================================================
 *
 * Copyright 2005 Jeff Genender for some portions of the code below which
 * was inspired/copied from the JBoss Maven2 plugin (dated 1st Feb 2006)
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
package org.codehaus.cargo.container.jboss;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.RemoteContainer;
import org.codehaus.cargo.container.jboss.internal.HttpURLConnection;
import org.codehaus.cargo.container.jboss.internal.JdkHttpURLConnection;
import org.codehaus.cargo.container.configuration.RuntimeConfiguration;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.container.property.RemotePropertySet;
import org.codehaus.cargo.container.spi.deployer.AbstractRemoteDeployer;

/**
 * Remote deployer that uses JMX to deploy to JBoss.
 * 
 * @version $Id$
 */
public class JBossRemoteDeployer extends AbstractRemoteDeployer
{
    /**
     * The default username to use when authenticating with JBoss server.
     */
    private static final String DEFAULT_USERNAME = "admin";

    /**
     * The default password to use when authenticating with JBoss server.
     */
    private static final String DEFAULT_PASSWORD = "";

    /**
     * The JBoss JMX deployment URL.
     */
    private String deployURL = "/jmx-console/HtmlAdaptor?action=invokeOpByName&"
        + "name=jboss.system:service%3DMainDeployer&methodName=deploy&argType=java.net.URL&arg0=";

    /**
     * The JBoss JMX undeployment URL.
     */
    private String undeployURL = "/jmx-console/HtmlAdaptor?action=invokeOpByName&"
        + "name=jboss.system:service%3DMainDeployer&methodName=undeploy&argType=java.net.URL&arg0=";

    /**
     * The JBoss JMX redeployment URL.
     */
    private String redeployURL = "/jmx-console/HtmlAdaptor?action=invokeOpByName&"
        + "name=jboss.system:service%3DMainDeployer&methodName=redeploy&argType=java.net.URL&arg0=";

    /**
     * The configuration object containing the deployer config data.
     */
    private RuntimeConfiguration configuration;

    /**
     * HTTP connection class to use for deploying/undeploying to JBoss.
     */
    private HttpURLConnection connection;

    /**
     * Use the {@link JdkHttpURLConnection} class to connect the JBoss remote URLs.
     * 
     * @param container the container containing the configuration to use to find the deployer
     *        properties such as url, user name and password to use to connect to the deployer
     */
    public JBossRemoteDeployer(RemoteContainer container)
    {
        this(container, new JdkHttpURLConnection());
    }

    /**
     * @param container the container containing the configuration to use to find the deployer
     *        properties such as url, user name and password to use to connect to the deployer
     * @param connection the connection class to use
     */
    protected JBossRemoteDeployer(RemoteContainer container, HttpURLConnection connection)
    {
        super();
        this.configuration = container.getConfiguration();
        this.connection = connection;
    }

    /**
     * @param deployURL the deployment URL that will override the default
     */
    public void setDeployURL(String deployURL)
    {
        this.deployURL = deployURL;
    }

    /**
     * @param undeployURL the undeployment URL that will override the default
     */
    public void setUndeployURL(String undeployURL)
    {
        this.undeployURL = undeployURL;
    }

    /**
     * @param redeployURL the redeployment URL that will override the default
     */
    public void setRedeployURL(String redeployURL)
    {
        this.redeployURL = redeployURL;
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.deployer.Deployer#deploy(Deployable)
     */
    @Override
    public void deploy(Deployable deployable)
    {
        invokeURL(createJBossRemoteURL(deployable, this.deployURL));
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.deployer.Deployer#undeploy(Deployable)
     */
    @Override
    public void undeploy(Deployable deployable)
    {
        invokeURL(createJBossRemoteURL(deployable, this.undeployURL));
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.deployer.Deployer#redeploy(Deployable)
     */
    @Override
    public void redeploy(Deployable deployable)
    {
        invokeURL(createJBossRemoteURL(deployable, this.redeployURL));
    }
    
    /**
     * @param url the JBoss JMX URL to invoke
     */
    private void invokeURL(String url)
    {
        String username = this.configuration.getPropertyValue(RemotePropertySet.USERNAME);
        String password = this.configuration.getPropertyValue(RemotePropertySet.PASSWORD);

        if (username == null)
        {
            getLogger().info("No remote username specified, using default [" + DEFAULT_USERNAME
                + "]", this.getClass().getName());
            username = DEFAULT_USERNAME;
        }

        if (password == null)
        {
            getLogger().info("No remote password specified, using default [" + DEFAULT_PASSWORD
                + "]", this.getClass().getName());
            password = DEFAULT_PASSWORD;
        }

        this.connection.connect(url, username, password);
    }

    /**
     * @param deployable the deployable for which we'll URL-encode the location
     * @return the URL-encoded location that can be passed in a URL
     */
    private String encodeDeployableLocation(Deployable deployable)
    {
        String encodedString;

        try
        {
            encodedString = URLEncoder.encode(deployable.getFile(), "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            throw new ContainerException("Failed to encode Deployable location ["
                + deployable.getFile() + "] using an [UTF-8] encoding", e);
        }

        return encodedString;
    }

    /**
     * Compute the JBoss deploy/undeploy URL.
     *
     * @param deployable the file to deploy/undeploy
     * @param urlPrefix the JBoss static part of the deploy§undeploy URL
     * @return the full deploy/undeploy URL
     */
    protected String createJBossRemoteURL(Deployable deployable, String urlPrefix)
    {
        return this.configuration.getPropertyValue(GeneralPropertySet.PROTOCOL) + "://"
            + this.configuration.getPropertyValue(GeneralPropertySet.HOSTNAME)
            + ":"
            + this.configuration.getPropertyValue(ServletPropertySet.PORT)
            + urlPrefix
            + "file:"
            + encodeDeployableLocation(deployable);
    }
}
