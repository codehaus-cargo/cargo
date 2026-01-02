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
package org.codehaus.cargo.container.weblogic.internal.configuration.commands.user;

import java.util.Map;

import org.codehaus.cargo.container.configuration.Configuration;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.configuration.script.AbstractResourceScriptCommand;
import org.codehaus.cargo.container.internal.util.JythonUtils;
import org.codehaus.cargo.container.property.User;

/**
 * Implementation of create user configuration script command.
 */
public class CreateUserScriptCommand extends AbstractResourceScriptCommand
{

    /**
     * User.
     */
    private User user;

    /**
     * Sets configuration containing all needed information for building configuration scripts.
     * 
     * @param configuration Container configuration.
     * @param resourcePath Path to configuration script resources.
     * @param user User.
     */
    public CreateUserScriptCommand(Configuration configuration, String resourcePath, User user)
    {
        super(configuration, resourcePath);
        this.user = user;
    }

    @Override
    protected String getScriptRelativePath()
    {
        return "user/create-user.py";
    }

    @Override
    protected void addConfigurationScriptProperties(Map<String, String> propertiesMap)
    {
        propertiesMap.put("cargo.weblogic.user.name", user.getName());

        String escapedPassword = JythonUtils.escapeStringLiteral(user.getPassword());
        propertiesMap.put("cargo.weblogic.user.password", escapedPassword);

        LocalConfiguration configuration = (LocalConfiguration) getConfiguration();
        String domainName = configuration.getFileHandler().getName(configuration.getHome());
        propertiesMap.put("cargo.weblogic.domain.name", domainName);
    }
}
