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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import org.codehaus.cargo.container.ContainerException;

/**
 * Represent an authenticating user for the Servlet container.
 * 
 * @version $Id$
 */
public final class User
{
    /**
     * @see #setName(String)
     */
    private String name;

    /**
     * @see #setPassword(String)
     */
    private String password;

    /**
     * @see #addRoles(java.util.List)
     */
    private List<String> roles = new ArrayList<String>();

    /**
     * @param name the user name
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * @return the user name
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * Sets the authenticated user password.
     * 
     * @param password the user password
     */
    public void setPassword(String password)
    {
        this.password = password;
    }

    /**
     * @return the user password
     */
    public String getPassword()
    {
        return this.password;
    }

    /**
     * @param role a role attached to this user
     */
    public void addRole(String role)
    {
        this.roles.add(role);
    }

    /**
     * @param roles a list of roles attached to this user
     */
    public void addRoles(List<String> roles)
    {
        this.roles.addAll(roles);
    }

    /**
     * @return the list of roles attached to this user
     */
    public List<String> getRoles()
    {
        return this.roles;
    }

    /**
     * {@inheritDoc}
     * @see Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object userObject)
    {
        boolean result = false;

        if (userObject != null && userObject instanceof User)
        {
            User user = (User) userObject;
            if (user.getName().equals(getName()) && user.getPassword().equals(getPassword()))
            {
                result = user.getRoles().equals(getRoles());
            }
        }

        return result;
    }

    /**
     * {@inheritDoc}
     * @see Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        return (getName() + getPassword()).hashCode();
    }

    /**
     * Parse a string representing the users (see {@link ServletPropertySet#USERS}.
     * 
     * @param usersAsString the string representing the users
     * @return a list of {@link User} objects
     */
    public static List<User> parseUsers(String usersAsString)
    {
        List<User> users = new ArrayList<User>();

        // The format to parse is "name1:pwd1:role11,...,role1N|name2:pwd2:role21,...,role2N|..."
        StringTokenizer userTokens = new StringTokenizer(usersAsString, "|");
        while (userTokens.hasMoreTokens())
        {
            users.add(parseUser(userTokens.nextToken()));
        }

        return users;
    }

    /**
     * Parse a user defined in the format "name:pwd:role1,...roleN".
     * 
     * @param userAsString the user defines as a string
     * @return the parsed user
     */
    protected static User parseUser(String userAsString)
    {
        User user = new User();

        StringTokenizer fieldTokens = new StringTokenizer(userAsString, ":", true);

        try
        {
            user.setName(fieldTokens.nextToken());

            if (!":".equals(fieldTokens.nextToken()))
            {
                throw new ContainerException("Invalid format for [" + userAsString + "]");
            }

            String token = fieldTokens.nextToken();
            if (":".equals(token))
            {
                user.setPassword("");
            }
            else
            {
                user.setPassword(token);
                if (!":".equals(fieldTokens.nextToken()))
                {
                    throw new ContainerException("Invalid format for [" + userAsString + "]");
                }
            }

            user.addRoles(parseRoles(fieldTokens.nextToken()));
        }
        catch (NoSuchElementException exception)
        {
            throw new ContainerException("Invalid format for [" + userAsString + "]");
        }

        return user;
    }

    /**
     * Parse roles defined as a list in the format "role1,role2,...,roleN".
     * 
     * @param rolesAsString the roles defined as a string
     * @return the parsed list of roles
     */
    protected static List<String> parseRoles(String rolesAsString)
    {
        List<String> roles = new ArrayList<String>();

        StringTokenizer roleTokens = new StringTokenizer(rolesAsString, ",");
        while (roleTokens.hasMoreTokens())
        {
            String roleToken = roleTokens.nextToken();
            roles.add(roleToken);
        }

        return roles;
    }

    /**
     * Create a user map indexed on the roles.
     * 
     * @param users list of {@link User} for which to extract roles from
     * @return a map of roles containing users
     */
    public static Map<String, List<User>> createRoleMap(List<User> users)
    {
        Map<String, List<User>> roles = new HashMap<String, List<User>>();

        for (User user : users)
        {
            for (String role : user.getRoles())
            {
                List<User> usersForRole;
                if (roles.containsKey(role))
                {
                    usersForRole = roles.get(role);
                }
                else
                {
                    usersForRole = new ArrayList<User>();
                }

                if (!usersForRole.contains(user))
                {
                    usersForRole.add(user);
                }

                roles.put(role, usersForRole);
            }
        }

        return roles;
    }
}
