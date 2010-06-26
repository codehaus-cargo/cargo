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

import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.VFS;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.AllFileSelector;
import org.apache.commons.vfs.FileObject;

import java.io.OutputStream;
import java.io.InputStream;
import java.util.List;

/**
 * File operations that are performed in Cargo. All file operations must use this class. This
 * implementation uses Jakarta Commons VFS to resolve URIs allowing files to be accessed using
 * a variety of file systems (see http://jakarta.apache.org/commons/vfs/filesystems.html).
 *
 * <p>Note: This class should be moved to the main core API once if we decide to use VFS in our
 * runtime.</p>
 *
 * @TODO This class shouldn't extend DefaultFileHandler. I have cheated because I was just
 *       prototyping this. We really need to implement all methods using the VFS API.
 *
 * @version $Id$
 */
public class VFSFileHandler extends DefaultFileHandler
{
    private FileSystemManager fileSystemManager;

    public VFSFileHandler(FileSystemManager fsManager)
    {
        this.fileSystemManager = fsManager;
    }

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

    public FileSystemManager getFileSystemManager()
    {
        return this.fileSystemManager;
    }

    public void setFileSystemManager(FileSystemManager fileSystemManager)
    {
        this.fileSystemManager = fileSystemManager;
    }

    @Override
    public void copyFile(String source, String target)
    {
        copyDirectory(source, target);
    }

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

    @Override
    public void copyDirectory(String source, String target, List excludes)
    {
        throw new RuntimeException("Not implemented yet");
    }

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

    @Override
    public String createDirectory(String parent, String file)
    {
        if (file == null)
        {
            file = "";
        }

        if (parent!= null && !parent.endsWith("/") && 
            !file.startsWith("/"))
        {
            parent += "/";
        }

        String filename = parent == null ? file : parent + file;
        try
        {
            FileObject fileObject = getFileSystemManager().resolveFile(filename);
            fileObject.createFolder();
            return fileObject.toString();
        }
        catch (FileSystemException e)
        {
            throw new CargoException("Failed to create folder [" + filename + "]", e);
        }
    }

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

    @Override
    public boolean isDirectoryEmpty(String dir)
    {
        boolean isEmpty;
        try
        {
            isEmpty = (getFileSystemManager().resolveFile(dir).getChildren().length == 0);
        }
        catch (FileSystemException e)
        {
            throw new CargoException("Failed to find out if directory [" + dir + "] is empty.", e);
        }
        return isEmpty;
    }

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
     * @see FileHandler#isDirectory(String)
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
     * @see FileHandler#getName(String)
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
