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
package org.codehaus.cargo.container.jetty;

import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.spi.jvm.JvmLauncher;
import org.codehaus.cargo.util.CargoException;

/**
 * Special container support for the Jetty 10.x servlet container.
 */
public class Jetty10xInstalledLocalContainer extends Jetty9xInstalledLocalContainer
{
    /**
     * Unique container id.
     */
    public static final String ID = "jetty10x";

    /**
     * Default list of Jetty modules to activate.
     * @see JettyPropertySet#MODULES
     */
    public static final String DEFAULT_MODULES = "server,ext,http,annotations,plus,jsp,deploy";

    /**
     * Jetty10xInstalledLocalContainer Constructor.
     * @param configuration The configuration associated with the container
     */
    public Jetty10xInstalledLocalContainer(LocalConfiguration configuration)
    {
        super(configuration);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getJettyPortPropertyName()
    {
        return "jetty.http.port";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getId()
    {
        return ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String[] getStartArguments(String classpath)
    {
        String configuredModules = getConfiguration().getPropertyValue(JettyPropertySet.MODULES);
        if (configuredModules == null || configuredModules.trim().length() == 0)
        {
            throw new CargoException(
                "Configuration property [" + JettyPropertySet.MODULES + "] not set");
        }
        String[] modules = configuredModules.split(",");
        String[] startArguments = new String[modules.length + (classpath == null ? 0 : 1)];
        boolean httpsModule = false;
        for (int i = 0; i < modules.length; i++)
        {
            startArguments[i] = "--module=" + modules[i];
            if ("https".equals(modules[i]))
            {
                httpsModule = true;
            }
        }
        boolean keystoreFile = getConfiguration().getPropertyValue(
            JettyPropertySet.CONNECTOR_KEY_STORE_FILE) != null;
        if ((httpsModule || keystoreFile) && !(httpsModule && keystoreFile))
        {
            throw new CargoException("To enable HTTPS, you need to BOTH add the https module and "
                + "provide the configuration value " + JettyPropertySet.CONNECTOR_KEY_STORE_FILE);
        }
        if (classpath != null)
        {
            startArguments[startArguments.length - 1] = "path=" + classpath;
        }
        return startArguments;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String[] getStopArguments()
    {
        return new String[]
        {
            "STOP.PORT=" + getConfiguration().getPropertyValue(GeneralPropertySet.RMI_PORT),
            "STOP.KEY=secret"
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void invoke(JvmLauncher java, boolean isGettingStarted) throws Exception
    {
        String keystoreFile =
            getConfiguration().getPropertyValue(JettyPropertySet.CONNECTOR_KEY_STORE_FILE);
        if (keystoreFile != null)
        {
            java.setSystemProperty("jetty.ssl.port",
                getConfiguration().getPropertyValue(JettyPropertySet.CONNECTOR_HTTPS_PORT));
            java.setSystemProperty("jetty.sslContext.keyStorePath", keystoreFile);
            String keystorePassword =
                getConfiguration().getPropertyValue(JettyPropertySet.CONNECTOR_KEY_STORE_PASSWORD);
            if (keystorePassword != null)
            {
                java.setSystemProperty("jetty.sslContext.keyStorePassword", keystorePassword);
            }
            String keystoreType =
                getConfiguration().getPropertyValue(JettyPropertySet.CONNECTOR_KEY_STORE_TYPE);
            if (keystoreType != null)
            {
                java.setSystemProperty("jetty.sslContext.keyStoreType", keystoreType);
            }
        }

        super.invoke(java, isGettingStarted);
    }
}
