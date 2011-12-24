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

        this.defaultFinalOptions = "jmx,resources,websocket,ext,plus,annotations";
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
     * @return Arguments to add to the Jetty <code>start.jar</code> command.
     */
    @Override
    protected String[] getStartArguments()
    {
        return new String[]
        {
            "--pre",
            getFileHandler().append(getConfiguration().getHome(), "etc/jetty-logging.xml"),

            getFileHandler().append(getConfiguration().getHome(), "etc/jetty.xml"),
            getFileHandler().append(getConfiguration().getHome(), "etc/jetty-annotations.xml"),
            getFileHandler().append(getConfiguration().getHome(), "etc/jetty-deploy.xml"),
            getFileHandler().append(getConfiguration().getHome(), "etc/jetty-webapps.xml"),
            getFileHandler().append(getConfiguration().getHome(), "etc/jetty-contexts.xml"),
            getFileHandler().append(getConfiguration().getHome(), "etc/jetty-testrealm.xml")
        };
    }
}
