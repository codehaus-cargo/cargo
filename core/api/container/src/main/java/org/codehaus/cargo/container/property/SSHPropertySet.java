/* 
 * ========================================================================
 * 
 * Copyright 2006 Vincent Massol.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * ========================================================================
 */
package org.codehaus.cargo.container.property;

/**
 * Gathers all ssh properties valid for all types of containers.
 * 
 * @version $Id: DatasourcePropertySet.java 1877 2009-02-13 14:09:28Z adriancole $
 */
public interface SSHPropertySet
{   
    /**
     * Host of the ssh destination.
     */
    String HOST = "cargo.ssh.host";

    /**
     * Username to use to authenticate against a ssh host.
     */
    String USERNAME = "cargo.ssh.username";

    /**
     * Password to use to authenticate against a ssh host.
     */
    String PASSWORD = "cargo.ssh.password";

    /**
     * RSA key used to authenticate against a ssh host.
     */
    String KEYFILE = "cargo.ssh.keyfile";

    /**
     * Base directory to deploy all files under
     */
    String REMOTEBASE = "cargo.ssh.remotebase";
}
