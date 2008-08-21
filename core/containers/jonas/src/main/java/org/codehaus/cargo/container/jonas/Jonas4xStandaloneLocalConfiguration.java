/*
 * ========================================================================
 *
 * Copyright 2007-2008 OW2.
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
package org.codehaus.cargo.container.jonas;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.tools.ant.types.FilterChain;
import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.jonas.internal.Jonas4xStandaloneLocalConfigurationCapability;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.container.property.User;
import org.codehaus.cargo.container.spi.configuration.AbstractStandaloneLocalConfiguration;
import org.codehaus.cargo.util.CargoException;

/**
 * Implementation of a standalone {@link org.codehaus.cargo.container.configuration.Configuration}
 * for JOnAS.
 * 
 * @version $Id: Jonas4xStandaloneLocalConfiguration.java 14641 2008-07-25 11:46:29Z alitokmen $
 */
public class Jonas4xStandaloneLocalConfiguration extends AbstractStandaloneLocalConfiguration
{
    /**
     * Jetty web container class name.
     */
    public static final String JETTY_WEB_CONTAINER_CLASS_NAME =
        "org.objectweb.jonas.web.jetty50.JettyJWebContainerServiceImpl";

    /**
     * Tomcat web container class name.
     */
    public static final String CATALINA_WEB_CONTAINER_CLASS_NAME =
        "org.objectweb.jonas.web.wrapper.catalina55.CatalinaJWebContainerServiceWrapper";

    /**
     * Token filter key for users' role.
     */
    protected static final String TOKEN_FILTER_KEY_USERS_ROLE = "cargo.servlet.users.role";

    /**
     * Token filter key for users' user name.
     */
    protected static final String TOKEN_FILTER_KEY_USERS_USER = "cargo.servlet.users.user";

    /**
     * JOnAS container capability.
     */
    private static ConfigurationCapability capability =
        new Jonas4xStandaloneLocalConfigurationCapability();

    /**
     * JOnAS installed container.
     */
    private InstalledLocalContainer installedContainer;

    /**
     * {@inheritDoc}
     * 
     * @see AbstractStandaloneLocalConfiguration#AbstractStandaloneLocalConfiguration(String)
     */
    public Jonas4xStandaloneLocalConfiguration(String dir)
    {
        super(dir);

        setProperty(GeneralPropertySet.RMI_PORT, "1099");
        setProperty(GeneralPropertySet.PROTOCOL, "http");
        setProperty(GeneralPropertySet.HOSTNAME, "localhost");
        setProperty(ServletPropertySet.PORT, "9000");
        setProperty(GeneralPropertySet.JVMARGS, "-Xms128m -Xmx512m");
        setProperty(JonasPropertySet.JONAS_REALM_NAME, "memrlm_1");
        setProperty(JonasPropertySet.JONAS_AVAILABLES_DATASOURCES, "HSQL1");
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.codehaus.cargo.container.configuration.Configuration#getCapability()
     */
    public ConfigurationCapability getCapability()
    {
        return capability;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.codehaus.cargo.container.spi.configuration.AbstractLocalConfiguration#configure(LocalContainer)
     */
    protected void doConfigure(LocalContainer container) throws Exception
    {
        this.installedContainer = (InstalledLocalContainer) container;
        setupConfigurationDir();

        FilterChain filterChain = createJonasFilterChain(this.installedContainer);

        // setting the JONAS_BASE environment
        getFileHandler().createDirectory(getHome(), "/webapps");
        getFileHandler().createDirectory(getHome(), "/webapps/autoload");
        getFileHandler().createDirectory(getHome(), "/ejbjars");
        getFileHandler().createDirectory(getHome(), "/ejbjars/autoload");
        getFileHandler().createDirectory(getHome(), "/apps");
        getFileHandler().createDirectory(getHome(), "/apps/autoload");
        getFileHandler().createDirectory(getHome(), "/rars");
        getFileHandler().createDirectory(getHome(), "/rars/autoload");

        getFileHandler().createDirectory(getHome(), "/logs");

        String confDir = getFileHandler().createDirectory(getHome(), "/conf");

        // Copy configuration files from cargo resources directory with token replacement
        String[] cargoFiles = new String[]
        {
            "carol.properties", "jaas.config", "jetty5.xml", "jonas.properties", "jonas-realm.xml",
            "server.xml", "trace.properties"
        };
        for (int i = 0; i < cargoFiles.length; i++)
        {
            getResourceUtils().copyResource(
                RESOURCE_PATH + container.getId() + "/" + cargoFiles[i],
                getFileHandler().append(confDir, cargoFiles[i]), getFileHandler(), filterChain);
        }

        // Copy resources from JOnAS installation folder and exclude files
        // that already copied from cargo resources folder
        copyExternalResources(new File(installedContainer.getHome(), "conf"), new File(confDir),
            cargoFiles);

        // Deploy with user defined deployables with the appropriate deployer
        Jonas4xInstalledLocalDeployer deployer = new Jonas4xInstalledLocalDeployer(
            installedContainer);
        deployer.deploy(getDeployables());

        // Deploy the CPC (Cargo Ping Component) to the webapps directory
        getResourceUtils().copyResource(RESOURCE_PATH + "cargocpc.war",
            getFileHandler().append(getHome(), "/webapps/autoload/cargocpc.war"), getFileHandler());
    }

    /**
     * Copy external resources to cargo configuration directory. This method will copy entire
     * resources in the sourceDir (recursive), if it's a directory.
     * 
     * @param sourceDir resource file / directory to be copied
     * @param destDir cargo configuration directory
     * @param cargoFiles list of cargo resources file that will excluded
     * @throws IOException If an error occurs during the copy.
     */
    private void copyExternalResources(File sourceDir, File destDir, String[] cargoFiles)
        throws IOException
    {
        File[] sourceFiles = sourceDir.listFiles();
        if (sourceFiles != null)
        {
            for (int i = 0; i < sourceFiles.length; i++)
            {
                if (!isExcluded(cargoFiles, sourceFiles[i].getName()))
                {
                    if (sourceFiles[i].isDirectory())
                    {
                        getFileHandler().createDirectory(destDir.getPath(),
                            sourceFiles[i].getName());
                        copyExternalResources(sourceFiles[i], new File(destDir, sourceFiles[i]
                            .getName()), cargoFiles);
                    }
                    else
                    {
                        getFileHandler().copy(new FileInputStream(sourceFiles[i]),
                            new FileOutputStream(new File(destDir, sourceFiles[i].getName())));
                    }

                }
            }
        }
    }

    /**
     * Check if file with name <code>filename</code> is one of cargo resources file.
     * 
     * @param cargoFiles list of cargo resources files
     * @param filename filename of the file
     * @return true if <code>filename</code> is one of cargo resources file
     */
    private boolean isExcluded(String[] cargoFiles, String filename)
    {
        for (int i = 0; i < cargoFiles.length; i++)
        {
            if (cargoFiles[i].equals(filename))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.codehaus.cargo.container.spi.configuration.AbstractStandaloneLocalConfiguration#createFilterChain()
     */
    protected FilterChain createJonasFilterChain(InstalledLocalContainer installedContainer)
    {
        // protocol, port, hostname are handled by the abstract impl
        FilterChain filterChain = super.createFilterChain();

        getAntUtils().addTokenToFilterChain(filterChain, GeneralPropertySet.RMI_PORT,
            getPropertyValue(GeneralPropertySet.RMI_PORT));

        getAntUtils().addTokenToFilterChain(filterChain, GeneralPropertySet.LOGGING,
            getJonasLogLevel(getPropertyValue(GeneralPropertySet.LOGGING)));

        getAntUtils().addTokenToFilterChain(filterChain, JonasPropertySet.JONAS_REALM_NAME,
            getPropertyValue(JonasPropertySet.JONAS_REALM_NAME));

        getAntUtils().addTokenToFilterChain(filterChain,
            JonasPropertySet.JONAS_AVAILABLES_DATASOURCES,
            getPropertyValue(JonasPropertySet.JONAS_AVAILABLES_DATASOURCES));

        getAntUtils().addTokenToFilterChain(filterChain,
            JonasPropertySet.JONAS_WEBCONTAINER_CLASS_NAME,
            getWebContainerClassName(installedContainer));

        createUserFilterChain(filterChain);

        return filterChain;

    }

    /**
     * Get the Web Container Class Name.
     * 
     * @param installedContainer theinstalled container
     * @return he Web Container Class Name.
     * @throws CargoException if Unable to detect JOnAS web container
     */
    protected String getWebContainerClassName(InstalledLocalContainer installedContainer)
        throws CargoException
    {
        String providedSetting = getPropertyValue(JonasPropertySet.JONAS_WEBCONTAINER_CLASS_NAME);
        if (providedSetting == null || providedSetting.trim().length() < 1)
        {
            if (isWebContainerInstalled("lib/jetty/lib", installedContainer))
            {
                providedSetting = JETTY_WEB_CONTAINER_CLASS_NAME;
            }
            else if (isWebContainerInstalled("lib/catalina/server/lib", installedContainer))
            {
                providedSetting = CATALINA_WEB_CONTAINER_CLASS_NAME;
            }
        }

        if (providedSetting == null || providedSetting.trim().length() < 1)
        {
            throw new CargoException("Unable to detect JOnAS web container "
                + "implementation please provide a "
                + JonasPropertySet.JONAS_WEBCONTAINER_CLASS_NAME
                + " setting containg the web runtime class name");
        }
        else
        {
            return providedSetting;
        }
    }

    /**
     * Checks whether a given web container is installed.
     * 
     * @param webContainerLibDir Web container library directory.
     * @param installedContainer Container to look for.
     * 
     * @return true if installed, false otherwise.
     */
    private boolean isWebContainerInstalled(String webContainerLibDir,
        InstalledLocalContainer installedContainer)
    {

        String libsDirName = getFileHandler().append(installedContainer.getHome(),
            webContainerLibDir);
        boolean exists = getFileHandler().exists(libsDirName);

        if (exists)
        {
            String[] files = getFileHandler().getChildren(libsDirName);
            // more than 1 jar is present in webContainerLibDir if the container is installed
            int jarCount = 0;
            for (int i = 0; i < files.length; i++)
            {
                if (files[i].toLowerCase().endsWith(".jar"))
                {
                    jarCount++;
                }
            }

            if (jarCount > 1)
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Creates the user filter chain that should be applied while copying container configuration
     * files to the working directory from which the container is started.
     * 
     * @param filterChain The user filter chain
     */
    protected void createUserFilterChain(FilterChain filterChain)
    {
        StringBuffer rolesBuffer = new StringBuffer("<!-- no cargo roles defined -->");
        StringBuffer usersBuffer = new StringBuffer("<!-- no cargo users defined -->");
        if (getPropertyValue(ServletPropertySet.USERS) != null)
        {

            rolesBuffer.setLength(0);
            usersBuffer.setLength(0);
            Set processedRoles = new HashSet();
            Iterator users = User.parseUsers(getPropertyValue(ServletPropertySet.USERS)).iterator();
            while (users.hasNext())
            {
                User user = (User) users.next();
                usersBuffer.append("<user name=\"").append(user.getName().trim()).append(
                    "\" password=\"").append(user.getPassword()).append("\" roles=\"");
                Iterator roles = user.getRoles().iterator();
                while (roles.hasNext())
                {
                    String role = (String) roles.next();
                    if (!processedRoles.contains(role))
                    {
                        rolesBuffer.append("<role name=\"").append(role).append(
                            "\" description=\"Cargo standalone configuration "
                                + "auto generated role\" />\n");
                        processedRoles.add(role);
                    }
                    usersBuffer.append(role);
                    if (roles.hasNext())
                    {
                        usersBuffer.append(",");
                    }
                }
                usersBuffer.append("\" />\n");
            }
        }
        getAntUtils().addTokenToFilterChain(filterChain, TOKEN_FILTER_KEY_USERS_ROLE,
            rolesBuffer.toString().trim());
        getAntUtils().addTokenToFilterChain(filterChain, "cargo.servlet.users.user",
            usersBuffer.toString().trim());
    }

    /**
     * Translate Cargo logging levels into JOnAS logging levels.
     * 
     * @param cargoLogLevel Cargo logging level
     * @return the corresponding JOnAS logging level
     */
    private String getJonasLogLevel(String cargoLogLevel)
    {
        String level;

        if (cargoLogLevel.equalsIgnoreCase("low"))
        {
            level = "ERROR";
        }
        else if (cargoLogLevel.equalsIgnoreCase("medium"))
        {
            level = "WARN";
        }
        else
        {
            level = "INFO";
        }

        return level;
    }

    /**
     * {@inheritDoc}
     * 
     * @see Object#toString()
     */
    public String toString()
    {
        return "JOnAS Standalone Configuration";
    }
}
