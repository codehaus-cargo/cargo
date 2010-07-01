/* 
 * ========================================================================
 * 
 * Codehaus CARGO, copyright 2004-2010 Vincent Massol.
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
package org.codehaus.cargo.ant;

import junit.framework.TestCase;

import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.deployable.EAR;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.stub.StandaloneLocalConfigurationStub;
import org.codehaus.cargo.container.ContainerType;

/**
 * Unit tests for {@link ConfigurationElement}.
 *
 * @version $Id$
 */
public class ConfigurationElementTest extends TestCase
{
    public void testCreateStandaloneConfigurationWithDeployables()
    {
        ConfigurationElement configElement = new ConfigurationElement();
        configElement.setType(ConfigurationType.STANDALONE.getType());
        configElement.setHome("somewhere");

        DeployableElement warElement = new DeployableElement();
        warElement.setType(DeployableType.WAR.getType());
        warElement.setFile("some/war/file");
        configElement.addConfiguredDeployable(warElement);
        
        DeployableElement earElement = new DeployableElement();
        earElement.setType(DeployableType.EAR.getType());
        earElement.setFile("some/ear/file");
        configElement.addConfiguredDeployable(earElement);

        // Register a standalone configuration with the current container so that the factory used
        // by the ConfigurationElement is able to create the configuration.
        configElement.setClass(StandaloneLocalConfigurationStub.class);

        LocalConfiguration configuration = 
            (LocalConfiguration) configElement.createConfiguration("someContainerId",
                ContainerType.INSTALLED);

        assertEquals(2, configuration.getDeployables().size());
        assertEquals("some/war/file", ((WAR) configuration.getDeployables().get(0)).getFile());
        assertEquals("some/ear/file", ((EAR) configuration.getDeployables().get(1)).getFile());
    }
}
