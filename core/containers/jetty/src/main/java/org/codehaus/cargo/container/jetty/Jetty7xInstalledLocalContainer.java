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
package org.codehaus.cargo.container.jetty;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.LoggingLevel;
import org.codehaus.cargo.container.spi.jvm.JvmLauncher;

/**
 * Special container support for the Jetty 7.x servlet container.
 */
public class Jetty7xInstalledLocalContainer extends Jetty6xInstalledLocalContainer
{
    /**
     * Unique container id.
     */
    public static final String ID = "jetty7x";

    /**
     * Jetty default options.
     */
    protected String defaultFinalOptions;

    /**
     * Jetty options.
     */
    private String options;

    /**
     * Jetty7xInstalledLocalContainer Constructor.
     * @param configuration The configuration associated with the container
     */
    public Jetty7xInstalledLocalContainer(LocalConfiguration configuration)
    {
        super(configuration);
        this.defaultFinalOptions = "jmx,resources,websocket,ext,plus";
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
    protected void invoke(JvmLauncher java, boolean isGettingStarted) throws Exception
    {
        if (getConfiguration().getPropertyValue(GeneralPropertySet.RUNTIME_ARGS) == null
                || !getConfiguration().getPropertyValue(
                    GeneralPropertySet.RUNTIME_ARGS).contains("--ini="))
        {
            // If logging is set to "high" the turn it on by setting the DEBUG system property
            if (LoggingLevel.HIGH.equalsLevel(getConfiguration().getPropertyValue(
                GeneralPropertySet.LOGGING)))
            {
                java.setSystemProperty("org.eclipse.jetty.DEBUG", "true");
            }
        }

        super.invoke(java, isGettingStarted);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String[] getStartArguments(String classpath)
    {
        String etc = getFileHandler().append(getConfiguration().getHome(), "etc");

        List<String> arguments = new ArrayList<String>();
        arguments.add(getOptions());
        arguments.add("--ini");
        arguments.add(getFileHandler().append(etc, "jetty-logging.xml"));
        arguments.add(getFileHandler().append(etc, "jetty.xml"));
        // Jetty 7.2.x seperated jetty-deploy, jetty-contexts and jetty-webapps,
        // before there was only jetty-deploy
        arguments.add(getFileHandler().append(etc, "jetty-deploy.xml"));
        if (getFileHandler().exists(getFileHandler().append(etc, "jetty-webapps.xml")))
        {
            arguments.add(getFileHandler().append(etc, "jetty-webapps.xml"));
        }
        if (getFileHandler().exists(getFileHandler().append(etc, "jetty-contexts.xml")))
        {
            arguments.add(getFileHandler().append(etc, "jetty-contexts.xml"));
        }
        arguments.add("--pre=" + getFileHandler().append(etc, "jetty-testrealm.xml"));
        arguments.add("path=" + classpath);

        String[] result = new String[arguments.size()];
        return arguments.toArray(result);
    }

    /**
     * @return Jetty <code>OPTIONS</code> argument.
     */
    protected synchronized String getOptions()
    {
        if (this.options == null)
        {
            StringBuilder options = new StringBuilder("OPTIONS=Server");

            File jspLib = new File(getHome(), "lib/jsp");
            if (jspLib.isDirectory())
            {
                options.append(",jsp");
            }
            else
            {
                getLogger().warn("JSP librairies not found in " + jspLib
                    + ", JSP support will be disabled", this.getClass().getName());
            }

            options.append("," + this.defaultFinalOptions);
            this.options = options.toString();
        }

        return this.options;
    }
}
