/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2024 Ali Tokmen.
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
package org.codehaus.cargo.daemon;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.cargo.util.DefaultFileHandler;

/**
 * Cargo daemon authentication filter. Supports Basic authentication only, the password file is
 * specified using the {@link #PASSWORD_PROPERTIES_FILE} property (either in the filter init
 * configuration or as a system property).
 */
public class CargoDaemonAuthenticationFilter implements Filter
{
    /**
     * Property used for the password file, can be passed as a system property or a filter init
     * param. The password file can use hashes. Example file contents:<br>
     * <br>
     * <code>cargo-daemon-user1=cargo-password<br>
     * cargo-daemon-user2={MD5}9addb63b65b01292700094b0ef056036<br>
     * cargo-daemon-user3={SHA-1}2681c738294805939045be2a4af53b687c25bf4d</code><br>
     * <br>
     * The hashing algorithms can be any algorithm supported by {@link MessageDigest}.
     */
    public static final String PASSWORD_PROPERTIES_FILE = "cargo.daemon.passwordFile";

    /**
     * Username and password pairs.
     */
    private Map<String, PasswordWithHash> usernamePasswords = null;

    /**
     * Parses a given password input stream. The contents can use hashes, some examples:<br>
     * <br>
     * <code>cargo-daemon-user1=cargo-password<br>
     * cargo-daemon-user2={MD5}9addb63b65b01292700094b0ef056036<br>
     * cargo-daemon-user3={SHA-1}2681c738294805939045be2a4af53b687c25bf4d</code><br>
     * <br>
     * The hashing algorithms can be any algorithm supported by {@link MessageDigest}.
     * 
     * @param resource Input stream for the password file.
     * @return Parsed username and password pairs.
     * @throws IOException If reading fails.
     * @throws NoSuchAlgorithmException If the digest algorithm isn't known to the JVM.
     */
    public static Map<String, PasswordWithHash> parsePasswordFile(InputStream resource)
        throws IOException, NoSuchAlgorithmException
    {
        Properties usernamePasswordsProperties = new Properties();
        usernamePasswordsProperties.load(resource);
        Set<String> usernames = usernamePasswordsProperties.stringPropertyNames();
        Map<String, PasswordWithHash> usernamePasswords =
            new HashMap<String, PasswordWithHash>(usernames.size());
        for (String username : usernames)
        {
            String passwordWithDigest = usernamePasswordsProperties.getProperty(username);
            if (passwordWithDigest == null || passwordWithDigest.isEmpty())
            {
                usernamePasswords.put(username, null);
            }
            else
            {
                usernamePasswords.put(username, new PasswordWithHash(passwordWithDigest));
            }
        }
        return usernamePasswords;
    }

    /**
     * Loads the password file. {@inheritDoc}
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException
    {
        String passwordPropertiesFile = System.getProperty(PASSWORD_PROPERTIES_FILE);
        if (passwordPropertiesFile == null)
        {
            passwordPropertiesFile = filterConfig.getInitParameter(PASSWORD_PROPERTIES_FILE);
        }
        if (passwordPropertiesFile != null)
        {
            try (InputStream resource =
                new DefaultFileHandler().getInputStream(passwordPropertiesFile))
            {
                usernamePasswords = CargoDaemonAuthenticationFilter.parsePasswordFile(resource);
            }
            catch (IOException | NoSuchAlgorithmException e)
            {
                throw new ServletException(
                    "Cannot load password properties file [" + passwordPropertiesFile + "]", e);
            }
            if (usernamePasswords == null || usernamePasswords.isEmpty())
            {
                throw new ServletException(
                    "Password properties file [" + passwordPropertiesFile + "] is empty");
            }
        }
    }

    /**
     * Checks the incoming query against the known passwords. {@inheritDoc}
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException
    {
        boolean authorized = false;

        HttpServletRequest req = (HttpServletRequest) request;
        if (usernamePasswords == null || req.getRequestURI().startsWith("/error"))
        {
            // No authentication required
            authorized = true;
        }
        else
        {
            final String authorization = req.getHeader("Authorization");
            if (authorization != null)
            {
                if (!authorization.toLowerCase(Locale.ENGLISH).startsWith("basic"))
                {
                    throw new ServletException("Only HTTP Basic authentication is supported");
                }

                // Authorization: Basic base64credentials
                String base64Credentials = authorization.substring("Basic".length()).trim();
                byte[] credDecoded = Base64.getDecoder().decode(base64Credentials);
                String credentials = new String(credDecoded, StandardCharsets.UTF_8);

                // credentials = username:password
                final String[] values = credentials.split(":", 2);

                if (usernamePasswords.containsKey(values[0]))
                {
                    PasswordWithHash passwordDigest = usernamePasswords.get(values[0]);
                    if (passwordDigest == null)
                    {
                        // Username with empty or no password
                        if (values.length == 1 || values[1].length() == 0)
                        {
                            authorized = true;
                        }
                    }
                    else
                    {
                        if (values.length > 1 && passwordDigest.matches(values[1]))
                        {
                            // Username matches expected password (or hash)
                            authorized = true;
                        }
                    }
                }
            }
        }

        if (authorized)
        {
            chain.doFilter(request, response);
        }
        else
        {
            HttpServletResponse resp = (HttpServletResponse) response;
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void destroy()
    {
        // Nothing
    }
}
