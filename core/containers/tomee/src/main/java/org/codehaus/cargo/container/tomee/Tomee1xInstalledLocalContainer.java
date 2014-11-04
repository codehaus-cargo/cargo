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
package org.codehaus.cargo.container.tomee;

import org.codehaus.cargo.container.ContainerCapability;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.tomcat.Tomcat7xInstalledLocalContainer;

/**
 * Special container support for the Apache TomEE 1.x servlet container.
 * 
 * @version $Id$
 */
public class Tomee1xInstalledLocalContainer extends Tomcat7xInstalledLocalContainer
{
    /**
     * Unique container id.
     */
    public static final String ID = "tomee1x";

    /**
     * Capability of the TomEE container.
     */
    private ContainerCapability capability = new Tomee1xContainerCapability();

    /**
     * {@inheritDoc}
     * 
     * @see Tomcat7xInstalledLocalContainer#Tomcat7xInstalledLocalContainer(org.codehaus.cargo.container.configuration.LocalConfiguration)
     */
    public Tomee1xInstalledLocalContainer(LocalConfiguration configuration)
    {
        super(configuration);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.codehaus.cargo.container.Container#getId()
     */
    @Override
    public String getId()
    {
        return ID;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.codehaus.cargo.container.Container#getName()
     */
    @Override
    public String getName()
    {
        return "TomEE " + getVersion("1.x");
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.codehaus.cargo.container.Container#getCapability()
     */
    @Override
    public ContainerCapability getCapability()
    {
        return this.capability;
    }
}
