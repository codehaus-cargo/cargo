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
package org.codehaus.cargo.container.tomcat.internal;

import org.codehaus.cargo.container.ContainerCapability;
import org.codehaus.cargo.container.configuration.RuntimeConfiguration;
import org.codehaus.cargo.container.internal.ServletContainerCapability;
import org.codehaus.cargo.container.spi.AbstractRemoteContainer;

/**
 * Special container support for wrapping a running instance of Apache Tomcat.
 * 
 * @version $Id$
 */
public abstract class AbstractTomcatRemoteContainer extends AbstractRemoteContainer
{
    /**
     * Capability of the Tomcat/Catalina container.
     */
    private ContainerCapability capability = new ServletContainerCapability();

    /**
     * {@inheritDoc}
     * @see AbstractRemoteContainer#AbstractRemoteContainer(org.codehaus.cargo.container.configuration.RuntimeConfiguration)
     */
    public AbstractTomcatRemoteContainer(RuntimeConfiguration configuration)
    {
        super(configuration);
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.Container#getCapability()
     */
    public ContainerCapability getCapability()
    {
        return this.capability;
    }
}
