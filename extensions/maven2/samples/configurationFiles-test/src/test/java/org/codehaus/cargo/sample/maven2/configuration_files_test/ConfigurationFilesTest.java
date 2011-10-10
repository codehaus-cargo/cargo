/*
 * ========================================================================
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
package org.codehaus.cargo.sample.maven2.configuration_files_test;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import junit.framework.TestCase;

public class ConfigurationFilesTest extends TestCase
{

    public void testConfigFileDirectory() throws Exception
    {
        Properties properties = loadProperties("test-configFile-directory/test-subfolder");
        assertEquals("12345", properties.getProperty("cargo.servlet.port"));
    }

    public void testConfigFileFile() throws Exception
    {
        Properties properties = loadProperties("test-configFile-file");
        assertEquals("12345", properties.getProperty("cargo.servlet.port"));
    }

    public void testFileDirectory() throws Exception
    {
        Properties properties = loadProperties("test-file-directory/test-subfolder");
        assertEquals("@cargo.servlet.port@", properties.getProperty("cargo.servlet.port"));
    }

    public void testFileFile() throws Exception
    {
        Properties properties = loadProperties("test-file-file");
        assertEquals("@cargo.servlet.port@", properties.getProperty("cargo.servlet.port"));
    }

    private Properties loadProperties(String subDirectory) throws Exception
    {
        File jettyBase = new File("target/jetty-base");
        assertTrue(jettyBase + " is not a directory", jettyBase.isDirectory());

        File propertiesFile = new File(jettyBase, subDirectory + "/test.properties");
        assertTrue(propertiesFile + " is not a file", propertiesFile.isFile());

        Properties properties = new Properties();
        FileInputStream fis = new FileInputStream(propertiesFile);
        try
        {
            properties.load(fis);
        }
        finally
        {
            fis.close();
            fis = null;
            System.gc();
        }

        return properties;
    }

}
