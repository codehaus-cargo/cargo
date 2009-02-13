/*
 * ========================================================================
 *
 * Copyright 2005 Vincent Massol.
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

import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.configuration.LocalConfiguration;

/**
 * Tests for the Tomcat 6 implementation of StandaloneLocalConfigurationTest
 */
public class Tomcat6xStandaloneLocalConfigurationTest extends
    Tomcat5xStandaloneLocalConfigurationTest
{

    public LocalConfiguration createLocalConfiguration(String home)
    {
        return new Tomcat6xStandaloneLocalConfiguration(home);
    }

    public InstalledLocalContainer createLocalContainer(LocalConfiguration configuration)
    {
        return new Tomcat6xInstalledLocalContainer(configuration);
    }

    protected void setUpManager()
    {
        configuration.getFileHandler().mkdirs(container.getHome() + "/lib");
        configuration.getFileHandler().createFile(container.getHome() + "/lib/catalina.jar");
        configuration.getFileHandler().mkdirs(container.getHome() + "/webapps/manager");
        configuration.getFileHandler().mkdirs(container.getHome() + "/webapps/host-manager");
    }

    /**
     * note that manager is under webapps, not server/webapps in 5x.
     */
    public void testConfigureManager()
    {
        configuration.configure(container);
        assertTrue(configuration.getFileHandler().exists(
            configuration.getHome() + "/lib/catalina.jar"));
        assertTrue(configuration.getFileHandler().exists(
            configuration.getHome() + "/webapps/manager"));
        assertTrue(configuration.getFileHandler().exists(
            configuration.getHome() + "/webapps/host-manager"));
    }

}
