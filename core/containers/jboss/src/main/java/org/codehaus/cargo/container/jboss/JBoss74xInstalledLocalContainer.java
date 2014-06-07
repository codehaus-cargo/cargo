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
package org.codehaus.cargo.container.jboss;

import java.io.File;

import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.spi.jvm.JvmLauncher;

/**
 * JBoss 7.4.x series container implementation.
 * 
 * @version $Id$
 */
public class JBoss74xInstalledLocalContainer extends JBoss73xInstalledLocalContainer
{
    /**
     * JBoss 7.4.x series unique id.
     */
    public static final String ID = "jboss74x";

    /**
     * {@inheritDoc}
     * @see JBoss73xInstalledLocalContainer#JBoss73xInstalledLocalContainer(LocalConfiguration)
     */
    public JBoss74xInstalledLocalContainer(LocalConfiguration configuration)
    {
        super(configuration);
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.Container#getId()
     */
    @Override
    public String getId()
    {
        return ID;
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.Container#getName()
     */
    @Override
    public String getName()
    {
        return "JBoss " + getVersion("7.4.x");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doStart(JvmLauncher java) throws Exception
    {
        copyExtraClasspathJars();

        setProperties(java);
        java.addJvmArgumentLine("-D[Standalone]");

        java.setJarFile(new File(getHome(), "jboss-modules.jar"));

        String modules = getConfiguration().getPropertyValue(
            JBossPropertySet.ALTERNATIVE_MODULES_DIR);
        if (!new File(modules).isAbsolute())
        {
            modules = getFileHandler().append(getHome(), modules);
        }

        java.addAppArguments(
            "-mp", modules,
            "org.jboss.as.standalone",
            "--server-config="
                + getConfiguration().getPropertyValue(JBossPropertySet.CONFIGURATION) + ".xml");

        String runtimeArgs = getConfiguration().getPropertyValue(GeneralPropertySet.RUNTIME_ARGS);
        if (runtimeArgs != null)
        {
            // Replace new lines and tabs, so that Maven or ANT plugins can
            // specify multiline runtime arguments in their XML files
            runtimeArgs = runtimeArgs.replace('\n', ' ');
            runtimeArgs = runtimeArgs.replace('\r', ' ');
            runtimeArgs = runtimeArgs.replace('\t', ' ');
            java.addAppArgumentLine(runtimeArgs);
        }

        java.start();
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.jboss.JBoss7xInstalledLocalContainer#getConfigAdminDirectory()
     */
    @Override
    protected File getConfigAdminDirectory()
    {
        return new File(getHome(),
            "bundles/system/layers/base/org/jboss/as/osgi/configadmin/main");
    }
}
