/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2025 Ali Tokmen.
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
 * A mostly canned configuration for an embedded Jetty 10.x instance.
 */
public class Jetty10xEmbeddedStandaloneLocalConfiguration extends
    Jetty9xEmbeddedStandaloneLocalConfiguration
{
    /**
     * {@inheritDoc}
     * @see Jetty9xEmbeddedStandaloneLocalConfiguration#Jetty9xEmbeddedStandaloneLocalConfiguration(String)
     */
    public Jetty10xEmbeddedStandaloneLocalConfiguration(String dir)
    {
        super(dir);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void activateLogging(LocalContainer container)
    {
        getLogger().info("Jetty 10.x log configuration not implemented", this.getClass().getName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return "Jetty 10.x Embedded Standalone Configuration";
    }
}
