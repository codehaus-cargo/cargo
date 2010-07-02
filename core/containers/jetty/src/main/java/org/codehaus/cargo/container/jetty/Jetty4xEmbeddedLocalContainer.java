/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2010 Vincent Massol.
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
import org.codehaus.cargo.container.jetty.internal.AbstractJetty4x5xEmbeddedLocalContainer;

/**
 * Special container support for the Jetty 4.x servlet container, using Jetty in embedded mode.
 * 
 * @version $Id$
 */
public class Jetty4xEmbeddedLocalContainer extends AbstractJetty4x5xEmbeddedLocalContainer
{
    /**
     * Unique container id.
     */
    public static final String ID = "jetty4x";

    /**
     * {@inheritDoc}
     * @see AbstractJetty4x5xEmbeddedLocalContainer#AbstractJetty4x5xEmbeddedLocalContainer(LocalConfiguration)
     */
    public Jetty4xEmbeddedLocalContainer(LocalConfiguration configuration)
    {
        super(configuration);
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.Container#getId()
     */
    public final String getId()
    {
        return ID;
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.Container#getName()
     */
    public String getName()
    {
        return "Jetty 4.x Embedded";
    }

    /**
     * {@inheritDoc}
     * @see AbstractJetty4x5xEmbeddedLocalContainer#performExtraSetupOnDeployable(Object)
     */
    @Override
    protected void performExtraSetupOnDeployable(Object webapp)
    {
        // Nothing to do for Jetty 4.x
    }
}
