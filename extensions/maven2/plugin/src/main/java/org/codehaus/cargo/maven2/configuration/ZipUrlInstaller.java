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
package org.codehaus.cargo.maven2.configuration;

import java.net.URL;

import org.codehaus.cargo.container.installer.ZipURLInstaller;
import org.codehaus.cargo.container.installer.Proxy;

/**
 * Holds configuration data for the <code>&lt;zipUrlInstaller&gt;</code> tag used to configure
 * the plugin in the <code>pom.xml</code> file.
 *  
 * @version $Id$
 */
public class ZipUrlInstaller 
{
    /**
     * URL where to find the zipped container installation file.
     */
    private URL url;

    /**
     * Location where the container distribution zip will be downloaded and installed.
     * If not specified it will default to <code>{java.io.tmpdir}/installs</code>.
     */
    private String installDir;

    /**
     * Proxy properties.
     */
    private Proxy proxy;
    
    /**
     * @see org.codehaus.cargo.container.installer.Installer#install()
     */
    public final void setInstallDir(String installDir)
    {
        this.installDir = installDir;
    }

    /**
     * @see org.codehaus.cargo.container.installer.Installer#install()
     */
    public final void setUrl(URL url)
    {
        this.url = url;
    }
    
    /**
     * @see #setInstallDir(String)
     */
    public final String getInstallDir()
    {
        return this.installDir;
    }
    
    /**
     * @see #setUrl(URL)
     */
    public final URL getUrl()
    {
        return this.url;
    }

    /**
     * @see #createProxy()
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
        ZipURLInstaller installer = new ZipURLInstaller(getUrl(), getInstallDir());
        if (getProxy() != null)
        {
            installer.setProxy(getProxy());
        }
        return installer;
    }

}
