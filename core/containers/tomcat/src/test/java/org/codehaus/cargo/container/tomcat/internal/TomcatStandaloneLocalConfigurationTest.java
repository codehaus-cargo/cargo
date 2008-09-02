/* 
 * ========================================================================
 * 
 * Copyright 2004-2005 Vincent Massol.
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
package org.codehaus.cargo.container.tomcat.internal;

import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.container.tomcat.Tomcat3xStandaloneLocalConfiguration;
import org.codehaus.cargo.container.tomcat.internal.AbstractTomcatStandaloneLocalConfiguration;

import junit.framework.TestCase;

import java.io.File;

/**
 * Unit tests for {@link AbstractTomcatStandaloneLocalConfiguration}.
 * 
 * @version $Id$
 */
public class TomcatStandaloneLocalConfigurationTest extends TestCase
{
    public void testGetSecurityToken()
    {
        Tomcat3xStandaloneLocalConfiguration configuration =
            new Tomcat3xStandaloneLocalConfiguration("somewhere");
        configuration.setProperty(ServletPropertySet.USERS, "n1:p1:r1|n2:p2:r2");

        assertEquals(" <user name=\"n1\" password=\"p1\" roles=\"r1\"/>"
            + "<user name=\"n2\" password=\"p2\" roles=\"r2\"/>", configuration.getSecurityToken());
    }
}
