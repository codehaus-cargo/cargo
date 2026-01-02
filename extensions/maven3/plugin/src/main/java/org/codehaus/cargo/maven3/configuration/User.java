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
package org.codehaus.cargo.maven3.configuration;

import java.util.Arrays;

/**
 * Holds configuration data for the <code>&lt;user&gt;</code> tag used to configure the plugin
 * in the <code>pom.xml</code> file.
 */
public class User
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
    private String[] roles;

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
    public String[] getRoles()
    {
        return this.roles;
    }

    /**
     * @param roles User roles.
     */
    public void setRoles(String[] roles)
    {
        this.roles = roles;
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
        user.addRoles(Arrays.asList(getRoles()));

        return user;
    }
}
