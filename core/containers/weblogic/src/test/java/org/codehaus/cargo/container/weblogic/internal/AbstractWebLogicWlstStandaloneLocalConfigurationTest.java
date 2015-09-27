package org.codehaus.cargo.container.weblogic.internal;

import java.util.List;

import org.codehaus.cargo.container.configuration.entry.Resource;
import org.codehaus.cargo.container.weblogic.WebLogic121xWlstInstalledLocalContainer;
import org.codehaus.cargo.container.weblogic.WebLogic121xWlstStandaloneLocalConfiguration;
import org.codehaus.cargo.container.weblogic.internal.configuration.WebLogicConfigurationEntryType;

import junit.framework.TestCase;

/**
 * Unit tests for {@link AbstractWebLogicWlstStandaloneLocalConfiguration}.
 */
public class AbstractWebLogicWlstStandaloneLocalConfigurationTest extends TestCase
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
     * Test sorting of resources. Resources should be sorted according to their priority set in
     * properties.
     *
     * @throws Exception If anything goes wrong.
     */
    public void testSortResources() throws Exception
    {
        Resource jmsServer =
            new Resource("TestJmsServer", WebLogicConfigurationEntryType.JMS_SERVER);
        jmsServer.setParameter("priority", "10");
        Resource jmsModule =
            new Resource("TestJmsModule", WebLogicConfigurationEntryType.JMS_MODULE);
        jmsModule.setParameter("priority", "20");
        Resource jmsSubdeployment =
            new Resource("TestJmsSubdeployment", WebLogicConfigurationEntryType.JMS_SUBDEPLOYMENT);
        jmsSubdeployment.setParameter("priority", "30");
        Resource jmsQueue =
            new Resource("jms/queue/REQUEST", WebLogicConfigurationEntryType.JMS_QUEUE);

        // adding resources in random order
        configuration.addResource(jmsSubdeployment);
        configuration.addResource(jmsQueue);
        configuration.addResource(jmsModule);
        configuration.addResource(jmsServer);

        configuration.sortResources();

        // resources are sorted according to priority
        List<Resource> resources = configuration.getResources();
        assertEquals(4, resources.size());
        assertEquals(WebLogicConfigurationEntryType.JMS_SERVER, resources.get(0).getType());
        assertEquals(WebLogicConfigurationEntryType.JMS_MODULE, resources.get(1).getType());
        assertEquals(WebLogicConfigurationEntryType.JMS_SUBDEPLOYMENT, resources.get(2).getType());
        assertEquals(WebLogicConfigurationEntryType.JMS_QUEUE, resources.get(3).getType());
    }
}
