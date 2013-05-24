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
package org.codehaus.cargo.daemon.file;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import org.codehaus.cargo.daemon.HandleDatabase;
import org.codehaus.cargo.util.DefaultFileHandler;
import org.codehaus.cargo.util.FileHandler;

/**
 * File manager to deal with files and directories in the daemon workspace.
 * 
 * @version $Id$
 */
public class FileManager
{

    /**
     * The time between refresh.
     */
    private static final int REFRESH_TIME = 500;

    /**
     * The cargo home directory.
     */
    private String cargoHomeDirectory;

    /**
     * The workspace directory.
     */
    private String workspaceDirectory;

    /**
     * The install directory.
     */
    private String installDirectory;

    /**
     * The configuration home directory.
     */
    private String configurationHomeDirectory;

    /**
     * The log directory.
     */
    private String logDirectory;

    /**
     * The configuration record file.
     */
    private String handleRecordFile;

    /**
     * The file handler.
     */
    private final FileHandler fileHandler = new DefaultFileHandler();

    /**
     * Get the cargo home directory.
     * 
     * @return the cargo home directory
     */
    public String getCargoHomeDirectory()
    {
        if (cargoHomeDirectory == null)
        {
            boolean gotHomeViaProperty = true;
            String home = System.getProperty("cargo.home");
            if (home == null)
            {
                gotHomeViaProperty = false;
                home = System.getProperty("user.home");
            }
            File homeDir = null;

            if (home == null)
            {
                homeDir = new File(System.getProperty("java.io.tmpdir"), "cargo");
            }
            else if (!gotHomeViaProperty)
            {
                homeDir = new File(home, ".cargo");
            }
            else
            {
                homeDir = new File(home);
            }

            cargoHomeDirectory = homeDir.getAbsolutePath();
        }

        return cargoHomeDirectory;
    }

    /**
     * Get the workspace directory.
     * 
     * @return the workspace directory
     */
    public String getWorkspaceDirectory()
    {
        if (workspaceDirectory == null)
        {
            workspaceDirectory = fileHandler.append(getCargoHomeDirectory(), "workspace");
        }

        return workspaceDirectory;
    }

    /**
     * Get the install directory.
     * 
     * @return the install directory
     */
    public String getInstallDirectory()
    {
        if (installDirectory == null)
        {
            installDirectory = fileHandler.append(getCargoHomeDirectory(), "installs");
        }

        return installDirectory;
    }

    /**
     * Gets the file path of the handle record file. The handle record file keeps track of the
     * handle id's and if they should be automatically started.
     * 
     * @return the file path to handle record file
     */
    public String getHandleDatabaseFile()
    {
        if (handleRecordFile == null)
        {
            handleRecordFile = fileHandler.append(getCargoHomeDirectory(), "handle.properties");
        }

        return handleRecordFile;
    }

    /**
     * @return The handle database loaded from disk.
     * @throws IOException if error occurs
     */
    public HandleDatabase loadHandleDatabase() throws IOException
    {
        String file = getHandleDatabaseFile();
        HandleDatabase database = new HandleDatabase();

        if (fileHandler.exists(file))
        {
            database.load(fileHandler.getInputStream(file));
        }

        return database;
    }

    /**
     * Save handle database to disk.
     * 
     * @param database The handle database to save.
     * @throws IOException if error occurs.
     */
    public void saveHandleDatabase(HandleDatabase database) throws IOException
    {
        String file = getHandleDatabaseFile();

        database.store(fileHandler.getOutputStream(file));
    }

    /**
     * Saves the start request properties to a file to the workspace directory, so that it can later
     * be started again.
     * 
     * @param handleId The handle identifier of a container
     * @param properties The start request properties
     * @throws IOException if exception happens
     */
    public void saveRequestProperties(String handleId, Properties properties) throws IOException
    {
        String file =
            fileHandler.append(getConfigurationDirectory(handleId), "request.properties");

        properties.store(fileHandler.getOutputStream(file), null);
    }

    /**
     * Get the workspace directory for a container.
     * 
     * @param handleId The handle identifier of a container
     * @return the workspace directory
     */
    public String getWorkspaceDirectory(String handleId)
    {
        if (workspaceDirectory == null)
        {
            workspaceDirectory = fileHandler.append(getCargoHomeDirectory(), "workspace");
        }

        return fileHandler.append(workspaceDirectory, handleId);
    }

    /**
     * Get the configuration home directory.
     * 
     * @return the configuration home directory
     */
    public String getConfigurationDirectory()
    {
        if (configurationHomeDirectory == null)
        {
            configurationHomeDirectory =
                fileHandler.append(getCargoHomeDirectory(), "configurations");
        }

        return configurationHomeDirectory;
    }

    /**
     * Get the log directory.
     * 
     * @return the log directory
     */
    public String getLogDirectory()
    {
        if (logDirectory == null)
        {
            logDirectory = fileHandler.append(getCargoHomeDirectory(), "logs");
        }

        return logDirectory;
    }

    /**
     * Get the log directory for a container.
     * 
     * @param handleId The handle identifier of a container
     * @return the log directory for a container
     */
    public String getLogDirectory(String handleId)
    {
        return fileHandler.append(getLogDirectory(), handleId);
    }

    /**
     * Get the log file for a container.
     * 
     * @param handleId The handle identifier of a container
     * @param filename The log file name
     * @return the log directory for a container
     */
    public String getLogFile(String handleId, String filename)
    {
        File file = new File(filename);

        if (file.isAbsolute())
        {
            return filename;
        }
        else
        {
            return fileHandler.append(getLogDirectory(handleId), filename);
        }
    }

    /**
     * Get the configuration home directory for a container.
     * 
     * @param handleId The handle identifier of a container
     * @return the default configuration home directory for a container
     */
    public String getConfigurationDirectory(String handleId)
    {
        return fileHandler.append(getConfigurationDirectory(), handleId);
    }

    /**
     * Delete temporary files.
     */
    public void deleteWorkspaceFiles()
    {
        fileHandler.delete(getWorkspaceDirectory());
    }

    /**
     * Delete workspace directory.
     * 
     * @param handleId The handle identifier of a container
     */
    public void deleteWorkspaceDirectory(String handleId)
    {
        fileHandler.delete(getWorkspaceDirectory(handleId));
    }

    /**
     * Saves the input stream to a file, relative to the workspace directory.
     * 
     * @param relativeFile The relative filename
     * @param inputStream The inputstream containing the file contents
     * @return path to the saved file
     */
    public String saveFile(String relativeFile, InputStream inputStream)
    {
        String file = fileHandler.append(getWorkspaceDirectory(), relativeFile);

        fileHandler.copy(inputStream, fileHandler.getOutputStream(file));

        return file;
    }

    /**
     * Saves the input stream to a file, relative to the workspace directory of a container.
     * 
     * @param handleId The handle identifier of a container
     * @param relativeFile The relative filename
     * @param inputStream The inputstream containing the file contents
     * @return path to the saved file
     */
    public String saveFile(String handleId, String relativeFile, InputStream inputStream)
    {
        String file = fileHandler.append(getWorkspaceDirectory(handleId), relativeFile);

        if (inputStream != null)
        {
            fileHandler.copy(inputStream, fileHandler.getOutputStream(file));
        }

        return file;
    }

    /**
     * Saves the input stream to a file, relative to the workspace directory of a container and a
     * given directory.
     * 
     * @param handleId The handle identifier of a container
     * @param relativeDirectory The relative directory
     * @param relativeFile The relative filename
     * @param inputStream The inputstream containing the file contents
     * @return path to the saved file
     */
    public String saveFile(String handleId, String relativeDirectory, String relativeFile,
        InputStream inputStream)
    {
        String file =
            fileHandler.append(
                fileHandler.append(getWorkspaceDirectory(handleId), relativeDirectory),
                relativeFile);

        fileHandler.copy(inputStream, fileHandler.getOutputStream(file));

        return file;
    }

    /**
     * Check if filename exists in the workspace.
     * 
     * @param filename The file to check
     * @return true if file exists
     */
    public boolean existsFile(String filename)
    {
        String filepath = fileHandler.append(getWorkspaceDirectory(), filename);
        return fileHandler.exists(filepath);
    }

    /**
     * Get the URL for a filename in the workspace.
     * 
     * @param filename The filename to construct URL for
     * @return the URL for the filename
     */
    public String getFileURL(String filename)
    {
        String filepath = fileHandler.append(getWorkspaceDirectory(), filename);
        return fileHandler.getURL(filepath);
    }

    /**
     * Get the file input stream
     * 
     * @param filename The filename to get input stream from
     * @return the input stream
     */
    public InputStream getFileInputStream(String filename)
    {
        return fileHandler.getInputStream(filename);
    }

    /**
     * Copies the inputstream to the output stream, and keeps the output stream open.
     * 
     * @param in The soruce input stream
     * @param out The destination output stream
     * @throws IOException if error happens
     */
    public void copyHeader(InputStream in, OutputStream out) throws IOException
    {
        BufferedInputStream is = new BufferedInputStream(in);
        byte[] buf = new byte[64 * 1024];
        int bytesRead;
        while ((bytesRead = is.read(buf)) != -1)
        {
            out.write(buf, 0, bytesRead);
        }
    }

    /**
     * Copies the given file to the output stream continously, i.e. not stop when end of file is
     * reached, but rather wait for additional data to be appended to the file.
     * 
     * @param filename The file to copy
     * @param out The destination output stream
     * @throws IOException if error happens
     * @throws InterruptedException if error happens when sleeping
     */
    public void copyContinuous(String filename, OutputStream out) throws IOException,
        InterruptedException
    {
        BufferedInputStream is = new BufferedInputStream(getFileInputStream(filename));
        byte[] buf = new byte[64 * 1024];
        int bytesRead;

        try
        {
            while (true)
            {
                while ((bytesRead = is.read(buf)) != -1)
                {
                    out.write(buf, 0, bytesRead);
                }

                out.flush();
                Thread.sleep(REFRESH_TIME);
            }
        }
        catch (Exception e) 
        {
            // Ignore
        }
        finally
        {
            try
            {
                out.close();
                is.close();
            }
            catch (Exception e) 
            {
                // Ignore
            }
        }
    }

    /**
     * Resolves a path relative to the workspace directory to an absolute path.
     * 
     * @param handleId The handle id.
     * @param relativePath The path relative to the workspace.
     * @return The absolute file path.
     */
    public String resolveWorkspacePath(String handleId, String relativePath)
    {
        return fileHandler.append(getWorkspaceDirectory(handleId), relativePath);
    }
    
    /**
     * Resolves a path relative to the configuration directory to an absolute path.
     * 
     * @param handleId The handle id.
     * @param relativePath The path relative to the configuration.
     * @return The absolute file path.
     */
    public String resolveConfigurationPath(String handleId, String relativePath)
    {
        return fileHandler.append(getConfigurationDirectory(handleId), relativePath);
    }

}
