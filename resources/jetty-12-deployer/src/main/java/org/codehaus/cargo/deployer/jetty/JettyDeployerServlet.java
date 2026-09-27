/*
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2026 Ali Tokmen.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.cargo.deployer.jetty;

import java.io.File;

/**
 * Contains common servlet-independent initialization for the Cargo Jetty
 * deployer servlets.<br>
 * <br>
 * This class contains no dependency on either the
 * {@code javax.servlet} or {@code jakarta.servlet} APIs.
 */
public abstract class JettyDeployerServlet
{
    /**
     * Name of the deployment timeout parameter.
     */
    private static final String TIMEOUT_PARAMETER = "timeout";

    /**
     * System property overriding the deployment timeout.
     */
    private static final String TIMEOUT_PROPERTY =
        "cargo.jetty.deployer.timeout";

    /**
     * Common deployment logic.
     */
    private JettyDeployer deployer;

    /**
     * Initializes the common deployer.
     *
     * @param servletTimeout servlet initialization parameter
     * @param contextTimeout servlet context initialization parameter
     * @throws Exception if initialization fails
     */
    protected void initialize(
        String servletTimeout, String contextTimeout)
        throws Exception
    {
        String configHome = getConfigHome();

        if (configHome == null)
        {
            throw new IllegalStateException(
                "Cannot find the Jetty configuration home");
        }

        String timeoutValue =
            System.getProperty(TIMEOUT_PROPERTY);

        if (timeoutValue == null)
        {
            timeoutValue = servletTimeout;
        }

        if (timeoutValue == null)
        {
            timeoutValue = contextTimeout;
        }

        if (timeoutValue == null)
        {
            throw new IllegalStateException(
                "Cannot find the [" + TIMEOUT_PARAMETER
                    + "] servlet parameter");
        }

        long timeout;

        try
        {
            timeout = Long.parseLong(timeoutValue);
        }
        catch (NumberFormatException e)
        {
            throw new IllegalStateException(
                "Cannot parse the [" + TIMEOUT_PARAMETER
                    + "] servlet parameter: " + timeoutValue, e);
        }

        if (timeout < 1)
        {
            throw new IllegalStateException(
                "Timeout is smaller than 1: " + timeout);
        }

        this.deployer = new JettyDeployer(
            createContextAdapter(),
            new File(configHome, "webapps"),
            timeout);
    }

    /**
     * Returns the common deployer.
     *
     * @return common deployer
     */
    protected JettyDeployer getDeployer()
    {
        return this.deployer;
    }

    /**
     * Creates the Jetty context adapter.
     *
     * @return Jetty context adapter
     */
    protected abstract JettyDeployer.Context createContextAdapter();

    /**
     * Finds the Jetty configuration directory.
     *
     * @return configuration directory, or {@code null}
     */
    private String getConfigHome()
    {
        String configHome = System.getProperty("config.home");

        if (configHome == null)
        {
            configHome = System.getProperty("jetty.base");
        }

        if (configHome == null)
        {
            configHome = System.getProperty("jetty.home");
        }

        return configHome;
    }
}
