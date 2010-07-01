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

import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.spi.configuration.AbstractStandaloneLocalConfiguration;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.container.property.User;
import org.codehaus.cargo.container.property.RemotePropertySet;
import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.geronimo.internal.GeronimoStandaloneLocalConfigurationCapability;
import org.apache.tools.ant.types.FilterChain;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.taskdefs.Copy;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;

/**
 * Geronimo 1.x series standalone {@link org.codehaus.cargo.container.configuration.Configuration}
 * implementation.
 *
 * @version $Id$
 */
public class Geronimo1xStandaloneLocalConfiguration extends AbstractStandaloneLocalConfiguration
{
    /**
     * Geronimo configuration capability.
     */
    private static ConfigurationCapability capability =
        new GeronimoStandaloneLocalConfigurationCapability();

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.spi.configuration.AbstractStandaloneLocalConfiguration#AbstractStandaloneLocalConfiguration(String)
     */
    public Geronimo1xStandaloneLocalConfiguration(String dir)
    {
        super(dir);
        setProperty(GeneralPropertySet.RMI_PORT, "1099");
        setProperty(RemotePropertySet.USERNAME, "system");
        setProperty(RemotePropertySet.PASSWORD, "manager");
        setProperty(GeronimoPropertySet.GERONIMO_CONSOLE_LOGLEVEL, "INFO");
        setProperty(GeronimoPropertySet.GERONIMO_FILE_LOGLEVEL, "DEBUG");
        setProperty(GeronimoPropertySet.GERONIMO_SERVLET_CONTAINER_ID, "tomcat");
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.configuration.Configuration#getCapability()
     */
    public ConfigurationCapability getCapability()
    {
        return capability;
    }

    /**
     * Geronimo does not support static deployments, warn the user.
     *
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.configuration.LocalConfiguration#addDeployable(org.codehaus.cargo.container.deployable.Deployable)
     */
    @Override
    public synchronized void addDeployable(Deployable newDeployable)
    {
        getLogger().warn("Geronimo doesn't support static deployments. Ignoring deployable ["
            + newDeployable.getFile() + "].", this.getClass().getName());
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.spi.configuration.AbstractLocalConfiguration#configure(LocalContainer)
     */
    @Override
    protected void doConfigure(LocalContainer container) throws Exception
    {
        setupConfigurationDir();

        FilterChain filterChain = createGeronimoFilterChain(container);

        // The tmp directory needs to exist before the container starts
        getFileHandler().createDirectory(getHome(), "/var/temp");

        // TODO: Remove this once the system property for changing the var/ directory is
        // implemented in Geronimo.
        copyExtraStuffTemporarily(new File(((InstalledLocalContainer) container).getHome()));

        // Copy the geronimo configuration file
        String configDir = getFileHandler().createDirectory(getHome(), "var/config");
        getResourceUtils().copyResource(RESOURCE_PATH + container.getId() + "/"
            + getPropertyValue(GeronimoPropertySet.GERONIMO_SERVLET_CONTAINER_ID)
            + "/config.xml", new File(configDir, "config.xml"), filterChain);

        // Copy security-related files
        String securityDir = getFileHandler().createDirectory(getHome(), "/var/security");
        getResourceUtils().copyResource(RESOURCE_PATH + container.getId() + "/users.properties",
            new File(securityDir, "users.properties"), filterChain);
        getResourceUtils().copyResource(RESOURCE_PATH + container.getId() + "/groups.properties",
            new File(securityDir, "groups.properties"), filterChain);
        getResourceUtils().copyResource(RESOURCE_PATH + container.getId() + "/keystore",
            new File(securityDir, "keystore"));

        // Copy log settings
        String logDir = getFileHandler().createDirectory(getHome(), "/var/log");
        getResourceUtils().copyResource(RESOURCE_PATH + container.getId()
            + "/server-log4j.properties",
            new File(logDir, "server-log4j.properties"), filterChain);
        getResourceUtils().copyResource(RESOURCE_PATH + container.getId()
            + "/client-log4j.properties", new File(logDir, "client-log4j.properties"), filterChain);
        getResourceUtils().copyResource(RESOURCE_PATH + container.getId()
            + "/deployer-log4j.properties",
            new File(logDir, "deployer-log4j.properties"), filterChain);

        String deployDir = getFileHandler().createDirectory(getHome(), "deploy");

        if (!getFileHandler().exists(deployDir))
        {
            getFileHandler().mkdirs(deployDir);
        }
    }

    /**
     * Copy extra stuff to create a valid Geronimo configuration. Remove this once the system
     * property for changing the var/ directory is implemented in Geronimo.
     *
     * @param containerHome location where the container is installed
     */
    private void copyExtraStuffTemporarily(File containerHome)
    {
        // The config store needs to exist before the container starts
        File configStore = new File(containerHome, "config-store");
        if (configStore.isDirectory())
        {
            Copy copyStore = (Copy) getAntUtils().createAntTask("copy");
            FileSet fileSetStore = new FileSet();
            fileSetStore.setDir(new File(containerHome, "config-store"));
            copyStore.addFileset(fileSetStore);
            copyStore.setTodir(new File(getHome(), "config-store"));
            copyStore.execute();
        }
        else
        {
            new File(getHome(), "config-store").mkdirs();
        }

        // Create the Geronimo repository by copying it.
        Copy copyRepo = (Copy) getAntUtils().createAntTask("copy");
        FileSet fileSetRepo = new FileSet();
        fileSetRepo.setDir(new File(containerHome, "repository"));
        copyRepo.addFileset(fileSetRepo);
        copyRepo.setTodir(new File(getHome(), "repository"));
        copyRepo.execute();
    }

    /**
     * Create filter to replace tokens in configuration file with user defined values.
     *
     * @param container the instance representing the Geronimo container
     * @return token with all the user-defined token value
     * @exception java.net.MalformedURLException If an URL is malformed.
     */
    protected FilterChain createGeronimoFilterChain(LocalContainer container)
        throws MalformedURLException
    {
        FilterChain filterChain = getFilterChain();

        getAntUtils().addTokenToFilterChain(filterChain, GeneralPropertySet.RMI_PORT,
            getPropertyValue(GeneralPropertySet.RMI_PORT));

        getAntUtils().addTokenToFilterChain(filterChain, ServletPropertySet.PORT,
            getPropertyValue(ServletPropertySet.PORT));

        // Add token filters for authenticated users
        String[] userTokens = getUserTokens();
        getAntUtils().addTokenToFilterChain(filterChain, "geronimo.users", userTokens[0]);
        getAntUtils().addTokenToFilterChain(filterChain, "geronimo.groups", userTokens[1]);
        getAntUtils().addTokenToFilterChain(filterChain, "geronimo.manager.username",
            getPropertyValue(RemotePropertySet.USERNAME));
        getAntUtils().addTokenToFilterChain(filterChain, "geronimo.manager.password",
            getPropertyValue(RemotePropertySet.PASSWORD));

        getAntUtils().addTokenToFilterChain(filterChain, "geronimo.console.log.level",
            getPropertyValue(GeronimoPropertySet.GERONIMO_CONSOLE_LOGLEVEL));
        getAntUtils().addTokenToFilterChain(filterChain, "geronimo.file.log.level",
            getPropertyValue(GeronimoPropertySet.GERONIMO_FILE_LOGLEVEL));

        return filterChain;
    }

    /**
     * Generate properties file entries for Geronimo users and groups.
     *
     * @return Array holding the entries for the users and groups properties file entries
     */
    private String[] getUserTokens()
    {
        String[] tokens = new String[]{" ", " "};
        StringBuffer usersToken = new StringBuffer("");
        StringBuffer groupsToken = new StringBuffer("");

        // Add token filters for authenticated users
        String usersString = getPropertyValue(GeronimoPropertySet.GERONIMO_USERS);
        if (usersString != null)
        {
            Iterator users = User.parseUsers(usersString).iterator();
            Map groupUsersMapping = new HashMap();

            while (users.hasNext())
            {
                User user = (User) users.next();
                usersToken.append(user.getName());
                usersToken.append('=');
                usersToken.append(user.getPassword());
                usersToken.append(System.getProperty("line.separator"));

                List roles = user.getRoles();
                for (int i = 0; i < roles.size(); i++)
                {
                    Set groupUsers = (Set) groupUsersMapping.get(roles.get(i));
                    if (groupUsers == null)
                    {
                        groupUsers = new HashSet();
                        groupUsersMapping.put(roles.get(i), groupUsers);
                    }

                    groupUsers.add(user.getName());
                }
            }

            Set groups = groupUsersMapping.keySet();
            Iterator iter = groups.iterator();
            while (iter.hasNext())
            {
                Object key = iter.next();
                Set gUsers = (Set) groupUsersMapping.get(key);
                groupsToken.append(key);
                groupsToken.append('=');
                Iterator iter2 = gUsers.iterator();
                while (iter2.hasNext())
                {
                    groupsToken.append(iter2.next());
                    if (iter2.hasNext())
                    {
                        groupsToken.append(',');
                    }
                }
                groupsToken.append(System.getProperty("line.separator"));
            }

            tokens[0] = usersToken.toString();
            tokens[1] = groupsToken.toString();
        }

        return tokens;
    }
}
