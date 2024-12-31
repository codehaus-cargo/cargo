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
package org.codehaus.cargo.sample.maven3.inPlaceDevelopment_test;

import java.io.File;
import java.io.FileWriter;
import java.net.URL;

import junit.framework.TestCase;

import org.codehaus.cargo.sample.java.PingUtils;
import org.codehaus.cargo.util.log.Logger;
import org.codehaus.cargo.util.log.SimpleLogger;

/**
 * Test the in-place development functionality.
 */
public class InPlaceDevelopmentTest extends TestCase
{

    /**
     * Logger.
     */
    private Logger logger = new SimpleLogger();

    /**
     * Test the in-place development functionality.
     * @throws Exception If anything fails.
     */
    public void testInPlaceDevelopment() throws Exception
    {
        final URL url = new URL("http://localhost:" + System.getProperty("http.port")
            + "/cargo-sample-maven3-inPlaceDevelopment-test/");

        final String initialExpected = "Sample page for testing";
        PingUtils.assertPingTrue(url.getPath() + " not started", initialExpected, url, logger);

        final String modifiedExpected = "Modified page for testing";
        File index = new File(System.getProperty("expandedWebapp.directory"), "index.html");
        assertTrue(index + " does not exist", index.isFile());
        try (FileWriter writer = new FileWriter(index))
        {
            writer.write(modifiedExpected);
        }
        PingUtils.assertPingTrue(url.getPath() + " not modified", modifiedExpected, url, logger);
    }

}
