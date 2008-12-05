/* 
 * ========================================================================
 * 
 * Copyright 2004-2006 Vincent Massol.
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
package org.codehaus.cargo.container.tomcat;

import org.apache.tools.ant.types.FilterChain;
import org.apache.tools.ant.util.FileUtils;
import org.codehaus.cargo.container.Container;
import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.tomcat.internal.AbstractTomcatStandaloneLocalConfiguration;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Tomcat standalone {@link org.codehaus.cargo.container.spi.configuration.ContainerConfiguration} 
 * implementation.
 *  
 * @version $Id$
 */
public class Tomcat3xStandaloneLocalConfiguration extends AbstractTomcatStandaloneLocalConfiguration
{
    /**
     * {@inheritDoc}
     * @see AbstractTomcatStandaloneLocalConfiguration#AbstractTomcatStandaloneLocalConfiguration(String)
     */
    public Tomcat3xStandaloneLocalConfiguration(String dir)
    {
        super(dir);
        setProperty(TomcatPropertySet.AJP_PORT, "8009");
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.spi.configuration.AbstractLocalConfiguration#configure(LocalContainer)
     */
    public void doConfigure(LocalContainer container) throws Exception
    {
        FilterChain filterChain = createTomcatFilterChain();

        setupConfigurationDir();
        
        setupConfFiles(container, filterChain);
        String confDir = getFileHandler().append(getHome(), "conf");
        String usersDir = getFileHandler().createDirectory(confDir, "users");
        getResourceUtils().copyResource(RESOURCE_PATH + container.getId() + "/tomcat-users.xml",
            getFileHandler().append(usersDir, "tomcat-users.xml"), getFileHandler(), filterChain);
        
        setupWebApps(container);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.codehaus.cargo.container.spi.configuration.AbstractTomcatStandaloneLocalConfiguration#getConfFiles()
     */
    protected Set getConfFiles()
    {
        Set confFiles = new HashSet();
        confFiles.add("server.xml");
        confFiles.add("modules.xml");
        confFiles.add("apps.xml");
        return confFiles;
    }

    
    /**
     * Setup the web apps directory and deploy applications.
     *
     * @param container the container to configure
     */
    private void setupWebApps(Container container)
    {
        try 
        {
            FileUtils fileUtils = FileUtils.newFileUtils();

            // Create a webapps directory for automatic deployment of WARs 
            // dropped inside.
            String appDir = getFileHandler().createDirectory(getHome(), "webapps");

            // Check validity of deployables
            Iterator it = getDeployables().iterator();
            while (it.hasNext())
            {
                Deployable deployable = (Deployable) it.next();

                // It seems there is a problem with Tomcat 3.x when deploying
                // a WAR file defined by conf/apps-*.xml file and this file
                // is not located in webapps/.
                // Thus we copy all WAR files into webapps/.
                if ((deployable.getType() == DeployableType.WAR)
                    && !((WAR) deployable).isExpandedWar())
                {
                    String appName = getFileHandler().getName(deployable.getFile());
                    fileUtils.copyFile(new File(deployable.getFile()).getAbsolutePath(),
                        getFileHandler().append(appDir, appName), null, true);                    
                }
                else if (deployable.getType() != DeployableType.WAR)
                {
                    throw new ContainerException("Only WAR archives are "
                        + "supported for deployment in Tomcat. Got ["
                        + deployable.getFile() + "]");
                }
            }
            
            // Deploy the CPC (Cargo Ping Component) to the webapps directory
            getResourceUtils().copyResource(RESOURCE_PATH + "cargocpc.war",
                getFileHandler().append(appDir, "cargocpc.war"), getFileHandler());

        }
        catch (Exception e)
        {
            throw new ContainerException("Failed to create a " + container.getName() 
                + " container configuration", e);
        }
    }
    
    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.spi.configuration.AbstractStandaloneLocalConfiguration#createFilterChain()
     */
    private FilterChain createTomcatFilterChain()
    {
        FilterChain filterChain = getFilterChain();

        // Add logging property tokens

        String logEventsEnabled = "false";
        String logLevel = getPropertyValue(GeneralPropertySet.LOGGING);
        if (logLevel.equalsIgnoreCase("error")) 
        {
            logEventsEnabled = "true"; 
        }
        getAntUtils().addTokenToFilterChain(filterChain, "tomcat3x.logEvents.enabled",
            logEventsEnabled);

        // Add AJP connector port token
        getAntUtils().addTokenToFilterChain(filterChain, TomcatPropertySet.AJP_PORT,
            getPropertyValue(TomcatPropertySet.AJP_PORT));

        getAntUtils().addTokenToFilterChain(filterChain, "tomcat3x.logging.level", 
            getTomcatLoggingLevel(getPropertyValue(GeneralPropertySet.LOGGING)));

        // Add token filters for authenticated users
        getAntUtils().addTokenToFilterChain(filterChain, "tomcat.users", getSecurityToken());
        
        // Add webapp contexts in order to explicitely point to where the
        // expanded wars are located.
        StringBuffer webappTokenValue = new StringBuffer(" ");
        
        Iterator it = getDeployables().iterator();
        while (it.hasNext())
        {
            WAR deployable = (WAR) it.next();

            if (deployable.isExpandedWar())
            {
                webappTokenValue.append("<Context path=\"");
                webappTokenValue.append("/" + deployable.getContext());
                webappTokenValue.append("\" docBase=\"");
                webappTokenValue.append(deployable.getFile());
                webappTokenValue.append("\" debug=\"");
                webappTokenValue.append(getTomcatLoggingLevel(
                    getPropertyValue(GeneralPropertySet.LOGGING)));
                webappTokenValue.append("\"/>");
            }
        }
        
        getAntUtils().addTokenToFilterChain(filterChain,
            "tomcat.webapps", webappTokenValue.toString());
        
        return filterChain;
    }

    /**
     * Translate Cargo logging levels into Tomcat logging levels.
     *
     * @param cargoLoggingLevel Cargo logging level
     * @return the corresponding Tomcat logging level
     */
    private String getTomcatLoggingLevel(String cargoLoggingLevel)
    {
        String level;
        
        if (cargoLoggingLevel.equalsIgnoreCase("low"))
        {
            level = "FATAL";
        }
        else if (cargoLoggingLevel.equalsIgnoreCase("medium"))
        {
            level = "WARNING";
        }
        else
        {
            level = "DEBUG";
        }
        
        return level;
    }

    /**
     * {@inheritDoc}
     * @see Object#toString()
     */
    public String toString()
    {
        return "Tomcat 3.x Standalone Configuration";
    }
}
