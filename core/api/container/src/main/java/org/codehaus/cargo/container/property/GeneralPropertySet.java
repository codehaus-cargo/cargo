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
 * Gathers all general container properties valid for all types of containers.
 */
public interface GeneralPropertySet
{
    /**
     * Protocol on which the container is listening to.
     */
    String PROTOCOL = "cargo.protocol";

    /**
     * Hostname on which the container is listening to.<br><br>
     * Note: In order to listen to all IP addresses, you can set this to <code>0.0.0.0</code>,
     * in that case the container will be set up to use that address but the Cargo ping component
     * will still ping on <code>localhost</code>.
     */
    String HOSTNAME = "cargo.hostname";

    /**
     * Logging level for logging container information.
     * 
     * @see LoggingLevel
     */
    String LOGGING = "cargo.logging";

    /**
     * JVM args to be used when starting/stopping containers (ex: <code>-Xmx500m</code>).
     */
    String JVMARGS = "cargo.jvmargs";

    /**
     * JVM args to be used when starting containers
     */
    String START_JVMARGS = "cargo.start.jvmargs";

    /**
     * Runtime args to be used when starting/stopping containers (ex: <code>-userThreads</code>).
     */
    String RUNTIME_ARGS = "cargo.runtime.args";

    /**
     * The port to use when communicating with this server, for example to start and stop it.
     */
    String RMI_PORT = "cargo.rmi.port";

    /**
     * The location of the jvm to use when starting/stopping containers.
     */
    String JAVA_HOME = "cargo.java.home";

    /**
     * Specify if the process should run spawned; i.e. outlive CARGO's process and that the started
     * container keeps running even after CARGO itself has terminated.
     */
    String SPAWN_PROCESS = "cargo.process.spawn";

    /**
     * Specify if CARGO's configuration generator for standalone containers should ignore when a
     * property cannot be replaced because it does not exist in the source file.
     */
    String IGNORE_NON_EXISTING_PROPERTIES = "cargo.standalone.ignoreNonExistingProperties";

    /**
     * The port offset to apply to the container ports.
     */
    String PORT_OFFSET = "cargo.port.offset";
}
