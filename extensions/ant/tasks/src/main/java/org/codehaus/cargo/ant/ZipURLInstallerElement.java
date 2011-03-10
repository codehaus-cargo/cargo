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
package org.codehaus.cargo.ant;

import java.net.URL;
import java.util.logging.Logger;

import org.codehaus.cargo.container.installer.Proxy;
import org.codehaus.cargo.container.installer.ZipURLInstaller;

/**
 * Nested Ant element to wrap the {@link ZipURLInstaller} class.
 * 
 * @version $Id$
 */
public class ZipURLInstallerElement
{
    /**
     * URL where to find the zipped container installation file.
     */
    private URL installURL;

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
     * @param installURL the install URL to download container from
     */
    public final void setInstallURL(URL installURL)
    {
        this.installURL = installURL;
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
     * @return the install URL to download container from
     */
    public final URL getInstallURL()
    {
        return this.installURL;
    }

    /**
     * @return proxy properties.
     */
    public final Proxy getProxy()
    {
        return this.proxy;
    }

    /**
     * @return the configured {@link Proxy} element
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
     * @return a new instance of {@link ZipURLInstaller} configured using the attributes specified
     * by the user
     */
    public ZipURLInstaller createInstaller()
    {
        ZipURLInstaller installer = new ZipURLInstaller(getInstallURL(), getDownloadDir(),
            getExtractDir());
        if (getProxy() != null)
        {
            installer.setProxy(getProxy());
        }
        return installer;
    }
}
