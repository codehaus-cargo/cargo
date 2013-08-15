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
import org.codehaus.cargo.container.ContainerException;

import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.spi.jvm.JvmLauncher;

/**
 * JBoss 7.1.x series container implementation.
 * 
 * @version $Id$
 */
public class JBoss71xInstalledLocalContainer extends JBoss7xInstalledLocalContainer
{
    /**
     * JBoss 7.1.x series unique id.
     */
    public static final String ID = "jboss71x";

    /**
     * Since JBoss 7.1.x is not always very stable when stopping, try twice.
     */
    private boolean firstAttemptStopping = true;

    /**
     * {@inheritDoc}
     * @see JBoss7xInstalledLocalContainer#JBoss7xInstalledLocalContainer(LocalConfiguration)
     */
    public JBoss71xInstalledLocalContainer(LocalConfiguration configuration)
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
        return "JBoss " + getVersion("7.1.x");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doStart(JvmLauncher java) throws Exception
    {
        setProperties(java);

        java.setJarFile(new File(getHome(), "jboss-modules.jar"));

        java.addAppArguments(
            "-mp", getHome() + "/modules",
            "-jaxpmodule", "javax.xml.jaxp-provider",
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
     */
    @Override
    protected void doStop(JvmLauncher java) throws Exception
    {
        String port =
            getConfiguration().getPropertyValue(JBossPropertySet.JBOSS_MANAGEMENT_NATIVE_PORT);

        java.setJarFile(new File(getHome(), "jboss-modules.jar"));

        java.addAppArguments(
            "-mp", getHome() + "/modules",
            "org.jboss.as.cli",
            "--connect", "--controller=localhost:" + port,
            "command=:shutdown");

        java.start();
    }

    /**
     * {@inheritDoc}. Since JBoss 7.1.x is not always very stable when stopping, try twice.
     */
    protected void waitForCompletion(boolean waitForStarting) throws InterruptedException
    {
        if (!waitForStarting)
        {
            try
            {
                super.waitForCompletion(waitForStarting);
            }
            catch (ContainerException e)
            {
                if (this.firstAttemptStopping)
                {
                    // Since JBoss 7.1.x is not always very stable when stopping, try twice.
                    getLogger().debug("First attempt to stop the JBoss server has failed (" + e
                        + "), trying one last time", this.getClass().getName());
                    this.firstAttemptStopping = false;
                    this.stop();
                }
                else
                {
                    throw e;
                }
            }
        }
        else
        {
            super.waitForCompletion(waitForStarting);
        }
    }
}
