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
package org.codehaus.cargo.container.websphere;

/**
 * Gathers all WebSphere properties.
 * 
 * @version $Id$
 */
public interface WebSpherePropertySet
{
    /**
     * User with administrator rights.
     */
    String ADMIN_USERNAME = "cargo.websphere.administrator.user";

    /**
     * Password for user with administrator rights.
     */
    String ADMIN_PASSWORD = "cargo.websphere.administrator.password";

    /**
     * WebSphere profile name.
     */
    String PROFILE = "cargo.websphere.profile";

    /**
     * WebSphere node name.
     */
    String NODE = "cargo.websphere.node";

    /**
     * WebSphere cell name.
     */
    String CELL = "cargo.websphere.cell";

    /**
     * WebSphere server name.
     */
    String SERVER = "cargo.websphere.server";

    /**
     * Log level used in the server log.
     */
    String LOGGING = "cargo.websphere.logging";


    /**
     * The processor type within lib/native/{OS} (e.g. 32, 64)
     */
    String PROCESSOR_ARCH = "cargo.websphere.arch";
}
