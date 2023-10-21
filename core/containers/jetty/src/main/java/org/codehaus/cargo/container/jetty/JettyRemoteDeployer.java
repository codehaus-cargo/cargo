/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2023 Ali Tokmen.
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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.RemoteContainer;
import org.codehaus.cargo.container.configuration.Configuration;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.internal.http.HttpFileRequest;
import org.codehaus.cargo.container.internal.http.HttpRequest;
import org.codehaus.cargo.container.internal.http.HttpResult;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.RemotePropertySet;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.container.spi.deployer.AbstractRemoteDeployer;

/**
 * A remote deployer for the Jetty container.<br><br>
 * <b>NOTE</b>: undeploy <u>DELETES</u> the webapp from the Jetty webapp directory.<br><br>
 * Limitations:
 * <ul>
 * <li>Will not undeploy files from anywhere other than the servers webapp directory</li>
 * <li>Cannot be used to undeploy webapps that were deployed using a xml context file in
 * <code>/contexts</code></li>
 * <li>Should not be used with multiple webapps sharing a common war</li>
 * </ul>
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
     * Operation timeout when communicating with Jetty remote deployer.
     */
    private int timeout = 0;

    /**
     * Remote deployer for the Jetty container.
     * @param container The container used for deployment
     */
    public JettyRemoteDeployer(RemoteContainer container)
    {
        super(container);

        Configuration configuration = container.getConfiguration();

        username = configuration.getPropertyValue(RemotePropertySet.USERNAME);
        password = configuration.getPropertyValue(RemotePropertySet.PASSWORD);
        deployerUrl = configuration.getPropertyValue(JettyPropertySet.DEPLOYER_URL);
        String timeoutStr = configuration.getPropertyValue(RemotePropertySet.TIMEOUT);
        if (timeoutStr != null && !timeoutStr.isEmpty())
        {
            timeout = Integer.parseInt(timeoutStr);
        }

        if (deployerUrl == null)
        {
            this.deployerUrl = createDefaultDeployerUrl(configuration);
        }
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
     * {@inheritDoc}
     */
    @Override
    public void deploy(Deployable deployable)
    {
        try
        {
            WAR war = (WAR) deployable;
            String path = URLEncoder.encode(war.getContext(), StandardCharsets.UTF_8.name());
            invoke("/deploy?path=/" + path, new File(war.getFile()));
        }
        catch (IOException e)
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
            WAR war = (WAR) deployable;
            String path = URLEncoder.encode(war.getContext(), StandardCharsets.UTF_8.name());
            invoke("/undeploy?path=/" + path);
        }
        catch (IOException e)
        {
            throw new ContainerException("Failed to undeploy [" + deployable + "]", e);
        }
    }

    /**
     * Invokes Tomcat manager with the specified command.
     * 
     * @param path the Tomcat manager command to invoke
     * @throws IOException If there is an I/O issue communicating with the server
     */
    protected void invoke(String path) throws IOException
    {
        invoke(path, null);
    }

    /**
     * Invokes Jetty remote deployer web app with the specified command and content data.
     * 
     * @param path the Jetty remote deployer web app command to invoke
     * @param fileData the file to stream as content data, if needed
     * @return the result of the invoking command, as returned by the Jetty remote deployer web app
     * @throws IOException If there is an I/O issue communicating with the server
     */
    protected String invoke(String path, File fileData) throws IOException
    {
        getLogger().debug("Invoking Jetty remote deployer using path [" + path + "]",
            getClass().getName());

        HttpResult response;
        URL invokeURL = new URL(this.deployerUrl + path);
        if (fileData == null)
        {
            getLogger().debug("Performing GET request", getClass().getName());
            HttpRequest request = new HttpRequest(invokeURL, this.timeout);
            request.setLogger(this.getLogger());
            request.setAuthentication(username, password);
            response = request.get();
        }
        else
        {
            getLogger().debug("Performing PUT request", getClass().getName());
            HttpFileRequest request = new HttpFileRequest(invokeURL, fileData, this.timeout);
            request.setLogger(this.getLogger());
            request.setAuthentication(username, password);
            response = request.put();
        }
        if (!response.isSuccessful())
        {
            throw new IOException("HTTP request failed, response code: "
                + response.getResponseCode() + ", response message: "
                    + response.getResponseMessage() + ", response body: "
                        + response.getResponseBody());
        }
        else
        {
            String responseBody = response.getResponseBody();
            if (responseBody == null
                || !lastLine(responseBody).startsWith("OK -")
                    && !lastLine(responseBody).startsWith("Webapp deployed at context "))
            {
                throw new ContainerException("The Jetty remote deployer webapp responded \""
                    + response + "\" instead of the expected success message");
            }
            return responseBody;
        }
    }

    /**
     * Returns the last line of a string.
     * @param string String of which to get the last line
     * @return Last line of <code>string</code>
     * @throws IOException if an i/o error occurs
     */
    protected String lastLine(String string) throws IOException
    {
        try (BufferedReader reader = new BufferedReader(new StringReader(string)))
        {
            String lastNonNullLine = "";
            String lastLine = null;
            while ((lastLine = reader.readLine()) != null)
            {
                if (!"".equals(lastLine))
                {
                    lastNonNullLine = lastLine;
                }
            }
            return lastNonNullLine;
        }
    }

}
