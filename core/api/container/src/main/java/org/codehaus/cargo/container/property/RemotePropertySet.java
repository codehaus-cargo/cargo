/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2023 Ali Tokmen.
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
package org.codehaus.cargo.container.property;

/**
 * Gathers all general container properties valid for all types of remote containers.
 */
public interface RemotePropertySet
{
    /**
     * URI to use when manipulating a remote container.
     */
    String URI = "cargo.remote.uri";

    /**
     * Username to use to authenticate against a remote container (when deploying for example).
     */
    String USERNAME = "cargo.remote.username";

    /**
     * Password to use to authenticate against a remote container (when deploying for example).
     */
    String PASSWORD = "cargo.remote.password";

    /**
     * Timeout used in remote deployments (in milliseconds).
     */
    String TIMEOUT = "cargo.remote.timeout";
}
