/*
 * ========================================================================
 *
 * Copyright 2003 The Apache Software Foundation. Code from this file 
 * was originally imported from the Jakarta Cactus project.
 *
 * Codehaus CARGO, copyright 2004-2010 Vincent Massol.
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
package org.codehaus.cargo.module;

import java.io.InputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.impl.StandardFileSystemManager;
import org.codehaus.cargo.util.AbstractResourceTest;
import org.codehaus.cargo.util.VFSFileHandler;

/**
 * Unit tests for {@link JarArchive}.
 * 
 * @version $Id$
 */
public final class JarArchiveTest extends AbstractResourceTest
{
    /**
     * Package path.
     */
    private static final String PACKAGE_PATH = "org/codehaus/cargo/module/";

    /**
     * File system manager.
     */
    private FileSystemManager fsManager;

    /**
     * Creates the file system manager. {@inheritdoc}
     * @throws Exception If anything goes wrong.
     */
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        this.fsManager = new StandardFileSystemManager();
        ((StandardFileSystemManager) this.fsManager).init();
    }

    /**
     * Closes the file system manager. {@inheritdoc}
     * @throws Exception If anything goes wrong.
     */
    @Override
    protected void tearDown() throws Exception
    {
        if (this.fsManager != null)
        {
            ((StandardFileSystemManager) this.fsManager).close();
        }

        super.tearDown();
    }

    /**
     * Verifies that a <code>NullPointerException</code> is thrown when the constructor is passed a
     * <code>null</code> argument as input stream.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testConstructorWithNullInputStream() throws Exception
    {
        try
        {
            new DefaultJarArchive((InputStream) null);
            fail("NullPointerException expected");
        }
        catch (NullPointerException expected)
        {
            // expected
        }
    }

    /**
     * Verifies that random access to resources in the JAR is provided.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testRandomAccess() throws Exception
    {
        JarArchive jar = new DefaultJarArchive(getResourcePath(PACKAGE_PATH + "randomaccess.jar"));
        assertContains(jar.getResource("firstEntry.txt"), "firstEntry");
        assertContains(jar.getResource("secondEntry.txt"), "secondEntry");
        assertContains(jar.getResource("secondEntry.txt"), "secondEntry");
        assertContains(jar.getResource("firstEntry.txt"), "firstEntry");
    }

    /**
     * Verifies that the method <code>containsClass()</code> returns <code>true</code> if the JAR
     * contains the requested class.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testContainsClass() throws Exception
    {
        JarArchive jar = new DefaultJarArchive(getResourcePath(PACKAGE_PATH + "containsclass.jar"));
        assertTrue(jar.containsClass("test.Test"));
    }

    /**
     * Verifies that the method <code>containsClass()</code> returns <code>false</code> if the JAR
     * does not contain such a class.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testContainsClassEmpty() throws Exception
    {
        JarArchive jar = new DefaultJarArchive(getResourcePath(PACKAGE_PATH + "empty.jar"));
        assertTrue(!jar.containsClass("test.Test"));
    }

    /**
     * Verifies that the method <code>findResource()</code> works.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testFindResource() throws Exception
    {
        JarArchive jar =
            new DefaultJarArchive(getResourcePath(PACKAGE_PATH + "test.jar"));
        assertEquals("rootResource.txt", jar.findResource("rootResource.txt"));
        assertEquals("folder1/resourceOne.txt", jar.findResource("resourceOne.txt"));
        assertNull(jar.findResource("foo"));
    }

    /**
     * Verifies that the method <code>getResources()</code> works.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testGetResources() throws Exception
    {
        JarArchive jar = new DefaultJarArchive(getResourcePath(PACKAGE_PATH + "test.jar"));

        List<String> resources = jar.getResources("folder1");
        assertEquals(2, resources.size());
        assertTrue(resources.contains("folder1/resourceOne.txt"));
        assertTrue(resources.contains("folder1/resourceTwo.txt"));

        resources = jar.getResources("folder1/");
        assertEquals(2, resources.size());
        assertTrue(resources.contains("folder1/resourceOne.txt"));
        assertTrue(resources.contains("folder1/resourceTwo.txt"));

        resources = jar.getResources("");
        assertEquals(6, resources.size());
        assertTrue(resources.contains("rootResource.txt"));
        assertTrue(resources.contains("folder1/"));
        assertTrue(resources.contains("folder1/resourceOne.txt"));
        assertTrue(resources.contains("folder1/resourceTwo.txt"));
        assertTrue(resources.contains("folder2/"));
        assertTrue(resources.contains("folder2/resourceTwo.txt"));

        resources = jar.getResources("foo");
        assertEquals(0, resources.size());
    }

    /**
     * Verifies that the method <code>expandToPath()</code> works.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testExpandToPath() throws Exception
    {
        FileObject testJar = this.fsManager.resolveFile("ram:///test.jar");
        ZipOutputStream zos = new ZipOutputStream(testJar.getContent().getOutputStream());
        ZipEntry zipEntry = new ZipEntry("rootResource.txt");
        zos.putNextEntry(zipEntry);
        zos.write("Some content".getBytes());
        zos.closeEntry();
        zos.close();

        DefaultJarArchive jarArchive = new DefaultJarArchive("ram:///test.jar");
        jarArchive.setFileHandler(new VFSFileHandler(this.fsManager));

        jarArchive.expandToPath("ram:///test");

        // Verify that the rootResource.txt file has been correctly expanded
        assertTrue(this.fsManager.resolveFile("ram:///test/rootResource.txt").exists());
    }
}
