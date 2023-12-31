/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2024 Ali Tokmen.
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
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.codehaus.cargo.container.ContainerCapability;
import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.deployer.Deployer;
import org.codehaus.cargo.container.internal.ServletContainerCapability;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.container.spi.AbstractEmbeddedLocalContainer;
import org.codehaus.cargo.container.tomcat.TomcatEmbeddedLocalDeployer;
import org.codehaus.cargo.container.tomcat.TomcatPropertySet;
import org.codehaus.cargo.util.CargoException;

/**
 * Base support for Catalina based embedded local containers.
 */
public abstract class AbstractCatalinaEmbeddedLocalContainer extends AbstractEmbeddedLocalContainer
{
    /**
     * Root of the Tomcat object model.
     */
    protected TomcatEmbedded.Embedded controller;

    /**
     * Tomcat host object.
     */
    protected TomcatEmbedded.Host host;

    /**
     * Tomcat connector object.
     */
    protected TomcatEmbedded.Connector connector;

    /**
     * Previous value of <code>catalina.base</code>
     */
    private String previousCatalinaBase;

    /**
     * Previous log handlers of the root logger.
     */
    private Handler[] previousRootLoggerHandlers;

    /**
     * Capability of the Tomcat/Catalina container.
     */
    private final ContainerCapability capability = new ServletContainerCapability();

    /**
     * {@link WAR}s to be deployed once the container is started.<br>
     * <br>
     * One can only deploy to an embedded container after it's started, but cargo allows you to
     * deploy apps before the container starts. so we need to remember what's supposed to be
     * deployed.
     */
    private final Map<String, WAR> scheduledDeployables = new HashMap<String, WAR>();

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
     */
    @Override
    protected void doStart() throws Exception
    {
        // Tomcat will resolve relative path against CATALINA_BASE, so make it absolute here.
        File home = new File(getConfiguration().getHome()).getAbsoluteFile();
        this.previousCatalinaBase = System.setProperty("catalina.base", home.getAbsolutePath());

        try
        {
            if (Boolean.parseBoolean(getConfiguration().getPropertyValue(
                TomcatPropertySet.EMBEDDED_OVERRIDE_JAVA_LOGGING)))
            {
                LogManager lm = LogManager.getLogManager();

                String loggingFile = getFileHandler().append(
                    getConfiguration().getHome(), "conf/logging.properties");
                if (getFileHandler().exists(loggingFile))
                {
                    getLogger().info(
                        "Overriding Java logging with file [" + loggingFile + "]",
                            this.getClass().getName());
                    try (InputStream is = getFileHandler().getInputStream(loggingFile))
                    {
                        lm.readConfiguration(is);
                    }
                }

                if (getOutput() != null)
                {
                    getLogger().info(
                        "Overriding Java logging output to [" + getOutput() + "]",
                            this.getClass().getName());
                    Logger root = lm.getLogger("");
                    this.previousRootLoggerHandlers = root.getHandlers();
                    FileHandler fileOutput = new FileHandler(getOutput());
                    if (this.previousRootLoggerHandlers != null)
                    {
                        for (Handler handler : this.previousRootLoggerHandlers)
                        {
                            fileOutput.setEncoding(handler.getEncoding());
                            fileOutput.setErrorManager(handler.getErrorManager());
                            fileOutput.setFilter(handler.getFilter());
                            fileOutput.setFormatter(handler.getFormatter());
                            fileOutput.setLevel(handler.getLevel());

                            root.removeHandler(handler);
                        }
                    }
                    root.addHandler(fileOutput);
                }
            }
            TomcatEmbedded wrapper = new TomcatEmbedded(getClassLoader());

            controller = wrapper.new Embedded();

            controller.setCatalinaBase(home);
            prepareController(wrapper, home,
                Integer.parseInt(getConfiguration().getPropertyValue(ServletPropertySet.PORT)));
            if (connector == null || host == null)
            {
                throw new CargoException("Programming error: attributes connector or host not set "
                    + "after prepareController");
            }

            controller.start();

            // We don't want Tomcat to deploy WARs by itself, else we cannot undeploy them.
            // As a result, once Tomcat is started, deploy WARs manually.
            File[] webapps = new File(getConfiguration().getHome(),
                getConfiguration().getPropertyValue(TomcatPropertySet.WEBAPPS_DIRECTORY))
                    .listFiles();
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

                    WAR war = new WAR(webapp.getAbsolutePath());
                    if (!scheduledDeployables.containsKey(war.getContext()))
                    {
                        scheduledDeployables.put(war.getContext(), war);
                    }
                }
            }
            if (!scheduledDeployables.isEmpty())
            {
                Deployer deployer = new TomcatEmbeddedLocalDeployer(this);
                Map<String, WAR> scheduledDeployablesCopy =
                    new HashMap<String, WAR>(scheduledDeployables);
                for (Map.Entry<String, WAR> deployable : scheduledDeployablesCopy.entrySet())
                {
                    deployer.redeploy(deployable.getValue());
                    scheduledDeployables.remove(deployable.getKey());
                }
            }
        }
        catch (Exception e)
        {
            restoreEnvironment();
            throw e;
        }
    }

    /**
     * Embedded Tomcat's start method is synchronous, so no need for waiting when starting.
     * {@inheritDoc}
     */
    @Override
    protected void waitForCompletion(boolean waitForStarting) throws InterruptedException
    {
        if (waitForStarting)
        {
            // Nothing to do here as Tomcat start method is synchronous
        }
        else
        {
            super.waitForCompletion(waitForStarting);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doStop() throws Exception
    {
        if (controller != null)
        {
            try
            {
                controller.stop();
                connector.destroy();
                controller = null;
                connector = null;
                host = null;
            }
            finally
            {
                restoreEnvironment();
            }
        }
        else
        {
            throw new ContainerException("Embedded Tomcat container is not started");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
        WAR war = (WAR) deployable;
        scheduledDeployables.put(war.getContext(), war);
    }

    /**
     * Prepare the Tomcat controller. After this method returns, the <code>host</code> and
     * <code>connector</code> protected attributes <u>must</u> be set.
     * 
     * @param wrapper Tomcat wrapper.
     * @param home <code>CATALINA_BASE</code> directory.
     * @param port HTTP port.
     */
    protected abstract void prepareController(TomcatEmbedded wrapper, File home, int port);

    /**
     * Restore the <code>catalina.base</code> as well as the Java logging.
     */
    private void restoreEnvironment()
    {
        if (this.previousCatalinaBase == null)
        {
            System.clearProperty("catalina.base");
        }
        else
        {
            System.setProperty("catalina.base", this.previousCatalinaBase);
        }

        if (Boolean.parseBoolean(getConfiguration().getPropertyValue(
            TomcatPropertySet.EMBEDDED_OVERRIDE_JAVA_LOGGING)))
        {
            getLogger().info(
                "Restoring (part of) the Java logging configuration", this.getClass().getName());
            Logger root = LogManager.getLogManager().getLogger("");
            Handler[] currentRootLoggerHandlers = root.getHandlers();
            if (currentRootLoggerHandlers != null)
            {
                for (Handler handler : currentRootLoggerHandlers)
                {
                    root.removeHandler(handler);
                }
            }
            if (this.previousRootLoggerHandlers != null)
            {
                for (Handler handler : this.previousRootLoggerHandlers)
                {
                    root.addHandler(handler);
                }
            }
            getLogger().info(
                "Restored (part of) the Java logging configuration", this.getClass().getName());
            this.previousRootLoggerHandlers = null;
        }
    }
}
