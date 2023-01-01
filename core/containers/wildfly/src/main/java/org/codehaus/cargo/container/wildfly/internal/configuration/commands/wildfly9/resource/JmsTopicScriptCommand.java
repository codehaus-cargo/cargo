/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2023 Ali Tokmen.
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
 * Implementation of JMS topic configuration script command.
 */
public class JmsTopicScriptCommand extends AbstractWildFlyScriptCommand
{

    /**
     * JMS topic resource.
     */
    private Resource resource;

    /**
     * Sets configuration containing all needed information for building configuration scripts.
     * 
     * @param configuration Container configuration.
     * @param resourcePath Path to configuration script resources.
     * @param resource Resource.
     */
    public JmsTopicScriptCommand(Configuration configuration, String resourcePath,
            Resource resource)
    {
        super(configuration, resourcePath);
        this.resource = resource;
    }

    @Override
    protected String getScriptRelativePath()
    {
        return "resource/jms-topic.cli";
    }

    @Override
    protected void addConfigurationScriptProperties(Map<String, String> propertiesMap)
    {
        propertiesMap.put("cargo.resource.id", resource.getId());
        propertiesMap.put("cargo.resource.name", getResourceJndi(resource));
    }
}
