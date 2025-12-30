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

/**
 * Special container support for the Oracle WebLogic 12.2 application server. Contains WLST support.
 */
public class WebLogic122xInstalledLocalContainer extends WebLogic121xInstalledLocalContainer
{

    /**
     * Unique container id.
     */
    public static final String ID = "weblogic122x";

    /**
     * {@inheritDoc}
     * @see WebLogic121xInstalledLocalContainer#WebLogic121xInstalledLocalContainer(org.codehaus.cargo.container.configuration.LocalConfiguration)
     */
    public WebLogic122xInstalledLocalContainer(LocalConfiguration configuration)
    {
        super(configuration);
    }

    /**
     * {@inheritDoc}. The WLST JAR has changed between WebLogic 12.1 and WebLogic 12.2, see <a
     * href="https://codehaus-cargo.atlassian.net/browse/CARGO-1452" target="_blank">CARGO-1452</a>
     * for details.
     */
    @Override
    protected String getWsltClasspath()
    {
        return "modules/features/wlst.wls.classpath.jar";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName()
    {
        return "WebLogic " + getVersion("12.2.x");
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
