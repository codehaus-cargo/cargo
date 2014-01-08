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
package org.codehaus.cargo.tools.daemon;

/**
 * Gathers all properties related to the Cargo daemon.
 * 
 * @version $Id$
 */
public interface DaemonPropertySet
{
    /**
     * Full URL which specifies the daemon location
     */
    String URL = "cargo.daemon.url";

    /**
     * Username used when authenticating against the daemon host
     */
    String USERNAME = "cargo.daemon.username";

    /**
     * Password used when authenticating against the daemon host
     */
    String PASSWORD = "cargo.daemon.password";

    /**
     * Handle identifier for a container and deployables delivery
     */
    String HANDLE = "cargo.daemon.handleid";

    /**
     * Set this property to enable the daemon jvm launcher
     */
    String JVMLAUNCHER = "cargo.daemon.jvmlauncher";

    /**
     * Set this property to enable autostart for a container
     */
    String AUTOSTART = "cargo.daemon.autostart";
}
