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
package org.codehaus.cargo.container.tomcat;

import org.codehaus.cargo.container.configuration.RuntimeConfiguration;

/**
 * Special container support for wrapping a running instance of Apache Tomcat.
 */
public class Tomcat9xRemoteContainer extends Tomcat8xRemoteContainer
{
    /**
     * Unique container id.
     */
    public static final String ID = "tomcat9x";

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.tomcat.Tomcat8xRemoteContainer#Tomcat8xRemoteContainer(RuntimeConfiguration)
     */
    public Tomcat9xRemoteContainer(RuntimeConfiguration configuration)
    {
        super(configuration);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName()
    {
        return "Tomcat 9.x Remote";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getId()
    {
        return ID;
    }
}
