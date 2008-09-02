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
package org.codehaus.cargo.container.jonas;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import junit.framework.TestCase;

import org.apache.commons.vfs.impl.StandardFileSystemManager;
import org.apache.tools.ant.filters.ReplaceTokens;
import org.apache.tools.ant.types.FilterChain;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.util.FileHandler;
import org.codehaus.cargo.util.VFSFileHandler;

/**
 * Unit tests for {@link Jonas5xStandaloneLocalConfiguration}.
 */
public class Jonas5xStandaloneLocalConfigurationTest extends TestCase
{
    private static final String JONAS_ROOT = "ram:///jonasroot";

    private static final String JONAS_BASE = "ram:///jonasbase";

    private Jonas5xInstalledLocalContainer container;

    private StandardFileSystemManager fsManager;

    private FileHandler fileHandler;

    private Jonas5xStandaloneLocalConfiguration configuration;

    /**
     * {@inheritDoc}
     * 
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();

        this.fsManager = new StandardFileSystemManager();
        this.fsManager.init();
        this.fileHandler = new VFSFileHandler(this.fsManager);

        fileHandler.createDirectory(null, JONAS_ROOT);
        fileHandler.createDirectory(null, JONAS_BASE);

        configuration = new Jonas5xStandaloneLocalConfiguration(JONAS_BASE);
        configuration.setFileHandler(fileHandler);

        this.container = new Jonas5xInstalledLocalContainer(configuration);
        this.container.setFileHandler(this.fileHandler);
        this.container.setHome(JONAS_ROOT);
    }

    public void testDoConfigure() throws Exception
    {
        configuration.setProperty(JonasPropertySet.JONAS_WEBCONTAINER_CLASS_NAME,
                "com.foo.bar.WebContainerImpl");

        configuration.doConfigure(container);

        assertTrue(fileHandler.exists(JONAS_BASE + "/deploy"));

        assertTrue(fileHandler.exists(JONAS_BASE + "/conf"));
        assertTrue(fileHandler.exists(JONAS_BASE + "/logs"));
    }

    public void testCreateJonasFilterChain() throws Exception
    {
        configuration.setProperty(GeneralPropertySet.PROTOCOL, "https");
        configuration.setProperty(ServletPropertySet.PORT, "8080");
        configuration.setProperty(GeneralPropertySet.HOSTNAME, "testhost");
        configuration.setProperty(GeneralPropertySet.RMI_PORT, "1098");
        configuration.setProperty(GeneralPropertySet.LOGGING, "low"); // cargo compliant level
        configuration.setProperty(JonasPropertySet.JONAS_REALM_NAME, "test_realm_name");
        configuration.setProperty(JonasPropertySet.JONAS_AVAILABLES_DATASOURCES, "testds1,testds2");
        configuration
                .setProperty(JonasPropertySet.JONAS_WEBCONTAINER_CLASS_NAME, "test.impl.Class");

        Map replacements = new HashMap();
        replacements.put(GeneralPropertySet.PROTOCOL, "test @" + GeneralPropertySet.PROTOCOL
                + "@ test");
        replacements.put(ServletPropertySet.PORT, "test @" + ServletPropertySet.PORT + "@ test");
        replacements.put(GeneralPropertySet.HOSTNAME, "test @" + GeneralPropertySet.HOSTNAME
                + "@ test");
        replacements.put(GeneralPropertySet.RMI_PORT, "test @" + GeneralPropertySet.RMI_PORT
                + "@ test");
        replacements.put(GeneralPropertySet.LOGGING, "test @" + GeneralPropertySet.LOGGING
                + "@ test");
        replacements.put(JonasPropertySet.JONAS_REALM_NAME, "test @"
                + JonasPropertySet.JONAS_REALM_NAME + "@ test");
        replacements.put(JonasPropertySet.JONAS_AVAILABLES_DATASOURCES, "test @"
                + JonasPropertySet.JONAS_AVAILABLES_DATASOURCES + "@ test");
        replacements.put(JonasPropertySet.JONAS_WEBCONTAINER_CLASS_NAME, "test @"
                + JonasPropertySet.JONAS_WEBCONTAINER_CLASS_NAME + "@ test");
        Map replacementsResults = new HashMap();

        FilterChain jonasFilterChain = configuration.createJonasFilterChain(this.container);

        processTestChainReplacements(replacements, replacementsResults, jonasFilterChain);

        assertEquals("test testhost test", (String) replacementsResults
                .get(GeneralPropertySet.HOSTNAME));
        assertEquals("test https test", (String) replacementsResults
                .get(GeneralPropertySet.PROTOCOL));
        assertEquals("test 8080 test", (String) replacementsResults.get(ServletPropertySet.PORT));
        assertEquals("test 1098 test", (String) replacementsResults
                .get(GeneralPropertySet.RMI_PORT));
        assertEquals("test ERROR test", (String) replacementsResults
                .get(GeneralPropertySet.LOGGING)); // transformed to log4j compliant level by
        // createJonasFilterChain
        assertEquals("test test_realm_name test", (String) replacementsResults
                .get(JonasPropertySet.JONAS_REALM_NAME));
        assertEquals("test testds1,testds2 test", (String) replacementsResults
                .get(JonasPropertySet.JONAS_AVAILABLES_DATASOURCES));
        assertEquals("test test.impl.Class test", (String) replacementsResults
                .get(JonasPropertySet.JONAS_WEBCONTAINER_CLASS_NAME));
    }

    public void testCreateUserFilterChain() throws Exception
    {
        Map replacementsResults = new HashMap();
        Map replacements = new HashMap();
        replacements.put(Jonas5xStandaloneLocalConfiguration.TOKEN_FILTER_KEY_USERS_ROLE, "test @"
                + Jonas5xStandaloneLocalConfiguration.TOKEN_FILTER_KEY_USERS_ROLE + "@ test");
        replacements.put(Jonas5xStandaloneLocalConfiguration.TOKEN_FILTER_KEY_USERS_USER, "test @"
                + Jonas5xStandaloneLocalConfiguration.TOKEN_FILTER_KEY_USERS_USER + "@ test");

        FilterChain usersFilterChain = new FilterChain();
        configuration.createUserFilterChain(usersFilterChain);
        processTestChainReplacements(replacements, replacementsResults, usersFilterChain);
        assertEquals("test <!-- no cargo roles defined --> test", (String) replacementsResults
                .get(Jonas5xStandaloneLocalConfiguration.TOKEN_FILTER_KEY_USERS_ROLE));
        assertEquals("test <!-- no cargo users defined --> test", (String) replacementsResults
                .get(Jonas5xStandaloneLocalConfiguration.TOKEN_FILTER_KEY_USERS_USER));

        replacementsResults.clear();
        usersFilterChain = new FilterChain();
        configuration.setProperty(ServletPropertySet.USERS, "test:pwd:testrole1,testrole2");
        configuration.createUserFilterChain(usersFilterChain);
        processTestChainReplacements(replacements, replacementsResults, usersFilterChain);
        assertEquals(
                "test <role name=\"testrole1\" description=\"Cargo standalone configuration auto generated role\" />\n<role name=\"testrole2\" description=\"Cargo standalone configuration auto generated role\" /> test",
                (String) replacementsResults
                        .get(Jonas5xStandaloneLocalConfiguration.TOKEN_FILTER_KEY_USERS_ROLE));
        assertEquals(
                "test <user name=\"test\" password=\"pwd\" roles=\"testrole1,testrole2\" /> test",
                (String) replacementsResults
                        .get(Jonas5xStandaloneLocalConfiguration.TOKEN_FILTER_KEY_USERS_USER));

        replacementsResults.clear();
        usersFilterChain = new FilterChain();
        configuration.setProperty(ServletPropertySet.USERS,
                "test1:pwd:testrole1,testrole2|test2:pwd:testrole1");
        configuration.createUserFilterChain(usersFilterChain);
        processTestChainReplacements(replacements, replacementsResults, usersFilterChain);
        assertEquals(
                "test <role name=\"testrole1\" description=\"Cargo standalone configuration auto generated role\" />\n<role name=\"testrole2\" description=\"Cargo standalone configuration auto generated role\" /> test",
                (String) replacementsResults
                        .get(Jonas5xStandaloneLocalConfiguration.TOKEN_FILTER_KEY_USERS_ROLE));
        assertEquals(
                "test <user name=\"test1\" password=\"pwd\" roles=\"testrole1,testrole2\" />\n<user name=\"test2\" password=\"pwd\" roles=\"testrole1\" /> test",
                (String) replacementsResults
                        .get(Jonas5xStandaloneLocalConfiguration.TOKEN_FILTER_KEY_USERS_USER));
    }

    private void processTestChainReplacements(Map replacements, Map replacementsResults,
            FilterChain filterChain) throws IOException
    {
        for (Iterator i = replacements.keySet().iterator(); i.hasNext();)
        {
            String keyName = (String) i.next();
            Vector readers = filterChain.getFilterReaders();
            for (Enumeration e = readers.elements(); e.hasMoreElements();)
            {
                ReplaceTokens replaceToken = (ReplaceTokens) e.nextElement();
                Reader replacedReader = replaceToken.chain(new StringReader((String) replacements
                        .get(keyName)));
                int readen = 0;
                StringBuffer replaced = new StringBuffer();
                while ((readen = replacedReader.read()) != -1)
                {
                    replaced.append((char) readen);
                }
                if (replaced.indexOf("@") == -1)
                {
                    replacementsResults.put(keyName, replaced.toString());
                }
            }
        }
    }
}
