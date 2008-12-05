/* 
 * ========================================================================
 * 
 * Copyright 2004-2005 Vincent Massol.
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
package org.codehaus.cargo.container.tomcat.internal;

import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.container.tomcat.Tomcat3xInstalledLocalContainer;
import org.codehaus.cargo.container.tomcat.Tomcat3xStandaloneLocalConfiguration;
import org.codehaus.cargo.container.tomcat.TomcatPropertySet;
import org.codehaus.cargo.container.tomcat.internal.AbstractTomcatStandaloneLocalConfiguration;
import org.custommonkey.xmlunit.XMLAssert;

/**
 * Unit tests for {@link AbstractTomcatStandaloneLocalConfiguration}.
 * 
 * @version $Id$
 */
public class TomcatStandaloneLocalConfigurationTest extends AbstractTomcatStandaloneLocalConfigurationTest
{
    protected String getTestHome()
    {
        return "ram:/tomcat3xconfig";
    }

    protected void setUpContainerDefaults()
    {
        this.configuration = new Tomcat3xStandaloneLocalConfiguration(CONFIG_HOME);
        this.container = new Tomcat3xInstalledLocalContainer(configuration);
    }

    public void testConfigure() throws Exception
    {
        configuration.configure(container);

        assertTrue(fileHandler.exists(CONFIG_HOME + "/conf/server.xml"));
        assertTrue(fileHandler.exists(CONFIG_HOME + "/conf/modules.xml"));
        assertTrue(fileHandler.exists(CONFIG_HOME + "/conf/apps.xml"));
        assertTrue(fileHandler.exists(CONFIG_HOME + "/conf/users/tomcat-users.xml"));
        assertTrue(fileHandler.exists(CONFIG_HOME + "/webapps/cargocpc.war"));
    }

    public void testConfigureSetsDefaultAJPPort() throws Exception
    {
        configuration.configure(container);
        String config = slurp(CONFIG_HOME + "/conf/server.xml");
        XMLAssert.assertXpathEvaluatesTo(configuration
            .getPropertyValue(TomcatPropertySet.AJP_PORT), "//CoyoteConnector[2]/@port", config);

    }

    public void testConfigureSetsAJPPort() throws Exception
    {
        configuration.setProperty(TomcatPropertySet.AJP_PORT, AJP_PORT);
        configuration.configure(container);
        String config = slurp(CONFIG_HOME + "/conf/server.xml");
        XMLAssert.assertXpathEvaluatesTo(AJP_PORT,
            "//CoyoteConnector[2]/@port",
            config);

    }
    
    public void testGetSecurityToken()
    {
        configuration.setProperty(ServletPropertySet.USERS, "n1:p1:r1|n2:p2:r2");

        assertEquals(" <user name=\"n1\" password=\"p1\" roles=\"r1\"/>"
            + "<user name=\"n2\" password=\"p2\" roles=\"r2\"/>", configuration.getSecurityToken());
    }
}
