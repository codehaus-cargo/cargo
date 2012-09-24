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

import java.io.File;

import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.geronimo.Geronimo1xInstalledLocalContainer;

/**
 * Geronimo WAR deployable.
 * 
 * @version $Id$
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
            sb.append("     xmlns:sys=\"http://geronimo.apache.org/xml/ns/deployment-1.2\">\n");

            if (localContainer.getExtraClasspath() != null
                && localContainer.getExtraClasspath().length > 0)
            {
                sb.append("  <sys:environment>\n");
                sb.append("    <sys:dependencies>\n");
                sb.append("      <sys:dependency>\n");
                for (String extraClasspathElement : localContainer.getExtraClasspath())
                {
                    extraClasspathElement = new File(extraClasspathElement).getName();

                    String extension = extraClasspathElement.substring(
                        extraClasspathElement.lastIndexOf('.') + 1);
                    String artifact = extraClasspathElement.substring(
                        0, extraClasspathElement.lastIndexOf('.'));
                    String version;
                    if (artifact.indexOf('-') == -1)
                    {
                        version = "1.0";
                    }
                    else
                    {
                        version = artifact.substring(artifact.lastIndexOf('-') + 1);
                        artifact = artifact.substring(0, artifact.lastIndexOf('-'));
                    }

                    sb.append("        <sys:groupId>org.codehaus.cargo.classpath</sys:groupId>\n");
                    sb.append("        <sys:artifactId>" + artifact + "</sys:artifactId>\n");
                    sb.append("        <sys:version>" + version + "</sys:version>\n");
                    sb.append("        <sys:type>" + extension + "</sys:type>\n");
                }
                sb.append("      </sys:dependency>\n");
                sb.append("    </sys:dependencies>\n");
                sb.append("  </sys:environment>\n");
            }
        }

        sb.append("  <context-root>" + this.getContext() + "</context-root>\n");

        sb.append("</web-app>");

        return sb.toString();
    }
}
