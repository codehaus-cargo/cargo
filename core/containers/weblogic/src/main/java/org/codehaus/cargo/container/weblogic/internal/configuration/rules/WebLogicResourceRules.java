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
package org.codehaus.cargo.container.weblogic.internal.configuration.rules;

import java.util.List;

import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.configuration.entry.Resource;
import org.codehaus.cargo.container.weblogic.WebLogicPropertySet;
import org.codehaus.cargo.container.weblogic.internal.configuration.WebLogicConfigurationEntryType;

/**
 * Rule class defining rules which need to be applied for WebLogic resources.
 */
public final class WebLogicResourceRules
{
    /**
     * Private constructor, no need to instantiate utility class.
     */
    private WebLogicResourceRules()
    {
    }

    /**
     * Method used for adding resources needed by WebLogic JMS in case they weren't defined in Cargo
     * properties.
     * 
     * @param configuration WebLogic configuration containing all we need to add resources.
     */
    public static void addMissingJmsResources(LocalConfiguration configuration)
    {
        // check what all JMS resources we want to create
        boolean containsJmsServer = false;
        boolean containsJmsSubdeployment = false;
        boolean containsJmsModule = false;
        boolean containsJmsConnectionFactory = false;
        boolean containsJmsQueue = false;

        List<Resource> weblogicResources = configuration.getResources();
        for (Resource resource : weblogicResources)
        {
            if (null != resource.getType())
            {
                switch (resource.getType())
                {
                    case WebLogicConfigurationEntryType.JMS_SERVER:
                        containsJmsServer = true;
                        break;
                    case WebLogicConfigurationEntryType.JMS_MODULE:
                        containsJmsModule = true;
                        break;
                    case WebLogicConfigurationEntryType.JMS_SUBDEPLOYMENT:
                        containsJmsSubdeployment = true;
                        break;
                    case WebLogicConfigurationEntryType.JMS_CONNECTION_FACTORY:
                        containsJmsConnectionFactory = true;
                        break;
                    case WebLogicConfigurationEntryType.JMS_QUEUE:
                        containsJmsQueue = true;
                        break;
                    default:
                        break;
                }
            }
        }

        // add resources which are missing but needed to be created

        // if we have JMS connection factory or queue to create, but missing JMS server then add it
        if ((containsJmsConnectionFactory || containsJmsQueue) && !containsJmsServer)
        {
            String jmsServerName = configuration.getPropertyValue(WebLogicPropertySet.JMS_SERVER);
            Resource jmsServer =
                new Resource(jmsServerName, WebLogicConfigurationEntryType.JMS_SERVER);
            jmsServer.setId(jmsServerName);
            weblogicResources.add(jmsServer);
        }
        // if we have JMS connection factory or queue to create, but missing JMS module then add it
        if ((containsJmsConnectionFactory || containsJmsQueue) && !containsJmsModule)
        {
            String jmsModuleName = configuration.getPropertyValue(WebLogicPropertySet.JMS_MODULE);
            Resource jmsModule =
                new Resource(jmsModuleName, WebLogicConfigurationEntryType.JMS_MODULE);
            jmsModule.setId(jmsModuleName);
            weblogicResources.add(jmsModule);
        }
        // if we have JMS connection factory or queue to create, but missing JMS subdeployment then
        // add it
        if ((containsJmsConnectionFactory || containsJmsQueue) && !containsJmsSubdeployment)
        {
            String jmsSubdeploymentName =
                configuration.getPropertyValue(WebLogicPropertySet.JMS_SUBDEPLOYMENT);
            Resource jmsSubdeployment =
                new Resource(jmsSubdeploymentName,
                    WebLogicConfigurationEntryType.JMS_SUBDEPLOYMENT);
            jmsSubdeployment.setId(jmsSubdeploymentName);
            weblogicResources.add(jmsSubdeployment);
        }
    }
}
