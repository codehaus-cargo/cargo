/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2023 Ali Tokmen.
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
package org.codehaus.cargo.container.tomcat;

import org.codehaus.cargo.container.configuration.LocalConfiguration;

/**
 * Embedded Tomcat 6.x container.
 */
public class Tomcat6xEmbeddedLocalContainer extends Tomcat5xEmbeddedLocalContainer
{
    /**
     * Creates a Tomcat 6.x {@link org.codehaus.cargo.container.EmbeddedLocalContainer}.
     * 
     * @param configuration the configuration of the newly created container.
     */
    public Tomcat6xEmbeddedLocalContainer(LocalConfiguration configuration)
    {
        super(configuration);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getId()
    {
        return "tomcat6x";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName()
    {
        return "Tomcat 6.x Embedded";
    }
}
