/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2026 Ali Tokmen.
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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import org.codehaus.cargo.container.ContainerException;

/**
 * Unit tests for {@link User}.
 */
public class UserTest
{
    /**
     * Test roles parsing.
     */
    @Test
    public void testParseRoles()
    {
        List<String> roles = User.parseRoles("role1,role2,role3");
        Assertions.assertEquals(3, roles.size());
        Assertions.assertEquals("role1", roles.get(0));
        Assertions.assertEquals("role2", roles.get(1));
        Assertions.assertEquals("role3", roles.get(2));
    }

    /**
     * Test user parsing.
     */
    @Test
    public void testParseUser()
    {
        User expectedUser = new User();
        expectedUser.setName("name");
        expectedUser.setPassword("pwd");
        expectedUser.addRole("role");

        User user = User.parseUser("name:pwd:role");
        Assertions.assertEquals(expectedUser, user);
    }

    /**
     * Test multiple user parsing.
     */
    @Test
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
        Assertions.assertEquals(2, users.size());
        Assertions.assertEquals(expectedUser1, users.get(0));
        Assertions.assertEquals(expectedUser2, users.get(1));
    }

    /**
     * Test user parsing with a missing field.
     */
    @Test
    public void testParseUserWithMissingField()
    {
        try
        {
            User.parseUser("name:password:role:");
            Assertions.fail("Should have raised an exception here");
        }
        catch (ContainerException expected)
        {
            Assertions.assertEquals(
                "Invalid format for [name:password:role:]", expected.getMessage());
        }
    }

    /**
     * Test user parsing empty field.
     */
    @Test
    public void testParseUserWithEmptyField()
    {
        try
        {
            User.parseUser("");
            Assertions.fail("Should have raised an exception here");
        }
        catch (ContainerException expected)
        {
            Assertions.assertEquals("User property has empty value.", expected.getMessage());
        }
    }

    /**
     * Test user parsing.
     */
    @Test
    public void testParseUserWithWhitespace()
    {
        User expectedUser = new User();
        expectedUser.setName("name");
        expectedUser.setPassword("pwd");
        expectedUser.addRole("role");

        User user = User.parseUser("\n\t  name:pwd:role");
        Assertions.assertEquals(expectedUser, user);
    }

    /**
     * Test user parsing with empty password.
     */
    @Test
    public void testParseUserWithEmptyPassword()
    {
        User expectedUser = new User();
        expectedUser.setName("name");
        expectedUser.setPassword("");
        expectedUser.addRole("role");

        User user = User.parseUser("name::role");
        Assertions.assertEquals(expectedUser, user);
    }

    /**
     * Test user parsing with no roles.
     */
    @Test
    public void testParseUserWithNoRoles()
    {
        User expectedUser = new User();
        expectedUser.setName("name");
        expectedUser.setPassword("pwd");

        User user = User.parseUser("name:pwd");
        Assertions.assertEquals(expectedUser, user);
    }

    /**
     * Test role to users list map creation.
     */
    @Test
    public void testCreateRoleMap()
    {
        List<User> users = User.parseUsers("u1:p1:r1,r2|u2:p2:r2,r3");
        Map<String, List<User>> roles = User.createRoleMap(users);

        Assertions.assertNotNull(roles.get("r1"));
        Assertions.assertNotNull(roles.get("r2"));
        Assertions.assertNotNull(roles.get("r3"));

        Assertions.assertEquals(1, roles.get("r1").size());
        Assertions.assertEquals("u1", roles.get("r1").get(0).getName());

        Assertions.assertEquals(2, roles.get("r2").size());
        Assertions.assertEquals("u1", roles.get("r2").get(0).getName());
        Assertions.assertEquals("u2", roles.get("r2").get(1).getName());

        Assertions.assertEquals(1, roles.get("r3").size());
        Assertions.assertEquals("u2", roles.get("r3").get(0).getName());
    }
}
