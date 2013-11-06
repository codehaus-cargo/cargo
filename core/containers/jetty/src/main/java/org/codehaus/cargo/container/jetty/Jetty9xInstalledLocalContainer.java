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
import org.codehaus.cargo.container.property.GeneralPropertySet;

/**
 * Special container support for the Jetty 9.x servlet container.
 * 
 * @version $Id$
 */
public class Jetty9xInstalledLocalContainer extends Jetty8xInstalledLocalContainer
{
    /**
     * Unique container id.
     */
    public static final String ID = "jetty9x";

    /**
     * Jetty9xInstalledLocalContainer Constructor.
     * @param configuration The configuration associated with the container
     */
    public Jetty9xInstalledLocalContainer(LocalConfiguration configuration)
    {
        super(configuration);
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.Container#getId()
     */
    @Override
    public String getId()
    {
        return ID;
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.Container#getName()
     */
    @Override
    public String getName()
    {
        return "Jetty 9.x";
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.jetty.Jetty7xInstalledLocalContainer#getStartArguments()
     */
    @Override
    protected String[] getStartArguments()
    {
        return new String[]
        {
            "--ini",
            "--module=logging",
            "--module=server",
            "--module=deploy",
            "--module=websocket",
            "--module=jsp",
            "--module=ext",
            "--module=resources",
            "--module=http",
            "--module=plus"
        };
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.jetty.Jetty7xInstalledLocalContainer#getStartArguments()
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
