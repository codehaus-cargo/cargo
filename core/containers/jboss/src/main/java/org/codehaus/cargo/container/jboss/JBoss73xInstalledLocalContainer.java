/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2026 Ali Tokmen.
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

import org.codehaus.cargo.container.configuration.LocalConfiguration;

/**
 * JBoss 7.3.x (EAP 6.2.x) series container implementation.
 */
public class JBoss73xInstalledLocalContainer extends JBoss72xInstalledLocalContainer
{
    /**
     * JBoss 7.3.x (EAP 6.2.x) series unique id.
     */
    public static final String ID = "jboss73x";

    /**
     * {@inheritDoc}
     * @see JBoss72xInstalledLocalContainer#JBoss72xInstalledLocalContainer(LocalConfiguration)
     */
    public JBoss73xInstalledLocalContainer(LocalConfiguration configuration)
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
        return "JBoss " + getVersion("7.3.x") + " (EAP 6.2.x)";
    }
}
