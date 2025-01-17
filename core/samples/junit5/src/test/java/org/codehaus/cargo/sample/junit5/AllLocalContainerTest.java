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
package org.codehaus.cargo.sample.junit5;

import java.lang.reflect.Method;

import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.State;
import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.junit.jupiter.api.Assertions;

/**
 * Test for local containers.
 */
public class AllLocalContainerTest extends AbstractCargoTestCase
{
    @Override
    public boolean isSupported(String containerId, ContainerType containerType, Method testMethod)
    {
        if (!containerType.isLocal())
        {
            return false;
        }

        if ("testRestartWithNoDeployable".equals(testMethod.getName()))
        {
            // GlassFish 4.1.1 and 4.1.2 have a bug where redeployment sometimes causes exception:
            // Keys cannot be duplicate. Old value of this key property, null will be retained.
            if ("glassfish4x".equals(containerId))
            {
                return false;
            }

            // JOnAS 4.x has trouble restarting too quickly, skip
            if ("jonas4x".equals(containerId))
            {
                return false;
            }

            // Embedded Jetty containers have trouble restarting too quickly, skip
            if (ContainerType.EMBEDDED.equals(containerType)
                && containerId.startsWith("jetty"))
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Smoke test: startup with no deployable.
     * @throws Exception If anything goes wrong.
     */
    @CargoTestcase
    public void testStartWithNoDeployable() throws Exception
    {
        setContainer(createContainer(createConfiguration(ConfigurationType.STANDALONE)));

        getLocalContainer().start();
        Assertions.assertEquals(State.STARTED, getContainer().getState());

        try
        {
            getLocalContainer().start();
            Assertions.fail("the second start attempt did not fail");
        }
        catch (ContainerException expected)
        {
            Assertions.assertTrue(expected.getMessage().contains("restart"),
                expected.getMessage() + " does not contain the word 'restart'");
        }

        getLocalContainer().stop();
        Assertions.assertEquals(State.STOPPED, getContainer().getState());
    }

    /**
     * Smoke test: startup (twice) with no deployable.
     * @throws Exception If anything goes wrong.
     */
    @CargoTestcase
    public void testRestartWithNoDeployable() throws Exception
    {
        setContainer(createContainer(createConfiguration(ConfigurationType.STANDALONE)));

        getLocalContainer().start();
        Assertions.assertEquals(State.STARTED, getContainer().getState());

        getLocalContainer().restart();
        Assertions.assertEquals(State.STARTED, getContainer().getState());

        getLocalContainer().stop();
        Assertions.assertEquals(State.STOPPED, getContainer().getState());

        getLocalContainer().restart();
        Assertions.assertEquals(State.STARTED, getContainer().getState());

        getLocalContainer().stop();
        Assertions.assertEquals(State.STOPPED, getContainer().getState());
    }
}
