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

import org.codehaus.cargo.container.LocalContainer;

/**
 * A mostly canned configuration for an embedded Jetty 9.x instance.
 * 
 * @version $Id$
 */
public class Jetty9xEmbeddedStandaloneLocalConfiguration extends
    Jetty8xEmbeddedStandaloneLocalConfiguration
{
    /**
     * {@inheritDoc}
     * @see Jetty8xEmbeddedStandaloneLocalConfiguration#Jetty8xEmbeddedStandaloneLocalConfiguration(String)
     */
    public Jetty9xEmbeddedStandaloneLocalConfiguration(String dir)
    {
        super(dir);
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.jetty.internal.AbstractJettyStandaloneLocalConfiguration#activateLogging(org.codehaus.cargo.container.LocalContainer)
     */
    @Override
    protected void activateLogging(LocalContainer container)
    {
        getLogger().info("Jetty9x log configuration not implemented", this.getClass().getName());
    }

    /**
     * {@inheritDoc}
     * @see Object#toString()
     */
    @Override
    public String toString()
    {
        return "Jetty 9.x Embedded Standalone Configuration";
    }
}
