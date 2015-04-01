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

import junit.framework.TestCase;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.impl.StandardFileSystemManager;

/**
 * Unit tests for {@link VFSFileHandler}.
 * 
 */
public class VFSFileHandlerTest extends TestCase
{
    /**
     * File system handler.
     */
    private StandardFileSystemManager fsManager;

    /**
     * File handler.
     */
    private FileHandler fileHandler;

    /**
     * Creates the various file system and handler attributes. {@inheritDoc}
     * @throws Exception If anything goes wrong.
     */
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        this.fsManager = new StandardFileSystemManager();
        this.fsManager.init();
        this.fileHandler = new VFSFileHandler(this.fsManager);
    }

    /**
     * Test the {@link FileHandler#isDirectory(java.lang.String)} method.
     * @throws Exception If anything goes wrong.
     */
    public void testIsDirectory() throws Exception
    {
        this.fsManager.resolveFile("ram:///some/file").createFile();
        assertFalse(this.fileHandler.isDirectory("ram:///some/file"));

        this.fsManager.resolveFile("ram:///some/path").createFolder();
        assertTrue(this.fileHandler.isDirectory("ram:///some/path"));
    }

    /**
     * Test the {@link FileHandler#copyDirectory(java.lang.String, java.lang.String)} method.
     * @throws Exception If anything goes wrong.
     */
    public void testCopyDirectory() throws Exception
    {
        String source = "ram:///some/path1";
        this.fsManager.resolveFile(source).resolveFile("file1").createFile();

        String target = "ram:///other/path2";
        FileObject targetObject = this.fsManager.resolveFile(target);

        assertFalse(targetObject.exists());
        assertFalse(this.fsManager.resolveFile("ram:///other/path2/file1").exists());

        this.fileHandler.copyDirectory(source, target);

        assertTrue(targetObject.exists());
        assertTrue(this.fsManager.resolveFile("ram:///other/path2/file1").exists());
    }

    /**
     * Test the {@link FileHandler#copyFile(java.lang.String, java.lang.String)} method.
     * @throws Exception If anything goes wrong.
     */
    public void testCopyFile() throws Exception
    {
        String source = "ram:///some/path/file.war";
        String target = "ram:///other/path/newfile.war";

        FileObject sourceObject = this.fsManager.resolveFile(source);
        sourceObject.createFile();

        FileObject targetObject = this.fsManager.resolveFile(target);

        assertFalse(targetObject.exists());

        this.fileHandler.copyFile(source, target);

        assertTrue(targetObject.exists());
    }

    /**
     * Test the {@link FileHandler#copyFile(java.lang.String, java.lang.String, boolean)} method.
     * @throws Exception If anything goes wrong.
     */
    public void testCopyFileOverwrite() throws Exception
    {
        String source = "ram:///some/path/file.war";
        String target = "ram:///other/path/newfile.war";

        FileObject sourceObject = this.fsManager.resolveFile(source);
        sourceObject.createFile();

        FileObject targetObject = this.fsManager.resolveFile(target);

        assertFalse(targetObject.exists());

        this.fileHandler.copyFile(source, target, true);

        assertTrue(targetObject.exists());
    }

    /**
     * Test the {@link FileHandler#getName(java.lang.String)} method.
     * @throws Exception If anything goes wrong.
     */
    public void testGetName() throws Exception
    {
        assertEquals("file.txt", this.fileHandler.getName("ram:///some/file.txt"));
    }

    /**
     * Test the {@link FileHandler#getChildren(java.lang.String)} method.
     * @throws Exception If anything goes wrong.
     */
    public void testGetChildren() throws Exception
    {
        this.fsManager.resolveFile("ram:///some/directory/file.txt").createFile();

        String[] children = this.fileHandler.getChildren("ram:///some/directory");

        assertEquals(1, children.length);
        assertEquals("ram:///some/directory/file.txt", children[0]);
    }

    /**
     * Test the {@link FileHandler#createDirectory(java.lang.String, java.lang.String)} method.
     */
    public void testCreateDirectory()
    {
        this.fileHandler.createDirectory("ram://test", "test");
        assertTrue(this.fileHandler.exists("ram:///test/test"));
        assertTrue(this.fileHandler.isDirectory("ram:///test/test"));

        this.fileHandler.createDirectory("ram://test2/", "test");
        assertTrue(this.fileHandler.exists("ram:///test2/test"));
        assertTrue(this.fileHandler.isDirectory("ram:///test2/test"));

        this.fileHandler.createDirectory("ram://test3", "/test");
        assertTrue(this.fileHandler.exists("ram:///test3/test"));
        assertTrue(this.fileHandler.isDirectory("ram:///test3/test"));

        this.fileHandler.createDirectory(null, "ram://test4");
        assertTrue(this.fileHandler.exists("ram:///test4"));
        assertTrue(this.fileHandler.isDirectory("ram:///test4"));

        this.fileHandler.createDirectory("ram://test5", null);
        assertTrue(this.fileHandler.exists("ram:///test5"));
        assertTrue(this.fileHandler.isDirectory("ram:///test5"));
    }
}
