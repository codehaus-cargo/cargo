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

import java.net.URL;

import org.codehaus.cargo.container.internal.util.HttpUtils;

/**
 * Monitor that verifies if a {@link org.codehaus.cargo.container.deployable.Deployable} is deployed
 * by pinging a URL provided by the user.
 */
public class URLDeployableMonitor extends AbstractDeployableMonitor
{
    /**
     * The URL to ping.
     */
    private URL pingURL;

    /**
     * Useful HTTP methods (specifically the ping method).
     */
    private HttpUtils httpUtils = new HttpUtils();

    /**
     * String that must be contained in the HTTP response.
     */
    private String contains;

    /**
     * @param pingURL the URL to be pinged and which will tell when the
     * {@link org.codehaus.cargo.container.deployable.Deployable} is deployed
     */
    public URLDeployableMonitor(URL pingURL)
    {
        this.pingURL = pingURL;
    }

    /**
     * @param pingURL the URL to be pinged and which will tell when the
     * {@link org.codehaus.cargo.container.deployable.Deployable} is deployed
     * @param timeout the timeout after which we stop monitoring the deployment
     */
    public URLDeployableMonitor(URL pingURL, long timeout)
    {
        super(timeout);
        this.pingURL = pingURL;
    }

    /**
     * @param pingURL the URL to be pinged and which will tell when the
     * {@link org.codehaus.cargo.container.deployable.Deployable} is deployed
     * @param timeout the timeout after which we stop monitoring the deployment
     * @param contains a string that must be contained
     */
    public URLDeployableMonitor(URL pingURL, long timeout, String contains)
    {
        super(timeout);
        this.pingURL = pingURL;
        this.contains = contains;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDeployableName()
    {
        return this.pingURL.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void monitor()
    {
        getLogger().debug("Checking URL [" + this.pingURL + "] for status using a timeout of ["
            + getTimeout() + "] ms...", this.getClass().getName());

        // We check if the deployable is servicing requests by pinging a URL specified by the user
        HttpUtils.HttpResult result = new HttpUtils.HttpResult();
        boolean isDeployed = this.httpUtils.ping(this.pingURL, result, getTimeout());
        if (isDeployed && this.contains != null)
        {
            if (result.responseBody != null)
            {
                isDeployed = result.responseBody.contains(this.contains);
            }
            else
            {
                isDeployed = false;
            }
        }

        String msg = "URL [" + this.pingURL + "] is ";
        if (isDeployed)
        {
            msg += "responding...";
        }
        else
        {
            msg += "not responding: " + result.responseCode + " " + result.responseMessage;
        }
        getLogger().debug(msg, this.getClass().getName());

        notifyListeners(isDeployed);
    }
}
