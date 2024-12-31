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
package org.codehaus.cargo.container.wildfly.internal.configuration.commands.wildfly9.resource;

import java.util.Map;

import org.codehaus.cargo.container.configuration.Configuration;
import org.codehaus.cargo.container.configuration.entry.Resource;
import org.codehaus.cargo.container.wildfly.internal.configuration.commands.AbstractWildFlyScriptCommand;

/**
 * Implementation of Mail configuration script command.
 */
public class MailScriptCommand extends AbstractWildFlyScriptCommand
{

    /**
     * Mail resource.
     */
    private Resource resource;

    /**
     * Sets configuration containing all needed information for building configuration scripts.
     * 
     * @param configuration Container configuration.
     * @param resourcePath Path to configuration script resources.
     * @param resource Resource.
     */
    public MailScriptCommand(Configuration configuration, String resourcePath,
            Resource resource)
    {
        super(configuration, resourcePath);
        this.resource = resource;
    }

    @Override
    protected String getScriptRelativePath()
    {
        return "resource/mail.cli";
    }

    @Override
    protected void addConfigurationScriptProperties(Map<String, String> propertiesMap)
    {
        propertiesMap.put("cargo.resource.id", resource.getId());
        propertiesMap.put("cargo.resource.name", getResourceJndi(resource));

        StringBuilder sessionParameters = new StringBuilder();
        if (resource.getParameter("mail.smtp.from") != null)
        {
            sessionParameters.append("from=");
            sessionParameters.append(resource.getParameter("mail.smtp.from"));
        }
        propertiesMap.put("cargo.mail.session.parameters", sessionParameters.toString());

        String host = resource.getParameter("mail.smtp.host") != null
                ? resource.getParameter("mail.smtp.host") : "localhost";
        String port = resource.getParameter("mail.smtp.port") != null
                ? resource.getParameter("mail.smtp.port") : "25";
        propertiesMap.put("cargo.mail.smtp.host", host);
        propertiesMap.put("cargo.mail.smtp.port", port);
    }
}
