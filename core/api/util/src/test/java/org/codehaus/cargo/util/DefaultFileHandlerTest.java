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
package org.codehaus.cargo.util;

import java.util.UUID;
import junit.framework.TestCase;
import org.apache.tools.ant.types.FilterChain;

/**
 * Unit tests for {@link DefaultFileHandler}.
 * 
 * @version $Id$
 */
public class DefaultFileHandlerTest extends TestCase
{

    /**
     * File handler.
     */
    private FileHandler fileHandler;

    /**
     * Creates the file handler. {@inheritdoc}
     * @throws Exception If anything goes wrong.
     */
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        this.fileHandler = new DefaultFileHandler();
    }

    /**
     * Test relative to absolute path.
     */
    public void testGetAbsolutePathFromRelative()
    {
        String path = this.fileHandler.getAbsolutePath("path");
        assertEquals(path, System.getProperty("user.dir") + System.getProperty("file.separator")
            + "path");
    }

    /**
     * Test explicit to absolute path.
     */
    public void testGetAbsolutePathFromExplicit()
    {
        String path = this.fileHandler.getAbsolutePath(System.getProperty("user.home"));
        assertEquals(path, System.getProperty("user.home"));
    }

    /**
     * Test file copy to a non-existing path.<br />
     * This has been raised by https://jira.codehaus.org/browse/CARGO-1004
     */
    public void testCopyToNonExistingPath()
    {
        String random = UUID.randomUUID().toString();
        assertFalse("Subdirectory " + random + " already exists",
            this.fileHandler.isDirectory("target/" + random));
        this.fileHandler.createFile("target/random.txt");
        this.fileHandler.copyFile("target/random.txt", "target/" + random + "/random.txt",
            new FilterChain(), "UTF-8");
        assertTrue("Subdirectory " + random + " does not exist after copy",
            this.fileHandler.isDirectory("target/" + random));
        assertTrue("File in subdirectory " + random + " missing after copy",
            this.fileHandler.exists("target/" + random + "/random.txt"));
    }

}
