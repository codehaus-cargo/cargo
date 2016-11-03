/* 
 * ========================================================================
 *
 * Copyright 2005 Jeff Genender. Code from this file
 * was originally imported from the JBoss Maven2 plugin.
 *
 * Codehaus CARGO, copyright 2004-2011 Vincent Massol, 2012-2016 Ali Tokmen.
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

import java.net.MalformedURLException;
import java.net.URL;

import org.codehaus.cargo.container.RemoteContainer;
import org.codehaus.cargo.container.configuration.RuntimeConfiguration;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.internal.http.HttpConnection;
import org.codehaus.cargo.container.internal.http.HttpResult;
import org.codehaus.cargo.container.internal.http.request.HttpFormRequest;
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
     * Username for remote deployment.
     */
    private String username;

    /**
     * Password for remote deployment.
     */
    private String password;

    /**
     * Hostname.
     */
    private String hostname;

    /**
     * Management port.
     */
    private int managementPort;

    /**
     * @param container the container containing the configuration to use to find the deployer
     * properties such as url, user name and password to use to connect to the deployer
     */
    public AbstractWildFlyRemoteDeployer(RemoteContainer container)
    {
        super(container);

        RuntimeConfiguration configuration = container.getConfiguration();

        this.marshaller = new WildFlyRemoteDeploymentJsonMarshaller();

        this.username = configuration.getPropertyValue(RemotePropertySet.USERNAME);
        this.password = configuration.getPropertyValue(RemotePropertySet.PASSWORD);
        this.hostname = configuration.getPropertyValue(GeneralPropertySet.HOSTNAME);
        String managementPort = configuration.getPropertyValue(
                JBossPropertySet.JBOSS_MANAGEMENT_HTTP_PORT);
        this.managementPort = Integer.valueOf(managementPort);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deploy(Deployable deployable)
    {
        String bytesValue = uploadDeployable(deployable);
        deployDeployable(deployable, bytesValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void undeploy(Deployable deployable)
    {
        undeployDeployable(deployable);
        removeDeployable(deployable);
    }

    /**
     * Upload deployable to remote server.
     *
     * @param deployable Deployable to be uploaded.
     * @return Value of BYTES_VALUE field in response.
     */
    private String uploadDeployable(Deployable deployable)
    {
        URL addContentUrl = getAddContentUrl();
        String deploymentName = deployable.getName() + "." + deployable.getType().getType();

        HttpFormRequest formRequest = new HttpFormRequest(addContentUrl);
        formRequest.setAuthentication(username, password);

        StringBuffer sb = new StringBuffer();
        sb.append("Content-Disposition: form-data; ");
        sb.append("name=\"file\"; ");
        sb.append("filename=\"" + deploymentName + "\"");

        HttpResult response = formRequest.execute(sb.toString(), deployable.getFile());
        verifyResponse(response);

        return marshaller.unmarshallAddContentResponse(response);
    }

    /**
     * Deploy deployable, which is uploaded on remote server.
     *
     * @param deployable Deployable to be deployed.
     * @param bytesValue BYTES_VALUE marking uploaded content.
     */
    private void deployDeployable(Deployable deployable, String bytesValue)
    {
        URL managementUrl = getManagementUrl();
        String deployRequest = marshaller.marshallDeployRequest(deployable, bytesValue);

        HttpConnection connection = new HttpConnection(managementUrl);
        connection.addRequestProperty("Content-Type", "application/json");
        connection.setAuthentication(username, password);
        connection.setRequestBody(deployRequest);

        HttpResult response = connection.post();
        verifyResponse(response);
    }

    /**
     * Undeploy deployable on server.
     *
     * @param deployable Deployable to be undeployed.
     */
    private void undeployDeployable(Deployable deployable)
    {
        URL managementUrl = getManagementUrl();
        String undeployRequest = marshaller.marshallUndeployRequest(deployable);

        HttpConnection connection = new HttpConnection(managementUrl);
        connection.addRequestProperty("Content-Type", "application/json");
        connection.setAuthentication(username, password);
        connection.setRequestBody(undeployRequest);

        HttpResult response = connection.post();
        verifyResponse(response);
    }

    /**
     * Remove deployable from server.
     *
     * @param deployable Deployable to be removed.
     */
    private void removeDeployable(Deployable deployable)
    {
        URL managementUrl = getManagementUrl();
        String removeRequest = marshaller.marshallRemoveRequest(deployable);

        HttpConnection connection = new HttpConnection(managementUrl);
        connection.addRequestProperty("Content-Type", "application/json");
        connection.setAuthentication(username, password);
        connection.setRequestBody(removeRequest);

        HttpResult response = connection.post();
        verifyResponse(response);
    }

    /**
     * @return URL for adding content.
     */
    private URL getAddContentUrl()
    {
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
