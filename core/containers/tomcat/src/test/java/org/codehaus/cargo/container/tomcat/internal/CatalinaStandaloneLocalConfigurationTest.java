/* 
 * ========================================================================
 * 
 * Copyright 2005-2006 Vincent Massol.
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

import org.codehaus.cargo.container.deployable.EAR;
import org.codehaus.cargo.container.tomcat.Tomcat4xStandaloneLocalConfiguration;
import org.codehaus.cargo.util.CargoException;

import junit.framework.TestCase;

/**
 * Unit tests for {@link AbstractCatalinaStandaloneLocalConfiguration}.
 * 
 * @version $Id$
 */
public class CatalinaStandaloneLocalConfigurationTest extends TestCase
{
    public void testCreateTomcatFilterChainWhenTryingToDeployAnEar()
    {
        Tomcat4xStandaloneLocalConfiguration configuration = 
            new Tomcat4xStandaloneLocalConfiguration("somewhere");
        configuration.addDeployable(new EAR("some.ear"));

        try
        {
            configuration.createTomcatFilterChain();
            fail("An exception should have been raised here!");
        }
        catch (CargoException expected)
        {
            assertEquals("Only WAR archives are supported for deployment in Tomcat. Got [some.ear]",
                expected.getMessage());
        }
    }
}
