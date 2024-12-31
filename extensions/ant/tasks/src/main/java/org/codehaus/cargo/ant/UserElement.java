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
package org.codehaus.cargo.ant;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds configuration data for the <code>&lt;user&gt;</code> tag used to configure the ANT plugin
 */
public class UserElement
{
    /**
     * User name.
     */
    private String name;

    /**
     * User password.
     */
    private String password;

    /**
     * User roles.
     */
    private List<RoleElement> roles = new ArrayList<RoleElement>();

    /**
     * @return User name.
     */
    public String getName()
    {
        return name;
    }

    /**
     * @param name User name.
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * @return User password.
     */
    public String getPassword()
    {
        return password;
    }

    /**
     * @param password User password.
     */
    public void setPassword(String password)
    {
        this.password = password;
    }

    /**
     * @return User roles.
     */
    public List<RoleElement> getRoles()
    {
        return this.roles;
    }

    /**
     * @param roleElement the nested user role
     */
    public void addConfiguredRole(RoleElement roleElement)
    {
        this.roles.add(roleElement);
    }

    /**
     * Create user object.
     * @return Cargo user object.
     */
    public org.codehaus.cargo.container.property.User createUser()
    {
        org.codehaus.cargo.container.property.User user =
            new org.codehaus.cargo.container.property.User();
        user.setName(getName());
        user.setPassword(getPassword());

        for (RoleElement role : getRoles())
        {
            user.addRole(role.getName());
        }

        return user;
    }
}
