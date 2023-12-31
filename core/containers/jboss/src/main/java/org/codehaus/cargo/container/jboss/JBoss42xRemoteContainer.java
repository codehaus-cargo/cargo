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
package org.codehaus.cargo.container.jboss;

import org.codehaus.cargo.container.ContainerCapability;
import org.codehaus.cargo.container.configuration.RuntimeConfiguration;
import org.codehaus.cargo.container.jboss.internal.JBoss4xContainerCapability;

/**
 * Special container support for wrapping a running instance of JBoss 4.2.x.
 */
public class JBoss42xRemoteContainer extends JBoss4xRemoteContainer
{
    /**
     * Unique container id.
     */
    public static final String ID = "jboss42x";

    /**
     * Capability of the JBoss container.
     */
    private static final ContainerCapability CAPABILITY = new JBoss4xContainerCapability();

    /**
     * {@inheritDoc}
     * @see JBoss4xRemoteContainer#JBoss4xRemoteContainer(org.codehaus.cargo.container.configuration.RuntimeConfiguration)
     */
    public JBoss42xRemoteContainer(RuntimeConfiguration configuration)
    {
        super(configuration);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName()
    {
        return "JBoss 4.2.x Remote";
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
    public ContainerCapability getCapability()
    {
        return CAPABILITY;
    }
}
