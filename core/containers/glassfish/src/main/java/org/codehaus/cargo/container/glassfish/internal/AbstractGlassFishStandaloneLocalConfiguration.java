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
package org.codehaus.cargo.container.glassfish.internal;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.FileConfig;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.glassfish.GlassFishPropertySet;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.RemotePropertySet;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.container.spi.configuration.AbstractStandaloneLocalConfiguration;
import org.codehaus.cargo.util.CargoException;

/**
 * GlassFish standalone local configuration.
 */
public abstract class AbstractGlassFishStandaloneLocalConfiguration
    extends AbstractStandaloneLocalConfiguration
{

    /**
     * Creates the local configuration object.
     * 
     * @param home The work directory where files needed to run GlassFish will be created.
     */
    public AbstractGlassFishStandaloneLocalConfiguration(String home)
    {
        super(home);

        // default properties
        this.setProperty(RemotePropertySet.USERNAME, "admin");
        this.setProperty(RemotePropertySet.PASSWORD, "adminadmin");
        this.setProperty(GeneralPropertySet.HOSTNAME, "localhost");
        this.setProperty(GlassFishPropertySet.REMOVE_DEFAULT_DATASOURCE, "true");
        this.setProperty(GlassFishPropertySet.ADMIN_PORT, "4848");
        this.setProperty(GlassFishPropertySet.JMS_PORT, "7676");
        this.setProperty(GlassFishPropertySet.IIOP_PORT, "3700");
        this.setProperty(GlassFishPropertySet.HTTPS_PORT, "8181");
        this.setProperty(GlassFishPropertySet.IIOPS_PORT, "3820");
        this.setProperty(GlassFishPropertySet.IIOP_MUTUAL_AUTH_PORT, "3920");
        this.setProperty(GlassFishPropertySet.JMX_ADMIN_PORT, "8686");
        this.setProperty(GlassFishPropertySet.DEBUGGER_PORT, "9009");
        this.setProperty(GlassFishPropertySet.OSGI_SHELL_PORT, "6666");
        this.setProperty(GlassFishPropertySet.DOMAIN_NAME, "cargo-domain");
        this.setProperty(GlassFishPropertySet.DEBUG_MODE, "false");

        // ServletPropertySet.PORT default set to 8080 by the super class
    }

    @Override
    public void verify()
    {
        // If portBase is specified we override settings for various ports accordingly.
        // It is neccessary that depolyer has valid admin and http ports.
        String portBase = this.getPropertyValue(GlassFishPropertySet.PORT_BASE);
        if (portBase != null && !portBase.trim().isEmpty())
        {
            try
            {
                int base = Integer.parseInt(portBase);
                this.setProperty(ServletPropertySet.PORT, Integer.toString(base + 80));
                this.setProperty(GlassFishPropertySet.ADMIN_PORT, Integer.toString(base + 48));
                this.setProperty(GlassFishPropertySet.JMS_PORT, Integer.toString(base + 76));
                this.setProperty(GlassFishPropertySet.IIOP_PORT, Integer.toString(base + 37));
                this.setProperty(GlassFishPropertySet.HTTPS_PORT, Integer.toString(base + 81));
                this.setProperty(GlassFishPropertySet.IIOPS_PORT, Integer.toString(base + 38));
                this.setProperty(GlassFishPropertySet.IIOP_MUTUAL_AUTH_PORT,
                        Integer.toString(base + 39));
                this.setProperty(GlassFishPropertySet.JMX_ADMIN_PORT, Integer.toString(base + 86));
                this.setProperty(GlassFishPropertySet.DEBUGGER_PORT, Integer.toString(base + 9));
                this.setProperty(GlassFishPropertySet.OSGI_SHELL_PORT, Integer.toString(base + 66));
            }
            catch (NumberFormatException e)
            {
                throw new ContainerException("Invalid portbase value ["
                        + portBase + "]. The portbase value must be an integer", e);
            }
        }

        // We call super at the end, so it has a chance to verify
        // properties we could have overriden.
        super.verify();
    }

    /**
     * Creates a new domain and set up the workspace by invoking the "asadmin" command.
     * 
     * {@inheritDoc}
     */
    @Override
    protected void doConfigure(LocalContainer container) throws Exception
    {
        InstalledLocalContainer installedLocalContainer = (InstalledLocalContainer) container;

        this.getFileHandler().delete(this.getFileHandler().append(this.getHome(),
            this.getPropertyValue(GlassFishPropertySet.DOMAIN_NAME)));

        int exitCode = configureUsingAsAdmin((AbstractGlassFishInstalledLocalContainer) container);

        if (exitCode != 0)
        {
            throw new CargoException("Could not create domain, asadmin command returned exit code "
                + exitCode);
        }

        String domainXmlPath =
            getFileHandler().append(getHome(),
                this.getPropertyValue(GlassFishPropertySet.DOMAIN_NAME) + "/config/domain.xml");

        String domainXml = getFileHandler().readTextFile(domainXmlPath, StandardCharsets.UTF_8);

        Map<String, String> domainXmlReplacements = new HashMap<String, String>();

        String javaHome = this.getPropertyValue(GeneralPropertySet.JAVA_HOME);
        if (javaHome != null)
        {
            if ("jre".equals(getFileHandler().getName(javaHome)))
            {
                javaHome = getFileHandler().getParent(javaHome);
            }

            domainXmlReplacements.put(
                "</config>",
                "  <system-property name='com.sun.aas.javaRoot' value='"
                    + javaHome.replace("&", "&amp;") + "'/>\n    </config>");

            if (!domainXml.contains(" java-home="))
            {
                /*
                 * The java-home attribute is not explicitly set in GlassFish 3.x and as per their
                 * docs should default to the above property. Strangely though, it requires to
                 * explicitly set the attribute for the desired java home location to take effect.
                 */
                domainXmlReplacements.put("<java-config ",
                    "<java-config java-home='${com.sun.aas.javaRoot}' ");
            }
        }

        StringBuilder jvmOptions = new StringBuilder();

        String jvmArgs = this.getPropertyValue(GeneralPropertySet.JVMARGS);
        if (jvmArgs != null)
        {
            List<String> jvmArgsList = new ArrayList<String>();
            Matcher m = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(jvmArgs);
            while (m.find())
            {
                jvmArgsList.add(m.group(1).replace("\"", ""));
            }
            for (String jvmArg : jvmArgsList)
            {
                if (jvmArg.startsWith("-Xmx"))
                {
                    domainXmlReplacements.put("-Xmx512m", jvmArg);
                }
                else if (jvmArg.startsWith("-XX:MaxPermSize"))
                {
                    domainXmlReplacements.put("-XX:MaxPermSize=192m", jvmArg);
                }
                else
                {
                    jvmOptions.append("<jvm-options>" + xmlEscape(jvmArg) + "</jvm-options>\n");
                }
            }
        }

        if (installedLocalContainer.getSystemProperties() != null)
        {
            for (Map.Entry<String, String> systemProperty
                : installedLocalContainer.getSystemProperties().entrySet())
            {
                jvmOptions.append("<jvm-options>-D" + systemProperty.getKey()
                    + "=" + xmlEscape(systemProperty.getValue()) + "</jvm-options>\n");
            }
        }

        jvmOptions.append("</java-config>");
        domainXmlReplacements.put("</java-config>", jvmOptions.toString());

        this.replaceInFile(this.getPropertyValue(GlassFishPropertySet.DOMAIN_NAME)
            + "/config/domain.xml", domainXmlReplacements, StandardCharsets.UTF_8);

        // schedule cargocpc for deployment
        String cpcWar = this.getFileHandler().append(this.getHome(), "cargocpc.war");
        this.getResourceUtils().copyResource(RESOURCE_PATH + "cargocpc.war", new File(cpcWar));
        this.getDeployables().add(new WAR(cpcWar));

        InstalledLocalContainer installedContainer = (InstalledLocalContainer) container;
        String[] classPath = installedContainer.getExtraClasspath();
        if (classPath != null)
        {
            String toDir = this.getPropertyValue(GlassFishPropertySet.DOMAIN_NAME) + "/lib";

            for (String path : classPath)
            {
                FileConfig fc = new FileConfig();
                fc.setFile(path);
                fc.setToDir(toDir);
                setFileProperty(fc);
            }
        }
    }

    /**
     * Returns a system property value string.
     * 
     * @param key Key to look for.
     * @return Associated value.
     */
    protected String getPropertyValueString(String key)
    {
        String value = this.getPropertyValue(key);
        return key.substring("cargo.glassfish.".length()) + '=' + value;
    }

    /**
     * Performs escaping of special XML characters.<br><br>
     * @see https://codehaus-cargo.atlassian.net/browse/CARGO-1190
     * @param string String to escape
     * @return XML escaped <code>string</code>
     */
    private String xmlEscape(String string)
    {
        if (string == null)
        {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < string.length(); i++)
        {
            char ch = string.charAt(i);
            switch (ch)
            {
                case '<':
                    sb.append("&lt;");
                    break;
                case '>':
                    sb.append("&gt;");
                    break;
                case '&':
                    sb.append("&amp;");
                    break;
                default:
                    if (ch < 0x20 || ch > 0x7e)
                    {
                        sb.append("&#" + (int) ch + ";");
                    }
                    else
                    {
                        sb.append(ch);
                    }
            }
        }
        return sb.toString();
    }

    /**
     * Configures the domain using <code>asadmin</code>.
     * @param abstractGlassFishInstalledLocalContainer GlassFish container.
     * @return Whatever <code>asadmin</code> returned.
     */
    protected abstract int configureUsingAsAdmin(
        AbstractGlassFishInstalledLocalContainer abstractGlassFishInstalledLocalContainer);

}
