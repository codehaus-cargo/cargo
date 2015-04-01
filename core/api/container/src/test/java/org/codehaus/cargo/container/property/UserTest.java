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
package org.codehaus.cargo.container.property;

import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.codehaus.cargo.container.ContainerException;

/**
 * Unit tests for {@link User}.
 * 
 */
public class UserTest extends TestCase
{
    /**
     * Test roles parsing.
     */
    public void testParseRoles()
    {
        List<String> roles = User.parseRoles("role1,role2,role3");
        assertEquals(3, roles.size());
        assertEquals("role1", roles.get(0));
        assertEquals("role2", roles.get(1));
        assertEquals("role3", roles.get(2));
    }

    /**
     * Test user parsing.
     */
    public void testParseUser()
    {
        User expectedUser = new User();
        expectedUser.setName("name");
        expectedUser.setPassword("pwd");
        expectedUser.addRole("role");

        User user = User.parseUser("name:pwd:role");
        assertEquals(expectedUser, user);
    }

    /**
     * Test multiple user parsing.
     */
    public void testParseUsers()
    {
        User expectedUser1 = new User();
        expectedUser1.setName("n1");
        expectedUser1.setPassword("p1");
        expectedUser1.addRole("r1");

        User expectedUser2 = new User();
        expectedUser2.setName("n2");
        expectedUser2.setPassword("p2");
        expectedUser2.addRole("r2");

        List<User> users = User.parseUsers("n1:p1:r1|n2:p2:r2");
        assertEquals(2, users.size());
        assertEquals(expectedUser1, users.get(0));
        assertEquals(expectedUser2, users.get(1));
    }

    /**
     * Test user parsing with a missing field.
     */
    public void testParseUserWithMissingField()
    {
        try
        {
            User.parseUser("name:password:role:");
            fail("Should have raised an exception here");
        }
        catch (ContainerException expected)
        {
            assertEquals("Invalid format for [name:password:role:]", expected.getMessage());
        }
    }

    /**
     * Test user parsing with empty password.
     */
    public void testParseUserWithEmptyPassword()
    {
        User expectedUser = new User();
        expectedUser.setName("name");
        expectedUser.setPassword("");
        expectedUser.addRole("role");

        User user = User.parseUser("name::role");
        assertEquals(expectedUser, user);
    }

    /**
     * Test user parsing with no roles.
     */
    public void testParseUserWithNoRoles()
    {
        User expectedUser = new User();
        expectedUser.setName("name");
        expectedUser.setPassword("pwd");

        User user = User.parseUser("name:pwd");
        assertEquals(expectedUser, user);
    }

    /**
     * Test role to users list map creation.
     */
    public void testCreateRoleMap()
    {
        List<User> users = User.parseUsers("u1:p1:r1,r2|u2:p2:r2,r3");
        Map<String, List<User>> roles = User.createRoleMap(users);

        assertNotNull(roles.get("r1"));
        assertNotNull(roles.get("r2"));
        assertNotNull(roles.get("r3"));

        assertEquals(1, roles.get("r1").size());
        assertEquals("u1", roles.get("r1").get(0).getName());

        assertEquals(2, roles.get("r2").size());
        assertEquals("u1", roles.get("r2").get(0).getName());
        assertEquals("u2", roles.get("r2").get(1).getName());

        assertEquals(1, roles.get("r3").size());
        assertEquals("u2", roles.get("r3").get(0).getName());
    }
}
