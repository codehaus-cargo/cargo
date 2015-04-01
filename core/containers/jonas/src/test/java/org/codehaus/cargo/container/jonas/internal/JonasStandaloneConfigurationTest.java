/*
 * ========================================================================
 *
 * Copyright 2007-2008 OW2. Code from this file
 * was originally imported from the OW2 JOnAS project.
 *
 * Codehaus CARGO, copyright 2004-2011 Vincent Massol.
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
package org.codehaus.cargo.container.jonas.internal;

import junit.framework.TestCase;

import org.codehaus.cargo.container.jonas.JonasPropertySet;

/**
 * Unit tests for {@link AbstractJonasRemoteDeployer}.
 * 
 */
public class JonasStandaloneConfigurationTest extends TestCase
{

    /**
     * Test the method for getting the configurator setter name based on the property name.
     */
    public void testConfiguratorSetterNameWithInvalidPropertyName()
    {
        try
        {
            AbstractJonasStandaloneLocalConfiguration.getConfiguratorSetterName("test");
            fail("Non-configurator property name got accepted");
        }
        catch (IllegalArgumentException expected)
        {
            assertTrue("Exception should contain " + JonasPropertySet.CONFIGURATOR_PREFIX,
                expected.getMessage().contains(JonasPropertySet.CONFIGURATOR_PREFIX));
        }
    }

    /**
     * Test the method for getting the configurator setter name based on the property name.
     */
    public void testConfiguratorSetterNameWithUppercaseFirstLetter()
    {
        String setterName = AbstractJonasStandaloneLocalConfiguration.getConfiguratorSetterName(
            JonasPropertySet.CONFIGURATOR_PREFIX + "HttpSessionReplicationActivation");
        assertEquals("setHttpSessionReplicationActivation", setterName);
    }

    /**
     * Test the method for getting the configurator setter name based on the property name.
     */
    public void testConfiguratorSetterNameWithLowercaseFirstLetter()
    {
        String setterName = AbstractJonasStandaloneLocalConfiguration.getConfiguratorSetterName(
            JonasPropertySet.CONFIGURATOR_PREFIX + "httpSessionReplicationActivation");
        assertEquals("setHttpSessionReplicationActivation", setterName);
    }

}
