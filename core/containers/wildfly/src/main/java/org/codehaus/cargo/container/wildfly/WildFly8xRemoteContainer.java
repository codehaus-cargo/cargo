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
package org.codehaus.cargo.container.wildfly;

import org.codehaus.cargo.container.configuration.RuntimeConfiguration;
import org.codehaus.cargo.container.jboss.JBoss71xRemoteContainer;

/**
 * Special container support for wrapping a running instance of WildFly 8.x.
 * 
 * @version $Id$
 */
public class WildFly8xRemoteContainer extends JBoss71xRemoteContainer
{
    /**
     * Unique container id.
     */
    private static final String ID = "wildfly8x";

    /**
     * {@inheritDoc}
     * @see JBoss71xRemoteContainer#JBoss71xRemoteContainer(org.codehaus.cargo.container.configuration.RuntimeConfiguration)
     */
    public WildFly8xRemoteContainer(RuntimeConfiguration configuration)
    {
        super(configuration);
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.Container#getName()
     */
    @Override
    public String getName()
    {
        return "WildFly 8.x Remote";
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
}
