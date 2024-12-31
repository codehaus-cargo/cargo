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

import java.util.ArrayList;
import java.util.List;

import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.spi.jvm.JvmLauncher;
import org.codehaus.cargo.container.weblogic.internal.AbstractWebLogicInstalledLocalContainer;

/**
 * Special container support for the Bea WebLogic 12.x application server.
 */
public class WebLogic12xInstalledLocalContainer extends AbstractWebLogicInstalledLocalContainer
{
    /**
     * Unique container id.
     */
    public static final String ID = "weblogic12x";

    /**
     * {@inheritDoc}
     * @see AbstractWebLogicInstalledLocalContainer#AbstractWebLogicInstalledLocalContainer(org.codehaus.cargo.container.configuration.LocalConfiguration)
     */
    public WebLogic12xInstalledLocalContainer(LocalConfiguration configuration)
    {
        super(configuration);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName()
    {
        return "WebLogic " + getVersion("12.x");
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

    /**
     * {@inheritDoc} Also includes checking of the modules directory, which is unique to WebLogic
     * 10.
     */
    @Override
    protected List<String> getBeaHomeDirs()
    {
        List<String> beaHomeDirs = super.getBeaHomeDirs();
        beaHomeDirs.add(getFileHandler().append(getWeblogicHome(), "modules"));
        return beaHomeDirs;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<String> getBeaHomeFiles()
    {
        List<String> requiredFiles = new ArrayList<String>();
        requiredFiles.add(getFileHandler().append(getBeaHome(), "inventory/registry.xml"));
        return requiredFiles;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void addMemoryArguments(JvmLauncher java)
    {
        // if the jvmArgs don't alread contain memory settings add the default
        String jvmArgs = getConfiguration().getPropertyValue(GeneralPropertySet.JVMARGS);
        String startJvmargs =
            getConfiguration().getPropertyValue(GeneralPropertySet.START_JVMARGS);
        if (startJvmargs != null)
        {
            if (jvmArgs == null)
            {
                jvmArgs = startJvmargs;
            }
            else
            {
                jvmArgs += " " + startJvmargs;
            }
        }
        if (jvmArgs == null || !jvmArgs.contains("-XX:PermSize"))
        {
            java.addJvmArguments("-XX:PermSize=128m");
        }
        if (jvmArgs == null || !jvmArgs.contains("-XX:MaxPermSize"))
        {
            java.addJvmArguments("-XX:MaxPermSize=256m");
        }

        super.addMemoryArguments(java);
    }
}
