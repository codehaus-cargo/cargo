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
package org.codehaus.cargo.container.configuration.entry;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link Resource} implementation.
 */
public class ResourceTest
{

    /**
     * Test resource.
     */
    private Resource resource;

    /**
     * Creates the test resource.
     */
    @BeforeEach
    protected void setUp()
    {
        resource = new Resource("jdbc/someConnection", "javax.sql.DataSource");
    }

    /**
     * Destroys the test resource.
     */
    @AfterEach
    protected void tearDown()
    {
        resource = null;
    }

    /**
     * Test the name getter.
     * @throws Exception If anything goes wrong.
     */
    @Test
    public void testGetName() throws Exception
    {
        Assertions.assertEquals(
            "jdbc/someConnection", resource.getName(), "did not get correct name of resource");
    }

    /**
     * Test the type getter.
     * @throws Exception If anything goes wrong.
     */
    @Test
    public void testGetType() throws Exception
    {
        Assertions.assertEquals(
            "javax.sql.DataSource", resource.getType(), "did not get correct type");
    }

    /**
     * Test the parameter getters.
     * @throws Exception If anything goes wrong.
     */
    @Test
    public void testParameters() throws Exception
    {
        resource.setParameter("username", "foo");
        resource.setParameter("password", "bar");

        Assertions.assertEquals(
            "foo", resource.getParameter("username"), "username not set");
        Assertions.assertEquals(
            "bar", resource.getParameter("password"), "password not set");
    }

}
