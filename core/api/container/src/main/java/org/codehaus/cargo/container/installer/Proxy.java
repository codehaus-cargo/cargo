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
package org.codehaus.cargo.container.installer;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.Properties;

import org.codehaus.cargo.util.log.LoggedObject;

/**
 * Nested Ant element to specify proxy properties.
 * 
 * @version $Id$
 */
public class Proxy extends LoggedObject
{
    /**
     * @see #getHost()
     */
    private String host;

    /**
     * @see #getPort()
     */
    private int port = 80;

    /**
     * @see #getUser()
     */
    private String user;

    /**
     * @see #getPassword()
     */
    private String password;

    /**
     * @see #getExcludeHosts()
     */
    private String excludeHosts = "";

    /**
     * @param host the proxy host
     */
    public final void setHost(String host)
    {
        this.host = host;
    }

    /**
     * @return the proxy host
     */
    public final String getHost()
    {
        return this.host;
    }

    /**
     * @param port the proxy port. Defaults to 80 if not set
     */
    public final void setPort(int port)
    {
        this.port = port;
    }

    /**
     * @return the proxy port or 80 if not set
     */
    public final int getPort()
    {
        return this.port;
    }

    /**
     * @param user the user for authenticating proxies
     */
    public final void setUser(String user)
    {
        this.user = user;
    }

    /**
     * @return the user for authenticating proxies
     */
    public final String getUser()
    {
        return this.user;
    }

    /**
     * @param password the password for authenticating proxies
     */
    public final void setPassword(String password)
    {
        this.password = password;
    }

    /**
     * @return the password for authenticating proxies
     */
    public final String getPassword()
    {
        return this.password;
    }

    /**
     * List of hosts which should be connected too directly and not through the proxy server. The
     * value can be a list of hosts, each seperated by a |, and in addition a wildcard character (*)
     * can be used for matching. For example: -Dhttp.nonProxyHosts="*.foo.com|localhost".
     * 
     * @param proxyExcludeHosts list of hosts that do not go through the proxy
     */
    public final void setExcludeHosts(String proxyExcludeHosts)
    {
        this.excludeHosts = proxyExcludeHosts;
    }

    /**
     * @return the list of hosts which should be connected too directly and not through the proxy
     *         server
     * @see #setExcludeHosts(String)
     */
    public final String getExcludeHosts()
    {
        return this.excludeHosts;
    }

    /**
     * Set the Java system properties related to proxies.
     */
    public void configure()
    {
        if ((getHost() != null) && (getHost().trim().length() > 0))
        {
            getLogger().debug("host : " + getHost(), this.getClass().getName());
            getLogger().debug("port : " + getPort(), this.getClass().getName());
            getLogger().debug("excludeHosts : " + getExcludeHosts(), this.getClass().getName());
            getLogger().debug("user : " + getUser(), this.getClass().getName());

            Properties sysprops = System.getProperties();
            String portString = Integer.toString(getPort());

            sysprops.put("http.proxyHost", getHost());
            sysprops.put("http.proxyPort", portString);
            sysprops.put("https.proxyHost", getHost());
            sysprops.put("https.proxyPort", portString);
            sysprops.put("ftp.proxyHost", getHost());
            sysprops.put("ftp.proxyPort", portString);

            if (getExcludeHosts() != null)
            {
                sysprops.put("http.nonProxyHosts", getExcludeHosts());
                sysprops.put("https.nonProxyHosts", getExcludeHosts());
                sysprops.put("ftp.nonProxyHosts", getExcludeHosts());
            }
            if (getUser() != null)
            {
                sysprops.put("http.proxyUser", getUser());
                sysprops.put("http.proxyPassword", getPassword());
                Authenticator.setDefault(new ProxyAuthenticator(getUser(), getPassword()));
            }
        }
    }

    /**
     * Clear all proxy settings.
     */
    public void clear()
    {
        Properties sysprops = System.getProperties();
        sysprops.remove("http.proxyHost");
        sysprops.remove("http.proxyPort");
        sysprops.remove("http.proxyUser");
        sysprops.remove("http.proxyPassword");
        sysprops.remove("https.proxyHost");
        sysprops.remove("https.proxyPort");
        sysprops.remove("ftp.proxyHost");
        sysprops.remove("ftp.proxyPort");
        Authenticator.setDefault(new ProxyAuthenticator("", ""));
    }

    /**
     * Authenticator for the Proxy.
     */
    private static final class ProxyAuthenticator extends Authenticator
    {
        /**
         * Authentication using a username/password.
         */
        private PasswordAuthentication authentication;

        /**
         * @param user the username
         * @param password the password
         */
        private ProxyAuthenticator(String user, String password)
        {
            this.authentication = new PasswordAuthentication(user, password.toCharArray());
        }

        /**
         * {@inheritDoc}
         * @see java.net.Authenticator#getPasswordAuthentication()
         */
        @Override
        protected PasswordAuthentication getPasswordAuthentication()
        {
            return this.authentication;
        }
    }
}
