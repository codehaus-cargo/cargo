/*
 * ========================================================================
 *
 *  Codehaus CARGO, copyright 2004-2011 Vincent Massol, 2012-2017 Ali Tokmen.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  ========================================================================
 */

package org.codehaus.cargo.container.wildfly.swarm.internal.jvm;

import org.codehaus.cargo.container.spi.jvm.JvmLauncher;
import org.codehaus.cargo.container.spi.jvm.JvmLauncherFactory;
import org.codehaus.cargo.container.spi.jvm.JvmLauncherRequest;

/**
 * Factory for a custom WildFly Swarm JVM launcher.
 * */
public class SwarmJvmLauncherFactory implements JvmLauncherFactory
{
    /**
     * Creates a new @see {@link SwarmJvmLauncher} and sets logger.
     * {@inheritDoc}
     * @return Swarm JVM launcher instance.
     * */
    @Override
    public JvmLauncher createJvmLauncher(final JvmLauncherRequest request)
    {
        return new SwarmJvmLauncher(request.getLoggable().getLogger());
    }
}
