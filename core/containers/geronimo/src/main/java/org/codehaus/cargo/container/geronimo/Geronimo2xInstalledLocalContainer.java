/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2010 Vincent Massol.
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
package org.codehaus.cargo.container.geronimo;

import java.io.File;
import java.util.Iterator;

import org.apache.tools.ant.taskdefs.Java;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.internal.AntContainerExecutorThread;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.RemotePropertySet;
import org.codehaus.cargo.container.property.ServletPropertySet;

/**
 * Geronimo 2.x series container implementation.
 *
 * @version $Id$
 */
public class Geronimo2xInstalledLocalContainer extends Geronimo1xInstalledLocalContainer
{
    /**
     * Geronimo 1.x series unique id.
     */
    public static final String ID = "geronimo2x";

    /**
     * {@inheritDoc}
     * @see Geronimo1xInstalledLocalContainer#Geronimo1xInstalledLocalContainer(org.codehaus.cargo.container.configuration.LocalConfiguration)
     */
    public Geronimo2xInstalledLocalContainer(LocalConfiguration configuration)
    {
        super(configuration);
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.Container#getId()
     */
    public String getId()
    {
        return ID;
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.Container#getName()
     */
    public String getName()
    {
        return "Geronimo " + getVersion("2.x");
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.spi.AbstractInstalledLocalContainer#doStop(org.apache.tools.ant.taskdefs.Java)
     */
    @Override
    protected void doStart(Java java) throws Exception
    {
        java.setJar(new File(getHome(), "bin/server.jar"));

        // -javaagent:$GERONIMO_HOME/bin/jpa.jar
        // -D=...:$JAVA_HOME/lib/endorsed
        // -Djava.ext.dirs=$GERONIMO_HOME/lib/ext:$JAVA_HOME/lib/ext

        java.addSysproperty(getAntUtils().createSysProperty("org.apache.geronimo.home.dir",
            getHome()));
        java.addSysproperty(getAntUtils().createSysProperty("org.apache.geronimo.server.dir",
            getConfiguration().getHome()));
        java.addSysproperty(getAntUtils().createSysProperty("java.endorsed.dirs",
            new File(getHome(), "lib/endorsed").getAbsolutePath().replace(File.separatorChar,
                '/')));
        java.addSysproperty(getAntUtils().createSysProperty("java.io.tmpdir",
            new File(getConfiguration().getHome(), "/var/temp").getAbsolutePath().replace(
                File.separatorChar, '/')));

        java.addSysproperty(getAntUtils().createSysProperty(
            "org.apache.geronimo.config.substitution.NamingPort",
            getConfiguration().getPropertyValue(GeneralPropertySet.RMI_PORT)));
        java.addSysproperty(getAntUtils().createSysProperty(
            "org.apache.geronimo.config.substitution.HTTPPort",
            getConfiguration().getPropertyValue(ServletPropertySet.PORT)));
        java.addSysproperty(getAntUtils().createSysProperty(
            "org.apache.geronimo.config.substitution.EndPointURI",
            "http://localhost:" + getConfiguration().getPropertyValue(ServletPropertySet.PORT)));

        // --long

        AntContainerExecutorThread geronimoStarter = new AntContainerExecutorThread(java);
        geronimoStarter.start();

        waitForCompletion(true);

        // deploy scheduled deployables
        GeronimoInstalledLocalDeployer deployer = new GeronimoInstalledLocalDeployer(this);
        for (Iterator iterator = this.getConfiguration().getDeployables().iterator(); iterator
            .hasNext();)
        {
            deployer.deploy((Deployable) iterator.next());
        }
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.spi.AbstractInstalledLocalContainer#doStop(org.apache.tools.ant.taskdefs.Java)
     */
    @Override
    protected void doStop(Java java) throws Exception
    {
        java.setJar(new File(getHome(), "bin/shutdown.jar"));

        java.addSysproperty(getAntUtils().createSysProperty("org.apache.geronimo.server.dir",
            getConfiguration().getHome()));
        java.addSysproperty(getAntUtils().createSysProperty("java.io.tmpdir",
            new File(getConfiguration().getHome(), "/var/temp").getPath()));

        java.createArg().setValue("--user");
        java.createArg().setValue(getConfiguration().getPropertyValue(RemotePropertySet.USERNAME));
        java.createArg().setValue("--password");
        java.createArg().setValue(getConfiguration().getPropertyValue(RemotePropertySet.PASSWORD));
        java.createArg().setValue("--port");
        java.createArg().setValue(getConfiguration().getPropertyValue(
            GeneralPropertySet.RMI_PORT));

        AntContainerExecutorThread geronimoStopper = new AntContainerExecutorThread(java);
        geronimoStopper.start();
    }

    /**
     * {@inheritDoc}
     * @see Geronimo1xInstalledLocalContainer#getVersion(String)
     */
    @Override
    protected String getVersion(String defaultVersion)
    {
        //TODO get actual version of installed Geronimo server
        return defaultVersion;
    }
}
