/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2026 Ali Tokmen.
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
import java.util.HashMap;
import java.util.Map;

import org.codehaus.cargo.util.log.LoggedObject;

/**
 * Nested Ant element to specify proxy properties.
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
    public void setHost(String host)
    {
        this.host = host;
    }

    /**
     * @return the proxy host
     */
    public String getHost()
    {
        return this.host;
    }

    /**
     * @param port the proxy port. Defaults to 80 if not set
     */
    public void setPort(int port)
    {
        this.port = port;
    }

    /**
     * @return the proxy port or 80 if not set
     */
    public int getPort()
    {
        return this.port;
    }

    /**
     * @param user the user for authenticating proxies
     */
    public void setUser(String user)
    {
        this.user = user;
    }

    /**
     * @return the user for authenticating proxies
     */
    public String getUser()
    {
        return this.user;
    }

    /**
     * @param password the password for authenticating proxies
     */
    public void setPassword(String password)
    {
        this.password = password;
    }

    /**
     * @return the password for authenticating proxies
     */
    public String getPassword()
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
    public void setExcludeHosts(String proxyExcludeHosts)
    {
        this.excludeHosts = proxyExcludeHosts;
    }

    /**
     * @return the list of hosts which should be connected too directly and not through the proxy
     * server
     * @see #setExcludeHosts(String)
     */
    public String getExcludeHosts()
    {
        return this.excludeHosts;
    }

    /**
     * Set the Java system properties related to proxies.
     * 
     * @return Previous proxy properties, to be used when {@link Proxy#clear(java.util.Map)} is
     * called.
     */
    public Map<String, String> configure()
    {
        Map<String, String> previousProperties = new HashMap<String, String>();

        if (getHost() != null && !getHost().trim().isEmpty())
        {
            getLogger().debug("host : " + getHost(), this.getClass().getName());
            getLogger().debug("port : " + getPort(), this.getClass().getName());
            getLogger().debug("excludeHosts : " + getExcludeHosts(), this.getClass().getName());
            getLogger().debug("user : " + getUser(), this.getClass().getName());

            String host = getHost().trim();
            String port = Integer.toString(getPort());

            previousProperties.put("http.proxyHost", System.setProperty("http.proxyHost", host));
            previousProperties.put("http.proxyPort", System.setProperty("http.proxyPort", port));
            previousProperties.put("https.proxyHost", System.setProperty("https.proxyHost", host));
            previousProperties.put("https.proxyPort", System.setProperty("https.proxyPort", port));
            previousProperties.put("ftp.proxyHost", System.setProperty("ftp.proxyHost", host));
            previousProperties.put("ftp.proxyPort", System.setProperty("ftp.proxyPort", port));

            if (getExcludeHosts() != null && !getExcludeHosts().trim().isEmpty())
            {
                String excludedHosts = getExcludeHosts().trim();

                previousProperties.put("http.nonProxyHosts",
                    System.setProperty("http.nonProxyHosts", excludedHosts));
                previousProperties.put("https.nonProxyHosts",
                    System.setProperty("https.nonProxyHosts", excludedHosts));
                previousProperties.put("ftp.nonProxyHosts",
                    System.setProperty("ftp.nonProxyHosts", excludedHosts));
            }
            else
            {
                previousProperties.put("http.nonProxyHosts",
                    System.clearProperty("http.nonProxyHosts"));
                previousProperties.put("https.nonProxyHosts",
                    System.clearProperty("https.nonProxyHosts"));
                previousProperties.put("ftp.nonProxyHosts",
                    System.clearProperty("ftp.nonProxyHosts"));
            }

            if (getUser() != null && !getUser().trim().isEmpty())
            {
                String user = getUser().trim();

                previousProperties.put("http.proxyUser",
                    System.setProperty("http.proxyUser", user));
                previousProperties.put("https.proxyUser",
                    System.setProperty("https.proxyUser", user));
                previousProperties.put("ftp.proxyUser",
                    System.setProperty("ftp.proxyUser", user));

                String password;
                if (getPassword() != null && !getPassword().trim().isEmpty())
                {
                    password = getPassword().trim();

                    previousProperties.put("http.proxyPassword",
                        System.setProperty("http.proxyPassword", password));
                    previousProperties.put("https.proxyPassword",
                        System.setProperty("https.proxyPassword", password));
                    previousProperties.put("ftp.proxyPassword",
                        System.setProperty("ftp.proxyPassword", password));
                }
                else
                {
                    password = "";

                    previousProperties.put("http.proxyPassword",
                        System.clearProperty("http.proxyPassword"));
                    previousProperties.put("https.proxyPassword",
                        System.clearProperty("https.proxyPassword"));
                    previousProperties.put("ftp.proxyPassword",
                        System.clearProperty("ftp.proxyPassword"));
                }

                Authenticator.setDefault(new ProxyAuthenticator(user, password));
            }
            else
            {
                previousProperties.put("http.proxyUser",
                    System.clearProperty("http.proxyUser"));
                previousProperties.put("https.proxyUser",
                    System.clearProperty("https.proxyUser"));
                previousProperties.put("ftp.proxyUser",
                    System.clearProperty("ftp.proxyUser"));

                previousProperties.put("http.proxyPassword",
                    System.clearProperty("http.proxyPassword"));
                previousProperties.put("https.proxyPassword",
                    System.clearProperty("https.proxyPassword"));
                previousProperties.put("ftp.proxyPassword",
                    System.clearProperty("ftp.proxyPassword"));
            }
        }

        return previousProperties;
    }

    /**
     * Clear all proxy settings.
     * 
     * @param previousProperties Previous proxy properties, as returned by
     * {@link Proxy#configure()}.
     */
    public void clear(Map<String, String> previousProperties)
    {
        if (previousProperties != null)
        {
            for (Map.Entry<String, String> previousProperty : previousProperties.entrySet())
            {
                if (previousProperty.getValue() != null)
                {
                    System.setProperty(previousProperty.getKey(), previousProperty.getValue());
                }
                else
                {
                    System.clearProperty(previousProperty.getKey());
                }
            }
            Authenticator.setDefault(null);
        }
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
