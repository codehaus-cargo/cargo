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
package org.codehaus.cargo.container.geronimo.internal;

import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;

import org.apache.tools.ant.types.FilterChain;

import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.spi.configuration.AbstractStandaloneLocalConfiguration;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.container.property.User;
import org.codehaus.cargo.container.property.RemotePropertySet;
import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.geronimo.GeronimoPropertySet;

/**
 * Abstract Geronimo standalone {@link org.codehaus.cargo.container.configuration.Configuration}
 * implementation.
 *
 * @version $Id$
 */
public abstract class AbstractGeronimoStandaloneLocalConfiguration extends
    AbstractStandaloneLocalConfiguration
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
    public AbstractGeronimoStandaloneLocalConfiguration(String dir)
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
    protected String[] getUserTokens()
    {
        String[] tokens = new String[]{" ", " "};
        StringBuilder usersToken = new StringBuilder("");
        StringBuilder groupsToken = new StringBuilder("");

        // Add token filters for authenticated users
        String usersString = getPropertyValue(GeronimoPropertySet.GERONIMO_USERS);
        if (usersString != null)
        {
            List<User> users = User.parseUsers(usersString);
            Map<String, Set<String>> groupUsersMapping = new HashMap<String, Set<String>>();

            for (User user : users)
            {
                usersToken.append(user.getName());
                usersToken.append('=');
                usersToken.append(user.getPassword());
                usersToken.append(System.getProperty("line.separator"));

                List<String> roles = user.getRoles();
                for (int i = 0; i < roles.size(); i++)
                {
                    Set<String> groupUsers = groupUsersMapping.get(roles.get(i));
                    if (groupUsers == null)
                    {
                        groupUsers = new HashSet<String>();
                        groupUsersMapping.put(roles.get(i), groupUsers);
                    }

                    groupUsers.add(user.getName());
                }
            }

            for (Map.Entry<String, Set<String>> groupUsers : groupUsersMapping.entrySet())
            {
                String key = groupUsers.getKey();
                Set<String> gUsers = groupUsers.getValue();
                groupsToken.append(key);
                groupsToken.append('=');
                Iterator<String> iter2 = gUsers.iterator();
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
