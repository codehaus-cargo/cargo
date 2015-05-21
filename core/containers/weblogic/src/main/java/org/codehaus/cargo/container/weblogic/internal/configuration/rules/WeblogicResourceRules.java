package org.codehaus.cargo.container.weblogic.internal.configuration.rules;

import java.util.List;

import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.configuration.entry.Resource;
import org.codehaus.cargo.container.weblogic.WebLogicPropertySet;
import org.codehaus.cargo.container.weblogic.internal.configuration.WeblogicConfigurationEntryType;

/**
 * Rule class defining rules which need to be applied for Weblogic resources.
 */
public final class WeblogicResourceRules
{
    /**
     * Private constructor, no need to instantiate utility class.
     */
    private WeblogicResourceRules()
    {
    }

    /**
     * Method used for adding resources needed by Weblogic JMS in case they weren't defined in Cargo
     * properties.
     *
     * @param configuration Weblogic configuration containing all we need to add resources.
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
            if (WeblogicConfigurationEntryType.JMS_SERVER.equals(resource.getType()))
            {
                containsJmsServer = true;
            }
            else if (WeblogicConfigurationEntryType.JMS_MODULE.equals(resource.getType()))
            {
                containsJmsModule = true;
            }
            else if (WeblogicConfigurationEntryType.JMS_SUBDEPLOYMENT.equals(resource.getType()))
            {
                containsJmsSubdeployment = true;
            }
            else if (WeblogicConfigurationEntryType.JMS_CONNECTION_FACTORY.equals(resource
                .getType()))
            {
                containsJmsConnectionFactory = true;
            }
            else if (WeblogicConfigurationEntryType.JMS_QUEUE.equals(resource.getType()))
            {
                containsJmsQueue = true;
            }
        }

        // add resources which are missing but needed to be created

        // if we have JMS connection factory or queue to create, but missing JMS server then add it
        if ((containsJmsConnectionFactory || containsJmsQueue) && !containsJmsServer)
        {
            String jmsServerName = configuration.getPropertyValue(WebLogicPropertySet.JMS_SERVER);
            Resource jmsServer =
                new Resource(jmsServerName, WeblogicConfigurationEntryType.JMS_SERVER);
            jmsServer.setParameter("priority", "10");
            weblogicResources.add(jmsServer);
        }
        // if we have JMS connection factory or queue to create, but missing JMS module then add it
        if ((containsJmsConnectionFactory || containsJmsQueue) && !containsJmsModule)
        {
            String jmsModuleName = configuration.getPropertyValue(WebLogicPropertySet.JMS_MODULE);
            Resource jmsModule =
                new Resource(jmsModuleName, WeblogicConfigurationEntryType.JMS_MODULE);
            jmsModule.setParameter("priority", "20");
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
                    WeblogicConfigurationEntryType.JMS_SUBDEPLOYMENT);
            jmsSubdeployment.setParameter("priority", "30");
            weblogicResources.add(jmsSubdeployment);
        }
    }
}
