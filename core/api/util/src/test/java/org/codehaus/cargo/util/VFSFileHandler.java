/*
 * ========================================================================
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
package org.codehaus.cargo.util;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.apache.commons.vfs.AllFileSelector;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.VFS;

/**
 * File operations that are performed in Cargo. All file operations must use this class. This
 * implementation uses Jakarta Commons VFS to resolve URIs allowing files to be accessed using a
 * variety of file systems (see http://jakarta.apache.org/commons/vfs/filesystems.html).
 * 
 * <p>
 * Note: This class should be moved to the main core API once if we decide to use VFS in our
 * runtime.
 * </p>
 * 
 * TODO: This class shouldn't extend DefaultFileHandler. I have cheated because I was just
 * prototyping this. We really need to implement all methods using the VFS API.
 * 
 * @version $Id$
 */
public class VFSFileHandler extends DefaultFileHandler
{
    /**
     * File system manager.
     */
    private FileSystemManager fileSystemManager;

    /**
     * Creates a VFS file handler with a given manager.
     * @param fsManager File system manager.
     */
    public VFSFileHandler(FileSystemManager fsManager)
    {
        this.fileSystemManager = fsManager;
    }

    /**
     * Creates a VFS file handler with the default file system manager.
     */
    public VFSFileHandler()
    {
        try
        {
            this.fileSystemManager = VFS.getManager();
        }
        catch (FileSystemException e)
        {
            throw new CargoException("Failed to get VFS system manager", e);
        }
    }

    /**
     * @return The file system manager.
     */
    public FileSystemManager getFileSystemManager()
    {
        return this.fileSystemManager;
    }

    /**
     * @param fileSystemManager The file system manager to set.
     */
    public void setFileSystemManager(FileSystemManager fileSystemManager)
    {
        this.fileSystemManager = fileSystemManager;
    }

    /**
     * Copy a file. {@inheritdoc}
     * @param source Source file.
     * @param target Destination file.
     */
    @Override
    public void copyFile(String source, String target)
    {
        copyDirectory(source, target);
    }

    /**
     * Copy a directory. {@inheritdoc}
     * @param source Source directory.
     * @param target Destination directory.
     */
    @Override
    public void copyDirectory(String source, String target)
    {
        try
        {
            FileObject sourceObject = getFileSystemManager().resolveFile(source);
            getFileSystemManager().resolveFile(target).copyFrom(sourceObject,
                new AllFileSelector());
        }
        catch (FileSystemException e)
        {
            throw new CargoException("Failed to copy [" + source + "] to [" + target + "]", e);
        }
    }

    /**
     * <b>WARNING</b>: Not implemented! {@inheritdoc}
     * @param source Source directory.
     * @param target Destination directory.
     * @param excludes Exclusions list.
     */
    @Override
    public void copyDirectory(String source, String target, List<String> excludes)
    {
        throw new RuntimeException("Not implemented yet");
    }

    /**
     * Create a file. {@inheritdoc}
     * @param file File name.
     */
    @Override
    public void createFile(String file)
    {
        try
        {
            getFileSystemManager().resolveFile(file).createFile();
        }
        catch (FileSystemException e)
        {
            throw new CargoException("Failed to create file [" + file + "]", e);
        }
    }

    /**
     * Create a directory. {@inheritdoc}
     * @param parent Parent directory name.
     * @param path Directory name.
     * @return Created directory name.
     */
    @Override
    public String createDirectory(String parent, String path)
    {
        String directoryname;
        if (path != null)
        {
            directoryname = path;
        }
        else
        {
            directoryname = "";
        }

        if (parent != null)
        {
            if (!parent.endsWith("/") && !directoryname.startsWith("/"))
            {
                directoryname = "/" + directoryname;
            }

            directoryname = parent + directoryname;
        }

        try
        {
            FileObject fileObject = getFileSystemManager().resolveFile(directoryname);
            fileObject.createFolder();
            return fileObject.toString();
        }
        catch (FileSystemException e)
        {
            throw new CargoException("Failed to create folder [" + directoryname + "]", e);
        }
    }

    /**
     * Checks whether a given path exists. {@inheritdoc}
     * @param path Path to check.
     * @return <code>true</code> if <code>path</code> exists, <code>false</code> otherwise.
     */
    @Override
    public boolean exists(String path)
    {
        boolean result;
        try
        {
            result = getFileSystemManager().resolveFile(path).exists();
        }
        catch (FileSystemException e)
        {
            throw new CargoException("Failed to check for existence of path [" + path + "]", e);
        }
        return result;
    }

    /**
     * Gets the parent of a given path. {@inheritdoc}
     * @param path Path
     * @return Parent of <code>path</code>
     */
    @Override
    public String getParent(String path)
    {
        String result;
        try
        {
            result = getFileSystemManager().resolveFile(path).getParent().getName().getURI();
        }
        catch (FileSystemException e)
        {
            throw new CargoException("Failed to get parent for path [" + path + "]", e);
        }
        return result;
    }

    /**
     * Make all directories. {@inheritdoc}
     * @param path Directory Path.
     */
    @Override
    public void mkdirs(String path)
    {
        try
        {
            getFileSystemManager().resolveFile(path).createFolder();
        }
        catch (FileSystemException e)
        {
            throw new CargoException("Failed to create folders for path [" + path + "]", e);
        }
    }

    /**
     * Gets the {@link OutputStream} for a given file. {@inheritdoc}
     * @param file File name.
     * @return {@link OutputStream} for <code>file</code>.
     */
    @Override
    public OutputStream getOutputStream(String file)
    {
        OutputStream result;
        try
        {
            result = getFileSystemManager().resolveFile(file).getContent().getOutputStream();
        }
        catch (FileSystemException e)
        {
            throw new CargoException("Failed to open output stream for file [" + file + "]", e);
        }
        return result;
    }

    /**
     * Gets the {@link InputStream} for a given file. {@inheritdoc}
     * @param file File name.
     * @return {@link InputStream} for <code>file</code>.
     */
    @Override
    public InputStream getInputStream(String file)
    {
        InputStream result;
        try
        {
            result = getFileSystemManager().resolveFile(file).getContent().getInputStream();
        }
        catch (FileSystemException e)
        {
            throw new CargoException("Failed to find file [" + file + "]", e);
        }
        return result;
    }

    /**
     * Test if a directory is empty. {@inheritdoc}
     * @param dir Directory to check.
     * @return <code>true</code> if <code>dir</code> is empty, <code>false</code> otherwise.
     */
    @Override
    public boolean isDirectoryEmpty(String dir)
    {
        boolean isEmpty;
        try
        {
            isEmpty = getFileSystemManager().resolveFile(dir).getChildren().length == 0;
        }
        catch (FileSystemException e)
        {
            throw new CargoException("Failed to find out if directory [" + dir + "] is empty.", e);
        }
        return isEmpty;
    }

    /**
     * Delete a given path. {@inheritdoc}
     * @param path Path to delete.
     */
    @Override
    public void delete(String path)
    {
        try
        {
            getFileSystemManager().resolveFile(path).delete(new AllFileSelector());
        }
        catch (FileSystemException e)
        {
            throw new CargoException("Failed to delete all files and folders from [" + path + "]",
                e);
        }

    }

    /**
     * Test if a path is a directory. {@inheritdoc}
     * @see FileHandler#isDirectory(String)
     * @param path Path to check.
     * @return <code>true</code> if <code>path</code> is a directory, <code>false</code> otherwise.
     */
    @Override
    public boolean isDirectory(String path)
    {
        boolean isDirectory;
        try
        {
            isDirectory = getFileSystemManager().resolveFile(path).getType().hasChildren();
        }
        catch (FileSystemException e)
        {
            throw new CargoException("Failed to find whether the path [" + path
                + "] is directory or not", e);
        }
        return isDirectory;
    }

    /**
     * Get the name of a file. {@inheritdoc}
     * @see FileHandler#getName(String)
     * @param file File to check.
     * @return The name of <code>file</code>.
     */
    @Override
    public String getName(String file)
    {
        String name;
        try
        {
            name = getFileSystemManager().resolveFile(file).getName().getBaseName();
        }
        catch (FileSystemException e)
        {
            throw new CargoException("Failed to compute base name for [" + file + "]", e);
        }
        return name;
    }

    /**
     * Get the children of a directory. {@inheritdoc}
     * @param directory Directory to which to get the children.
     * @return Children of <code>directory</code>.
     */
    @Override
    public String[] getChildren(String directory)
    {
        String[] results;
        try
        {
            FileObject[] files = getFileSystemManager().resolveFile(directory).getChildren();
            results = new String[files.length];
            for (int i = 0; i < files.length; i++)
            {
                results[i] = files[i].getName().getURI();
            }
        }
        catch (FileSystemException e)
        {
            throw new CargoException("Failed to get childrens for [" + directory + "]", e);
        }
        return results;
    }
}
