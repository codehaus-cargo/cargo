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
package org.codehaus.cargo.container.wildfly;


import org.codehaus.cargo.container.configuration.LocalConfiguration;

/**
 * WildFly 11.x series container implementation.
 */
public class WildFly11xInstalledLocalContainer extends WildFly10xInstalledLocalContainer
{
    /**
     * WildFly 11.x series unique id.
     */
    public static final String ID = "wildfly11x";

    /**
     * {@inheritDoc}
     * @see WildFly10xInstalledLocalContainer#WildFly10xInstalledLocalContainer(LocalConfiguration)
     */
    public WildFly11xInstalledLocalContainer(LocalConfiguration configuration)
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
    protected String getDefaultName()
    {
        return "WildFly 11.x (JBoss EAP 7.1)";
    }
}
