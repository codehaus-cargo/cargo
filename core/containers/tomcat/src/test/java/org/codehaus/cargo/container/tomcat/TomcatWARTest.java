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
package org.codehaus.cargo.container.tomcat;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.util.AbstractResourceTest;

/**
 * Unit tests for {@link TomcatWAR}.
 */
public class TomcatWARTest extends AbstractResourceTest
{
    /**
     * Package path.
     */
    private static final String PACKAGE_PATH = "org/codehaus/cargo/container/tomcat/";

    /**
     * Test get path context for a WAR with a Tomcat context.xml with path context.
     * @throws Exception If anything goes wrong.
     */
    @Test
    public void testGetPathContextWhenTomcatContextXmlWithPathContext() throws Exception
    {
        TomcatWAR war = new TomcatWAR(getResourcePath(PACKAGE_PATH + "tomcat-context.war"));
        Assertions.assertEquals("testcontext", war.getContext());
    }

    /**
     * Test get path context for a WAR with a Tomcat context.xml with no context.
     * @throws Exception If anything goes wrong.
     */
    @Test
    public void testGetPathContextWhenTomcatContextXmlWithNoPathContext() throws Exception
    {
        TomcatWAR war = new TomcatWAR(getResourcePath(PACKAGE_PATH + "tomcat-nocontext.war"));
        Assertions.assertEquals("tomcat-nocontext", war.getContext());
    }

    /**
     * Test get path context for a WAR with no Tomcat context.xml.
     * @throws Exception If anything goes wrong.
     */
    @Test
    public void testGetPathContextWhenNoTomcatContextXml() throws Exception
    {
        TomcatWAR war = new TomcatWAR(getResourcePath(PACKAGE_PATH + "tomcat-empty.war"));
        Assertions.assertEquals("tomcat-empty", war.getContext());
    }

    /**
     * Test get path context for a WAR with an invalid Tomcat context.xml file.
     */
    @Test
    public void testGetPathContextWhenInvalidFile()
    {
        try
        {
            new TomcatWAR("some/invalid/file");
            Assertions.fail(
                "Should have thrown a ContainerException because the file doesn't exist");
        }
        catch (ContainerException expected)
        {
            Assertions.assertEquals("Failed to parse Tomcat WAR file in [some/invalid/file]",
                expected.getMessage());
        }
    }

    /**
     * Test set and get path context for a WAR with no Tomcat context.xml.
     * @throws Exception If anything goes wrong.
     */
    @Test
    public void testGetPathContextWhenContextAlreadySetupAndNoTomcatContextXml() throws Exception
    {
        TomcatWAR war = new TomcatWAR(getResourcePath(PACKAGE_PATH + "tomcat-empty.war"));
        war.setContext("context");
        Assertions.assertEquals("context", war.getContext());
    }

    /**
     * Test set and get path context for a WAR with a Tomcat context.xml file.
     * @throws Exception If anything goes wrong.
     */
    @Test
    public void testGetPathContextWhenContextAlreadySetupAndTomcatContextXml() throws Exception
    {
        TomcatWAR war = new TomcatWAR(getResourcePath(PACKAGE_PATH + "tomcat-context.war"));
        war.setContext("context");
        Assertions.assertEquals("testcontext", war.getContext());
    }
}
