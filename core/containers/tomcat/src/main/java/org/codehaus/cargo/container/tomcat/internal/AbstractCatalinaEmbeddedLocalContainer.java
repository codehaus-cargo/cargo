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
package org.codehaus.cargo.container.tomcat.internal;

import java.io.File;
import java.io.InputStream;
import java.util.Enumeration;
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
            // CARGO-1602: Load the Tomcat logging configuration
            String loggingFile = getFileHandler().append(
                getConfiguration().getHome(), "conf/logging.properties");
            if (getFileHandler().exists(loggingFile))
            {
                LogManager lm = LogManager.getLogManager();
                try (InputStream is = getFileHandler().getInputStream(loggingFile))
                {
                    if (System.getProperty("java.util.logging.manager") == null
                        && System.getProperty("java.util.logging.config.file") == null)
                    {
                        lm.readConfiguration(is);
                        if (getOutput() != null)
                        {
                            Logger root = lm.getLogger("");
                            Handler[] handlers = saveRestoreRootLoggerHandlers(true);
                            FileHandler fileOutput = new FileHandler(getOutput());
                            root.addHandler(fileOutput);
                            for (Handler handler : handlers)
                            {
                                fileOutput.setEncoding(handler.getEncoding());
                                fileOutput.setErrorManager(handler.getErrorManager());
                                fileOutput.setFilter(handler.getFilter());
                                fileOutput.setFormatter(handler.getFormatter());
                                fileOutput.setLevel(handler.getLevel());

                                root.removeHandler(handler);
                            }
                        }
                    }
                    else
                    {
                        Class juliLM = getClassLoader().loadClass(
                            "org.apache.juli.ClassLoaderLogManager");
                        LogManager juli = (LogManager) juliLM.getConstructor().newInstance();
                        juli.readConfiguration(is);
                        Enumeration<String> loggerNames = juli.getLoggerNames();
                        while (loggerNames.hasMoreElements())
                        {
                            String loggerName = loggerNames.nextElement();
                            lm.addLogger(juli.getLogger(loggerName));
                        }
                    }
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

            controller.start(this);

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
            if (this.previousCatalinaBase == null)
            {
                System.clearProperty("catalina.base");
            }
            else
            {
                System.setProperty("catalina.base", this.previousCatalinaBase);
            }
            saveRestoreRootLoggerHandlers(false);
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
                if (this.previousCatalinaBase == null)
                {
                    System.clearProperty("catalina.base");
                }
                else
                {
                    System.setProperty("catalina.base", this.previousCatalinaBase);
                }
                saveRestoreRootLoggerHandlers(false);
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
     * Save or restore root logger handlers.
     * 
     * @param toSave <code>true</code> to save, <code>false</code> to restore.
     * @return Handlers is saved, <code>null</code> if restored.
     */
    private synchronized Handler[] saveRestoreRootLoggerHandlers(boolean toSave)
    {
        Logger root = LogManager.getLogManager().getLogger("");
        if (toSave)
        {
            this.previousRootLoggerHandlers = root.getHandlers();
        }
        else if (this.previousRootLoggerHandlers != null)
        {
            Handler[] handlers = root.getHandlers();
            for (Handler handler : handlers)
            {
                root.removeHandler(handler);
            }
            for (Handler handler : this.previousRootLoggerHandlers)
            {
                root.addHandler(handler);
            }
            this.previousRootLoggerHandlers = null;
        }
        return this.previousRootLoggerHandlers;
    }
}
