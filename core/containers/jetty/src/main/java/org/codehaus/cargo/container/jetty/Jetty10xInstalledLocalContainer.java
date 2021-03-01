/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2011 Vincent Massol, 2012-2021 Ali Tokmen.
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
        return new String[]
        {
            "--ini",
            "--module=console-capture",
            "--module=server",
            "--module=client",
            "--module=deploy",
            "--module=websocket",
            "--module=jsp",
            "--module=ext",
            "--module=resources",
            "--module=http",
            "--module=plus",
            "--module=annotations",
            "path=" + classpath
        };
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
}
