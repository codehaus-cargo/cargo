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
package org.codehaus.cargo.maven2.configuration;

import java.io.File;
import java.net.URL;
import java.util.logging.Logger;

import org.codehaus.cargo.container.installer.Proxy;
import org.codehaus.cargo.container.installer.ZipURLInstaller;

/**
 * Holds configuration data for the <code>&lt;zipUrlInstaller&gt;</code> tag used to configure the
 * plugin in the <code>pom.xml</code> file.
 * 
 * @version $Id$
 */
public class ZipUrlInstaller
{
    /**
     * Default extraction subdirectory.
     */
    public static final String EXTRACT_SUBDIRECTORY = "cargo/installs";

    /**
     * URL where to find the zipped container installation file.
     */
    private URL url;

    /**
     * Destination directory where the zipped container install will be downloaded.
     */
    private String downloadDir;

    /**
     * Destination directory where the zipped container install will be extracted.
     */
    private String extractDir;

    /**
     * Proxy properties.
     */
    private Proxy proxy;

    /**
     * @param installDir the destination directory where the zipped container install will be
     * downloaded and installed.
     * @deprecated Use {@link #setDownloadDir(String)} and {@link #setExtractDir(String)} instead.
     */
    @Deprecated
    public void setInstallDir(String installDir)
    {
        Logger.getLogger(this.getClass().getName()).warning("The ZipURLInstallerElement's "
            + "installDir attribute has been deprecated! Please use the downloadDir and "
            + "extractDir attributes.");

        this.setDownloadDir(installDir);
        this.setExtractDir(installDir);
    }

    /**
     * @param downloadDir the destination directory where the zipped container install will be
     * downloaded.
     */
    public void setDownloadDir(String downloadDir)
    {
        this.downloadDir = downloadDir;
    }

    /**
     * @param extractDir the destination directory where the zipped container install will be
     * installed.
     */
    public void setExtractDir(String extractDir)
    {
        this.extractDir = extractDir;
    }

    /**
     * @param url URL where to find the zipped container installation file.
     */
    public final void setUrl(URL url)
    {
        this.url = url;
    }

    /**
     * @return <code>null</code>
     * @deprecated Use {@link #getDownloadDir()} and {@link #getExtractDir()} instead.
     */
    @Deprecated
    public final String getInstallDir()
    {
        Logger.getLogger(this.getClass().getName()).warning("The ZipURLInstallerElement's "
            + "installDir attribute has been deprecated! Please use the downloadDir and "
            + "extractDir attributes.");

        return null;
    }

    /**
     * @return the destination directory where the zipped container install will be downloaded.
     */
    public String getDownloadDir()
    {
        return this.downloadDir;
    }

    /**
     * @return the destination directory where the zipped container install will be installed.
     */
    public String getExtractDir()
    {
        return this.extractDir;
    }

    /**
     * @return URL where to find the zipped container installation file.
     */
    public final URL getUrl()
    {
        return this.url;
    }

    /**
     * @return Proxy properties.
     */
    public final Proxy getProxy()
    {
        return this.proxy;
    }

    /**
     * @return The configured {@link Proxy} element.
     */
    public final Proxy createProxy()
    {
        if (getProxy() == null)
        {
            this.proxy = new Proxy();
        }

        return this.proxy;
    }

    /**
     * Creates the {@link ZipURLInstaller} with the appropriate configuration.
     * @param projectBuildDirectory Project build directory.
     * @return a new instance of {@link ZipURLInstaller} configured using the attributes specified
     * by the user
     */
    public ZipURLInstaller createInstaller(String projectBuildDirectory)
    {
        String extractDir = getExtractDir();
        if (extractDir == null)
        {
            extractDir = new File(projectBuildDirectory, EXTRACT_SUBDIRECTORY).getPath();
        }
        ZipURLInstaller installer = new ZipURLInstaller(getUrl(), getDownloadDir(), extractDir);
        if (getProxy() != null)
        {
            installer.setProxy(getProxy());
        }
        return installer;
    }

}
