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
package org.codehaus.cargo.container.tomcat.internal;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.cargo.container.ContainerCapability;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.deployer.Deployer;
import org.codehaus.cargo.container.internal.ServletContainerCapability;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.container.spi.AbstractEmbeddedLocalContainer;
import org.codehaus.cargo.container.tomcat.TomcatEmbeddedLocalDeployer;
import org.codehaus.cargo.container.tomcat.TomcatPropertySet;
import org.codehaus.cargo.container.tomcat.internal.TomcatEmbedded.Embedded;
import org.codehaus.cargo.container.tomcat.internal.TomcatEmbedded.MemoryRealm;

/**
 * Base support for Catalina based embedded local containers.
 * 
 * @version $Id$
 */
public abstract class AbstractCatalinaEmbeddedLocalContainer extends AbstractEmbeddedLocalContainer
{
    /**
     * Capability of the Tomcat/Catalina container.
     */
    private ContainerCapability capability = new ServletContainerCapability();

    /**
     * Root of the Tomcat object model.
     */
    private TomcatEmbedded.Embedded controller;

    /**
     * Tomcat host object.
     */
    private TomcatEmbedded.Host host;

    /**
     * {@link Deployable}s to be deployed once the container is started.
     * 
     * One can only deploy to an embedded container after it's started, but cargo allows you to
     * deploy apps before the container starts. so we need to remember what's supposed to be
     * deployed.
     */
    private final List<Deployable> scheduledDeployables = new ArrayList<Deployable>();

    /**
     * Creates a Tomcat {@link org.codehaus.cargo.container.EmbeddedLocalContainer}.
     * 
     * @param configuration the configuration of the newly created container.
     */
    public AbstractCatalinaEmbeddedLocalContainer(LocalConfiguration configuration)
    {
        super(configuration);
    }

    /**
     * @return the Tomcat controller object. Always non-null.
     */
    public TomcatEmbedded.Embedded getController()
    {
        return controller;
    }

    /**
     * @return the Tomcat host object. Always non-null.
     */
    public TomcatEmbedded.Host getHost()
    {
        return host;
    }

    /**
     * {@inheritDoc}
     * @see AbstractEmbeddedLocalContainer#doStart()
     */
    @Override
    protected void doStart() throws Exception
    {
        TomcatEmbedded wrapper = new TomcatEmbedded(getClassLoader());

        controller = wrapper.new Embedded();

        // Tomcat always try to resolve relative path against CATALINA_BASE, so make it absolute
        // here.
        File home = new File(getConfiguration().getHome()).getAbsoluteFile();

        // NOTE: the following sets the system properties inside Tomcat (!),
        // which means you can't run two Tomcat instances inside the same VM.
        controller.setCatalinaBase(home);
        controller.setCatalinaHome(new File(getConfiguration().getHome()));

        // unless we set this explicitly, it will be loaded from CATALINA_BASE
        TomcatEmbedded.MemoryRealm realm = wrapper.new MemoryRealm();
        realm.setPathname(new File(home, "conf/tomcat-users.xml"));
        controller.setRealm(realm);

        // no easy way to do this with reflection
        // controller.setLogger(new LoggerAdapter(getLogger()));

        TomcatEmbedded.Engine engine = controller.createEngine();
        engine.setName("engine");
        engine.setBaseDir(home.getPath());
        engine.setParentClassLoader(getClassLoader());

        // create just one Host
        host = controller.createHost("localhost", new File(home,
            getConfiguration().getPropertyValue(TomcatPropertySet.WEBAPPS_DIRECTORY)));
        host.setAutoDeploy(true);
        engine.addChild(host);
        engine.setDefaultHost(host.getName());

        // publish engine
        controller.addEngine(engine);

        // create HTTP connector
        controller.addConnector(
            controller.createConnector(null, getPort(), false));

        controller.start();

        // ideally Tomcat should be able to auto-expand a war file,
        // but I couldn't make it work, so this is a workaround meanwhile
        File[] webapps = new File(getConfiguration().getHome(),
            getConfiguration().getPropertyValue(TomcatPropertySet.WEBAPPS_DIRECTORY)).listFiles();
        if (webapps != null)
        {
            for (File webapp : webapps)
            {
                if (webapp.isFile())
                {
                    String webappName = webapp.getAbsolutePath();
                    webappName = webappName.substring(0, webappName.length() - 4);
                    if (new File(webappName).isDirectory())
                    {
                        // We both have a .war file and directory with the same name
                        // In this case, ignore the file and only deploy the directory
                        continue;
                    }
                }

                Deployable deployable = new WAR(webapp.getAbsolutePath());
                if (!scheduledDeployables.contains(deployable))
                {
                    scheduledDeployables.add(deployable);
                }
            }
        }
        if (!scheduledDeployables.isEmpty())
        {
            Deployer deployer = new TomcatEmbeddedLocalDeployer(this);
            for (Deployable deployable : scheduledDeployables)
            {
                deployer.redeploy(deployable);
            }
        }
    }

    /**
     * Tomcat's start/stop methods are synchronous, so no need for waiting.
     * 
     * @param waitForStarting never used
     */
    @Override
    protected void waitForCompletion(boolean waitForStarting)
    {
        // Nothing to do here as Tomcat start/stop methods are synchronous.
    }

    /**
     * Gets the port number for which this Tomcat is configured.
     * 
     * @return the port number
     */
    private int getPort()
    {
        return Integer.parseInt(getConfiguration().getPropertyValue(ServletPropertySet.PORT));
    }

    /**
     * {@inheritDoc}
     * @see AbstractEmbeddedLocalContainer#doStop()
     */
    @Override
    protected void doStop() throws Exception
    {
        if (controller != null)
        {
            // the stop method is allowed to be invoked multiple times
            controller.stop();
            controller = null;
            host = null;
        }
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.Container#getCapability()
     */
    public ContainerCapability getCapability()
    {
        return capability;
    }

    /**
     * Used by {@link TomcatEmbeddedLocalDeployer} to register {@link Deployable}s that are to be
     * deployed once the container is started.
     * 
     * @param deployable {@link Deployable} to be deployed later.
     */
    public void scheduleDeployment(Deployable deployable)
    {
        scheduledDeployables.add(deployable);
    }
}
