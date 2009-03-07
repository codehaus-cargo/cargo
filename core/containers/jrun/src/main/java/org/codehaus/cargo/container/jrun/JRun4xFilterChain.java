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
import java.util.Iterator;

import org.apache.tools.ant.filters.ReplaceTokens;
import org.apache.tools.ant.filters.ReplaceTokens.Token;
import org.apache.tools.ant.types.FilterChain;
import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.container.property.User;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

/**
 * JRun {@link FilterChain} for {@link StandaloneLocalConfiguration} implementation.
 *
 * @version $Id: JRun4xFilterChain.java rconnolly $
 */
public class JRun4xFilterChain extends FilterChain
{
    
    /**
     * The {@link InstalledLocalContainer} this {@link FilterChain} is used by.
     */
    private InstalledLocalContainer jrunContainer;

    /**
     * The {@link LocalConfiguration} this {@link FilterChain} is used by.
     */
    private LocalConfiguration configuration;

    
    /**
     * Sole constructor.
     * @param jrunContainer {@link LocalContainer}
     */
    public JRun4xFilterChain(LocalContainer jrunContainer)
    {
        this.jrunContainer = (InstalledLocalContainer) jrunContainer;
        this.configuration = jrunContainer.getConfiguration();
        this.init();
    }
    
    /**
     * Initializes the {@link ReplaceTokens}s used in this {@link FilterChain}.
     */
    private void init()
    {
        ReplaceTokens jrunTokens = new ReplaceTokens();
        jrunTokens.addConfiguredToken(createServerNameToken());
        jrunTokens.addConfiguredToken(createPortToken());
        jrunTokens.addConfiguredToken(createLoggingToken());
        jrunTokens.addConfiguredToken(createUserToken());
        
        this.addReplaceTokens(jrunTokens);
        this.addReplaceTokens(createJvmConfigTokens());
    }
    
    
    /**
     * Creates tokens for the JRun Server Name.
     *
     * @return serverName token
     */
    private ReplaceTokens.Token createServerNameToken()
    {
        String serverName = getPropertyValue(JRun4xPropertySet.SERVER_NAME);
        ReplaceTokens.Token tokenServerName = new ReplaceTokens.Token();
        tokenServerName.setKey(JRun4xPropertySet.SERVER_NAME);
        tokenServerName.setValue(serverName);
        
        return tokenServerName;
    }
    
    /**
     * Creates the port token for inclusion in jrun.xml .
     *
     * @return port token
     */
    private ReplaceTokens.Token createPortToken()
    {
        String port = getPropertyValue(ServletPropertySet.PORT);
        // default to 8100
        if (port == null)
        {
            port = JRun4xPropertySet.DEFAULT_PORT;
        }

        ReplaceTokens.Token tokenPort = new ReplaceTokens.Token();
        tokenPort.setKey(ServletPropertySet.PORT);
        tokenPort.setValue(port);
        
        return tokenPort;
    }
    
    /**
     * Creates the logging token for inclusion in jrun.xml.
     * @return the logging token for inclusion in jrun.xml.
     */
    private ReplaceTokens.Token createLoggingToken()
    {
        String errorEnabled = "true";
        String warningEnabled = "true";
        String infoEnabled = "true";
        String debugEnabled = "false";
        
        String cargoLogLevel = getPropertyValue(GeneralPropertySet.LOGGING);
        if ("low".equalsIgnoreCase(cargoLogLevel))
        {
            warningEnabled = "false";
        }
        if ("high".equalsIgnoreCase(cargoLogLevel))
        {
            debugEnabled = "true";
        }
        StringBuffer logging = new StringBuffer();
        logging.append("<!-- cargo logging level: " + cargoLogLevel + " --> \n");
        logging.append("<attribute name=\"errorEnabled\">" + errorEnabled + "</attribute>\n");
        logging.append("<attribute name=\"warningEnabled\">" + warningEnabled + "</attribute>\n");
        logging.append("<attribute name=\"infoEnabled\">" + infoEnabled + "</attribute>\n");
        logging.append("<attribute name=\"debugEnabled\">" + debugEnabled + "</attribute>\n");
        
        ReplaceTokens.Token tokenLogging = new ReplaceTokens.Token();
        tokenLogging.setKey("cargo.jrun.logging");
        tokenLogging.setValue(logging.toString());
        
        return tokenLogging;
    }
    
    /**
     * Creates tokens needed for the jvm.config file.
     * @return classpath the {@link ReplaceTokens} needed for the jvm.config file.
     */
    private ReplaceTokens createJvmConfigTokens()
    {
        ReplaceTokens.Token tokenJavaHome = createJavaHomeToken();
        ReplaceTokens.Token tokenClassPath = createClassPathToken();
        ReplaceTokens.Token tokenVmArgs = createVmArgsToken();

        ReplaceTokens replaceConfig = new ReplaceTokens();
        replaceConfig.addConfiguredToken(tokenJavaHome);
        replaceConfig.addConfiguredToken(tokenClassPath);
        replaceConfig.addConfiguredToken(tokenVmArgs);
        
        return replaceConfig;
    }
    
    /**
     * Creates the java.home token used in the jvm.config file.
     * @return the java.home token used in the jvm.config file.
     */
    private Token createJavaHomeToken()
    {
        // the java.home token needs to be formatted just right or jrun fails to start.
        String javaHome = getPropertyValue(GeneralPropertySet.JAVA_HOME);

        ReplaceTokens.Token tokenJavaHome = new ReplaceTokens.Token();
        tokenJavaHome.setKey("jrun.java.home");
        tokenJavaHome.setValue(javaHome.replace('\\', '/'));

        return tokenJavaHome;
    }
    
    /**
     * Creates the classpath token for the jvm.config file.
     * @return the classpath token for the jvm.config file.
     */
    private Token createClassPathToken()
    {
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
        
        return tokenClasspath;
    }
    
    /**
     * Creates the VM args token for the jvm.config file.
     * @return the VM args token for the jvm.config file.
     */
    private Token createVmArgsToken()
    {
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
        
        return tokenVmArgs;
    }
    
    /**
     * @return an Ant filter token containing all the user-defined users
     */
    protected ReplaceTokens.Token createUserToken()
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
        
        return tokenUsers;
    }
    
    
    /**
     * Convenience method for retrieving the {@link LocalConfiguration}'s property value.
     * @param propertyName the property we want the value of.
     * @return the value of the named property.
     */
    private String getPropertyValue(String propertyName)
    {
        return configuration.getPropertyValue(propertyName); 
    }

}
