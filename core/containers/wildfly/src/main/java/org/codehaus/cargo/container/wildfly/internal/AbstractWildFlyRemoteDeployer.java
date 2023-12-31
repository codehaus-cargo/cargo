/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2024 Ali Tokmen.
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
package org.codehaus.cargo.container.wildfly.internal;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.codehaus.cargo.container.RemoteContainer;
import org.codehaus.cargo.container.configuration.RuntimeConfiguration;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.internal.http.HttpFormRequest;
import org.codehaus.cargo.container.internal.http.HttpRequest;
import org.codehaus.cargo.container.internal.http.HttpResult;
import org.codehaus.cargo.container.internal.http.MultipartFormContentType;
import org.codehaus.cargo.container.jboss.JBossPropertySet;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.RemotePropertySet;
import org.codehaus.cargo.container.spi.deployer.AbstractRemoteDeployer;
import org.codehaus.cargo.util.CargoException;

/**
 * Remote deployer that uses the remote API to deploy to WildFly.
 */
public abstract class AbstractWildFlyRemoteDeployer extends AbstractRemoteDeployer
{
    /**
     * Marshaller.
     */
    private WildFlyRemoteDeploymentJsonMarshaller marshaller;

    /**
     * Configuration.
     */
    private RuntimeConfiguration configuration;

    /**
     * @param container the container containing the configuration to use to find the deployer
     * properties such as url, user name and password to use to connect to the deployer
     */
    public AbstractWildFlyRemoteDeployer(RemoteContainer container)
    {
        super(container);

        this.configuration = container.getConfiguration();
        this.marshaller = new WildFlyRemoteDeploymentJsonMarshaller(this.configuration);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deploy(Deployable deployable)
    {
        try
        {
            String bytesValue = uploadDeployable(deployable);
            deployDeployable(deployable, bytesValue);
        }
        catch (IOException e)
        {
            throw new CargoException("Error connecting to the remote WildFly server.", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void undeploy(Deployable deployable)
    {
        try
        {
            undeployDeployable(deployable);
            removeDeployable(deployable);
        }
        catch (IOException e)
        {
            throw new CargoException("Error connecting to the remote WildFly server.", e);
        }
    }

    /**
     * Upload deployable to remote server.
     * 
     * @param deployable Deployable to be uploaded.
     * @return Value of BYTES_VALUE field in response.
     * @throws IOException If connecting to the server fails.
     */
    private String uploadDeployable(Deployable deployable) throws IOException
    {
        String username = configuration.getPropertyValue(RemotePropertySet.USERNAME);
        String password = configuration.getPropertyValue(RemotePropertySet.PASSWORD);

        URL addContentUrl = getAddContentUrl();
        String deployableFilename = marshaller.getDeployableFilename(deployable);

        MultipartFormContentType multipartFormContentType = new MultipartFormContentType();
        multipartFormContentType.setFormFile(deployableFilename, new File(deployable.getFile()));
        HttpFormRequest request = new HttpFormRequest(addContentUrl, multipartFormContentType);
        request.setLogger(this.getLogger());
        request.setAuthentication(username, password);
        HttpResult response = request.post();
        verifyResponse(response);

        return marshaller.unmarshallAddContentResponse(response);
    }

    /**
     * Deploy deployable, which is uploaded on remote server.
     * 
     * @param deployable Deployable to be deployed.
     * @param bytesValue BYTES_VALUE marking uploaded content.
     * @throws IOException If connecting to the server fails.
     */
    private void deployDeployable(Deployable deployable, String bytesValue) throws IOException
    {
        String username = configuration.getPropertyValue(RemotePropertySet.USERNAME);
        String password = configuration.getPropertyValue(RemotePropertySet.PASSWORD);

        URL managementUrl = getManagementUrl();
        String deployRequest = marshaller.marshallDeployRequest(deployable, bytesValue);

        HttpRequest request = new HttpRequest(managementUrl);
        request.setLogger(this.getLogger());
        request.addRequestProperty("Content-Type", "application/json");
        request.setAuthentication(username, password);
        request.setRequestBody(deployRequest);

        HttpResult response = request.post();
        verifyResponse(response);
    }

    /**
     * Undeploy deployable on server.
     * 
     * @param deployable Deployable to be undeployed.
     * @throws IOException If connecting to the server fails.
     */
    private void undeployDeployable(Deployable deployable) throws IOException
    {
        String username = configuration.getPropertyValue(RemotePropertySet.USERNAME);
        String password = configuration.getPropertyValue(RemotePropertySet.PASSWORD);

        URL managementUrl = getManagementUrl();
        String undeployRequest = marshaller.marshallUndeployRequest(deployable);

        HttpRequest request = new HttpRequest(managementUrl);
        request.setLogger(this.getLogger());
        request.addRequestProperty("Content-Type", "application/json");
        request.setAuthentication(username, password);
        request.setRequestBody(undeployRequest);

        HttpResult response = request.post();
        verifyResponse(response);
    }

    /**
     * Remove deployable from server.
     * 
     * @param deployable Deployable to be removed.
     * @throws IOException If connecting to the server fails.
     */
    private void removeDeployable(Deployable deployable) throws IOException
    {
        String username = configuration.getPropertyValue(RemotePropertySet.USERNAME);
        String password = configuration.getPropertyValue(RemotePropertySet.PASSWORD);

        URL managementUrl = getManagementUrl();
        String removeRequest = marshaller.marshallRemoveRequest(deployable);

        HttpRequest request = new HttpRequest(managementUrl);
        request.setLogger(this.getLogger());
        request.addRequestProperty("Content-Type", "application/json");
        request.setAuthentication(username, password);
        request.setRequestBody(removeRequest);

        HttpResult response = request.post();
        verifyResponse(response);
    }

    /**
     * @return URL for adding content.
     */
    private URL getAddContentUrl()
    {
        String hostname = configuration.getPropertyValue(GeneralPropertySet.HOSTNAME);
        String managementPortValue = configuration.getPropertyValue(
            JBossPropertySet.JBOSS_MANAGEMENT_HTTP_PORT);
        int managementPort = Integer.valueOf(managementPortValue);

        try
        {
            return new URL("http", hostname, managementPort, "/management/add-content");
        }
        catch (MalformedURLException e)
        {
            throw new CargoException("Error while trying to create URL.", e);
        }
    }

    /**
     * @return Management URL.
     */
    private URL getManagementUrl()
    {
        String hostname = configuration.getPropertyValue(GeneralPropertySet.HOSTNAME);
        String managementPortValue = configuration.getPropertyValue(
            JBossPropertySet.JBOSS_MANAGEMENT_HTTP_PORT);
        int managementPort = Integer.valueOf(managementPortValue);

        try
        {
            return new URL("http", hostname, managementPort, "/management");
        }
        catch (MalformedURLException e)
        {
            throw new CargoException("Error while trying to create URL.", e);
        }
    }

    /**
     * Verify HTTP response.
     * 
     * @param response Response to be verified.
     */
    private void verifyResponse(HttpResult response)
    {
        if (!response.isSuccessful())
        {
            throw new CargoException("HTTP request failed, response code: "
                + response.getResponseCode() + ", response message: "
                    + response.getResponseMessage() + ", response body: "
                        + response.getResponseBody());
        }
    }
}
