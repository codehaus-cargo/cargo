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
package org.codehaus.cargo.sample.java.jonas;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;

import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.jonas.JonasPropertySet;
import org.codehaus.cargo.sample.java.AbstractCargoTestCase;
import org.codehaus.cargo.sample.java.CargoTestSuite;
import org.codehaus.cargo.sample.java.EnvironmentTestData;
import org.codehaus.cargo.sample.java.validator.HasStandaloneConfigurationValidator;
import org.codehaus.cargo.sample.java.validator.IsInstalledLocalContainerValidator;
import org.codehaus.cargo.sample.java.validator.StartsWithContainerValidator;
import org.codehaus.cargo.sample.java.validator.Validator;

/**
 * Test the JOnAS-specific standalone local configuration options.
 * 
 * @version $Id$
 */
public class JonasStandaloneConfigurationTest extends AbstractCargoTestCase
{
    /**
     * Initializes the test case.
     * @param testName Test name.
     * @param testData Test environment data.
     * @throws Exception If anything goes wrong.
     */
    public JonasStandaloneConfigurationTest(String testName, EnvironmentTestData testData)
        throws Exception
    {
        super(testName, testData);
    }

    /**
     * Creates the test suite, using the {@link Validator}s.
     * @return Test suite.
     * @throws Exception If anything goes wrong.
     */
    public static Test suite() throws Exception
    {
        CargoTestSuite suite =
            new CargoTestSuite("Test that verifies JOnAS-specific standalone local configuration "
                + "options");

        suite.addTestSuite(JonasStandaloneConfigurationTest.class, new Validator[] {
            new StartsWithContainerValidator("jonas"),
            new IsInstalledLocalContainerValidator(),
            new HasStandaloneConfigurationValidator()
        });

        return suite;
    }

    /**
     * Test the method for getting the configurator setter name based on the property name.
     * @throws Exception If anything goes wrong.
     */
    public void testConfiguratorProperties() throws Exception
    {
        System.setProperty(JonasPropertySet.CONFIGURATOR_PREFIX + "ajpPort", "123456");
        System.setProperty(JonasPropertySet.CONFIGURATOR_PREFIX + "ajpConnectorActivation",
            "true");
        checkForValue("123456", true);

        System.getProperties().remove(JonasPropertySet.CONFIGURATOR_PREFIX + "ajpPort");
        System.getProperties().remove(
            JonasPropertySet.CONFIGURATOR_PREFIX + "ajpConnectorActivation");
        checkForValue("123456", false);
    }

    /**
     * Test for a given value in the Tomcat configuration.
     * @param check Value to check for.
     * @param checkExists Whether to check for existence or non-existence.
     * @throws Exception If anything goes wrong.
     */
    protected void checkForValue(String check, boolean checkExists) throws Exception
    {
        setContainer(createContainer(createConfiguration(ConfigurationType.STANDALONE)));
        getInstalledLocalContainer().getConfiguration().configure(getInstalledLocalContainer());

        File conf = new File(getInstalledLocalContainer().getConfiguration().getHome(), "conf");
        if (!conf.isDirectory())
        {
            throw new IllegalArgumentException(conf + " is not a directory");
        }

        List<File> serverXmlFiles = new ArrayList<File>();
        if (getContainer().getId().equals("jonas4x"))
        {
            serverXmlFiles.add(new File(conf, "server.xml"));
        }
        else if (getContainer().getId().equals("jonas5x"))
        {
            for (File confFile : conf.listFiles())
            {
                if (confFile.getName().startsWith("tomcat") && confFile.getName().endsWith(".xml"))
                {
                    serverXmlFiles.add(confFile);
                }
            }

            File deploy = new File(
                getInstalledLocalContainer().getConfiguration().getHome(), "deploy");
            if (deploy.isDirectory())
            {
                for (File deployFile : deploy.listFiles())
                {
                    if (deployFile.getName().startsWith("tomcat")
                        && deployFile.getName().endsWith(".xml"))
                    {
                        serverXmlFiles.add(deployFile);
                    }
                }
            }
        }

        if (serverXmlFiles.isEmpty())
        {
            throw new IllegalArgumentException("Unknown container: " + getContainer().getId());
        }

        for (File serverXmlFile : serverXmlFiles)
        {
            BufferedReader reader = new BufferedReader(new FileReader(serverXmlFile));
            try
            {
                String line;
                while ((line = reader.readLine()) != null)
                {
                    if (checkExists)
                    {
                        if (line.contains(check))
                        {
                            return;
                        }
                    }
                    else
                    {
                        assertTrue("Line in file " + serverXmlFile + " contains " + check,
                            !line.contains(check));
                    }
                }
            }
            finally
            {
                reader.close();
            }
        }

        if (checkExists)
        {
            fail("None of the files " + serverXmlFiles + " contains " + check);
        }
    }
}
