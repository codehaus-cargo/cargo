/* 
 * ========================================================================
 * 
 * Copyright 2004-2005 Vincent Massol.
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

import org.apache.tools.ant.types.FilterChain;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.container.property.User;
import org.codehaus.cargo.container.spi.configuration.AbstractStandaloneLocalConfiguration;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

/**
 * Common implementation of standalone
 * {@link org.codehaus.cargo.container.configuration.Configuration} for both Tomcat and Catalina.
 *  
 * @version $Id$
 */
public abstract class AbstractTomcatStandaloneLocalConfiguration 
    extends AbstractStandaloneLocalConfiguration
{
    /**
     * Capability of the Tomcat standalone configuration.
     */
    private static ConfigurationCapability capability = 
        new TomcatStandaloneLocalConfigurationCapability();

    /**
     * {@inheritDoc}
     * @see AbstractStandaloneLocalConfiguration#AbstractStandaloneLocalConfiguration(String)
     */
    public AbstractTomcatStandaloneLocalConfiguration(String dir)
    {
        super(dir);
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
     * @return an Ant filter token containing all the user-defined users
     */
    protected String getSecurityToken()
    {
        StringBuffer token = new StringBuffer(" ");

        // Add token filters for authenticated users
        if (getPropertyValue(ServletPropertySet.USERS) != null)
        {
            Iterator users = User.parseUsers(getPropertyValue(ServletPropertySet.USERS)).iterator();
            while (users.hasNext())
            {
                User user = (User) users.next();
                token.append("<user ");
                token.append("name=\"" + user.getName() + "\" ");
                token.append("password=\"" + user.getPassword() + "\" ");

                token.append("roles=\"");
                Iterator roles = user.getRoles().iterator();
                while (roles.hasNext())
                {
                    String role = (String) roles.next();
                    token.append(role);
                    if (roles.hasNext())
                    {
                        token.append(',');
                    }
                }
                token.append("\"/>");
            }
        }

        return token.toString();
    }

    /**
     * copy files to the conf directory, replacing tokens based on the filterchain parameter.
     * 
     * @param container - type of container configuration we are using.
     * @param filterChain - holds tokenization details
     * @throws IOException - if we cannot copy a file to the 'conf' directory
     */
    protected void setupConfFiles(LocalContainer container, FilterChain filterChain)
        throws IOException
    {
        String confDir = getFileHandler().createDirectory(getHome(), "conf");
        Iterator confFiles = getConfFiles().iterator();
        while (confFiles.hasNext())
        {
            String file = (String) confFiles.next();
            getResourceUtils().copyResource(RESOURCE_PATH + container.getId() + "/" + file,
                getFileHandler().append(confDir, file), getFileHandler(), filterChain);
        }
    }
    
    /**
     * files that should be copied to the conf directory for the server to operate.
     * 
     * @return set of filenames to copy upon doConfigure
     */
    protected abstract Set getConfFiles();
}
