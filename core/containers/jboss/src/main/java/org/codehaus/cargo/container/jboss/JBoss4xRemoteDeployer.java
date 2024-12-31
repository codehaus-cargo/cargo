/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2025 Ali Tokmen.
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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.RemoteContainer;
import org.codehaus.cargo.container.configuration.RuntimeConfiguration;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployer.DeployableMonitor;
import org.codehaus.cargo.container.jboss.internal.ISimpleHttpFileServer;
import org.codehaus.cargo.container.jboss.internal.JdkHttpURLConnection;
import org.codehaus.cargo.container.jboss.internal.SimpleHttpFileServer;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.RemotePropertySet;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.container.spi.deployer.AbstractRemoteDeployer;
import org.codehaus.cargo.container.spi.deployer.DeployerWatchdog;
import org.codehaus.cargo.util.CargoException;
import org.codehaus.cargo.util.DefaultFileHandler;
import org.codehaus.cargo.util.FileHandler;

/**
 * Remote deployer that uses the JMX console to deploy to JBoss.
 */
public class JBoss4xRemoteDeployer extends AbstractRemoteDeployer
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
    private JdkHttpURLConnection connection;

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
     * properties such as url, user name and password to use to connect to the deployer
     */
    public JBoss4xRemoteDeployer(RemoteContainer container)
    {
        this(container, new JdkHttpURLConnection(), new SimpleHttpFileServer());
    }

    /**
     * @param container the container containing the configuration to use to find the deployer
     * properties such as url, user name and password to use to connect to the deployer
     * @param connection the connection class to use
     * @param fileServer http file server to use
     */
    protected JBoss4xRemoteDeployer(RemoteContainer container, JdkHttpURLConnection connection,
        ISimpleHttpFileServer fileServer)
    {
        super(container);

        this.configuration = container.getConfiguration();
        this.connection = connection;
        this.deployableServerSocketAddress = buildSocketAddressForDeployableServer();
        this.fileHandler = new DefaultFileHandler();
        this.fileHandler.setLogger(this.getLogger());
        fileServer.setFileHandler(this.fileHandler);
        fileServer.setLogger(this.getLogger());
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
     */
    @Override
    public void deploy(Deployable deployable)
    {
        invokeRemotely(deployable, this.deployURL, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void undeploy(Deployable deployable)
    {
        invokeRemotely(deployable, this.undeployURL, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void redeploy(Deployable deployable)
    {
        invokeRemotely(deployable, this.redeployURL, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void redeploy(Deployable deployable, DeployableMonitor monitor)
    {
        this.redeploy(deployable);

        // Wait for the Deployable to be redeployed
        DeployerWatchdog watchdog = new DeployerWatchdog(monitor);
        watchdog.setLogger(getLogger());
        watchdog.watchForAvailability();
    }

    /**
     * @param deployable deployable to deploy
     * @param jmxConsoleURL URL to jmx console
     * @param expectDownload expect deployable to be downloaded
     */
    private void invokeRemotely(Deployable deployable, String jmxConsoleURL,
        boolean expectDownload)
    {
        this.fileServer.setFile(deployable,
            configuration.getPropertyValue(JBossPropertySet.DEPLOYER_KEEP_ORIGINAL_WAR_FILENAME));
        this.fileServer.setListeningParameters(this.deployableServerSocketAddress,
            configuration.getPropertyValue(JBossPropertySet.REMOTEDEPLOY_HOSTNAME));

        try
        {
            this.fileServer.start();

            // TODO: URLEncoder.encode(String, Charset) was introduced in Java 10,
            //       simplify the below code when Codehaus Cargo is on Java 10+
            String encodedURL = URLEncoder.encode(
                this.fileServer.getURL().toExternalForm(), StandardCharsets.UTF_8.name());
            String invokedURL =
                this.configuration.getPropertyValue(GeneralPropertySet.PROTOCOL) + "://"
                    + this.configuration.getPropertyValue(GeneralPropertySet.HOSTNAME) + ":"
                        + this.configuration.getPropertyValue(ServletPropertySet.PORT)
                            + jmxConsoleURL + encodedURL;

            String username = this.configuration.getPropertyValue(RemotePropertySet.USERNAME);
            if (username == null)
            {
                getLogger().info(
                    "No remote username specified, using default [" + DEFAULT_USERNAME + "]",
                        this.getClass().getName());
                username = DEFAULT_USERNAME;
            }

            String password = this.configuration.getPropertyValue(RemotePropertySet.PASSWORD);
            if (password == null)
            {
                getLogger().info(
                    "No remote password specified, using default [" + DEFAULT_PASSWORD + "]",
                        this.getClass().getName());
                password = DEFAULT_PASSWORD;
            }

            // Set a timeout in order to avoid CARGO-859
            String timeout = configuration.getPropertyValue(RemotePropertySet.TIMEOUT);

            // Request the deployment via the JBoss JMX console, which should connect to our
            // locally running Web server to download the file
            this.connection.connect(
                invokedURL, username, password, Integer.parseInt(timeout), getLogger());

            // Check if JBoss did access our locally running Web server
            if (this.fileServer.getCallCount() == 0 && expectDownload)
            {
                throw new CargoException("Application server didn't request the file");
            }
        }
        catch (MalformedURLException | UnsupportedEncodingException e)
        {
            throw new CargoException("Exception building JBoss JMX console URL", e);
        }
        catch (IOException e)
        {
            throw new CargoException("Cannot connect to the JBoss server", e);
        }
        catch (ContainerException e)
        {
            if (e.getCause() != null && e.getCause() instanceof SocketTimeoutException)
            {
                Throwable realCause = this.fileServer.getException();
                if (realCause != null)
                {
                    throw new ContainerException(
                        "The Codehaus Cargo embedded HTTP server failed", realCause);
                }
            }

            throw e;
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
}
