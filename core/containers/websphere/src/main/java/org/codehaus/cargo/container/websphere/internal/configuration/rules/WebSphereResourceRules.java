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
package org.codehaus.cargo.container.websphere.internal.configuration.rules;

import java.util.List;

import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.configuration.entry.Resource;
import org.codehaus.cargo.container.websphere.WebSpherePropertySet;
import org.codehaus.cargo.container.websphere.internal.configuration.WebSphereConfigurationEntryType;

/**
 * Rule class defining rules which need to be applied for WebSphere resources.
 */
public final class WebSphereResourceRules
{
    /**
     * Private constructor, no need to instantiate utility class.
     */
    private WebSphereResourceRules()
    {
    }

    /**
     * Method used for adding resources needed by WebSphere JMS in case they weren't defined
     * in Cargo properties.
     * 
     * @param configuration WebSphere configuration containing all we need to add resources.
     */
    public static void addMissingJmsResources(LocalConfiguration configuration)
    {
        // check what all JMS resources we want to create
        boolean containsJmsSiBus = false;
        boolean containsJmsSiBusMember = false;
        boolean containsJmsConnectionFactory = false;
        boolean containsJmsQueue = false;

        List<Resource> websphereResources = configuration.getResources();
        for (Resource resource : websphereResources)
        {
            if (null != resource.getType())
            {
                switch (resource.getType())
                {
                    case WebSphereConfigurationEntryType.JMS_SIBUS:
                        containsJmsSiBus = true;
                        break;
                    case WebSphereConfigurationEntryType.JMS_SIBUS_MEMBER:
                        containsJmsSiBusMember = true;
                        break;
                    case WebSphereConfigurationEntryType.JMS_CONNECTION_FACTORY:
                        containsJmsConnectionFactory = true;
                        break;
                    case WebSphereConfigurationEntryType.JMS_QUEUE:
                        containsJmsQueue = true;
                        break;
                    default:
                        break;
                }
            }
        }

        // add resources which are missing but needed to be created

        // if we have JMS connection factory or queue to create, but missing JMS SIBus then add it
        if ((containsJmsConnectionFactory || containsJmsQueue) && !containsJmsSiBus)
        {
            String jmsSiBusName = configuration.getPropertyValue(WebSpherePropertySet.JMS_SIBUS);
            Resource jmsSiBus =
                new Resource(jmsSiBusName, WebSphereConfigurationEntryType.JMS_SIBUS);
            jmsSiBus.setId(jmsSiBusName);
            websphereResources.add(jmsSiBus);
        }
        // if we have JMS connection factory or queue to create, but missing JMS SIBus member
        // then add it
        if ((containsJmsConnectionFactory || containsJmsQueue) && !containsJmsSiBusMember)
        {
            Resource jmsSiBusMember =
                new Resource("", WebSphereConfigurationEntryType.JMS_SIBUS_MEMBER);
            websphereResources.add(jmsSiBusMember);
        }
    }
}
