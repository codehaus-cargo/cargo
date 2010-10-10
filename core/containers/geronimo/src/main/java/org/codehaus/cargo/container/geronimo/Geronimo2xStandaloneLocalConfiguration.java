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
import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;

import org.apache.tools.ant.types.FilterChain;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.spi.configuration.AbstractStandaloneLocalConfiguration;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.User;
import org.codehaus.cargo.container.property.RemotePropertySet;
import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.geronimo.internal.GeronimoStandaloneLocalConfigurationCapability;

/**
 * Geronimo 2.x series standalone {@link org.codehaus.cargo.container.configuration.Configuration}
 * implementation.
 *
 * @version $Id$
 */
public class Geronimo2xStandaloneLocalConfiguration extends AbstractStandaloneLocalConfiguration
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
    public Geronimo2xStandaloneLocalConfiguration(String dir)
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
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.spi.configuration.AbstractLocalConfiguration#configure(LocalContainer)
     */
    @Override
    protected void doConfigure(LocalContainer container) throws Exception
    {
        setupConfigurationDir();

        FilterChain filterChain = createGeronimoFilterChain(container);

        final String varDirectory = getHome() + "/var";
        if (!getFileHandler().exists(varDirectory))
        {
            getFileHandler().createDirectory(getHome(), "/var");

            // The /var directory does not exist, create it
            final String originalVarDirectory = ((InstalledLocalContainer) container).getHome()
                + "/var";
            getFileHandler().copyDirectory(originalVarDirectory, varDirectory);

            String securityDir = getFileHandler().createDirectory(getHome(), "/var/security");
            getResourceUtils().copyResource(RESOURCE_PATH + container.getId() + "/users.properties",
                new File(securityDir, "users.properties"), filterChain);
            getResourceUtils().copyResource(RESOURCE_PATH + container.getId()
                + "/groups.properties", new File(securityDir, "groups.properties"), filterChain);

            getFileHandler().createDirectory(getHome(), "/var/deploy");
            getFileHandler().createDirectory(getHome(), "/var/temp");
        }
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

        // Add token filters for authenticated users
        String[] userTokens = getUserTokens();
        getAntUtils().addTokenToFilterChain(filterChain, "geronimo.users", userTokens[0]);
        getAntUtils().addTokenToFilterChain(filterChain, "geronimo.groups", userTokens[1]);
        getAntUtils().addTokenToFilterChain(filterChain, "geronimo.manager.username",
            getPropertyValue(RemotePropertySet.USERNAME));
        getAntUtils().addTokenToFilterChain(filterChain, "geronimo.manager.password",
            getPropertyValue(RemotePropertySet.PASSWORD));

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
        StringBuilder usersToken = new StringBuilder("");
        StringBuilder groupsToken = new StringBuilder("");

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
