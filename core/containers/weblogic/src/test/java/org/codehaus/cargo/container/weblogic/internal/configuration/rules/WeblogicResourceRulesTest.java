package org.codehaus.cargo.container.weblogic.internal.configuration.rules;

import java.util.List;

import org.codehaus.cargo.container.configuration.builder.ConfigurationEntryType;
import org.codehaus.cargo.container.configuration.entry.Resource;
import org.codehaus.cargo.container.weblogic.WebLogic121xWlstInstalledLocalContainer;
import org.codehaus.cargo.container.weblogic.WebLogic121xWlstStandaloneLocalConfiguration;
import org.codehaus.cargo.container.weblogic.internal.configuration.WeblogicConfigurationEntryType;

import junit.framework.TestCase;

/**
 * Unit tests for {@link WeblogicResourceRules}.
 */
public class WeblogicResourceRulesTest extends TestCase
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
    private WebLogic121xWlstInstalledLocalContainer container;

    /**
     * Configuration.
     */
    private WebLogic121xWlstStandaloneLocalConfiguration configuration;

    /**
     * Creates the test file system manager and the container. {@inheritDoc}
     *
     * @throws Exception If anything goes wrong.
     */
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        this.configuration = new WebLogic121xWlstStandaloneLocalConfiguration(DOMAIN_HOME);

        this.container = new WebLogic121xWlstInstalledLocalContainer(configuration);
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

        WeblogicResourceRules.addMissingJmsResources(configuration);

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
            new Resource("jms/queue/REQUEST", WeblogicConfigurationEntryType.JMS_QUEUE);

        // adding just JMS queue
        configuration.addResource(jmsQueueResource);

        WeblogicResourceRules.addMissingJmsResources(configuration);

        // JMS server, module and subdeployment are added
        List<Resource> resources = configuration.getResources();
        assertEquals(4, resources.size());

        Resource jmsServer = null;
        Resource jmsModule = null;
        Resource jmsSubdeployment = null;
        Resource jmsQueue = null;
        for (Resource resource : configuration.getResources())
        {
            if (WeblogicConfigurationEntryType.JMS_SERVER.equals(resource.getType()))
            {
                jmsServer = resource;
            }
            else if (WeblogicConfigurationEntryType.JMS_MODULE.equals(resource.getType()))
            {
                jmsModule = resource;
            }
            else if (WeblogicConfigurationEntryType.JMS_SUBDEPLOYMENT.equals(resource.getType()))
            {
                jmsSubdeployment = resource;
            }
            else if (WeblogicConfigurationEntryType.JMS_QUEUE.equals(resource.getType()))
            {
                jmsQueue = resource;
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
            new Resource("TestJmsServer", WeblogicConfigurationEntryType.JMS_SERVER);
        jmsServerResource.setParameter("priority", "10");
        Resource jmsSubdeploymentResource =
            new Resource("TestJmsSubdeployment", WeblogicConfigurationEntryType.JMS_SUBDEPLOYMENT);
        jmsSubdeploymentResource.setParameter("priority", "30");
        Resource jmsQueueResource =
            new Resource("jms/queue/REQUEST", WeblogicConfigurationEntryType.JMS_QUEUE);

        // adding JMS resources
        configuration.addResource(jmsServerResource);
        configuration.addResource(jmsSubdeploymentResource);
        configuration.addResource(jmsQueueResource);

        WeblogicResourceRules.addMissingJmsResources(configuration);

        // JMS module is added
        List<Resource> resources = configuration.getResources();
        assertEquals(4, resources.size());

        Resource jmsServer = null;
        Resource jmsModule = null;
        Resource jmsSubdeployment = null;
        Resource jmsQueue = null;
        for (Resource resource : configuration.getResources())
        {
            if (WeblogicConfigurationEntryType.JMS_SERVER.equals(resource.getType()))
            {
                jmsServer = resource;
            }
            else if (WeblogicConfigurationEntryType.JMS_MODULE.equals(resource.getType()))
            {
                jmsModule = resource;
            }
            else if (WeblogicConfigurationEntryType.JMS_SUBDEPLOYMENT.equals(resource.getType()))
            {
                jmsSubdeployment = resource;
            }
            else if (WeblogicConfigurationEntryType.JMS_QUEUE.equals(resource.getType()))
            {
                jmsQueue = resource;
            }
        }
        assertNotNull(jmsServer);
        assertNotNull(jmsModule);
        assertNotNull(jmsSubdeployment);
        assertNotNull(jmsQueue);
    }
}
