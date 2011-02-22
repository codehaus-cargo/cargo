/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2010 Vincent Massol.
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
package org.codehaus.cargo.generic;

import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import org.codehaus.cargo.container.Container;
import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.stub.EmbeddedLocalContainerStub;
import org.codehaus.cargo.container.stub.InstalledLocalContainerStub;
import org.codehaus.cargo.container.stub.RemoteContainerStub;
import org.codehaus.cargo.container.stub.StandaloneLocalConfigurationStub;

/**
 * Unit tests for {@link DefaultContainerFactory}.
 * 
 * @version $Id$
 */
public class DefaultContainerFactoryTest extends TestCase
{
    /**
     * Container factory.
     */
    private ContainerFactory factory;

    /**
     * Creates the container factory. {@inheritdoc}
     * @throws Exception If anything goes wrong.
     */
    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        this.factory = new DefaultContainerFactory();
    }

    /**
     * Test container creation with valid container id.
     */
    public void testCreateContainerWithValidContainerId()
    {
        factory.registerContainer(InstalledLocalContainerStub.ID, ContainerType.INSTALLED,
            InstalledLocalContainerStub.class);
        Container container = factory.createContainer(InstalledLocalContainerStub.ID,
            ContainerType.INSTALLED, new StandaloneLocalConfigurationStub("some/path"));
        assertEquals(InstalledLocalContainerStub.NAME, container.getName());
    }

    /**
     * Test container creation with invalid container id.
     */
    public void testCreateContainerWhenInvalidContainerId()
    {
        try
        {
            factory.createContainer("dummy", ContainerType.INSTALLED, null);
            fail("Should have received an exception for the invalid container id");
        }
        catch (ContainerException expected)
        {
            assertEquals("Cannot create container. There's no registered container for the "
                + "parameters (container [id = [dummy]], container type [installed]). Actually "
                + "there are no valid types registered for this container. Maybe you've made a "
                + "mistake spelling it?", expected.getMessage());
        }
    }

    /**
     * Test container creation with valid container id but invalid type.
     */
    public void testCreateContainerWhenValidContainerIdButInvalidType()
    {
        factory.registerContainer("dummy", ContainerType.EMBEDDED,
            EmbeddedLocalContainerStub.class);

        try
        {
            factory.createContainer("dummy", ContainerType.INSTALLED, null);
            fail("Should have received an exception for the invalid container name");
        }
        catch (ContainerException expected)
        {
            assertEquals("Cannot create container. There's no registered container for the "
                + "parameters (container [id = [dummy]], container type [installed]). Valid types "
                + "for this container are: \n  - embedded", expected.getMessage());
        }
    }

    /**
     * Test the registered containers getters.
     */
    public void testGetRegisteredContainers()
    {
        // Note: This test assumes that these container ids are not real IDs that we use in
        // production.

        // Saves the number of registered containers before our test starts so that we can know
        // how many we've added and verify those additions only.
        int existingSize = factory.getContainerIds().size();

        factory.registerContainer("id1", ContainerType.INSTALLED,
            InstalledLocalContainerStub.class);
        factory.registerContainer("id1", ContainerType.EMBEDDED,
            EmbeddedLocalContainerStub.class);
        factory.registerContainer("id2", ContainerType.INSTALLED,
            InstalledLocalContainerStub.class);
        factory.registerContainer("id3", ContainerType.REMOTE,
            RemoteContainerStub.class);

        Map<String, Set<ContainerType>> ids = factory.getContainerIds();

        assertEquals(3, ids.size() - existingSize);
        assertEquals(2, ids.get("id1").size());
        assertTrue(ids.get("id1").contains(ContainerType.INSTALLED));
        assertTrue(ids.get("id1").contains(ContainerType.EMBEDDED));
        assertFalse(ids.get("id1").contains(ContainerType.REMOTE));
    }
}
