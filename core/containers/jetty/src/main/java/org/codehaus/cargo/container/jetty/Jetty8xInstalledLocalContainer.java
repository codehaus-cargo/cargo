/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2011 Vincent Massol.
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

import org.codehaus.cargo.container.configuration.LocalConfiguration;

/**
 * Special container support for the Jetty 8.x servlet container.
 * 
 * @version $Id$
 */
public class Jetty8xInstalledLocalContainer extends Jetty7xInstalledLocalContainer
{
    /**
     * Unique container id.
     */
    public static final String ID = "jetty8x";

    /**
     * Jetty8xInstalledLocalContainer Constructor.
     * @param configuration The configuration associated with the container
     */
    public Jetty8xInstalledLocalContainer(LocalConfiguration configuration)
    {
        super(configuration);
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.Container#getId()
     */
    public String getId()
    {
        return ID;
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.Container#getName()
     */
    public String getName()
    {
        return "Jetty 8.x";
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.jetty.Jetty7xInstalledLocalContainer#getStartArguments()
     */
    @Override
    protected String[] getStartArguments()
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

        options.append(",jmx,resources,websocket,ext,plus,annotations");

        return new String[]
        {
            options.toString(),
            "--ini",
            "--pre=" + getFileHandler().append(getConfiguration().getHome(),
                "etc/jetty-logging.xml"),
            "--pre=" + getFileHandler().append(getConfiguration().getHome(),
                "etc/jetty.xml"),
            "--pre=" + getFileHandler().append(getConfiguration().getHome(),
                "etc/jetty-annotations.xml"),
            "--pre=" + getFileHandler().append(getConfiguration().getHome(),
                "etc/jetty-plus.xml"),
            "--pre=" + getFileHandler().append(getConfiguration().getHome(),
                "etc/jetty-deploy.xml"),
            "--pre=" + getFileHandler().append(getConfiguration().getHome(),
                "etc/jetty-webapps.xml"),
            "--pre=" + getFileHandler().append(getConfiguration().getHome(),
                "etc/jetty-contexts.xml"),
            "--pre=" + getFileHandler().append(getConfiguration().getHome(),
                "etc/jetty-testrealm.xml")
        };
    }
}
