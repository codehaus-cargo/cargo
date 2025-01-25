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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link Jetty12xInstalledLocalDeployer}.
 */public class Jetty12xInstalledLocalDeployerTest
{
    /**
     * Test the <code>getJettyResourceClassname</code> method.
     * @throws Exception If anything goes wrong.
     */
    @Test
    public void testGetJettyResourceClassname() throws Exception
    {
        Assertions.assertEquals("org.eclipse.jetty.plus.jndi.Resource",
            Jetty12xInstalledLocalDeployer.getJettyResourceClassname("11-beta", "ee10"));
        Assertions.assertEquals("org.eclipse.jetty.plus.jndi.Resource",
            Jetty12xInstalledLocalDeployer.getJettyResourceClassname("11", "ee10"));
        Assertions.assertEquals("org.eclipse.jetty.plus.jndi.Resource",
            Jetty12xInstalledLocalDeployer.getJettyResourceClassname("12-beta", "ee10"));
        Assertions.assertEquals("org.eclipse.jetty.plus.jndi.Resource",
            Jetty12xInstalledLocalDeployer.getJettyResourceClassname("12", "ee10"));
        Assertions.assertEquals("org.eclipse.jetty.plus.jndi.Resource",
            Jetty12xInstalledLocalDeployer.getJettyResourceClassname("12.0", "ee10"));
        Assertions.assertEquals("org.eclipse.jetty.ee10.plus.jndi.Resource",
            Jetty12xInstalledLocalDeployer.getJettyResourceClassname("12.0.0-beta", "ee10"));
        Assertions.assertEquals("org.eclipse.jetty.ee10.plus.jndi.Resource",
            Jetty12xInstalledLocalDeployer.getJettyResourceClassname("12.0.0", "ee10"));
        Assertions.assertEquals("org.eclipse.jetty.ee10.plus.jndi.Resource",
            Jetty12xInstalledLocalDeployer.getJettyResourceClassname("12.0.4", "ee10"));
        Assertions.assertEquals("org.eclipse.jetty.plus.jndi.Resource",
            Jetty12xInstalledLocalDeployer.getJettyResourceClassname("12.0.5", "ee10"));
        Assertions.assertEquals("org.eclipse.jetty.plus.jndi.Resource",
            Jetty12xInstalledLocalDeployer.getJettyResourceClassname("12.1", "ee10"));
        Assertions.assertEquals("org.eclipse.jetty.plus.jndi.Resource",
            Jetty12xInstalledLocalDeployer.getJettyResourceClassname("13", "ee10"));
    }
}
