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

    /**
     * Classloader mode used when deploying/starting the deployable(s).
     * Possible values are PARENT_FIRST and PARENT_LAST
     *
     * Specifies whether the classes are first loaded from the container
     * and only after that from the deployable (PARENT_FIRST) or the
     * other way around - first from the deployable and then from the
     * container (PARENT_LAST).
     *
     */
    String CLASSLOADER_MODE = "cargo.websphere.classloader.mode";

    /**
     * Classloader policy used when deploying/starting the deployable(s).
     * Possible values are MULTIPLE (default) and SINGLE.
     *
     * Specifies whether there is one classloader for all war files
     * in the application or separate classloader for each war.
     *
     */
    String WAR_CLASSLOADER_POLICY = "cargo.websphere.war.classloader.policy";

}
