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

/**
 * A factory to create JVM launchers.
 * 
 * @version $Id$
 */
public interface JvmLauncherFactory
{

    /**
     * Creates a new JVM launcher for the specified request.
     * 
     * @param request The details about the launch, must not be {@code null}.
     * @return The new JVM launcher, never {@code null}.
     */
    JvmLauncher createJvmLauncher(JvmLauncherRequest request);

}
