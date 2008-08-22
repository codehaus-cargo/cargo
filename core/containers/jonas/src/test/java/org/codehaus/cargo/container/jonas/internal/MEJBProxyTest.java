/* 
 * ========================================================================
 * 
 * Copyright 2007-2008 OW2.
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

import org.codehaus.cargo.container.configuration.RuntimeConfiguration;
import org.codehaus.cargo.container.jonas.JonasPropertySet;
import org.codehaus.cargo.container.jonas.JonasRuntimeConfiguration;
import org.codehaus.cargo.container.jonas.internal.MEJBMBeanServerConnectionFactory.MEJBProxy;
import org.jmock.MockObjectTestCase;

/**
 * Unit tests for {@link MEJBProxy}.
 */
public class MEJBProxyTest extends MockObjectTestCase
{
    public void testNoJAASOptions()
    {
        RuntimeConfiguration configuration = new JonasRuntimeConfiguration();
        MEJBProxy proxy = new MEJBProxy(null, configuration);

        assertNull(proxy.mejb);
        assertNull(proxy.jaasFile);
        assertNull(proxy.jaasRole);
    }

    public void testOnlyJAASFilename()
    {
        RuntimeConfiguration configuration = new JonasRuntimeConfiguration();
        configuration.setProperty(JonasPropertySet.JONAS_MEJB_JAAS_FILE, "foo");
        MEJBProxy proxy = new MEJBProxy(null, configuration);

        assertNull(proxy.mejb);
        assertNull(proxy.jaasFile);
        assertNull(proxy.jaasRole);
    }

    public void testOnlyJAASRole()
    {
        RuntimeConfiguration configuration = new JonasRuntimeConfiguration();
        configuration.setProperty(JonasPropertySet.JONAS_MEJB_JAAS_ROLE, "bar");
        MEJBProxy proxy = new MEJBProxy(null, configuration);

        assertNull(proxy.mejb);
        assertEquals(proxy.jaasFile, MEJBProxy.DEFAULT_JAAS_FILE);
        assertEquals(proxy.jaasRole, "bar");
    }

    public void testJAASRoleAndFilename()
    {
        RuntimeConfiguration configuration = new JonasRuntimeConfiguration();
        configuration.setProperty(JonasPropertySet.JONAS_MEJB_JAAS_FILE, "foo");
        configuration.setProperty(JonasPropertySet.JONAS_MEJB_JAAS_ROLE, "bar");
        MEJBProxy proxy = new MEJBProxy(null, configuration);

        assertNull(proxy.mejb);
        assertEquals(proxy.jaasFile, "foo");
        assertEquals(proxy.jaasRole, "bar");
    }
}
