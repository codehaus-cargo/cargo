/* 
 * ========================================================================
 * 
 * Copyright 2004-2006 Vincent Massol.
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
package org.codehaus.cargo.container.installer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.net.URL;

import org.apache.tools.ant.taskdefs.Expand;
import org.apache.tools.ant.taskdefs.Get;
import org.apache.tools.ant.taskdefs.Untar;
import org.apache.tools.ant.taskdefs.Untar.UntarCompressionMethod;
import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.util.AntTaskFactory;
import org.codehaus.cargo.util.AntUtils;
import org.codehaus.cargo.util.DefaultFileHandler;
import org.codehaus.cargo.util.FileHandler;
import org.codehaus.cargo.util.log.LoggedObject;

/**
 * Installs a zipped container file from a URL to a location on your local disk.
 * 
 * @version $Id$
 */
public class ZipURLInstaller extends LoggedObject implements Installer
{
    /**
     * Archive types supported.
     */
    private static final String[] ARCHIVE_ENDINGS = {
        ".zip",
        ".tgz",
        ".tar",
        ".bz2",
        ".tar.gz",
    };
    
    /**
     * URL where the zipped container is located.
     */
    private URL remoteLocation;

    /**
     * Destination directory where the zipped container install will be downloaded and installed.
     */
    private String installDir;

    /**
     * Proxy settings to use when downloading distributions.
     */
    private Proxy proxy;
    
    /**
     * Ant utility class.
     */
    private AntUtils antUtils;

    /**
     * File utility class.
     */
    private FileHandler fileHandler;

    /**
     * @param remoteLocation URL where the zipped container is located 
     */
    public ZipURLInstaller(URL remoteLocation)
    {
        this(remoteLocation, null);
    }

    /**
     * @param remoteLocation URL where the zipped container is located
     * @param installDir directory where we will unpack the zip container file 
     */
    public ZipURLInstaller(URL remoteLocation, String installDir)
    {
        this.remoteLocation = remoteLocation;
        this.installDir = installDir;
        this.fileHandler = new DefaultFileHandler();
        this.antUtils = new AntUtils();
    }

    /**
     * @param installDir the destination directory where the zipped container install will be
     *        downloaded and installed.
     */
    public void setInstallDir(String installDir)
    {
        this.installDir = installDir;
    }

    /**
     * Convenience method used for testing in isolation. Test cases can use it for introducing
     * a custom {@link AntTaskFactory} that returns a custom test-made Ant task.
     * 
     * @param antTaskFactory the test-provided {@link AntTaskFactory} 
     */
    protected void setAntTaskFactory(AntTaskFactory antTaskFactory)
    {
        this.antUtils = new AntUtils(antTaskFactory);
    }

    /**
     * @return the file utility class to use for performing all file I/O.
     */
    public FileHandler getFileHandler()
    {
        return this.fileHandler;
    }

    /**
     * @param fileHandler the file utility class to use for performing all file I/O.
     */
    public void setFileHandler(FileHandler fileHandler)
    {
        this.fileHandler = fileHandler;
    }

    /**
     * @see Installer#install()
     */
    public void install()
    {
        if (!isAlreadyInstalled())
        {
            getLogger().debug("Container [" + getSourceFileName() + "] is not yet installed.",
                this.getClass().getName());

            createDestinationDirectory();
            download();
            unpack();
            registerInstallation();
        }
        else
        {
            getLogger().debug("Container [" + getSourceFileName() + "] is already installed",
                this.getClass().getName());
            getLogger().debug("Using container installation dir [" + getDestinationDir()
                + "]", getClass().getName());
        }
    }

    /**
     * Create timestamp file to register that the installation has been successful. This allows
     * to prevent installing again next time. If the remote URL changes, then the container will
     * be reinstalled. 
     */
    public void registerInstallation()
    {
        try
        {
            File timestampFile = new File(getDestinationDir(), getInstallDirName() + "/.cargo");
            BufferedWriter bw = new BufferedWriter(new FileWriter(timestampFile));
            bw.write("Do not remove this file");
            bw.close();
        }
        catch (Exception e)
        {
            // Failed to write timestamp. Too bad. The application will be installed again next
            // time.
        }
    }

    /**
     * @return true if the container has already been installed, false otherwise
     */
    public boolean isAlreadyInstalled()
    {
        boolean isInstalled = false;
        String timestampFile =
            getFileHandler().append(getDestinationDir(), getInstallDirName() + "/.cargo");
        if (getFileHandler().exists(timestampFile))
        {
            isInstalled = true;
        }

        return isInstalled;
    }
    
    /**
     * {@inheritDoc}
     * @see Installer#getHome() 
     */
    public String getHome()
    {
        String home;

        if (!isAlreadyInstalled())
        {
            throw new ContainerException("Failed to get container installation home as the "
                + "container has not yet been installed. Please call install() first.");
        }

        String targetDir = getFileHandler().append(getDestinationDir(), getInstallDirName());
        String[] files = getFileHandler().getChildren(targetDir);
        int nbDirectories = 0;
        String foundDirectory = null;
        for (String file : files)
        {
            if (getFileHandler().isDirectory(file))
            {
                nbDirectories++;
                foundDirectory = file; 
            }
        }

        // If the unpacking has revealed only 1 directory, then it's the home dir. Otherwise, it
        // means the packing of the zip files did not have a root dir.
        if (nbDirectories != 1)
        {
            home = targetDir;
        }
        else
        {
            home = foundDirectory;
        }
        
        return home;
    }

    /**
     * Unpacks the zip file containing the container files.
     */
    private void unpack()
    {
        File targetDir = new File(getDestinationDir(), getInstallDirName());

        getLogger().info("Installing container in [" + targetDir.getPath() + "]",
            getClass().getName());

        Expand expandTask = createExpandTask();
        expandTask.setSrc(new File(getDestinationDir(), getSourceFileName()));
        expandTask.setDest(targetDir);
        expandTask.execute();
    }
    
    /**
     * Create an ant Expand task.
     * @return The expand task
     */
    private Expand createExpandTask()
    {
        String archivename = getSourceFileName().toLowerCase();
        Expand expand = null; 
        if (archivename.endsWith(".zip"))
        {
            expand = (Expand) this.antUtils.createAntTask("unzip");
        }
        else if (archivename.endsWith(".tar"))
        {
            expand = (Expand) this.antUtils.createAntTask("untar");
        }
        else if (archivename.endsWith(".tgz") || archivename.endsWith(".tar.gz"))
        {
            Untar untar = (Untar) this.antUtils.createAntTask("untar");
            UntarCompressionMethod compressionMethod = new Untar.UntarCompressionMethod();
            compressionMethod.setValue("gzip");
            untar.setCompression(compressionMethod);
            expand = untar;
        }
        else if (archivename.endsWith(".bz2"))
        {
            Untar untar = (Untar) this.antUtils.createAntTask("untar");
            UntarCompressionMethod compressionMethod = new Untar.UntarCompressionMethod();
            compressionMethod.setValue("bzip2");
            untar.setCompression(compressionMethod);
            expand = untar;
        }
        else
        {
            String errorMessage = "Unsupported archive type: [" + archivename + "]";
            getLogger().warn(errorMessage, getClass().getName());
            throw new IllegalArgumentException(errorMessage);
        }
        
        return expand;
    }

    /**
     * Downloads the zip file containing the container files.
     */
    protected void download()
    {
        // Try once with the proxy settings on (if set up by the user) and if it doesn't work, try 
        // again with no proxy settings...
        try
        {
            if (this.proxy != null)
            {
                this.proxy.configure();
            }
            doDownload();
        }
        catch (Exception e)
        {
            if (this.proxy != null)
            {
                try
                {
                    this.proxy.clear();
                    doDownload();
                }
                catch (Exception ee)
                {
                    throw new ContainerException("Failed to download [" + this.remoteLocation + "]",
                        ee);
                }
            }
            else
            {
                throw new ContainerException("Failed to download [" + this.remoteLocation + "]", e);
            }
        }
    }

    /**
     * Perform the actual HTTP download.
     */
    private void doDownload()
    {
        getLogger().info("Downloading container from [" + this.remoteLocation + "]",
            getClass().getName());

        Get getTask = (Get) this.antUtils.createAntTask("get");
        getTask.setUseTimestamp(true);
        getTask.setSrc(this.remoteLocation);
        String userInfo = this.remoteLocation.getUserInfo();
        if (userInfo != null)
        {
            int separator = userInfo.indexOf(":");
            if (separator > 0)
            {
                String username = userInfo.substring(0, separator);
                getTask.setUsername(username);
                String password = userInfo.substring(separator + 1);
                getTask.setPassword(password);
            }
            else
            {
                getTask.setUsername(userInfo);
            }
        }
        getTask.setDest(new File(getDestinationDir(), getSourceFileName()));
        getTask.execute();
    }
    
    /**
     * @return the name of the source zip file (without the path)
     */
    protected String getSourceFileName()
    {
        int slashPos = this.remoteLocation.getPath().lastIndexOf('/');
        String name = this.remoteLocation.getPath();
        if (slashPos > -1)
        {
            name = this.remoteLocation.getPath().substring(slashPos + 1);
        }

        return name;
    }

    /**
     * @return the directory where we will unpack the zip container file
     */
    protected String getInstallDirName()
    {
        String name = getSourceFileName();
        
        for (String element : ARCHIVE_ENDINGS)
        {
            int dotPos = name.lastIndexOf(element);
            if (dotPos > -1)
            {
                name = name.substring(0, dotPos);
                break;
            }
        }
        
        return name;        
    }

    /**
     * Create the directory (if need be) where we will unpack the zip container file. 
     */
    private void createDestinationDirectory()
    {
        String destinationDir = getDestinationDir();
        if (!getFileHandler().exists(destinationDir))
        {
            getFileHandler().mkdirs(destinationDir);
        }
    }

    /**
     * @return the directory where we will puth both the zip container file and its unpacking
     */
    protected String getDestinationDir()
    {
        String dir = this.installDir;
        
        if (dir == null)
        {
            dir = getFileHandler().getTmpPath("installs");
        }

        return dir;
    }

    /**
     * Sets proxy details.
     * 
     * @param proxy the proxy configuration to set
     */
    public void setProxy(Proxy proxy)
    {
        this.proxy = proxy;
    }
}
