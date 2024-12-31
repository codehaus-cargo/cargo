/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2025 Ali Tokmen.
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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.util.DefaultFileHandler;
import org.codehaus.cargo.util.FileHandler;
import org.codehaus.cargo.util.log.LoggedObject;
import org.codehaus.cargo.util.log.Logger;

/**
 * Installs a compressed container file from a URL to a location on your local disk.<br>
 * <br>
 * Though the name of this class is <code>ZipURLInstaller</code>, all formats supported by
 * <a href="https://commons.apache.org/proper/commons-compress/"
 * target="_blank">commons-compress</a> are supported.
 */
public class ZipURLInstaller extends LoggedObject implements Installer
{
    /**
     * URL where the compressed container is located.
     */
    private URL remoteLocation;

    /**
     * Destination directory where the compressed container install will be downloaded.
     */
    private String downloadDir;

    /**
     * Destination directory where the compressed container install will be extracted.
     */
    private String extractDir;

    /**
     * Proxy settings to use when downloading distributions.
     */
    private Proxy proxy;

    /**
     * File utility class.
     */
    private FileHandler fileHandler;

    /**
     * @param remoteLocation URL where the compressed container is located
     */
    public ZipURLInstaller(URL remoteLocation)
    {
        this(remoteLocation, null, null);
    }

    /**
     * @param remoteLocation URL where the compressed container is located
     * @param downloadDir directory where the compressed container install will be downloaded.
     * @param extractDir directory where the compressed container install will be extracted.
     */
    public ZipURLInstaller(URL remoteLocation, String downloadDir, String extractDir)
    {
        this.remoteLocation = remoteLocation;
        this.downloadDir = downloadDir;
        this.extractDir = extractDir;
        this.fileHandler = new DefaultFileHandler();
    }

    /**
     * Overriden in order to set the logger on ancillary components.
     * {@inheritDoc}
     * 
     * @param logger the logger to set and set in the ancillary objects
     */
    @Override
    public void setLogger(Logger logger)
    {
        super.setLogger(logger);
        this.fileHandler.setLogger(logger);
    }

    /**
     * @param downloadDir the destination directory where the compressed container install will be
     * downloaded.
     */
    public void setDownloadDir(String downloadDir)
    {
        this.downloadDir = downloadDir;
    }

    /**
     * @param extractDir the destination directory where the compressed container install will be
     * installed.
     */
    public void setExtractDir(String extractDir)
    {
        this.extractDir = extractDir;
    }

    /**
     * @return The destination directory where the compressed container install will be downloaded.
     */
    public String getDownloadDir()
    {
        if (this.downloadDir == null)
        {
            return getFileHandler().getTmpPath("installs");
        }
        else
        {
            return this.downloadDir;
        }
    }

    /**
     * @return The destination file where the compressed container install will be downloaded.
     */
    public String getDownloadFile()
    {
        return getFileHandler().append(getDownloadDir(), getSourceFileName());
    }

    /**
     * @return The destination directory where the compressed container install will be extracted.
     */
    public String getExtractDir()
    {
        String extractDir;

        if (this.extractDir == null)
        {
            extractDir = getFileHandler().getTmpPath("installs");
        }
        else
        {
            extractDir = this.extractDir;
        }

        String name = getSourceFileName();

        int dotPos = name.lastIndexOf(".");
        if (dotPos > -1)
        {
            name = name.substring(0, dotPos);
        }
        if (name.endsWith(".tar"))
        {
            name = name.substring(0, name.length() - 4);
        }

        return getFileHandler().append(extractDir, name);
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
    @Override
    public void install()
    {
        if (!isAlreadyExtracted())
        {
            getLogger().debug("Container [" + getSourceFileName() + "] is not yet installed.",
                this.getClass().getName());

            String targetFile = getFileHandler().append(getDownloadDir(), getSourceFileName());
            if (getFileHandler().isDirectory(targetFile))
            {
                throw new ContainerException(
                    "Target file [" + targetFile + "] already exists as a directory, either "
                        + "delete it or change the ZipURLInstaller target folder or file name");
            }
            if (!getFileHandler().exists(targetFile))
            {
                getLogger().debug("Container [" + getSourceFileName() + "] is not yet downloaded.",
                    this.getClass().getName());

                download();
            }

            try
            {
                getLogger().debug("Container [" + getSourceFileName()
                    + "] is downloaded, now unpacking.", this.getClass().getName());

                unpack();
            }
            catch (Exception e)
            {
                getLogger().debug("Container [" + getSourceFileName() + "] is broken.",
                    this.getClass().getName());

                getFileHandler().delete(targetFile);
                download();
                try
                {
                    getLogger().debug("As the container was broken, also deleting ["
                        + getExtractDir() + "] before extraction.", this.getClass().getName());
                    getFileHandler().delete(getExtractDir());

                    unpack();
                }
                catch (Exception ee)
                {
                    throw new ContainerException(
                        "Failed to unpack [" + getSourceFileName() + "]", ee);
                }
            }

            getLogger().debug("Container [" + getSourceFileName()
                + "] is unpacked, now registering.", this.getClass().getName());

            registerInstallation();
        }
        else
        {
            getLogger().debug("Container [" + getSourceFileName() + "] is already installed",
                this.getClass().getName());
            getLogger().debug("Using container installation dir [" + getExtractDir() + "]",
                getClass().getName());
        }
    }

    /**
     * Create timestamp file to register that the installation has been successful. This allows to
     * prevent installing again next time. If the remote URL changes, then the container will be
     * reinstalled.
     */
    public void registerInstallation()
    {
        try
        {
            getFileHandler().writeTextFile(getFileHandler().append(getExtractDir(), ".cargo"),
                "Do not remove this file", StandardCharsets.UTF_8);
        }
        catch (Exception e)
        {
            // Failed to write timestamp. Too bad.
            // The application will be installed again next time.
        }
    }

    /**
     * @return true if the container has already been downloaded, false otherwise
     */
    public boolean isAlreadyDownloaded()
    {
        boolean isDownloaded = false;
        String targetFile = getFileHandler().append(getDownloadDir(), getSourceFileName());
        if (getFileHandler().exists(targetFile))
        {
            isDownloaded = true;
        }

        return isDownloaded;
    }

    /**
     * @return true if the container has already been extracted, false otherwise
     */
    public boolean isAlreadyExtracted()
    {
        boolean isExtracted = false;
        String timestampFile = getFileHandler().append(getExtractDir(), ".cargo");
        if (getFileHandler().exists(timestampFile))
        {
            isExtracted = true;
        }

        return isExtracted;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getHome()
    {
        String home;

        if (!isAlreadyExtracted())
        {
            throw new ContainerException("Failed to get container installation home as the "
                + "container has not yet been installed. Please call install() first.");
        }

        String targetDir = getExtractDir();
        String[] files = getFileHandler().getChildren(targetDir);
        int nbDirectories = 0;
        String foundDirectory = null;
        for (String file : files)
        {
            if (getFileHandler().isDirectory(file)
                && !"PaxHeaders.X".equals(getFileHandler().getName(file)))
            {
                nbDirectories++;
                foundDirectory = file;
            }
        }

        // If the unpacking has revealed only 1 directory, then it's the home dir. Otherwise, it
        // means the packing of the compressed files did not have a root dir.
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
     * Unpacks the compressed file containing the container files.
     * @throws Exception If the compressed file is broken
     */
    private void unpack() throws Exception
    {
        String source = getFileHandler().append(getDownloadDir(), getSourceFileName());
        getLogger().info(
            "Installing container [" + source + "] in [" + getExtractDir() + "]",
                getClass().getName());

        if (source.endsWith(".7z"))
        {
            SevenZFile sevenZFile = new SevenZFile(new File(source));
            SevenZArchiveEntry sevenZEntry;
            while ((sevenZEntry = sevenZFile.getNextEntry()) != null)
            {
                String destinationEntry =
                    getFileHandler().append(getExtractDir(),
                        DefaultFileHandler.sanitizeFilename(sevenZEntry.getName(), getLogger()));
                if (sevenZEntry.isDirectory())
                {
                    getFileHandler().mkdirs(destinationEntry);
                }
                else
                {
                    String parent = getFileHandler().getParent(destinationEntry);
                    if (!getFileHandler().isDirectory(parent))
                    {
                        getFileHandler().mkdirs(parent);
                    }
                    try (OutputStream destinationFileOutputStream =
                        getFileHandler().getOutputStream(destinationEntry))
                    {
                        byte[] sevenZContent = new byte[DefaultFileHandler.FILE_BUFFER_SIZE];
                        int length;
                        while ((length = sevenZFile.read(sevenZContent)) != -1)
                        {
                            destinationFileOutputStream.write(sevenZContent, 0, length);
                        }
                    }
                }
            }
        }
        else
        {
            try (BufferedInputStream sourceInputStream =
                new BufferedInputStream(getFileHandler().getInputStream(source)))
            {
                boolean compressedStream;
                try
                {
                    CompressorStreamFactory.detect(sourceInputStream);
                    compressedStream = true;
                }
                catch (CompressorException e)
                {
                    // Source is not a compressed stream
                    compressedStream = false;
                }

                if (compressedStream)
                {
                    CompressorStreamFactory csf = new CompressorStreamFactory();
                    try (BufferedInputStream decompressedInputStream =
                        new BufferedInputStream(
                            csf.createCompressorInputStream(sourceInputStream)))
                    {
                        unpackStream(decompressedInputStream);
                    }
                }
                else
                {
                    unpackStream(sourceInputStream);
                }
            }
        }
    }

    /**
     * Handles the unpacking of the stream.
     * @param sourceInputStream Source input stream, must be decompressed (if TAR.GZ for example).
     * @throws Exception If the compressed file is broken
     */
    private void unpackStream(InputStream sourceInputStream) throws Exception
    {
        ArchiveStreamFactory asf = new ArchiveStreamFactory();
        try (ArchiveInputStream dearchivedInputStream =
            asf.createArchiveInputStream(sourceInputStream))
        {
            ArchiveEntry archiveEntry;
            while ((archiveEntry = dearchivedInputStream.getNextEntry()) != null)
            {
                String destinationEntry =
                    getFileHandler().append(getExtractDir(),
                        DefaultFileHandler.sanitizeFilename(archiveEntry.getName(), getLogger()));
                if (archiveEntry.isDirectory())
                {
                    getFileHandler().mkdirs(destinationEntry);
                }
                else
                {
                    String parent = getFileHandler().getParent(destinationEntry);
                    if (!getFileHandler().isDirectory(parent))
                    {
                        getFileHandler().mkdirs(parent);
                    }
                    try (OutputStream destinationFileOutputStream =
                        getFileHandler().getOutputStream(destinationEntry))
                    {
                        getFileHandler().copy(dearchivedInputStream, destinationFileOutputStream);
                    }
                }
            }
        }
    }

    /**
     * Downloads the compressed file containing the container files.
     */
    public void download()
    {
        // Try once with the proxy settings on (if set up by the user) and if it doesn't work,
        // try again with the previous proxy settings
        Map<String, String> previousProperties = null;
        try
        {
            if (this.proxy != null)
            {
                previousProperties = this.proxy.configure();
            }
            doDownload();
        }
        catch (Exception e)
        {
            if (this.proxy != null)
            {
                try
                {
                    this.proxy.clear(previousProperties);
                    doDownload();
                }
                catch (Exception ee)
                {
                    throw new ContainerException(
                        "Failed to download [" + this.remoteLocation + "]", ee);
                }
            }
            else
            {
                throw new ContainerException(
                    "Failed to download [" + this.remoteLocation + "]", e);
            }
        }
        finally
        {
            if (this.proxy != null)
            {
                this.proxy.clear(previousProperties);
            }
        }
    }

    /**
     * Perform the actual HTTP download.
     * @throws IOException if any I/O exception occurs (with the URL connection or file streams)
     */
    protected void doDownload() throws IOException
    {
        String downloadDir = getDownloadDir();
        if (!getFileHandler().exists(downloadDir))
        {
            getFileHandler().mkdirs(downloadDir);
        }
        String target = getFileHandler().append(downloadDir, getSourceFileName());

        getLogger().info("Downloading container from [" + this.remoteLocation + "] to ["
            + target + "]", getClass().getName());

        final URLConnection connection = this.remoteLocation.openConnection();
        connection.addRequestProperty("Accept", "*/*");
        connection.addRequestProperty("Accept-Encoding", "identity");

        connection.setUseCaches(false);
        if (connection instanceof HttpURLConnection)
        {
            HttpURLConnection httpConnection = (HttpURLConnection) connection;
            httpConnection.setInstanceFollowRedirects(true);

            String userInfo = this.remoteLocation.getUserInfo();
            if (userInfo != null)
            {
                connection.setRequestProperty("Authorization", "Basic "
                    + Base64.getEncoder().encodeToString(
                        userInfo.getBytes(StandardCharsets.UTF_8)));
            }
        }

        try (InputStream httpStream = connection.getInputStream();
            OutputStream fileStream = getFileHandler().getOutputStream(target))
        {
            getFileHandler().copy(httpStream, fileStream);
        }

        final long remoteTimestamp = connection.getLastModified();
        if (remoteTimestamp != 0)
        {
            File targetFile = new File(target);
            targetFile.setLastModified(remoteTimestamp);
        }
    }

    /**
     * @return the name of the source compressed file (without the path)
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
     * Sets proxy details.
     * 
     * @param proxy the proxy configuration to set
     */
    public void setProxy(Proxy proxy)
    {
        this.proxy = proxy;
    }
}
