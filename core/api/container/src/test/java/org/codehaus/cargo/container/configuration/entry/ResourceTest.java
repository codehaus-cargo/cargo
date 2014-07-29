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
package org.codehaus.cargo.container.configuration.entry;

import junit.framework.TestCase;

/**
 * Tests {@link Resource} implementation.
 * 
 * @version $Id$
 */
public class ResourceTest extends TestCase
{

    /**
     * Test resource.
     */
    private Resource resource;

    /**
     * Creates the test resource. {@inheritDoc}
     * @throws Exception If anything goes wrong.
     */
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        resource = new Resource("jdbc/someConnection", "javax.sql.DataSource");
    }

    /**
     * Destroys the test resource. {@inheritDoc}
     * @throws Exception If anything goes wrong.
     */
    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
        resource = null;
    }

    /**
     * Test the name getter.
     * @throws Exception If anything goes wrong.
     */
    public void testGetName() throws Exception
    {
        assertEquals("did not get correct name of resource", "jdbc/someConnection",
            resource.getName());
    }

    /**
     * Test the type getter.
     * @throws Exception If anything goes wrong.
     */
    public void testGetType() throws Exception
    {
        assertEquals("did not get correct type", "javax.sql.DataSource", resource.getType());
    }

    /**
     * Test the parameter getters.
     * @throws Exception If anything goes wrong.
     */
    public void testParameters() throws Exception
    {
        resource.setParameter("username", "foo");
        resource.setParameter("password", "bar");

        assertEquals("username not set", "foo", resource.getParameter("username"));
        assertEquals("password not set", "bar", resource.getParameter("password"));
    }

}
