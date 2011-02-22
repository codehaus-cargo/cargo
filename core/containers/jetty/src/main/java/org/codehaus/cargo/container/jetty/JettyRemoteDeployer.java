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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.RemoteContainer;
import org.codehaus.cargo.container.configuration.Configuration;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.RemotePropertySet;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.container.spi.deployer.AbstractRemoteDeployer;
import org.codehaus.cargo.util.Base64;

/**
 * A remote deployer for the Jetty container.
 * 
 * NOTE: undeploy DELETES the webapp from the Jetty webapp directory
 * 
 * Limitations: - will not undeploy files from anywhere other than the servers webapp directory -
 * cannot be used to undeploy webapps that were deployed using a xml context file in /contexts -
 * should not be used with multiple webapps sharing a common war.
 * 
 * @version $Id$
 */
public class JettyRemoteDeployer extends AbstractRemoteDeployer
{

    /**
     * The default context of the Jetty remote deployer.
     */
    private static final String DEFAULT_DEPLOYER_CONTEXT = "cargo-jetty-deployer";

    /**
     * The username to use for the remote server authentication.
     */
    private String username;

    /**
     * The password to be used for the remote server authentication.
     */
    private String password;

    /**
     * The url to the Jetty remote deployer.
     */
    private String deployerUrl;

    /**
     * Remote deployer for the Jetty container.
     * @param container The container used for deployment
     */
    public JettyRemoteDeployer(RemoteContainer container)
    {
        Configuration configuration = container.getConfiguration();

        username = configuration.getPropertyValue(RemotePropertySet.USERNAME);
        password = configuration.getPropertyValue(RemotePropertySet.PASSWORD);
        deployerUrl = configuration.getPropertyValue(JettyPropertySet.DEPLOYER_URL);

        if (deployerUrl == null)
        {
            this.deployerUrl = createDefaultDeployerUrl(configuration);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deploy(Deployable deployable)
    {
        try
        {
            File webapp = new File(deployable.getFile());

            HttpURLConnection connection = createDeployConnection((WAR) deployable);

            pipe(new FileInputStream(webapp), connection.getOutputStream());

            String response = getResponseMessage(connection);

            if (!response.startsWith("OK -"))
            {
                throw new ContainerException("Response when calling " + connection.getURL()
                    + " was: " + response);
            }

        }
        catch (Exception e)
        {
            throw new ContainerException("Failed to deploy [" + deployable.getFile() + "]", e);
        }
    }

    /**
     * Undeploy a {@link Deployable} from the running container. NOTE: THIS WILL DELETE THE WAR FROM
     * THE WEBAPP DIRECTORY
     * @param deployable The deployable to be undeployed
     */
    @Override
    public void undeploy(Deployable deployable)
    {
        try
        {
            HttpURLConnection connection = createUndeployConnection((WAR) deployable);

            String response = getResponseMessage(connection);

            if (!response.startsWith("OK -"))
            {
                throw new ContainerException(response);
            }
        }
        catch (Exception e)
        {
            throw new ContainerException("Failed to undeploy [" + deployable + "]", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void redeploy(Deployable deployable)
    {
        undeploy(deployable);
        deploy(deployable);
    }

    /**
     * Creates an deploy connection for the deployer.
     * @param war The war to be deployed
     * @return The URL for the deployer
     * @throws IOException If an IOException occurs
     */
    protected HttpURLConnection createDeployConnection(WAR war) throws IOException
    {

        String deployUrl = this.deployerUrl + "/deploy?path=/" + war.getContext();

        URL url = new URL(deployUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setAllowUserInteraction(false);
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setUseCaches(false);
        connection.setRequestMethod("PUT");
        connection.setRequestProperty("Content-Type", "application/octet-stream");

        // When trying to upload large amount of data the internal connection buffer can become
        // too large and exceed the heap size, leading to a java.lang.OutOfMemoryError.
        // This was fixed in JDK 1.5 by introducing a new setChunkedStreamingMode() method.
        // As Cargo should also work with JDK versions lesser than 1.5 we use reflection to call
        // setChunkedStreamingMode(). If it fails, we assume we're running an older version of
        // the JDK. In that case the solution for the user is to increase it's heap size.
        // For reference, see the following discussions about this:
        // http://www.velocityreviews.com/forums/t149076-leaking-memory-when-writing-to-url.html
        // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=5026745
        // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4212479
        try
        {
            connection.getClass().getMethod("setChunkedStreamingMode",
                new Class[] {Integer.TYPE}).invoke(connection, new Object[] {new Integer(0)});
        }
        catch (Exception e)
        {
            // We assume we're on an older JDK version, do nothing.
            getLogger().debug("Not calling setChunkedStreamingMode() method as JVM ["
                + System.getProperty("java.version") + "] doesn't support it.",
                getClass().getName());
        }

        if (this.username != null)
        {
            String authorization = toAuthorization(this.username, this.password);
            connection.setRequestProperty("Authorization", authorization);
        }

        connection.connect();

        return connection;
    }

    /**
     * Creates an undeploy connection for the deployer.
     * @param war The war to be undeployed
     * @return The URL for the deployer
     * @throws IOException If an IOException occurs
     */
    protected HttpURLConnection createUndeployConnection(WAR war) throws IOException
    {
        String undeployURL = this.deployerUrl + "/undeploy?path=/" + war.getContext();
        URL url = new URL(undeployURL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setAllowUserInteraction(false);
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setUseCaches(false);
        connection.setDoOutput(false);
        connection.setRequestMethod("GET");

        if (this.username != null)
        {
            String authorization = toAuthorization(this.username, this.password);
            connection.setRequestProperty("Authorization", authorization);
        }

        connection.connect();

        return connection;
    }

    /**
     * Returns a deployerURL based on default values.
     * @param configuration The server configuration object
     * @return The url for the deployer
     */
    protected String createDefaultDeployerUrl(Configuration configuration)
    {
        String protocol = configuration.getPropertyValue(GeneralPropertySet.PROTOCOL);
        String host = configuration.getPropertyValue(GeneralPropertySet.HOSTNAME);
        String port = configuration.getPropertyValue(ServletPropertySet.PORT);

        String deployerUrl = protocol + "://" + host + ":" + port + "/" + DEFAULT_DEPLOYER_CONTEXT;

        return deployerUrl;
    }

    /**
     * Returns the response message from the server in a string format.
     * @param connection The connection used
     * @return The response message
     * @throws IOException If an IO Exception occured
     */
    protected String getResponseMessage(HttpURLConnection connection) throws IOException
    {
        int code = connection.getResponseCode();

        String response = "";

        try
        {
            // we got an error, try and get the error message
            if (code >= 400)
            {
                // we got an error, try and get the error message
                response = streamToString(connection.getInputStream(), "UTF-8");
            }
            else
            {
                // no error was reported so try and get the message from the
                // input stream
                response = streamToString(connection.getInputStream(), "UTF-8");
            }
        }
        catch (Exception e)
        {
            getLogger().warn("Exception while getting response: " + e, getClass().getName());
        }
        return response;
    }

    /**
     * Reads all the data from the specified input stream and writes it to the specified output
     * stream. Both streams are also closed.
     * 
     * TODO: make these commands as part of a generic helper class in Cargo. Duplicate function in
     * the Tomcat remote deployer
     * 
     * @param in the input stream to read from
     * @param out the output stream to write to
     * @throws IOException if an i/o error occurs
     */
    protected void pipe(InputStream in, OutputStream out) throws IOException
    {
        BufferedOutputStream bufferedOut = new BufferedOutputStream(out);
        int n;
        byte[] bytes = new byte[1024 * 4];
        while ((n = in.read(bytes)) != -1)
        {
            bufferedOut.write(bytes, 0, n);
        }
        bufferedOut.flush();
        bufferedOut.close();
        in.close();
    }

    /**
     * Gets the data from the specified input stream as a string using the specified charset.
     * 
     * TODO: make these commands as part of a generic helper class in Cargo. Duplicate function in
     * the Tomcat remote deployer
     * 
     * @param in the input stream to read from
     * @param charset the charset to use when constructing the string
     * @return a string representation of the data read from the input stream
     * @throws IOException if an i/o error occurs
     */
    protected String streamToString(InputStream in, String charset) throws IOException
    {
        InputStreamReader reader = new InputStreamReader(in, charset);

        StringBuilder buffer = new StringBuilder();
        char[] chars = new char[1024];
        int n;
        while ((n = reader.read(chars, 0, chars.length)) != -1)
        {
            buffer.append(chars, 0, n);
        }

        return buffer.toString();
    }

    /**
     * Gets the HTTP Basic Authorization header value for the supplied username and password.
     * 
     * TODO: make these commands as part of a generic helper class in Cargo. Duplicate function in
     * the Tomcat remote deployer
     * 
     * @param username the username to use for authentication
     * @param password the password to use for authentication
     * @return the HTTP Basic Authorization header value
     */
    protected static String toAuthorization(String username, String password)
    {
        StringBuilder buffer = new StringBuilder();
        buffer.append(username).append(':');
        if (password != null)
        {
            buffer.append(password);
        }
        return "Basic " + new String(Base64.encodeBase64(buffer.toString().getBytes()));
    }

}
