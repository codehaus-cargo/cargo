/*
 * ========================================================================
 *
 * Copyright 2006 Vincent Massol.
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
 * @version $Id$
 */
public class VFSFileHandlerTest extends TestCase
{
    private StandardFileSystemManager fsManager;
    private FileHandler fileHandler;

    protected void setUp() throws Exception
    {
        super.setUp();

        this.fsManager = new StandardFileSystemManager();
        this.fsManager.init();
        this.fileHandler = new VFSFileHandler(this.fsManager);
    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testIsDirectory() throws Exception
    {
        this.fsManager.resolveFile("ram:///some/file").createFile();
        assertFalse(this.fileHandler.isDirectory("ram:///some/file"));

        this.fsManager.resolveFile("ram:///some/path").createFolder();
        assertTrue(this.fileHandler.isDirectory("ram:///some/path"));        
    }

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

    public void testGetName() throws Exception
    {
        assertEquals("file.txt", this.fileHandler.getName("ram:///some/file.txt"));
    }

    public void testGetChildren() throws Exception
    {
        this.fsManager.resolveFile("ram:///some/directory/file.txt").createFile();

        String[] children = this.fileHandler.getChildren("ram:///some/directory");

        assertEquals(1, children.length);
        assertEquals("ram:///some/directory/file.txt", children[0]);
    }
}
