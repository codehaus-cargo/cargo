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
package org.codehaus.cargo.container.weblogic;

/**
 * All WebLogic configuration implementations must implement this interface which provides method to
 * find out the location of key directories needed to operate WebLogic.
 * 
 */
public interface WebLogicConfiguration
{

    /**
     * The DOMAIN_HOME holds the configuration and runtime files of a WebLogic domain. One or more
     * server processes execute from this directory and must have permissions to write to it.
     * 
     * @return The DOMAIN_HOME, or instance-specific installation of WebLogic
     */
    String getDomainHome();

}
