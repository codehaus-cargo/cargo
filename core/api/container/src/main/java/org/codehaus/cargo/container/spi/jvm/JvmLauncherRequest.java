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
package org.codehaus.cargo.container.spi.jvm;

import org.codehaus.cargo.util.log.Loggable;

/**
 * A request to construct a JVM launcher.
 * 
 * @version $Id$
 */
public class JvmLauncherRequest
{

    /**
     * {@code true} to launch a server process, {@code false} to launch a client/utility process.
     */
    private boolean server;

    /**
     * The object to forward all logging to.
     */
    private Loggable loggable;

    /**
     * {@code true} to launch a remote JVM via SSH, {@code false} to launch a local JVM
     */
    private boolean ssh;

    /**
     * Creates a new JVM launch request with the specified properties.
     * 
     * @param server {@code true} to launch a server process, {@code false} to launch a
     * client/utility process.
     * @param loggable The object to forward all logging to, must not be {@code null}.
     */
    public JvmLauncherRequest(boolean server, Loggable loggable)
    {
        this(server, loggable, false);
    }

    /**
     * Creates a new JVM launch request with the specified properties.
     * 
     * @param server {@code true} to launch a server process, {@code false} to launch a
     * client/utility process.
     * @param loggable The object to forward all logging to, must not be {@code null}.
     * @param ssh {@code true} to launch a remote JVM via SSH, {@code false} to launch a local JVM.
     */
    public JvmLauncherRequest(boolean server, Loggable loggable, boolean ssh)
    {
        if (loggable == null)
        {
            throw new IllegalArgumentException("JVM launch loggable missing");
        }
        this.server = server;
        this.loggable = loggable;
        this.ssh = ssh;
    }

    /**
     * Indicates whether the launched JVM denotes a server process or just some (usually
     * short-lived) client/utility process.
     * 
     * @return {@code true} to launch a server process, {@code false} to launch a client/utility
     * process.
     */
    public boolean isServer()
    {
        return this.server;
    }

    /**
     * Gets the object to forward all logging to.
     * 
     * @return The object to forward all logging to, never {@code null}.
     */
    public Loggable getLoggable()
    {
        return this.loggable;
    }

    /**
     * Indicates whether the JVM should be launched on a remote host via SSH or on the local
     * machine.
     * 
     * @return {@code true} to launch a remote JVM, {@code false} to launch a local JVM.
     */
    public boolean isSsh()
    {
        return this.ssh;
    }

}
