/* 
 * ========================================================================
 * 
 * Copyright 2004-2006 Vincent Massol.
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
package org.codehaus.cargo.container.orion.internal;

import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.container.orion.OrionStandaloneLocalConfiguration;

import junit.framework.TestCase;

/**
 * Unit tests for {@link org.codehaus.cargo.container.orion.OrionStandaloneLocalConfiguration}.
 * 
 * @version $Id$
 */
public class OrionStandaloneLocalConfigurationTest extends TestCase
{
    public void testGetRoleToken()
    {
        OrionStandaloneLocalConfiguration configuration = new OrionStandaloneLocalConfiguration(
            "something");
        configuration.setProperty(ServletPropertySet.USERS, "u1:p1:r1,r2|u2:p2:r2,r3");

        String token = configuration.getRoleToken();
        assertTrue(token.indexOf("<security-role-mapping name=\"r1\">"
            + "<user name=\"u1\"/></security-role-mapping>") > -1);
        assertTrue(token.indexOf("<security-role-mapping name=\"r2\">"
            + "<user name=\"u1\"/><user name=\"u2\"/></security-role-mapping>") > -1);
        assertTrue(token.indexOf("<security-role-mapping name=\"r3\">"
            + "<user name=\"u2\"/></security-role-mapping>") > -1);
    }

    public void testGetUserToken()
    {
        OrionStandaloneLocalConfiguration configuration = new OrionStandaloneLocalConfiguration(
            "something");
        configuration.setProperty(ServletPropertySet.USERS, "u1:p1:r1,r2|u2:p2:r2,r3");

        String token = configuration.getUserToken();
        assertEquals(" "
            + "<user deactivated=\"false\" username=\"u1\" password=\"p1\"/>"
            + "<user deactivated=\"false\" username=\"u2\" password=\"p2\"/>",
            token);
    }
}
