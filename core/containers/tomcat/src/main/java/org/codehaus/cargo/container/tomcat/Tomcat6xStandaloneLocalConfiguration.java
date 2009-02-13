/* 
 * ========================================================================
 * 
 * Copyright 2004-2008 Vincent Massol.
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

import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.EmbeddedLocalContainer;

/**
 * Catalina standalone {@link org.codehaus.cargo.container.spi.configuration.ContainerConfiguration}
 * implementation.
 *
 * @version $Id$
 */
public class Tomcat6xStandaloneLocalConfiguration
    extends Tomcat5xStandaloneLocalConfiguration
{
    /**
     * {@inheritDoc}
     * @see Tomcat5xStandaloneLocalConfiguration#Tomcat5xStandaloneLocalConfiguration(String)
     */
    public Tomcat6xStandaloneLocalConfiguration(String dir)
    {
        super(dir);
    }

    /**
     * {@inheritDoc}
     * 
     * the path to find the manager application is different between v5 and v6.
     * 
     * @see Tomcat5xStandaloneLocalConfiguration#setupManager(org.codehaus.cargo.container.LocalContainer)
     */
    protected void setupManager(LocalContainer container)
    {
        if (container instanceof EmbeddedLocalContainer)
        {
            // when running in the embedded mode, there's no need
            // of any manager application.
        }
        else
        {
            String from = ((InstalledLocalContainer) container).getHome();
            String to = getHome();
            getFileHandler().copyDirectory(from + "/webapps/manager",
                to + "/webapps/manager");
            getFileHandler().copyFile(from + "/lib/catalina.jar",
                to + "/lib/catalina.jar");
            getFileHandler().copyDirectory(from + "/webapps/host-manager",
                to + "/webapps/host-manager");
        }
    }
    
    /**
     * {@inheritDoc}
     * @see Object#toString()
     */
    public String toString()
    {
        return "Tomcat 6.x Standalone Configuration";
    }
}
