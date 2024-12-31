/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2025 Ali Tokmen.
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

import java.io.File;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.entry.DataSource;
import org.codehaus.cargo.container.geronimo.GeronimoInstalledLocalDeployer;
import org.codehaus.cargo.container.geronimo.GeronimoPropertySet;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.RemotePropertySet;
import org.codehaus.cargo.container.property.TransactionSupport;
import org.codehaus.cargo.container.property.User;
import org.codehaus.cargo.container.spi.configuration.AbstractStandaloneLocalConfiguration;
import org.codehaus.cargo.util.FileHandler;

/**
 * Abstract Geronimo standalone {@link org.codehaus.cargo.container.configuration.Configuration}
 * implementation.
 */
public abstract class AbstractGeronimoStandaloneLocalConfiguration extends
    AbstractStandaloneLocalConfiguration
{
    /**
     * {@inheritDoc}
     * @see AbstractStandaloneLocalConfiguration#AbstractStandaloneLocalConfiguration(String)
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
     * Create filter to replace tokens in configuration file with user defined values.
     * 
     * @param container the instance representing the Geronimo container
     * @return token with all the user-defined token value
     * @exception java.net.MalformedURLException If an URL is malformed.
     */
    protected Map<String, String> createGeronimoReplacements(LocalContainer container)
        throws MalformedURLException
    {
        Map<String, String> replacements = getReplacements();

        // Add token filters for authenticated users
        String[] userTokens = getUserTokens();
        replacements.put("geronimo.users", userTokens[0]);
        replacements.put("geronimo.groups", userTokens[1]);
        replacements.put("geronimo.manager.username",
            getPropertyValue(RemotePropertySet.USERNAME));
        replacements.put("geronimo.manager.password",
            getPropertyValue(RemotePropertySet.PASSWORD));

        replacements.put("geronimo.console.log.level",
            getPropertyValue(GeronimoPropertySet.GERONIMO_CONSOLE_LOGLEVEL));
        replacements.put("geronimo.file.log.level",
            getPropertyValue(GeronimoPropertySet.GERONIMO_FILE_LOGLEVEL));

        return replacements;
    }

    /**
     * Generate properties file entries for Geronimo users and groups.
     * 
     * @return Array holding the entries for the users and groups properties file entries
     */
    protected String[] getUserTokens()
    {
        String[] tokens = new String[] {" ", " "};
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
                usersToken.append(FileHandler.NEW_LINE);

                for (String role : user.getRoles())
                {
                    Set<String> groupUsers = groupUsersMapping.get(role);
                    if (groupUsers == null)
                    {
                        groupUsers = new HashSet<String>();
                        groupUsersMapping.put(role, groupUsers);
                    }

                    groupUsers.add(user.getName());
                }
            }

            for (Map.Entry<String, Set<String>> groupUsers : groupUsersMapping.entrySet())
            {
                String key = groupUsers.getKey();
                groupsToken.append(key);
                groupsToken.append('=');
                groupsToken.append(String.join(",", groupUsers.getValue()));
                groupsToken.append(FileHandler.NEW_LINE);
            }

            tokens[0] = usersToken.toString();
            tokens[1] = groupsToken.toString();
        }

        return tokens;
    }

    /**
     * Deploy datasources.
     * @param container Geronimo container.
     * @throws Exception If anything goes wrong.
     */
    public void deployDatasources(InstalledLocalContainer container) throws Exception
    {
        GeronimoInstalledLocalDeployer deployer = new GeronimoInstalledLocalDeployer(container);

        for (DataSource datasource : getDataSources())
        {
            Map<String, String> replacements = new HashMap<String, String>();
            replacements.put("id", datasource.getId());
            replacements.put("dependencies",
                GeronimoUtils.getGeronimoExtraClasspathDependiesXML(container));
            replacements.put("jndiLocation", datasource.getJndiLocation());
            replacements.put("driverClass", datasource.getDriverClass());
            replacements.put("password", datasource.getPassword());
            replacements.put("username", datasource.getUsername());
            replacements.put("url", datasource.getUrl());
            if (datasource.getTransactionSupport() == TransactionSupport.NO_TRANSACTION)
            {
                replacements.put("transactionSupport", "no-transaction");
            }
            else if (datasource.getTransactionSupport() == TransactionSupport.LOCAL_TRANSACTION)
            {
                replacements.put("transactionSupport", "local-transaction");
            }
            else if (datasource.getTransactionSupport() == TransactionSupport.XA_TRANSACTION)
            {
                replacements.put("transactionSupport", "xa-transaction");
            }
            else
            {
                throw new ContainerException("Unknown transaction support type: "
                    + datasource.getTransactionSupport());
            }

            File target = new File(getHome(),
                "var/temp/cargo-datasource-" + datasource.getId() + ".xml");
            getResourceUtils().copyResource(RESOURCE_PATH + "geronimo/DataSourceTemplate.xml",
                target, replacements, StandardCharsets.UTF_8);

            deployer.deployRar(
                "org.codehaus.cargo.datasource/" + datasource.getId() + "/1.0/car", target);
        }
    }
}
