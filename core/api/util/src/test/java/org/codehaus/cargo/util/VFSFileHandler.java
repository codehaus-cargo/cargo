/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2024 Ali Tokmen.
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
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.vfs2.AllFileSelector;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;

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
     * Copy a file. {@inheritDoc}
     * @param source Source file.
     * @param target Destination file.
     */
    @Override
    public void copyFile(String source, String target)
    {
        copyDirectory(source, target);
    }

    /**
     * Copy a file, ignoring the <code>overwrite</code> parameter. {@inheritDoc}
     * @param source Source file.
     * @param target Destination file.
     * @param overwrite Ignored.
     */
    @Override
    public void copyFile(String source, String target, boolean overwrite)
    {
        copyFile(source, target);
    }

    /**
     * Copy a directory. {@inheritDoc}
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
     * <b>WARNING</b>: Not implemented! {@inheritDoc}
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
     * Create a file. {@inheritDoc}
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
     * Create a directory. {@inheritDoc}
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
     * Checks whether a given path exists. {@inheritDoc}
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
     * Gets the parent of a given path. {@inheritDoc}
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
     * Make all directories. {@inheritDoc}
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
     * Gets the {@link OutputStream} for a given file. {@inheritDoc}
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
     * Gets the {@link InputStream} for a given file. {@inheritDoc}
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
     * Test if a directory is empty. {@inheritDoc}
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
     * Delete a given path. {@inheritDoc}
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
     * {@inheritDoc}
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
     * {@inheritDoc}
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
     * {@inheritDoc}
     */
    @Override
    public String[] getChildren(String directory)
    {
        return getChildren(directory, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getChildren(String directory, List<String> filters)
    {
        List<String> results = new ArrayList<String>();

        try
        {
            FileObject[] files = getFileSystemManager().resolveFile(directory).getChildren();
            for (int i = 0; i < files.length; i++)
            {
                if (filters != null)
                {
                    for (String filter : filters)
                    {
                        if (filter.contains("/"))
                        {
                            throw new CargoException("Unsupported file filter: " + filter);
                        }
                        else if (filter.startsWith("*"))
                        {
                            if (files[i].getName().getBaseName().endsWith(filter.substring(1)))
                            {
                                results.add(files[i].getName().getURI());
                            }
                        }
                        else if (filter.contains("*"))
                        {
                            throw new CargoException("Unsupported file filter: " + filter);
                        }
                        else if (files[i].getName().getBaseName().equals(filter))
                        {
                            results.add(files[i].getName().getURI());
                        }
                    }
                }
                else
                {
                    results.add(files[i].getName().getURI());
                }
            }
        }
        catch (FileSystemException e)
        {
            throw new CargoException("Failed to get childrens for [" + directory + "]", e);
        }

        return results.toArray(new String[results.size()]);
    }
}
