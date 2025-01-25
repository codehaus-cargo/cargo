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
package org.codehaus.cargo.container.jrun;

import java.io.File;
import java.util.HashMap;

import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.LoggingLevel;
import org.codehaus.cargo.container.property.User;
import org.codehaus.cargo.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * JRun replacements for {@link JRun4xStandaloneLocalConfiguration} implementation.
 */
public class JRun4xReplacements extends HashMap<String, String>
{

    /**
     * The {@link InstalledLocalContainer} these replacements are used by.
     */
    private InstalledLocalContainer jrunContainer;

    /**
     * The {@link LocalConfiguration} these replacements are used by.
     */
    private LocalConfiguration configuration;

    /**
     * XML utilities.
     */
    private XmlUtils xmlUtil;

    /**
     * Sole constructor.
     * @param jrunContainer {@link LocalContainer}
     */
    public JRun4xReplacements(LocalContainer jrunContainer)
    {
        this.jrunContainer = (InstalledLocalContainer) jrunContainer;
        this.configuration = jrunContainer.getConfiguration();
        this.xmlUtil = new XmlUtils(jrunContainer.getFileHandler());

        put("cargo.jrun.logging", createLoggingToken());
        put("jrun.java.home", configuration.getPropertyValue(GeneralPropertySet.JAVA_HOME));
        put(JRun4xPropertySet.JRUN_CLASSPATH, createClassPathToken());
        put("jrun.jvm.args", createVmArgsToken());
        put("jrun.users", createUserToken());
    }

    /**
     * Creates the logging token for inclusion in jrun.xml.
     * @return the logging token for inclusion in jrun.xml.
     */
    private String createLoggingToken()
    {
        String errorEnabled = "true";
        String warningEnabled = "true";
        String infoEnabled = "true";
        String debugEnabled = "false";

        String cargoLogLevel = configuration.getPropertyValue(GeneralPropertySet.LOGGING);
        if (LoggingLevel.LOW.equalsLevel(cargoLogLevel))
        {
            warningEnabled = "false";
            infoEnabled = "false";
        }
        else if (LoggingLevel.HIGH.equalsLevel(cargoLogLevel))
        {
            debugEnabled = "true";
        }
        StringBuilder logging = new StringBuilder();
        logging.append("<!-- cargo logging level: " + cargoLogLevel + " --> \n");
        logging.append("<attribute name=\"errorEnabled\">" + errorEnabled + "</attribute>\n");
        logging.append("<attribute name=\"warningEnabled\">" + warningEnabled + "</attribute>\n");
        logging.append("<attribute name=\"infoEnabled\">" + infoEnabled + "</attribute>\n");
        logging.append("<attribute name=\"debugEnabled\">" + debugEnabled + "</attribute>\n");

        return logging.toString();
    }

    /**
     * Creates the classpath token for the jvm.config file.
     * @return the classpath token for the jvm.config file.
     */
    private String createClassPathToken()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(jrunContainer.getHome() + "/servers/lib,");
        sb.append(jrunContainer.getHome() + "/lib/macromedia_drivers.jar,");
        sb.append(jrunContainer.getHome() + "/lib/webservices.jar");
        if (jrunContainer.getExtraClasspath().length > 0)
        {
            String[] extraPaths = jrunContainer.getExtraClasspath();
            for (int i = 0; i < extraPaths.length; i++)
            {
                sb.append(",");
                sb.append(extraPaths[i].replace('\\', '/'));
            }
        }
        return sb.toString();
    }

    /**
     * Creates the VM args token for the jvm.config file.
     * @return the VM args token for the jvm.config file.
     */
    private String createVmArgsToken()
    {
        StringBuilder jvmArgs = new StringBuilder();
        File hotFixJar = new File(jrunContainer.getHome() + "/servers/lib/54101.jar");
        if (hotFixJar.exists())
        {
            jvmArgs.append("-Djava.rmi.server.RMIClassLoaderSpi=jrunx.util.JRunRMIClassLoaderSpi ");
        }
        jvmArgs.append("-Dsun.io.useCanonCaches=false ");
        jvmArgs.append("-Djmx.invoke.getters=true ");
        jvmArgs.append("-Xms32m ");
        jvmArgs.append("-Xmx128m ");

        return jvmArgs.toString();
    }

    /**
     * @return token containing all the user-defined users
     */
    protected String createUserToken()
    {
        StringBuilder token = new StringBuilder();

        // Add token filters for authenticated users
        if (!configuration.getUsers().isEmpty())
        {
            for (User user : configuration.getUsers())
            {
                // create user elements
                Document document = xmlUtil.createDocument();
                Element userElement = document.createElement("user");
                Element usernameElement =
                    userElement.getOwnerDocument().createElement("user-name");
                usernameElement.setTextContent(user.getName());
                userElement.appendChild(usernameElement);
                Element passwordElement =
                    userElement.getOwnerDocument().createElement("password");
                passwordElement.setTextContent(user.getPassword());
                userElement.appendChild(passwordElement);

                token.append(xmlUtil.toString(userElement));

                // add role elements
                for (String role : user.getRoles())
                {
                    document = xmlUtil.createDocument();
                    Element roleElement = document.createElement("role");
                    Element rolenameElement =
                        roleElement.getOwnerDocument().createElement("role-name");
                    rolenameElement.setTextContent(role);
                    roleElement.appendChild(rolenameElement);
                    usernameElement = roleElement.getOwnerDocument().createElement("user-name");
                    usernameElement.setTextContent(user.getName());
                    roleElement.appendChild(usernameElement);

                    token.append(xmlUtil.toString(roleElement));
                }
            }
        }

        return token.toString();
    }

}
