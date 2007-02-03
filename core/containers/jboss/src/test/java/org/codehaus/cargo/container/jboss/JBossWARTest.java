/* 
 * ========================================================================
 * 
 * Copyright 2004-2006 Vincent Massol.
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
package org.codehaus.cargo.container.jboss;

import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.util.AbstractResourceTest;

/**
 * Unit tests for {@link JBossWAR}.
 * 
 * @version $Id$
 */
public class JBossWARTest extends AbstractResourceTest
{
    private static final String PACKAGE_PATH = "org/codehaus/cargo/container/jboss/";

    public void testGetContextWhenJbossWebXmlWithRootContext() throws Exception
    {
        JBossWAR war = new JBossWAR(getResourcePath(PACKAGE_PATH + "jboss-context.war"));
        assertEquals("testcontext", war.getContext());
    }

    public void testGetContextWhenJbossWebXmlWithNoRootContext() throws Exception
    {
        JBossWAR war = new JBossWAR(getResourcePath(PACKAGE_PATH + "jboss-nocontext.war"));
        assertEquals("jboss-nocontext", war.getContext());
    }

    public void testGetContextWhenNoJbossWebXml() throws Exception
    {
        JBossWAR war = new JBossWAR(getResourcePath(PACKAGE_PATH + "jboss-empty.war"));
        assertEquals("jboss-empty", war.getContext());
    }

    public void testGetContextWhenInvalidFile()
    {
        try
        {
            new JBossWAR("some/invalid/file");
            fail("Should have thrown a ContainerException because the file doesn't exist");
        }
        catch (ContainerException expected)
        {
            assertEquals("Failed to parse JBoss WAR file in [some/invalid/file]",
                expected.getMessage());
        }
    }

    public void testGetContextWhenContextAlreadySetupAndNoJBossWebXml() throws Exception
    {
        JBossWAR war = new JBossWAR(getResourcePath(PACKAGE_PATH + "jboss-empty.war"));
        war.setContext("context");
        assertEquals("context", war.getContext());
    }

    public void testGetContextWhenContextAlreadySetupAndJBossWebXml() throws Exception
    {
        JBossWAR war = new JBossWAR(getResourcePath(PACKAGE_PATH + "jboss-context.war"));
        war.setContext("context");
        assertEquals("testcontext", war.getContext());
    }
}
