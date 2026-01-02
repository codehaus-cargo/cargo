/*
 * ========================================================================
 *
 * Copyright 2007-2008 OW2. Code from this file
 * was originally imported from the OW2 JOnAS project.
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2026 Ali Tokmen.
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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import org.codehaus.cargo.container.jonas.JonasPropertySet;

/**
 * Unit tests for {@link AbstractJonasRemoteDeployer}.
 */
public class JonasStandaloneConfigurationTest
{

    /**
     * Test the method for getting the configurator setter name based on the property name.
     */
    @Test
    public void testConfiguratorSetterNameWithInvalidPropertyName()
    {
        try
        {
            AbstractJonasStandaloneLocalConfiguration.getConfiguratorSetterName("test");
            Assertions.fail("Non-configurator property name got accepted");
        }
        catch (IllegalArgumentException expected)
        {
            Assertions.assertTrue(
                expected.getMessage().contains(JonasPropertySet.CONFIGURATOR_PREFIX),
                    "Exception should contain " + JonasPropertySet.CONFIGURATOR_PREFIX);
        }
    }

    /**
     * Test the method for getting the configurator setter name based on the property name.
     */
    @Test
    public void testConfiguratorSetterNameWithUppercaseFirstLetter()
    {
        String setterName = AbstractJonasStandaloneLocalConfiguration.getConfiguratorSetterName(
            JonasPropertySet.CONFIGURATOR_PREFIX + "HttpSessionReplicationActivation");
        Assertions.assertEquals("setHttpSessionReplicationActivation", setterName);
    }

    /**
     * Test the method for getting the configurator setter name based on the property name.
     */
    @Test
    public void testConfiguratorSetterNameWithLowercaseFirstLetter()
    {
        String setterName = AbstractJonasStandaloneLocalConfiguration.getConfiguratorSetterName(
            JonasPropertySet.CONFIGURATOR_PREFIX + "httpSessionReplicationActivation");
        Assertions.assertEquals("setHttpSessionReplicationActivation", setterName);
    }

}
