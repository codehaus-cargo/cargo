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
package org.codehaus.cargo.container.websphere;

import junit.framework.TestCase;

import org.codehaus.cargo.container.deployable.EAR;
import org.codehaus.cargo.container.deployable.WAR;

/**
 * Unit tests for the {@link WebSphere85xInstalledLocalDeployer} class.
 * 
 */
public class WebSphere85xInstalledLocalDeployerTest extends TestCase
{
    /**
     * Test WAR deployment.
     * @throws Exception If anything goes wrong.
     */
    public void testDeployWar() throws Exception
    {
        WebSphere85xInstalledLocalDeployerWithNoWsAdminExecution deployer =
            new WebSphere85xInstalledLocalDeployerWithNoWsAdminExecution();
        WAR war = new WAR("target/test-artifacts/simple-war.war");
        deployer.deploy(war);
        assertContains(deployer.getCommands(), "cargo-deployable-simple-war");
        assertContains(deployer.getCommands(), "\"Simple Webapp\"");
        assertContains(deployer.getCommands(), "\"simple-war.war,WEB-INF/web.xml\"");
    }

    /**
     * Test EAR deployment.
     * @throws Exception If anything goes wrong.
     */
    public void testDeployEar() throws Exception
    {
        WebSphere85xInstalledLocalDeployerWithNoWsAdminExecution deployer =
            new WebSphere85xInstalledLocalDeployerWithNoWsAdminExecution();
        EAR ear = new EAR("target/test-artifacts/simple-ear.ear");
        deployer.deploy(ear);
        assertContains(deployer.getCommands(), "cargo-deployable-simple-ear");
        assertContains(deployer.getCommands(), "\"Simple Webapp\"");
        assertContains(deployer.getCommands(), "\"simple-war-"
            + System.getProperty("project.version") + ".war,WEB-INF/web.xml\"");
    }

    /**
     * Tests if a string contains another.
     * @param haystack String to look in.
     * @param needle String to look for.
     */
    private void assertContains(String haystack, String needle)
    {
        assertTrue("[" + haystack + "] does not contain [" + needle + "]",
            haystack.contains(needle));
    }
}
