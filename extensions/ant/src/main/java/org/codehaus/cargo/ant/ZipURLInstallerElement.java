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
     * Location where the container distribution zip will be downloaded and installed. If not
     * specified it will default to <code>{java.io.tmpdir}/installs</code>.
     */
    private String installDir; 

    /**
     * Proxy properties.
     */
    private Proxy proxy;
    
    /**
     * @param installDir the install directory
     */
    public final void setInstallDir(String installDir)
    {
        this.installDir = installDir;
    }

    /**
     * @param installURL the install URL to download container from
     */
    public final void setInstallURL(URL installURL)
    {
        this.installURL = installURL;
    }
    
    /**
     * @return the install directory
     */
    public final String getInstallDir()
    {
        return this.installDir;
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
     *         by the user
     */
    public ZipURLInstaller createInstaller()
    {
        ZipURLInstaller installer = new ZipURLInstaller(getInstallURL(), getInstallDir());
        if (getProxy() != null)
        {
            installer.setProxy(getProxy());
        }
        return installer;
    }
}
