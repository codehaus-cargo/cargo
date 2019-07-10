/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2011 Vincent Massol, 2012-2019 Ali Tokmen.
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
package org.codehaus.cargo.sample.maven2.cargo1489;

import junit.framework.TestCase;

/**
 * Test the Uberjar to check for CARGO-1489.
 */
public class Cargo1489Test extends TestCase
{

    /**
     * Test the Uberjar to check for CARGO-1489.
     * @throws Exception If anything fails.
     */
    public void testCargo1489() throws Exception
    {
        ClassLoader loader = this.getClass().getClassLoader();
        loader.loadClass("org.codehaus.cargo.container.jonas.internal."
            + "AbstractJonasStandaloneLocalConfiguration");
        try
        {
            loader.loadClass("org.ow2.jonas.tools.configurator.Jonas");
            fail("The JOnAS Configurator (LGPL) is in the Uberjar classpath, "
                + "this breaks the Codehaus Cargo license (Apache License)");
        }
        catch (ClassNotFoundException expected)
        {
            // Expected exception
        }   
    }

}
