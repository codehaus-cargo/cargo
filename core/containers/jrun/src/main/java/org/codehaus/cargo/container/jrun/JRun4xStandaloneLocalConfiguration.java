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
package org.codehaus.cargo.container.jrun;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.tools.ant.filters.ReplaceTokens;
import org.apache.tools.ant.types.FilterChain;
import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.configuration.builder.ConfigurationBuilder;
import org.codehaus.cargo.container.configuration.entry.DataSource;
import org.codehaus.cargo.container.configuration.entry.Resource;
import org.codehaus.cargo.container.configuration.entry.ResourceSupport;
import org.codehaus.cargo.container.jrun.internal.JRun4xConfigurationBuilder;
import org.codehaus.cargo.container.jrun.internal.JRun4xStandaloneLocalConfigurationCapability;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.container.property.User;
import org.codehaus.cargo.container.spi.configuration.builder.AbstractStandaloneLocalConfigurationWithXMLConfigurationBuilder;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;


/**
 * JRun standalone {@link org.codehaus.cargo.container.spi.configuration.ContainerConfiguration}
 * implementation.
 *
 * @version $Id: JRun4xStandaloneLocalConfiguration.java $
 */
public class JRun4xStandaloneLocalConfiguration extends
    AbstractStandaloneLocalConfigurationWithXMLConfigurationBuilder implements ResourceSupport 
{
    
    /**
     * Capability of the JRun4x standalone local configuration.
     */
    private static ConfigurationCapability capability = 
        new JRun4xStandaloneLocalConfigurationCapability();
    
    /**
     * {@inheritDoc}
     * @see AbstractCatalinaStandaloneLocalConfiguration#AbstractCatalinaStandaloneLocalConfiguration(String)
     */
    public JRun4xStandaloneLocalConfiguration(String dir)
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
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.spi.configuration.AbstractLocalConfiguration#configure(LocalContainer)
     */
    protected void doConfigure(LocalContainer container) throws Exception
    { 
        setupConfigurationDir();
        
        // set default server instance name if not set
        if (getPropertyValue(JRun4xPropertySet.SERVER_NAME) == null)
        {
            setProperty(JRun4xPropertySet.SERVER_NAME, JRun4xPropertySet.DEFAULT_SERVER_NAME);
        }

        filterResources(container);
        
        String to = getHome();
        String from = ((InstalledLocalContainer) container).getHome();        
        // copy required server files
        List requiredFiles = getRequiredFiles();
        for (int i = 0; i < requiredFiles.size(); i++)
        {
            String path = (String) requiredFiles.get(i);
            getFileHandler().copyFile(from + "/" + path, to + "/" + path);
        }
                
        // copy hot fix jar for spaces in path if present.
        File spaceHotFix = new File(from + "/servers/lib/54101.jar");
        if (spaceHotFix.exists())
        {
            getFileHandler().copyFile(spaceHotFix.getAbsolutePath(),
                to + "/servers/lib/54101.jar");
        }                

        // get the required binary
        if (System.getProperty("os.name").toLowerCase().indexOf("windows") != -1)
        {
            getFileHandler().copyFile(from + "/bin/jrun.exe", to + "/bin/jrun.exe");
        }
        else
        {
            getFileHandler().copyFile(from + "/bin/jrun", to + "/bin/jrun");
        }
        
        InstalledLocalContainer jrunContainer = (InstalledLocalContainer) container;
        JRun4xInstalledLocalDeployer deployer = new JRun4xInstalledLocalDeployer(jrunContainer);
        deployer.deploy(getDeployables());

        // Deploy the CPC (Cargo Ping Component) to the webapps directory.
        getResourceUtils().copyResource(RESOURCE_PATH + "cargocpc.war",
            new File(deployer.getDeployableDir(), "cargocpc.war"));
    }
    
    /**
     * The list of required files needed to start up JRun4.
     * @return the list of files required to start JRun4.
     */
    private List getRequiredFiles()
    {
        String serverName = getPropertyValue(JRun4xPropertySet.SERVER_NAME);
        String serverInfDirectory = "/servers/" + serverName + "/SERVER-INF/";

        List files = new ArrayList();
        files.add(serverInfDirectory + "auth.config");
        files.add(serverInfDirectory + "connector.properties");
        files.add(serverInfDirectory + "default-web.xml");
        files.add(serverInfDirectory + "jndi.properties");
        files.add(serverInfDirectory + "jrun-dtd-mappings.xml");
        files.add(serverInfDirectory + "jrun-jms.xml");
        files.add(serverInfDirectory + "jrun-resources.xml");
        files.add(serverInfDirectory + "jrun-security-policy.xml");

        files.add("/lib/jrun.jar");
        files.add("/lib/flashgateway.ear");
        files.add("/lib/jrun-comp.ear");
        files.add("/lib/security.properties");
        files.add("/lib/license.properties");
        files.add("/lib/jrun.policy");
        files.add("/lib/mime.types");

        return files;
    }
    
        
    /**
     * Filter configuration files.
     * @param container the current {@link LocalContainer}.
     * @throws IOException if something goes wrong with the file processing.
     */
    private void filterResources(LocalContainer container) throws IOException
    {
        FilterChain chain = getFilterChain();
        
        String to = getHome();
        String libDir = getFileHandler().createDirectory(to, "lib");
        String resourcePath = RESOURCE_PATH + container.getId();
        
        // filter server name in servers.xml
        chain.addReplaceTokens(createServerNameToken());
        getResourceUtils().copyResource(resourcePath + "/servers.xml", 
            new File(libDir, "/servers.xml"), chain);
        
        // filter VM config in jvm.config
        getFileHandler().createDirectory(to, "bin");
        chain.addReplaceTokens(createJvmConfigTokens(container));
        getResourceUtils().copyResource(resourcePath + "/jvm.config", 
            new File(to + "/bin/jvm.config"), chain);

        
        String serverName = getPropertyValue(JRun4xPropertySet.SERVER_NAME);
        String serverInf = "servers/" + serverName + "/SERVER-INF";
        String serverInfDir = getFileHandler().createDirectory(getHome(), serverInf);
        
        // filter port in jrun.xml
        chain.addReplaceTokens(createPortToken());
        getResourceUtils().copyResource(resourcePath + "/jrun.xml",
            new File(serverInfDir, "/jrun.xml"), chain);
        
        // filter users in jrun-users.xml
        chain.addReplaceTokens(createUserToken());
        getResourceUtils().copyResource(resourcePath + "/jrun-users.xml",
            new File(serverInfDir, "/jrun-users.xml"), chain);
        
        
    }
     
    /**
     * Creates tokens for the JRun Server Name.
     *
     * @return serverName token
     */
    private ReplaceTokens createServerNameToken()
    {
        ReplaceTokens.Token tokenServerName = new ReplaceTokens.Token();
        tokenServerName.setKey(JRun4xPropertySet.SERVER_NAME);

        String serverName = getPropertyValue(JRun4xPropertySet.SERVER_NAME);
        tokenServerName.setValue(serverName);
        ReplaceTokens replaceHostname = new ReplaceTokens();
        replaceHostname.addConfiguredToken(tokenServerName);
        
        return replaceHostname;
    }
    
    /**
     * Creates the port token.
     *
     * @return port token
     */
    private ReplaceTokens createPortToken()
    {
        ReplaceTokens.Token tokenPort = new ReplaceTokens.Token();
        tokenPort.setKey(ServletPropertySet.PORT);

        String port = getPropertyValue(ServletPropertySet.PORT);
        // default to 8100
        if (port == null)
        {
            port = JRun4xPropertySet.DEFAULT_PORT;
        }

        tokenPort.setValue(port);
        ReplaceTokens replacePort = new ReplaceTokens();
        replacePort.addConfiguredToken(tokenPort);
        
        return replacePort;
    }
    
    /**
     * Creates classpath token for the jvm.config file needed for extra classpath entries.
     * @param container  the current {@link LocalContainer} instance.
     * @return classpath token
     */
    private ReplaceTokens createJvmConfigTokens(LocalContainer container)
    {
        // the java.home token needs to be formatted just right or jrun fails to start.
        ReplaceTokens.Token tokenJavaHome = new ReplaceTokens.Token();
        tokenJavaHome.setKey("jrun.java.home");
        String javaHome = 
            container.getConfiguration().getPropertyValue(GeneralPropertySet.JAVA_HOME);
        tokenJavaHome.setValue(javaHome.replace('\\', '/'));
        
        InstalledLocalContainer jrunContainer = (InstalledLocalContainer) container;
        
        // classpath token
        StringBuffer sb = new StringBuffer();
        sb.append(jrunContainer.getHome() + "/servers/lib,");
        sb.append(jrunContainer.getHome() + "/lib/macromedia_drivers.jar,");
        sb.append(jrunContainer.getHome() + "/lib/webservices.jar");
        if (jrunContainer.getExtraClasspath().length > 0)
        {
            sb.append(",");
            String[] extraPaths = jrunContainer.getExtraClasspath();
            for (int i = 0; i < extraPaths.length; i++)
            {
                sb.append(extraPaths[i].replace('\\', '/'));
                if (i < (extraPaths.length - 1))
                {
                    sb.append(",");
                }
            }
        }
        ReplaceTokens.Token tokenClasspath = new ReplaceTokens.Token();
        tokenClasspath.setKey(JRun4xPropertySet.JRUN_CLASSPATH);
        tokenClasspath.setValue(sb.toString());

        // vm args token
        StringBuffer jvmArgs = new StringBuffer();
        File hotFixJar = new File(jrunContainer.getHome() + "/servers/lib/54101.jar");
        if (hotFixJar.exists())
        {
            jvmArgs.append("-Djava.rmi.server.RMIClassLoaderSpi=jrunx.util.JRunRMIClassLoaderSpi ");
        }
        jvmArgs.append("-Dsun.io.useCanonCaches=false ");
        jvmArgs.append("-Djmx.invoke.getters=true ");
        jvmArgs.append("-Xms32m ");
        jvmArgs.append("-Xmx128m ");
        
        ReplaceTokens.Token tokenVmArgs = new ReplaceTokens.Token();
        tokenVmArgs.setKey("jrun.jvm.args");
        tokenVmArgs.setValue(jvmArgs.toString());
        
        
        ReplaceTokens replaceConfig = new ReplaceTokens();
        replaceConfig.addConfiguredToken(tokenClasspath);
        replaceConfig.addConfiguredToken(tokenJavaHome);
        replaceConfig.addConfiguredToken(tokenVmArgs);
        
        return replaceConfig;
    }
    
    /**
     * @return an Ant filter token containing all the user-defined users
     */
    protected ReplaceTokens createUserToken()
    {
        StringBuffer token = new StringBuffer();

        // Add token filters for authenticated users
        if (getPropertyValue(ServletPropertySet.USERS) != null)
        {
            Iterator users = 
                User.parseUsers(getPropertyValue(ServletPropertySet.USERS)).iterator();
            
            while (users.hasNext())
            {
                User user = (User) users.next();
                
                // create user elements
                Element userElement = DocumentHelper.createDocument().addElement("user");
                userElement.addElement("user-name").setText(user.getName());
                userElement.addElement("password").setText(user.getPassword());
                
                token.append(userElement.asXML());
                
                // add role elements
                Iterator roles = user.getRoles().iterator();
                while (roles.hasNext())
                {
                    String role = (String) roles.next();
                    Element roleElement = DocumentHelper.createDocument().addElement("role");
                    roleElement.addElement("role-name").setText(role);
                    roleElement.addElement("user-name").setText(user.getName());

                    token.append(roleElement.asXML());
                }
            }
        }

        ReplaceTokens.Token tokenUsers = new ReplaceTokens.Token();
        tokenUsers.setKey("jrun.users");
        tokenUsers.setValue(token.toString());
        
        ReplaceTokens replaceUsers = new ReplaceTokens();
        replaceUsers.addConfiguredToken(tokenUsers);
        
        return replaceUsers;
    }
    
    /**
     * {@inheritDoc}
     * @see Object#toString()
     */
    public String toString()
    {
        return "JRun 4.x Standalone Configuration";
    }

    /**
     * Gets this configuration's {@link ConfigurationBuilder}.
     * @param container the current Container.
     * @return the {@link ConfigurationBuilder}.
     */
    protected ConfigurationBuilder createConfigurationBuilder(LocalContainer container) 
    {
        return new JRun4xConfigurationBuilder();
    }

    /**
     * The xml namespaces.
     * @return an empty Map. 
     */
    protected Map getNamespaces() 
    {
        return Collections.EMPTY_MAP;
    }

    /**
     * Returns the Xpath for the parent element of the datasource xml.
     * @return the Xpath for the parent element of the datasource xml.
     */
    protected String getXpathForDataSourcesParent() 
    {
        return getResourceXPath();
    }

    /**
     * Gets the file to insert Datasource Configuraton into.
     * @param ds the Datasource instance.
     * @param container the current Container.
     * @return the file to insert Datasource Configuraton into.
     */
    protected String getOrCreateDataSourceConfigurationFile(DataSource ds, 
            LocalContainer container) 
    {
        return getResourceFile(container);
    }

    /**
     * Gets the file to insert Resource Configuraton into.
     * @param resource the Resource definition.
     * @param container the current Container.
     * @return the file to insert Resource Configuraton into.
     */
    protected String getOrCreateResourceConfigurationFile(Resource resource, 
            LocalContainer container) 
    {
        return getResourceFile(container);
    }

    /**
     * The XPath of the parent Element of resource configuration.
     * @return XPath of the parent Element of resource configuration.
     */
    protected String getXpathForResourcesParent() 
    {
        return getResourceXPath();
    }
    
    /**
     * Get the file that Datasource and Resource configuration is defined in.
     * @param container the current Container.
     * @return  the path to the configuration file that holds Resource information.
     */
    private String getResourceFile(LocalContainer container)
    {
        String serverName = 
            container.getConfiguration().getPropertyValue(JRun4xPropertySet.SERVER_NAME);

        return getFileHandler().append(getHome(), 
                "servers/" + serverName + "/SERVER-INF/jrun-resources.xml");
    }

    /**
     * Returns the parent element XPath for Resource configuration.
     * @return the parent element XPath for Resource configuration. 
     */
    private String getResourceXPath()
    {
        return "//jrun-resources";
    }

}
