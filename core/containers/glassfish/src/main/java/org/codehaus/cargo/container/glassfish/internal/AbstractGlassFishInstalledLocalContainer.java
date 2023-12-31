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
package org.codehaus.cargo.container.glassfish.internal;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.configuration.StandaloneLocalConfiguration;
import org.codehaus.cargo.container.configuration.entry.DataSource;
import org.codehaus.cargo.container.configuration.entry.Resource;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.glassfish.GlassFishPropertySet;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.container.property.User;
import org.codehaus.cargo.container.spi.AbstractInstalledLocalContainer;
import org.codehaus.cargo.container.spi.jvm.JvmLauncher;
import org.codehaus.cargo.util.CargoException;

/**
 * Abstract GlassFish installed local container.
 */
public abstract class AbstractGlassFishInstalledLocalContainer
    extends AbstractInstalledLocalContainer
{

    /**
     * Calls parent constructor, which saves the configuration.
     * 
     * @param localConfiguration Configuration.
     */
    public AbstractGlassFishInstalledLocalContainer(LocalConfiguration localConfiguration)
    {
        super(localConfiguration);
    }

    /**
     * Invokes asadmin.
     * 
     * @param async Asynchronous invoke?
     * @param args Invoke arguments.
     * @return The exit code from asadmin, always {@code 0} when using asynchronous invocation.
     */
    public int invokeAsAdmin(boolean async, List<String> args)
    {
        JvmLauncher java = createJvmLauncher(false);
        String[] argsArray = new String[args.size()];
        argsArray = args.toArray(argsArray);
        return invokeAsAdmin(async, java, argsArray);
    }

    /**
     * Invokes asadmin.
     * 
     * @param async Asynchronous invoke?
     * @param args Invoke arguments.
     * @return The exit code from asadmin, always {@code 0} when using asynchronous invocation.
     */
    public int invokeAsAdmin(boolean async, String... args)
    {
        JvmLauncher java = createJvmLauncher(false);
        return invokeAsAdmin(async, java, args);
    }

    /**
     * Invokes asadmin using a Java container.
     * 
     * @param async Asynchronous invoke?
     * @param java JVM launcher.
     * @param args Invoke arguments.
     * @return The exit code from asadmin, always {@code 0} when using asynchronous invocation.
     */
    public int invokeAsAdmin(boolean async, JvmLauncher java, String... args)
    {
        AbstractAsAdmin asadmin = getAsAdmin();
        return asadmin.invokeAsAdmin(async, java, args);
    }

    /**
     * Returns the asadmin for the GlassFish server.
     * 
     * @return AsAdmin for the GlassFish server.
     */
    protected abstract AbstractAsAdmin getAsAdmin();

    /**
     * {@inheritDoc}. CARGO-1255: Remove the JVM arguments from the asadmin, else debugging and
     * potentially many other functions do work.
     */
    @Override
    protected void startInternal() throws Exception
    {
        synchronized (this.getConfiguration())
        {
            String jvmArguments =
                this.getConfiguration().getPropertyValue(GeneralPropertySet.JVMARGS);
            try
            {
                this.getConfiguration().getProperties().remove(GeneralPropertySet.JVMARGS);
                super.startInternal();
            }
            finally
            {
                if (jvmArguments != null)
                {
                    this.getConfiguration().setProperty(GeneralPropertySet.JVMARGS, jvmArguments);
                }
            }
        }
    }

    /**
     * {@inheritDoc}. CARGO-1255: Remove the JVM arguments from the asadmin, else debugging and
     * potentially many other functions do work.
     */
    @Override
    protected void stopInternal() throws Exception
    {
        synchronized (this.getConfiguration())
        {
            String jvmArguments =
                this.getConfiguration().getPropertyValue(GeneralPropertySet.JVMARGS);
            try
            {
                this.getConfiguration().getProperties().remove(GeneralPropertySet.JVMARGS);
                super.stopInternal();
            }
            finally
            {
                if (jvmArguments != null)
                {
                    this.getConfiguration().setProperty(GeneralPropertySet.JVMARGS, jvmArguments);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doStart(JvmLauncher java) throws Exception
    {
        this.getLogger().debug("Starting domain on HTTP port "
            + this.getConfiguration().getPropertyValue(ServletPropertySet.PORT)
            + " and admin port "
            + this.getConfiguration().getPropertyValue(GlassFishPropertySet.ADMIN_PORT),
            this.getClass().getName());

        // see https://glassfish.dev.java.net/issues/show_bug.cgi?id=885
        // needs to spawn
        this.invokeAsAdmin(true, java,
            "start-domain",
            "--interactive=false",
            "--domaindir",
            this.getConfiguration().getHome(),
            "--debug=" + this.getConfiguration().getPropertyValue(GlassFishPropertySet.DEBUG_MODE),
            this.getConfiguration().getPropertyValue(GlassFishPropertySet.DOMAIN_NAME)
        );

        // wait for the server to start
        boolean started = false;
        URL adminURL = new URL("http://"
            + this.getConfiguration().getPropertyValue(GeneralPropertySet.HOSTNAME) + ":"
            + this.getConfiguration().getPropertyValue(GlassFishPropertySet.ADMIN_PORT) + "/");
        long timeout = System.currentTimeMillis() + this.getTimeout();
        while (System.currentTimeMillis() < timeout)
        {
            try
            {
                InputStream is = adminURL.openConnection().getInputStream();
                is.close();
                started = true;
                break;
            }
            catch (IOException e)
            {
                // Keep on waiting
                Thread.sleep(1000);
            }
        }
        if (!started)
        {
            throw new CargoException("GlassFish server admin still not accessible on " + adminURL
                + " after " + this.getTimeout() + " milliseconds!");
        }

        AbstractGlassFishInstalledLocalDeployer deployer = getLocalDeployer();

        try
        {
            // Deploy datasources and resources
            // CARGO-1035: Only for standalone local configuration
            if (this.getConfiguration() instanceof StandaloneLocalConfiguration)
            {
                if (Boolean.parseBoolean(this.getConfiguration().getPropertyValue(
                    GlassFishPropertySet.REMOVE_DEFAULT_DATASOURCE)))
                {
                    try
                    {
                        deployer.undeployDatasource("DerbyPool", "jdbc/__default");
                    }
                    catch (CargoException e)
                    {
                        // CARGO-1516: Payara 5.201 onwards doesn't have the DerbyPool anymore
                        this.getLogger().debug("Failed removing default datasource",
                            this.getClass().getName());
                        this.getConfiguration().setProperty(
                            GlassFishPropertySet.REMOVE_DEFAULT_DATASOURCE, "false");
                    }
                }

                for (DataSource dataSource : this.getConfiguration().getDataSources())
                {
                    deployer.deployDatasource(dataSource);
                }

                for (Resource resource : this.getConfiguration().getResources())
                {
                    deployer.deployResource(resource);
                }

                // CARGO-1246: Create file users
                List<User> servletUsers = getConfiguration().getUsers();
                if (!servletUsers.isEmpty())
                {
                    deployer.activateDefaultPrincipalToRoleMapping();
                    for (final User user : servletUsers)
                    {
                        deployer.createFileUser(user);
                    }
                }
            }

            // Deploy scheduled deployables
            // CARGO-1039: Use redeploy and not deploy
            for (Deployable deployable : this.getConfiguration().getDeployables())
            {
                deployer.redeploy(deployable);
            }
        }
        catch (Throwable t)
        {
            StringBuilder sb = new StringBuilder();
            sb.append("At least one GlassFish deployment has failed: ");
            sb.append(t.toString());

            try
            {
                this.stop();
            }
            catch (Throwable stopException)
            {
                sb.append("; moreover stopping the container has also failed: ");
                sb.append(stopException.toString());
            }

            throw new CargoException(sb.toString(), t);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doStop(JvmLauncher java) throws Exception
    {
        try
        {
            this.invokeAsAdmin(false, java, new String[]
            {
                "stop-domain",
                "--domaindir",
                this.getConfiguration().getHome(),
                this.getConfiguration().getPropertyValue(GlassFishPropertySet.DOMAIN_NAME)
            });
        }
        finally
        {
            try
            {
                String pid = getFileHandler().append(getConfiguration().getHome(), "config/pid");
                if (!getFileHandler().exists(pid))
                {
                    pid = getFileHandler().append(getFileHandler().append(
                            getConfiguration().getHome(), getConfiguration().getPropertyValue(
                                GlassFishPropertySet.DOMAIN_NAME)), "config/pid");
                }
                if (getFileHandler().exists(pid))
                {
                    this.getLogger().debug(
                        "Found a pid file in [" + pid + "], attempting force kill",
                            this.getClass().getName());

                    pid = getFileHandler().readTextFile(pid, StandardCharsets.UTF_8);
                    if (System.getProperty("os.name")
                        .toLowerCase(Locale.ENGLISH).startsWith("windows"))
                    {
                        Runtime.getRuntime().exec(new String[] {"taskkill", "/PID", pid, "/F"});
                    }
                    else
                    {
                        Runtime.getRuntime().exec(new String[] {"kill", "-9", pid});
                    }
                }
            }
            catch (Throwable e)
            {
                // Ignore, we tried our best
                this.getLogger().debug(
                    "Failed forceful kill: " + e.toString(), this.getClass().getName());
            }
        }
    }

    /**
     * @return Local deployer for this local container.
     */
    protected abstract AbstractGlassFishInstalledLocalDeployer getLocalDeployer();

}
