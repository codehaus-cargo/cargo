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
package org.codehaus.cargo.container.weblogic;

import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.spi.jvm.JvmLauncher;

/**
 * Special container support for the Oracle WebLogic 15 application server. Contains WLST support.
 */
public class WebLogic15xInstalledLocalContainer extends WebLogic14xInstalledLocalContainer
{

    /**
     * Unique container id.
     */
    public static final String ID = "weblogic15x";

    /**
     * {@inheritDoc}
     * @see WebLogic14xInstalledLocalContainer#WebLogic14xInstalledLocalContainer(org.codehaus.cargo.container.configuration.LocalConfiguration)
     */
    public WebLogic15xInstalledLocalContainer(LocalConfiguration configuration)
    {
        super(configuration);
    }

    /**
     * {@inheritDoc}. The override includes the fix for <a
     * href="https://codehaus-cargo.atlassian.net/browse/CARGO-1653"
     * target="_blank">CARGO-1653</a>.
     */
    @Override
    protected void addWlstArguments(JvmLauncher java)
    {
        super.addWlstArguments(java);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName()
    {
        return "WebLogic " + getVersion("15.x");
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
