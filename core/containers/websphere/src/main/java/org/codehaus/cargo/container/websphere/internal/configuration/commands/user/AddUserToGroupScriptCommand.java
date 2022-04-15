/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2022 Ali Tokmen.
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
package org.codehaus.cargo.container.websphere.internal.configuration.commands.user;

import java.util.Map;

import org.codehaus.cargo.container.configuration.Configuration;
import org.codehaus.cargo.container.configuration.script.AbstractResourceScriptCommand;
import org.codehaus.cargo.container.property.User;

/**
 * Implementation of adding user to group configuration script command.
 */
public class AddUserToGroupScriptCommand extends AbstractResourceScriptCommand
{

    /**
     * User.
     */
    private User user;

    /**
     * Group role.
     */
    private String groupRole;

    /**
     * Sets configuration containing all needed information for building configuration scripts.
     * 
     * @param configuration Container configuration.
     * @param resourcePath Path to configuration script resources.
     * @param user User.
     * @param groupRole Group role.
     */
    public AddUserToGroupScriptCommand(Configuration configuration, String resourcePath, User user,
            String groupRole)
    {
        super(configuration, resourcePath);
        this.user = user;
        this.groupRole = groupRole;
    }

    @Override
    protected String getScriptRelativePath()
    {
        return "user/add-user-to-group.py";
    }

    @Override
    protected void addConfigurationScriptProperties(Map<String, String> propertiesMap)
    {
        propertiesMap.put("cargo.websphere.user.name", user.getName());
        propertiesMap.put("cargo.websphere.group", groupRole);
    }
}
