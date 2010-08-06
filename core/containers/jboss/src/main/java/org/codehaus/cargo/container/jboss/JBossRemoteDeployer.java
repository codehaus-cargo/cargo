/* 
 * ========================================================================
 *
 * Copyright 2005 Jeff Genender. Code from this file
 * was originally imported from the JBoss Maven2 plugin.
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
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;

import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.RemoteContainer;
import org.codehaus.cargo.container.configuration.RuntimeConfiguration;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.jboss.internal.HttpURLConnection;
import org.codehaus.cargo.container.jboss.internal.JdkHttpURLConnection;
import org.codehaus.cargo.container.jboss.internal.ISimpleHttpFileServer;
import org.codehaus.cargo.container.jboss.internal.SimpleHttpFileServer;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.RemotePropertySet;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.container.spi.deployer.AbstractRemoteDeployer;
import org.codehaus.cargo.util.CargoException;
import org.codehaus.cargo.util.DefaultFileHandler;
import org.codehaus.cargo.util.FileHandler;

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
     * Location remote JBoss servers will look for files.
     */
    private InetSocketAddress deployableServerSocketAddress;

    /**
     * Used to perform file checks.
     */
    private FileHandler fileHandler;

    /**
     * Serves the deployable file to JBoss.
     */
    private ISimpleHttpFileServer fileServer;

    /**
     * Use the {@link JdkHttpURLConnection} class to connect the JBoss remote URLs.
     * 
     * @param container the container containing the configuration to use to find the deployer
     *        properties such as url, user name and password to use to connect to the deployer
     */
    public JBossRemoteDeployer(RemoteContainer container)
    {
        this(container, new JdkHttpURLConnection(), new SimpleHttpFileServer());
    }

    /**
     * @param container the container containing the configuration to use to find the deployer
     *        properties such as url, user name and password to use to connect to the deployer
     * @param connection the connection class to use
     * @param fileServer http file server to use
     */
    protected JBossRemoteDeployer(RemoteContainer container, HttpURLConnection connection,
        ISimpleHttpFileServer fileServer)
    {
        super();
        this.configuration = container.getConfiguration();
        this.connection = connection;
        this.deployableServerSocketAddress = buildSocketAddressForDeployableServer();
        this.fileHandler = new DefaultFileHandler();
        this.fileServer = fileServer;
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
        invokeRemotely(deployable, this.deployURL, true);
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.deployer.Deployer#undeploy(Deployable)
     */
    @Override
    public void undeploy(Deployable deployable)
    {
        invokeRemotely(deployable, this.undeployURL, false);
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.deployer.Deployer#redeploy(Deployable)
     */
    @Override
    public void redeploy(Deployable deployable)
    {
        invokeRemotely(deployable, this.redeployURL, true);
    }

    /**
     * @param deployable deployable to deploy
     * @param jmxConsoleURL URL to jmx console
     * @param expectDownload expect deployable to be downloaded
     */
    private void invokeRemotely(Deployable deployable, String jmxConsoleURL,
        boolean expectDownload)
    {
        this.fileServer.setLogger(this.getLogger());
        this.fileServer.setFile(this.fileHandler, deployable.getFile());
        this.fileServer.setListeningParameters(this.deployableServerSocketAddress,
            configuration.getPropertyValue(JBossPropertySet.REMOTEDEPLOY_HOSTNAME));

        try
        {
            this.fileServer.start();
            String encodedURL = encodeURLLocation(this.fileServer.getURL());
            invokeURL(createJBossRemoteURL(deployable, jmxConsoleURL, encodedURL));
            if (this.fileServer.getCallCount() == 0 && expectDownload)
            {
                throw new CargoException("Application server didn't request the file");
            }
        } 
        finally
        {
            this.fileServer.stop();
        }
    }

    /**
     * return the socket address used for serving deployables to remote JBoss servers
     * 
     * @return socket address used for remote deployment
     */
    protected InetSocketAddress buildSocketAddressForDeployableServer()
    {
        String portStr = configuration.getPropertyValue(JBossPropertySet.REMOTEDEPLOY_PORT);
        if (portStr == null)
        {
            portStr = "1" + configuration.getPropertyValue(ServletPropertySet.PORT);
        }

        String addressStr = configuration.getPropertyValue(JBossPropertySet.REMOTEDEPLOY_HOSTNAME);
        if (addressStr == null)
        {
            try
            {
                addressStr = InetAddress.getLocalHost().getCanonicalHostName();
            }
            catch (UnknownHostException e)
            {
                throw new CargoException("Could not get hostname for remote deployer", e);
            }
        }

        return new InetSocketAddress(addressStr, Integer.parseInt(portStr));
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
            getLogger().info(
                "No remote username specified, using default [" + DEFAULT_USERNAME + "]",
                this.getClass().getName());
            username = DEFAULT_USERNAME;
        }

        if (password == null)
        {
            getLogger().info(
                "No remote password specified, using default [" + DEFAULT_PASSWORD + "]",
                this.getClass().getName());
            password = DEFAULT_PASSWORD;
        }

        this.connection.connect(url, username, password);
    }

    /**
     * @param url url to encode
     * @return the URL-encoded location that can be passed in a URL
     */
    private String encodeURLLocation(URL url)
    {
        String encodedString;

        try
        {
            encodedString = URLEncoder.encode(url.toExternalForm(), "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            throw new ContainerException("Failed to encode Deployable location [" + url
                + "] using an [UTF-8] encoding", e);
        }

        return encodedString;
    }

    /**
     * Compute the JBoss deploy/undeploy URL.
     * 
     * @param deployable the file to deploy/undeploy
     * @param urlPrefix the JBoss static part of the deploy§undeploy URL
     * @param httpURL URL for JBoss to call back on
     * @return the full deploy/undeploy URL
     */
    protected String createJBossRemoteURL(Deployable deployable, String urlPrefix, String httpURL)
    {
        return this.configuration.getPropertyValue(GeneralPropertySet.PROTOCOL) + "://"
            + this.configuration.getPropertyValue(GeneralPropertySet.HOSTNAME) + ":"
            + this.configuration.getPropertyValue(ServletPropertySet.PORT) + urlPrefix + httpURL;
    }
}
