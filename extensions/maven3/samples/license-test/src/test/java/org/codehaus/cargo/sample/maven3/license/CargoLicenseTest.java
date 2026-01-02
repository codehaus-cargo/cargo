/*
 * ========================================================================
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
package org.codehaus.cargo.sample.maven3.license;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test the Uberjar to check for license.
 */
public class CargoLicenseTest
{

    /**
     * Test the Uberjar to check for CARGO-1489 (The Uberjar itself should not contain JOnAS
     * classes).
     * @throws Exception If anything fails.
     */
    @Test
    public void testCargo1489() throws Exception
    {
        // The Uberjar itself should not contain JOnAS classes
        URL uberjar = new File(System.getProperty("cargo-uberjar.file")).toURI().toURL();
        URL[] uberjarUrl = new URL[1];
        uberjarUrl[0] = uberjar;
        try (URLClassLoader loader = new URLClassLoader(uberjarUrl))
        {
            loader.loadClass("org.codehaus.cargo.container.jonas.internal."
                + "AbstractJonasStandaloneLocalConfiguration");
            try
            {
                loader.loadClass("org.ow2.jonas.tools.configurator.Jonas");
                Assertions.fail("The JOnAS Configurator (LGPL) is in the Uberjar classpath, "
                    + "this breaks the Codehaus Cargo license (Apache License)");
            }
            catch (ClassNotFoundException expected)
            {
                // Expected exception
            }
        }

        // The Maven dependency for Uberjar should link to JOnAS classes
        ClassLoader loader = this.getClass().getClassLoader();
        loader.loadClass("org.codehaus.cargo.container.jonas.internal."
            + "AbstractJonasStandaloneLocalConfiguration");
        loader.loadClass("org.ow2.jonas.tools.configurator.Jonas");
    }

    /**
     * Test the Uberjar to check for CARGO-1494 (The Uberjar itself should not contain licensed
     * DTDs).
     * @throws Exception If anything fails.
     */
    @Test
    public void testCargo1494() throws Exception
    {
        // The Uberjar itself should not contain licensed DTDs
        URL uberjar = new File(System.getProperty("cargo-uberjar.file")).toURI().toURL();
        URL[] uberjarUrl = new URL[1];
        uberjarUrl[0] = uberjar;
        try (URLClassLoader loader = new URLClassLoader(uberjarUrl))
        {
            try (InputStream is = loader.getResourceAsStream(
                "org/codehaus/cargo/module/internal/resource/web-app_3_0.xsd"))
            {
                Assertions.assertNull(is, "The licensed DTDs are in the Uberjar classpath, "
                    + "this breaks the Codehaus Cargo license (Apache License)");
            }
        }

        // The Maven dependency for Uberjar should contain the licensed DTDs
        ClassLoader loader = this.getClass().getClassLoader();
        try (InputStream is = loader.getResourceAsStream(
            "org/codehaus/cargo/module/internal/resource/web-app_3_0.xsd"))
        {
            Assertions.assertNotNull(
                is, "The licensed DTDs are not in the Uberjar Maven dependency");
        }
    }

}
