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
package org.codehaus.cargo.container.weblogic;

import junit.framework.TestCase;

import org.apache.commons.vfs.impl.StandardFileSystemManager;
import org.codehaus.cargo.util.FileHandler;
import org.codehaus.cargo.util.VFSFileHandler;

/**
 * Unit tests for {@link WebLogicStandaloneLocalConfiguration}.
 */
public class WebLogic8xStandaloneLocalConfigurationTest extends TestCase
{
    private static final String BEA_HOME = "ram:/bea";
    private static final String DOMAIN_HOME = BEA_HOME + "/mydomain";
    private static final String WL_HOME = BEA_HOME + "/weblogic8";

    private WebLogic8xInstalledLocalContainer container;

    private WebLogicStandaloneLocalConfiguration configuration;

    private StandardFileSystemManager fsManager;
    private FileHandler fileHandler;

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
        fileHandler.mkdirs(DOMAIN_HOME);
        fileHandler.mkdirs(WL_HOME);
        this.configuration = new WebLogicStandaloneLocalConfiguration(
                DOMAIN_HOME);
        this.configuration.setFileHandler(this.fileHandler);

        this.container = new WebLogic8xInstalledLocalContainer(configuration);
        this.container.setHome(WL_HOME);
        this.container.setFileHandler(this.fileHandler);

    }

    public void testDoConfigure() throws Exception
    {
        configuration.doConfigure(container);

        assertTrue(fileHandler.exists(DOMAIN_HOME + "/config.xml"));
        assertTrue(fileHandler.exists(DOMAIN_HOME
                + "/DefaultAuthenticatorInit.ldift"));
        assertTrue(fileHandler
                .exists(DOMAIN_HOME + "/applications/cargocpc.war"));

    }

}
