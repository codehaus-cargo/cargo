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

import junit.framework.TestCase;

import org.apache.commons.vfs.impl.StandardFileSystemManager;
import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.util.FileHandler;
import org.codehaus.cargo.util.VFSFileHandler;

/**
 * Unit tests for {@link Jonas5xExistingLocalConfiguration}.
 */
public class Jonas5xExistingLocalConfigurationTest extends TestCase
{
    private static final String JONAS_ROOT = "ram:///jonasroot";

    private Jonas5xInstalledLocalContainer container;

    private StandardFileSystemManager fsManager;

    private FileHandler fileHandler;

    private Jonas5xExistingLocalConfiguration configuration;

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

        configuration = new Jonas5xExistingLocalConfiguration(JONAS_ROOT);
        configuration.setFileHandler(fileHandler);

        this.container = new Jonas5xInstalledLocalContainer(configuration);
        this.container.setFileHandler(this.fileHandler);
        this.container.setHome(JONAS_ROOT);

    }

    public void testDoConfigure() throws Exception
    {
        try
        {
            configuration.doConfigure(container);
            fail("No ContainerException raised");
        }
        catch (ContainerException ex)
        {
        }

        fileHandler.createDirectory(JONAS_ROOT, "conf");
        fileHandler.createDirectory(JONAS_ROOT, "deploy");

        try
        {
            configuration.doConfigure(container);
        }
        catch (ContainerException ex)
        {
            ex.printStackTrace();
            fail("ContainerException raised");
        }
    }
}
