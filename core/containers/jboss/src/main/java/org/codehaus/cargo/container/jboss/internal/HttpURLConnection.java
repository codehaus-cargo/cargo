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
package org.codehaus.cargo.container.jboss.internal;

/**
 * Perform a HTTP GET to a URL.
 * 
 * @version $Id$
 */
public interface HttpURLConnection
{
    /**
     * @param timeout socket read timeout, in milliseconds
     */
    void setTimeout(int timeout);

    /**
     * @param url authenticated URL to connect to (basic authentication)
     * @param username the username to use for authentication
     * @param password the password to use for authentication
     */
    void connect(String url, String username, String password);
}
