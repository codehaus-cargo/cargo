/*
 * ========================================================================
 *
 *  Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2024 Ali Tokmen.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  ========================================================================
 */
package org.codehaus.cargo.container.wildfly.swarm.internal.configuration.yaml;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import java.nio.charset.StandardCharsets;
import org.codehaus.cargo.container.property.User;
import org.codehaus.cargo.container.wildfly.swarm.internal.configuration.AbstractConfigurator;
import org.codehaus.cargo.container.wildfly.swarm.internal.configuration.ConfigurationContext;
import org.codehaus.cargo.container.wildfly.swarm.internal.configuration.UserAccountsConfigurator;
import org.codehaus.cargo.container.wildfly.swarm.internal.configuration.util.WildFlySwarmUserUtils;

/**
 * User accounts configurator writing configuration to yaml.
 */
public class UserAccountsYamlConfigurator extends AbstractConfigurator
        implements UserAccountsConfigurator
{
    /**
     * YAML generator instance.
     */
    private final YAMLGenerator yamlGenerator;

    /**
     * Constructor.
     * 
     * @param configurationContext configuration context.
     * @param yamlGenerator          yaml generator instance.
     */
    public UserAccountsYamlConfigurator(ConfigurationContext configurationContext,
                                        YAMLGenerator yamlGenerator)
    {
        super(configurationContext);
        this.yamlGenerator = yamlGenerator;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configureApplicationUsers(final List<User> users)
    {
        if (!users.isEmpty())
        {
            File applicationUsers = createApplicationUsersProperties(users);
            File applicationRoles = createApplicationRolesProperties(users);
            configureApplicationRealm(applicationUsers, applicationRoles);
        }
    }

    /**
     * Creates the yaml configuration pointing to users and roles properties files.
     * @param applicationUsers application users.
     * @param applicationRoles application roles.
     */
    private void configureApplicationRealm(File applicationUsers, File applicationRoles)
    {
        try
        {
            yamlGenerator.writeStartObject();
            yamlGenerator.writeFieldName("swarm");

            yamlGenerator.writeStartObject();
            yamlGenerator.writeFieldName("management");


            yamlGenerator.writeStartObject();
            yamlGenerator.writeFieldName("security-realms");

            yamlGenerator.writeStartObject();
            yamlGenerator.writeFieldName("ApplicationRealm");

            yamlGenerator.writeStartObject();
            yamlGenerator.writeFieldName("local-authentication");

            yamlGenerator.writeStartObject();
            yamlGenerator.writeStringField("default-user", "local");
            yamlGenerator.writeStringField("allowed-users", "local");
            yamlGenerator.writeStringField("skip-group-loading", "true");
            yamlGenerator.writeEndObject();

            yamlAuthenticationConfig(applicationUsers);
            yamlAuthorizationConfig(applicationRoles);


            yamlGenerator.writeEndObject();
            yamlGenerator.writeEndObject();

            yamlGenerator.writeEndObject();
            yamlGenerator.writeEndObject();
            yamlGenerator.writeEndObject();
        }
        catch (IOException ex)
        {
            throw new RuntimeException("Error writing YAML configuration.", ex);
        }
    }

    /**
     * Creates YAML configuration for properties authentication.
     * @param applicationUsers application-users.properties file
     * @throws IOException when writing YAML content fails.
     */
    private void yamlAuthenticationConfig(File applicationUsers) throws IOException
    {
        yamlGenerator.writeFieldName("properties-authentication");

        yamlGenerator.writeStartObject();
        yamlGenerator.writeStringField("path", applicationUsers.getAbsolutePath());
        yamlGenerator.writeStringField("plain-text", "false");
        yamlGenerator.writeEndObject();
    }

    /**
     * Creates YAML configuration for properties authorization.
     * @param applicationRoles application-roles.properties file
     * @throws IOException when writing YAML content fails.
     */
    private void yamlAuthorizationConfig(File applicationRoles) throws IOException
    {
        yamlGenerator.writeFieldName("properties-authorization");

        yamlGenerator.writeStartObject();
        yamlGenerator.writeStringField("path", applicationRoles.getAbsolutePath());
        yamlGenerator.writeEndObject();
    }

    /**
     * Create application users file (application-users.properties).
     * @param users list of users to create.
     * @return application-users.properties file.
     */
    private File createApplicationUsersProperties(List<User> users)
    {
        StringBuilder usersToken = new StringBuilder(
                "# WildFly Swarm application-users.properties file generated by CARGO\n");

        for (User user : users)
        {
            usersToken.append(WildFlySwarmUserUtils.generateUserPasswordLine(
                user, "ApplicationRealm"));
        }

        ConfigurationContext context = getConfigurationContext();
        File applicationUsers = new File(
            context.getConfigurationHome(), "/application-users.properties");

        context.getFileHandler().writeTextFile(applicationUsers.getAbsolutePath(),
            usersToken.toString(), StandardCharsets.ISO_8859_1);

        return applicationUsers;
    }

    /**
     * Create application roles file (application-roles.properties).
     * @param users list of users to create.
     * @return application-roles.properties file.
     */
    private File createApplicationRolesProperties(List<User> users)
    {
        StringBuilder rolesToken = new StringBuilder(
                "# WildFly Swarm application-roles.properties file generated by CARGO\n");

        for (User user : users)
        {
            rolesToken.append(user.getName());
            rolesToken.append("=");
            rolesToken.append(String.join(",", user.getRoles()));
            rolesToken.append('\n');
        }

        ConfigurationContext context = getConfigurationContext();
        File applicationRoles = new File(
            context.getConfigurationHome(), "/application-roles.properties");
        context.getFileHandler().writeTextFile(applicationRoles.getAbsolutePath(),
            rolesToken.toString(), StandardCharsets.ISO_8859_1);

        return applicationRoles;
    }
}
