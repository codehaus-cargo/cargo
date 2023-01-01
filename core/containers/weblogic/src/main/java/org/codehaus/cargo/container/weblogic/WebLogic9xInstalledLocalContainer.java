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
package org.codehaus.cargo.container.weblogic;

import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.weblogic.internal.AbstractWebLogicInstalledLocalContainer;

/**
 * Special container support for the Bea WebLogic 9.x application server. Author: 20060918 - Martin
 * Zeltner (MZE)
 */
public class WebLogic9xInstalledLocalContainer
    extends AbstractWebLogicInstalledLocalContainer
{
    /**
     * Unique container id.
     */
    public static final String ID = "weblogic9x";

    /**
     * {@inheritDoc}
     * @see AbstractWebLogicInstalledLocalContainer#AbstractWebLogicInstalledLocalContainer(org.codehaus.cargo.container.configuration.LocalConfiguration)
     */
    public WebLogic9xInstalledLocalContainer(LocalConfiguration configuration)
    {
        super(configuration);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName()
    {
        return "WebLogic " + getVersion("9.x");
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
    public String getAutoDeployDirectory()
    {
        return "autodeploy";
    }
}
