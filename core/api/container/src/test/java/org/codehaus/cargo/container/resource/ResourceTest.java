/**
 * 
 */
package org.codehaus.cargo.container.resource;

import junit.framework.TestCase;

/**
 * @author Alexander Brill <alexander.brill@nhst.no>
 * 
 */
public class ResourceTest extends TestCase
{

    private Resource resource;

    /*
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        resource = new Resource("jdbc/someConnection", "javax.sql.DataSource");
    }

    /*
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
        resource = null;
    }

    public void testGetName() throws Exception
    {
        assertEquals("did not get correct name of resource",
                "jdbc/someConnection", resource.getName());
    }

    public void testGetType() throws Exception
    {
        assertEquals("did not get correct type", "javax.sql.DataSource",
                resource.getType());
    }

    public void testParameters() throws Exception
    {
        resource.setParameter("username", "foo");
        resource.setParameter("password", "bar");

        assertEquals("username not set", "foo", resource
                .getParameter("username"));
        assertEquals("password not set", "bar", resource
                .getParameter("password"));

    }

}
