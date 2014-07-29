/*
 * ========================================================================
 *
 * Copyright 2007-2008 OW2. Code from this file
 * was originally imported from the OW2 JOnAS project.
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
package org.codehaus.cargo.container.jonas;

import junit.framework.TestCase;

import org.apache.commons.vfs.impl.StandardFileSystemManager;
import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.util.FileHandler;
import org.codehaus.cargo.util.VFSFileHandler;

/**
 * Unit tests for {@link Jonas4xExistingLocalConfiguration}.
 * 
 * @version $Id$
 */
public class Jonas4xExistingLocalConfigurationTest extends TestCase
{
    /**
     * JONAS_ROOT folder for tests.
     */
    private static final String JONAS_ROOT = "ram:///jonasroot";

    /**
     * Container.
     */
    private Jonas4xInstalledLocalContainer container;

    /**
     * Container configuration.
     */
    private Jonas4xExistingLocalConfiguration configuration;

    /**
     * File system manager.
     */
    private StandardFileSystemManager fsManager;

    /**
     * File handler.
     */
    private FileHandler fileHandler;

    /**
     * Creates the test file system manager and the container. {@inheritDoc}
     * @throws Exception If anything goes wrong.
     */
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        this.fsManager = new StandardFileSystemManager();
        this.fsManager.init();
        this.fileHandler = new VFSFileHandler(this.fsManager);

        this.fileHandler.createDirectory(null, JONAS_ROOT);

        this.configuration = new Jonas4xExistingLocalConfiguration(JONAS_ROOT);
        this.configuration.setFileHandler(fileHandler);

        this.container = new Jonas4xInstalledLocalContainer(configuration);
        this.container.setFileHandler(this.fileHandler);
        this.container.setHome(JONAS_ROOT);
    }

    /**
     * Closes the test file system manager. {@inheritDoc}
     * @throws Exception If anything goes wrong.
     */
    @Override
    protected void tearDown() throws Exception
    {
        if (fsManager != null)
        {
            fsManager.close();
        }

        super.tearDown();
    }

    /**
     * Test {@link
     * Jonas4xExistingLocalConfiguration#doConfigure(org.codehaus.cargo.container.LocalContainer)}
     * @throws Exception If anything goes wrong.
     */
    public void testDoConfigure() throws Exception
    {
        try
        {
            configuration.doConfigure(container);
            fail("No ContainerException raised");
        }
        catch (ContainerException expected)
        {
            // Expected
        }

        fileHandler.createDirectory(JONAS_ROOT, "conf");
        fileHandler.createDirectory(JONAS_ROOT, "apps");
        fileHandler.createDirectory(JONAS_ROOT, "apps/autoload");
        fileHandler.createDirectory(JONAS_ROOT, "webapps");
        fileHandler.createDirectory(JONAS_ROOT, "webapps/autoload");
        fileHandler.createDirectory(JONAS_ROOT, "ejbjars");
        fileHandler.createDirectory(JONAS_ROOT, "ejbjars/autoload");

        configuration.doConfigure(container);
    }
}
