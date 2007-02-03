/* 
 * ========================================================================
 * 
 * Copyright 2004 Vincent Massol.
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

import org.codehaus.cargo.container.ContainerException;

import junit.framework.TestCase;

/**
 * Unit tests for {@link User}.
 * 
 * @version $Id$
 */
public class UserTest extends TestCase
{
    public void testParseRoles()
    {
        List roles = User.parseRoles("role1,role2,role3");
        assertEquals(3, roles.size());
        assertEquals("role1", (String) roles.get(0));
        assertEquals("role2", (String) roles.get(1));
        assertEquals("role3", (String) roles.get(2));
    }

    public void testParseUser()
    {
        User expectedUser = new User();
        expectedUser.setName("name");
        expectedUser.setPassword("pwd");
        expectedUser.addRole("role");

        User user = User.parseUser("name:pwd:role");
        assertEquals(expectedUser, user);
    }

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

        List users = User.parseUsers("n1:p1:r1|n2:p2:r2");
        assertEquals(2, users.size());
        assertEquals(expectedUser1, users.get(0));
        assertEquals(expectedUser2, users.get(1));
    }

    public void testParseUserWithMissingField()
    {
        try
        {
            User.parseUser("name:role");
            fail("Should have raised an exception here");
        } 
        catch (ContainerException expected)
        {
            assertEquals("Invalid format for [name:role]", expected.getMessage());
        }
    }

    public void testParseUserWithEmptyPassword()
    {
        User expectedUser = new User();
        expectedUser.setName("name");
        expectedUser.setPassword("");
        expectedUser.addRole("role");

        User user = User.parseUser("name::role");
        assertEquals(expectedUser, user);
    }

    public void testCreateRoleMap()
    {
        List users = User.parseUsers("u1:p1:r1,r2|u2:p2:r2,r3");
        Map roles = User.createRoleMap(users);
        
        assertNotNull(roles.get("r1"));
        assertNotNull(roles.get("r2"));
        assertNotNull(roles.get("r3"));
        
        assertEquals(1, ((List) roles.get("r1")).size());
        assertEquals("u1", ((User) ((List) roles.get("r1")).get(0)).getName());

        assertEquals(2, ((List) roles.get("r2")).size());
        assertEquals("u1", ((User) ((List) roles.get("r2")).get(0)).getName());
        assertEquals("u2", ((User) ((List) roles.get("r2")).get(1)).getName());

        assertEquals(1, ((List) roles.get("r3")).size());
        assertEquals("u2", ((User) ((List) roles.get("r3")).get(0)).getName());
    }
}
