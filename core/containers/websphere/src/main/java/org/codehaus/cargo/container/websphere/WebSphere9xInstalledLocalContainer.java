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
package org.codehaus.cargo.container.websphere;

import org.codehaus.cargo.container.configuration.LocalConfiguration;

/**
 * WebSphere 9.x container implementation.
 */
public class WebSphere9xInstalledLocalContainer extends WebSphere85xInstalledLocalContainer
{
    /**
     * Unique container id.
     */
    public static final String ID = "websphere9x";

    /**
     * Container name (human-readable name).
     */
    private static final String NAME = "WebSphere 9.x";

    /**
     * {@inheritDoc}
     * @see WebSphere85xInstalledLocalContainer#WebSphere85xInstalledLocalContainer(org.codehaus.cargo.container.configuration.LocalConfiguration)
     */
    public WebSphere9xInstalledLocalContainer(LocalConfiguration configuration)
    {
        super(configuration);
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
    public String getName()
    {
        return NAME;
    }
}
