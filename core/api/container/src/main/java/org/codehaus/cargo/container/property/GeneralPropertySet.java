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
package org.codehaus.cargo.container.property;

/**
 * Gathers all general container properties valid for all types of containers.
 * 
 * @version $Id$
 */
public interface GeneralPropertySet
{
    /**
     * Protocol on which the container is listening to.
     */
    String PROTOCOL = "cargo.protocol";

    /**
     * Hostname on which the container is listening to.
     */
    String HOSTNAME = "cargo.hostname";

    /**
     * Logging level for logging container information.
     */
    String LOGGING = "cargo.logging";

    /**
     * JVM args to be used when starting/stopping containers (ex: "-Xmx500m").
     */
    String JVMARGS = "cargo.jvmargs";

    /**
     * Runtime args to be used when starting/stopping containers (ex: "-userThreads").
     */
    String RUNTIME_ARGS = "cargo.runtime.args";

    /**
     * The port to use when communicating with this server, for example to start and stop it.
     */
    String RMI_PORT = "cargo.rmi.port";
    
    /**
     * URI Encoding to set.
     */
    String URI_ENCODING = "cargo.servlet.uriencoding";
    
    /**
     * The location of the jvm to use when starting/stopping containers.
     */
    String JAVA_HOME = "cargo.java.home";
}
