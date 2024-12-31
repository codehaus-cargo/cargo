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
package org.codehaus.cargo.container.deployer;

import java.net.MalformedURLException;
import java.net.URL;

import org.codehaus.cargo.container.configuration.Configuration;
import org.codehaus.cargo.container.internal.util.HttpUtils;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.util.CargoException;

/**
 * Monitor that verifies if a {@link org.codehaus.cargo.container.deployable.Deployable} is deployed
 * by pinging a URL constructed from  URL path (provided by the user) and other parameters
 * provided by container configuration.
 */
public class UrlPathDeployableMonitor extends AbstractDeployableMonitor
{
    /**
     * Container configuration which carry informations about host, port....
     */
    private Configuration configuration;

    /**
     * The URL path of deployable to be pinged.
     */
    private String pingUrlPath;

    /**
     * Useful HTTP methods (specifically the ping method).
     */
    private HttpUtils httpUtils = new HttpUtils();

    /**
     * @param configuration container configuration
     * @param pingUrlPath the URL path to be pinged and which will tell when the
     * {@link org.codehaus.cargo.container.deployable.Deployable} is deployed
     */
    public UrlPathDeployableMonitor(Configuration configuration, String pingUrlPath)
    {
        super();
        this.configuration = configuration;
        this.pingUrlPath = pingUrlPath;
    }

    /**
     * @param configuration container configuration
     * @param pingUrlPath the URL path to be pinged and which will tell when the
     * {@link org.codehaus.cargo.container.deployable.Deployable} is deployed
     * @param timeout the timeout after which we stop monitoring the deployment
     */
    public UrlPathDeployableMonitor(Configuration configuration, String pingUrlPath, long timeout)
    {
        super(timeout);
        this.configuration = configuration;
        this.pingUrlPath = pingUrlPath;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDeployableName()
    {
        return pingUrlPath;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void monitor()
    {
        URL pingUrl = constructPingUrl();

        getLogger().debug("Checking URL [" + pingUrl + "] for status using a timeout of ["
            + getTimeout() + "] ms...", this.getClass().getName());

        // We check if the deployable is servicing requests by pinging a URL specified by the user
        HttpUtils.HttpResult results = new HttpUtils.HttpResult();
        boolean isDeployed = this.httpUtils.ping(pingUrl, results, getTimeout());

        String msg = "URL [" + pingUrl + "] is ";
        if (isDeployed)
        {
            msg += "responding...";
        }
        else
        {
            msg += "not responding: " + results.responseCode + " " + results.responseMessage;
        }
        getLogger().debug(msg, this.getClass().getName());

        notifyListeners(isDeployed);
    }

    /**
     * @return URL where we can check status of deployable.
     */
    private URL constructPingUrl()
    {
        URL deployableUrl = null;

        String protocolProperty = configuration.getPropertyValue(GeneralPropertySet.PROTOCOL);
        String hostnameProperty = configuration.getPropertyValue(GeneralPropertySet.HOSTNAME);
        String portProperty = configuration.getPropertyValue(ServletPropertySet.PORT);
        String offsetProperty = configuration.getPropertyValue(GeneralPropertySet.PORT_OFFSET);

        int port = Integer.parseInt(portProperty);
        if (offsetProperty != null)
        {
            int offset = Integer.parseInt(offsetProperty);
            port += offset;
        }

        try
        {
            deployableUrl = new URL(protocolProperty, hostnameProperty, port, pingUrlPath);
        }
        catch (MalformedURLException e)
        {
            throw new CargoException("Unable to construct deployable URL.", e);
        }

        return deployableUrl;
    }
}
