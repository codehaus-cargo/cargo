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
package org.codehaus.cargo.container.geronimo.deployable;

import java.io.IOException;

import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.configuration.entry.DataSource;
import org.codehaus.cargo.container.deployable.DeployableException;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.geronimo.Geronimo1xInstalledLocalContainer;
import org.codehaus.cargo.container.geronimo.internal.GeronimoUtils;

/**
 * Geronimo WAR deployable.
 * 
 */
public class GeronimoWAR extends WAR implements GeronimoDeployable
{
    /**
     * @param war the location of the WAR being wrapped. This must point to either a WAR file or an
     * expanded WAR directory.
     */
    public GeronimoWAR(String war)
    {
        super(war);
    }

    /**
     * {@inheritDoc}
     * @see GeronimoDeployable#getPlan(org.codehaus.cargo.container.InstalledLocalContainer) 
     */
    public String getPlan(InstalledLocalContainer localContainer)
    {
        StringBuilder sb = new StringBuilder();

        if (localContainer.getClass().equals(Geronimo1xInstalledLocalContainer.class))
        {
            sb.append(
                "<web-app xmlns=\"http://geronimo.apache.org/xml/ns/j2ee/web/tomcat-1.1\">\n");
        }
        else
        {
            sb.append("<web-app xmlns=\"http://geronimo.apache.org/xml/ns/j2ee/web-2.0.1\"\n");
            sb.append("     xmlns:dep=\"http://geronimo.apache.org/xml/ns/deployment-1.2\"\n");
            sb.append("     xmlns:naming=\"http://geronimo.apache.org/xml/ns/naming-1.2\">\n");

            sb.append("  <dep:environment>\n");
            String extraClasspathDependenciesXML;
            try
            {
                extraClasspathDependenciesXML =
                    GeronimoUtils.getGeronimoExtraClasspathDependiesXML(localContainer) + "\n";
            }
            catch (IOException e)
            {
                throw new DeployableException("Cannot read deployable " + this, e);
            }

            if (!localContainer.getConfiguration().getDataSources().isEmpty())
            {
                StringBuilder dataSourceDependencies = new StringBuilder("");

                for (DataSource datasource : localContainer.getConfiguration().getDataSources())
                {
                    dataSourceDependencies.append("      <dep:dependency>\n");
                    dataSourceDependencies.append("        <dep:groupId>"
                        + "org.codehaus.cargo.datasource</dep:groupId>\n");
                    dataSourceDependencies.append("        <dep:artifactId>"
                        + datasource.getId() + "</dep:artifactId>\n");
                    dataSourceDependencies.append("        <dep:version>1.0</dep:version>\n");
                    dataSourceDependencies.append("        <dep:type>car</dep:type>\n");
                    dataSourceDependencies.append("      </dep:dependency>\n");
                }

                extraClasspathDependenciesXML = extraClasspathDependenciesXML.replace(
                    "    </dep:dependencies>",
                        dataSourceDependencies.toString() + "    </dep:dependencies>");
            }

            sb.append(extraClasspathDependenciesXML);
            sb.append("  </dep:environment>\n");
        }

        sb.append("  <context-root>" + this.getContext() + "</context-root>\n");

        for (DataSource datasource : localContainer.getConfiguration().getDataSources())
        {
            sb.append("  <naming:resource-ref>\n");
            sb.append("    <naming:ref-name>"
                + datasource.getJndiLocation() + "</naming:ref-name>\n");
            sb.append("    <naming:resource-link>"
                + datasource.getJndiLocation() + "</naming:resource-link>\n");
            sb.append("  </naming:resource-ref>\n");
        }

        sb.append("</web-app>");

        return sb.toString();
    }
}
