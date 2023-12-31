/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2024 Ali Tokmen.
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

import org.codehaus.cargo.container.configuration.builder.ConfigurationEntryType;
import org.codehaus.cargo.container.configuration.entry.Resource;
import org.codehaus.cargo.container.weblogic.WebLogic121xInstalledLocalContainer;
import org.codehaus.cargo.container.weblogic.WebLogic121xStandaloneLocalConfiguration;
import org.codehaus.cargo.container.weblogic.internal.configuration.WebLogicConfigurationEntryType;

import junit.framework.TestCase;

/**
 * Unit tests for {@link WebLogicResourceRules}.
 */
public class WebLogicResourceRulesTest extends TestCase
{
    /**
     * BEA_HOME
     */
    private static final String BEA_HOME = "ram:/bea";

    /**
     * DOMAIN_HOME
     */
    private static final String DOMAIN_HOME = BEA_HOME + "/mydomain";

    /**
     * WL_HOME
     */
    private static final String WL_HOME = BEA_HOME + "/wlserver";

    /**
     * Container.
     */
    private WebLogic121xInstalledLocalContainer container;

    /**
     * Configuration.
     */
    private WebLogic121xStandaloneLocalConfiguration configuration;

    /**
     * Creates the test file system manager and the container. {@inheritDoc}
     * 
     * @throws Exception If anything goes wrong.
     */
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        this.configuration = new WebLogic121xStandaloneLocalConfiguration(DOMAIN_HOME);

        this.container = new WebLogic121xInstalledLocalContainer(configuration);
        this.container.setHome(WL_HOME);
    }

    /**
     * Test for adding missing JMS resources. If no JMS resource is created then nothing will be
     * added - we don't need JMS server, module or subdeployment.
     * 
     * @throws Exception If anything goes wrong.
     */
    public void testAddMissingJmsResourcesNoJmsResources() throws Exception
    {
        Resource mailSession =
            new Resource("jdbc/mailSession", ConfigurationEntryType.MAIL_SESSION);

        // adding mail session resource
        configuration.addResource(mailSession);

        WebLogicResourceRules.addMissingJmsResources(configuration);

        // no JMS resource is added
        List<Resource> resources = configuration.getResources();
        assertEquals(1, resources.size());
        assertEquals(mailSession, resources.get(0));
    }

    /**
     * Test for adding missing JMS resources. When there is defined at least one JMS resource then
     * we need to create also JMS server, JMS module and JMS subdeployment.
     * 
     * @throws Exception If anything goes wrong.
     */
    public void testAddMissingJmsResourcesAllJmsResources() throws Exception
    {
        Resource jmsQueueResource =
            new Resource("jms/queue/REQUEST", WebLogicConfigurationEntryType.JMS_QUEUE);

        // adding just JMS queue
        configuration.addResource(jmsQueueResource);

        WebLogicResourceRules.addMissingJmsResources(configuration);

        // JMS server, module and subdeployment are added
        List<Resource> resources = configuration.getResources();
        assertEquals(4, resources.size());

        Resource jmsServer = null;
        Resource jmsModule = null;
        Resource jmsSubdeployment = null;
        Resource jmsQueue = null;
        for (Resource resource : configuration.getResources())
        {
            if (null != resource.getType())
            {
                switch (resource.getType())
                {
                    case WebLogicConfigurationEntryType.JMS_SERVER:
                        jmsServer = resource;
                        break;
                    case WebLogicConfigurationEntryType.JMS_MODULE:
                        jmsModule = resource;
                        break;
                    case WebLogicConfigurationEntryType.JMS_SUBDEPLOYMENT:
                        jmsSubdeployment = resource;
                        break;
                    case WebLogicConfigurationEntryType.JMS_QUEUE:
                        jmsQueue = resource;
                        break;
                    default:
                        break;
                }
            }
        }
        assertNotNull(jmsServer);
        assertNotNull(jmsModule);
        assertNotNull(jmsSubdeployment);
        assertNotNull(jmsQueue);
    }

    /**
     * Test for adding missing JMS module resource.
     * 
     * @throws Exception If anything goes wrong.
     */
    public void testAddMissingJmsResourcesJmsModule() throws Exception
    {
        Resource jmsServerResource =
            new Resource("TestJmsServer", WebLogicConfigurationEntryType.JMS_SERVER);
        jmsServerResource.setParameter("priority", "10");
        Resource jmsSubdeploymentResource =
            new Resource("TestJmsSubdeployment", WebLogicConfigurationEntryType.JMS_SUBDEPLOYMENT);
        jmsSubdeploymentResource.setParameter("priority", "30");
        Resource jmsQueueResource =
            new Resource("jms/queue/REQUEST", WebLogicConfigurationEntryType.JMS_QUEUE);

        // adding JMS resources
        configuration.addResource(jmsServerResource);
        configuration.addResource(jmsSubdeploymentResource);
        configuration.addResource(jmsQueueResource);

        WebLogicResourceRules.addMissingJmsResources(configuration);

        // JMS module is added
        List<Resource> resources = configuration.getResources();
        assertEquals(4, resources.size());

        Resource jmsServer = null;
        Resource jmsModule = null;
        Resource jmsSubdeployment = null;
        Resource jmsQueue = null;
        for (Resource resource : configuration.getResources())
        {
            if (null != resource.getType())
            {
                switch (resource.getType())
                {
                    case WebLogicConfigurationEntryType.JMS_SERVER:
                        jmsServer = resource;
                        break;
                    case WebLogicConfigurationEntryType.JMS_MODULE:
                        jmsModule = resource;
                        break;
                    case WebLogicConfigurationEntryType.JMS_SUBDEPLOYMENT:
                        jmsSubdeployment = resource;
                        break;
                    case WebLogicConfigurationEntryType.JMS_QUEUE:
                        jmsQueue = resource;
                        break;
                    default:
                        break;
                }
            }
        }
        assertNotNull(jmsServer);
        assertNotNull(jmsModule);
        assertNotNull(jmsSubdeployment);
        assertNotNull(jmsQueue);
    }
}
