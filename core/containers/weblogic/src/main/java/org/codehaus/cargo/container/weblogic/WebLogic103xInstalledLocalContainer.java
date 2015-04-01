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
package org.codehaus.cargo.container.weblogic;

import java.util.List;

import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.weblogic.internal.AbstractWebLogicInstalledLocalContainer;

/**
 * Special container support for the Bea WebLogic 10.3.x application server.
 * 
 */
public class WebLogic103xInstalledLocalContainer extends AbstractWebLogicInstalledLocalContainer
{
    /**
     * Unique container id.
     */
    public static final String ID = "weblogic103x";

    /**
     * {@inheritDoc}
     * 
     * @see AbstractWebLogicInstalledLocalContainer#AbstractWebLogicInstalledLocalContainer(org.codehaus.cargo.container.configuration.LocalConfiguration)
     */
    public WebLogic103xInstalledLocalContainer(LocalConfiguration configuration)
    {
        super(configuration);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.codehaus.cargo.container.Container#getName()
     */
    public String getName()
    {
        return "WebLogic 10.3.x";
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.codehaus.cargo.container.Container#getId()
     */
    public String getId()
    {
        return ID;
    }

    /**
     * {@inheritDoc}
     */
    public String getAutoDeployDirectory()
    {
        return "autodeploy";
    }

    /**
     * {@inheritDoc} Also includes checking of the modules directory, which is unique to WebLogic
     * 10.
     * 
     * @see org.codehaus.cargo.container.weblogic.internal.AbstractWebLogicInstalledLocalContainer#getBeaHomeDirs()
     */
    @Override
    protected List<String> getBeaHomeDirs()
    {
        List<String> beaHomeDirs = super.getBeaHomeDirs();
        beaHomeDirs.add(getFileHandler().append(getBeaHome(), "modules"));
        return beaHomeDirs;
    }
}
