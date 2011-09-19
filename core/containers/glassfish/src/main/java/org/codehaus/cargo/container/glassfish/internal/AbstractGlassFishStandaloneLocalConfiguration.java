/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2011 Vincent Massol.
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
import java.util.HashMap;
import java.util.Map;

import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.FileConfig;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.glassfish.GlassFishPropertySet;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.RemotePropertySet;
import org.codehaus.cargo.container.spi.configuration.AbstractStandaloneLocalConfiguration;
import org.codehaus.cargo.util.CargoException;
import org.codehaus.cargo.util.DefaultFileHandler;

/**
 * GlassFish standalone local configuration.
 * 
 * @version $Id$
 */
public abstract class AbstractGlassFishStandaloneLocalConfiguration
    extends AbstractStandaloneLocalConfiguration
{

    /**
     * Creates the local configuration object.
     * 
     * @param home The work directory where files needed to run Glassfish will be created.
     */
    public AbstractGlassFishStandaloneLocalConfiguration(String home)
    {
        super(home);

        // default properties
        this.setProperty(RemotePropertySet.USERNAME, "admin");
        this.setProperty(RemotePropertySet.PASSWORD, "adminadmin");
        this.setProperty(GeneralPropertySet.HOSTNAME, "localhost");
        this.setProperty(GlassFishPropertySet.ADMIN_PORT, "4848");
        this.setProperty(GlassFishPropertySet.JMS_PORT, "7676");
        this.setProperty(GlassFishPropertySet.IIOP_PORT, "3700");
        this.setProperty(GlassFishPropertySet.HTTPS_PORT, "8181");
        this.setProperty(GlassFishPropertySet.IIOPS_PORT, "3820");
        this.setProperty(GlassFishPropertySet.IIOP_MUTUAL_AUTH_PORT, "3920");
        this.setProperty(GlassFishPropertySet.JMX_ADMIN_PORT, "8686");
        this.setProperty(GlassFishPropertySet.DOMAIN_NAME, "cargo-domain");

        // ServletPropertySet.PORT default set to 8080 by the super class
    }

    /**
     * Creates a new domain and set up the workspace by invoking the "asadmin" command.
     * 
     * {@inheritDoc}
     */
    @Override
    protected void doConfigure(LocalContainer container) throws Exception
    {
        DefaultFileHandler fileHandler = new DefaultFileHandler();
        fileHandler.delete(this.getHome());

        int exitCode = configureUsingAsAdmin((AbstractGlassFishInstalledLocalContainer) container);

        if (exitCode != 0)
        {
            throw new CargoException("Could not create domain, asadmin command returned exit code "
                + exitCode);
        }

        String domainXmlPath =
            getFileHandler().append(getHome(),
                this.getPropertyValue(GlassFishPropertySet.DOMAIN_NAME) + "/config/domain.xml");

        String domainXml = getFileHandler().readTextFile(domainXmlPath, "UTF-8");

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
                 * The java-home attribute is not explicitly set in Glassfish 3.x and as per their
                 * docs should default to the above property. Strangely though, it requires to
                 * explicitly set the attribute for the desired java home location to take effect.
                 */
                domainXmlReplacements.put("<java-config ",
                    "<java-config java-home='${com.sun.aas.javaRoot}' ");
            }
        }

        String jvmArgs = this.getPropertyValue(GeneralPropertySet.JVMARGS);
        if (jvmArgs != null)
        {
            String xmx = this.getJvmArg(jvmArgs, "-Xmx");
            if (xmx != null)
            {
                domainXmlReplacements.put("-Xmx512m", xmx);
            }

            String maxPermSize = this.getJvmArg(jvmArgs, "-XX:MaxPermSize");
            if (maxPermSize != null)
            {
                domainXmlReplacements.put("-XX:MaxPermSize=192m", maxPermSize);
            }

        }

        this.replaceInFile(this.getPropertyValue(GlassFishPropertySet.DOMAIN_NAME)
            + "/config/domain.xml", domainXmlReplacements, "UTF-8");

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
     * Extracts a JVM argument.
     * 
     * @param jvmArgs JVM arguments list.
     * @param key Key to look for.
     * @return Associated value, null if not found.
     */
    private String getJvmArg(String jvmArgs, String key)
    {
        int startIndex = jvmArgs.indexOf(key);
        if (startIndex == -1)
        {
            return null;
        }
        int endIndex = jvmArgs.indexOf(' ', startIndex);
        if (endIndex == -1)
        {
            endIndex = jvmArgs.length();
        }
        return jvmArgs.substring(startIndex, endIndex);
    }

    /**
     * Configures the domain using <code>asadmin</code>.
     * @param abstractGlassFishInstalledLocalContainer GlassFish container.
     * @return Whatever <code>asadmin</code> returned.
     */
    protected abstract int configureUsingAsAdmin(
        AbstractGlassFishInstalledLocalContainer abstractGlassFishInstalledLocalContainer);

}
