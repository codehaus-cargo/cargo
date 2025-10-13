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
package org.codehaus.cargo.container.jetty;

import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.entry.DataSource;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.util.FileHandler;

/**
 * A deployer for webapps that deploys to a Jetty 12.x installed instance.
 */
public class Jetty12xInstalledLocalDeployer extends Jetty9x10x11xInstalledLocalDeployer
{
    /**
     * Pattern for matching EE version numbers, for matching Jetty configuration XMLs.
     */
    private static final Pattern EE_VERSION_PATTERN = Pattern.compile("ee(\\d+)");

    /**
     * Pattern for matching version numbers, excluding non digits (alpha, dash, etc.).
     */
    private static final Pattern VERSION_NUMBER_PATTERN = Pattern.compile("(\\d+(\\.\\d+)*).*");

    /**
     * {@inheritDoc}
     * @see Jetty9x10x11xInstalledLocalDeployer#Jetty9x10x11xInstalledLocalDeployer(org.codehaus.cargo.container.LocalContainer)
     */
    public Jetty12xInstalledLocalDeployer(LocalContainer container)
    {
        super(container);
    }

    @Override
    protected String createContextXml(WAR war)
    {
        String eeVersion = getContainer().getConfiguration().getPropertyValue(
            JettyPropertySet.DEPLOYER_EE_VERSION);
        Matcher matcher = EE_VERSION_PATTERN.matcher(eeVersion);
        if (!matcher.matches())
        {
            throw new IllegalArgumentException(
                "EE version [" + eeVersion + "] doesn't match " + EE_VERSION_PATTERN);
        }
        int eeConfigureVersion = Integer.parseInt(matcher.group(1));
        String eeConfigure;
        if (eeConfigureVersion == 8)
        {
            eeConfigure = "";
        }
        else
        {
            eeConfigure = "_" + eeConfigureVersion + "_0";
        }

        // As per a .properties file is also required for Jetty 12.x
        // See https://github.com/eclipse/jetty.project/issues/10158 for details
        String contextPropertiesFile = getContextFilename(war, "properties");
        getFileHandler().writeTextFile(
            contextPropertiesFile, "environment=" + eeVersion + FileHandler.NEW_LINE,
                StandardCharsets.UTF_8);

        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\"  encoding=\"UTF-8\"?>\n");
        sb.append("<!DOCTYPE Configure PUBLIC \"-//Jetty//Configure//EN\" "
            + "\"https://eclipse.dev/jetty/configure" + eeConfigure + ".dtd\">\n");
        sb.append(
            "<Configure class=\"org.eclipse.jetty." + eeVersion + ".webapp.WebAppContext\">\n");
        sb.append("  <Set name=\"contextPath\">/" + war.getContext() + "</Set>\n");
        sb.append("  <Set name=\"war\">" + war.getFile() + "</Set>\n");
        sb.append("  <Set name=\"extractWAR\">true</Set>\n");
        sb.append("  <Set name=\"defaultsDescriptor\"><SystemProperty name=\"config.home\" "
            + "default=\".\"/>/etc/webdefault-" + eeVersion + ".xml</Set>\n");
        // Add datasources
        String resourceClassName = getJettyResourceClassname(
            ((Jetty12xInstalledLocalContainer) getContainer()).getVersion(), eeVersion);
        for (DataSource ds : getContainer().getConfiguration().getDataSources())
        {
            sb.append("  <New id=\"" + ds.getId() + "\" class=\"" + resourceClassName + "\">\n");
            sb.append("    <Arg>" + ds.getJndiLocation() + "</Arg>\n");
            sb.append("    <Arg>\n");
            sb.append("      <New class=\"com.mchange.v2.c3p0.ComboPooledDataSource\">\n");
            sb.append("        <Set name=\"driverClass\">" + ds.getDriverClass() + "</Set>\n");
            sb.append("        <Set name=\"jdbcUrl\">" + ds.getUrl() + "</Set>\n");
            sb.append("        <Set name=\"user\">" + ds.getUsername() + "</Set>\n");
            sb.append("        <Set name=\"password\">" + ds.getPassword() + "</Set>\n");
            sb.append("      </New>\n");
            sb.append("    </Arg>\n");
            sb.append("  </New>\n");
        }
        sb.append(getExtraClasspathXmlFragment(war));
        sb.append(getSharedClasspathXmlFragment());
        sb.append("</Configure>\n");
        return sb.toString();
    }

    /**
     * Returns the Jetty resource class name given the Jetty version.
     * @param jettyVersion Jetty version.
     * @param eeVersion EE version.
     * @return Jetty resource class name.
     */
    public static String getJettyResourceClassname(String jettyVersion, String eeVersion)
    {
        Matcher matcher = VERSION_NUMBER_PATTERN.matcher(jettyVersion);
        if (!matcher.matches())
        {
            throw new IllegalArgumentException(
                "Version [" + jettyVersion + "] doesn't match " + VERSION_NUMBER_PATTERN);
        }
        String[] versionParts = matcher.group(1).split("\\.");

        StringBuilder resourceClassName = new StringBuilder("org.eclipse.jetty.");
        if (versionParts.length > 2 && Integer.parseInt(versionParts[0]) == 12
            && Integer.parseInt(versionParts[1]) == 0 && Integer.parseInt(versionParts[2]) < 5)
        {
            resourceClassName.append(eeVersion + ".");
        }
        return resourceClassName.append("plus.jndi.Resource").toString();
    }
}
