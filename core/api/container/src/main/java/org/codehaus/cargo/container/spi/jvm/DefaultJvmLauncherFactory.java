/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2025 Ali Tokmen.
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

import java.util.List;

/**
 * The factory to create JVM launchers.
 */
public class DefaultJvmLauncherFactory implements JvmLauncherFactory
{
    /**
     * The additional classpath entries to use.
     */
    private List<String> additionalClasspathEntries;

    /**
     * Constructs a launcher with default settings.
     */
    public DefaultJvmLauncherFactory()
    {
        // Nothing
    }

    /**
     * Constructs a launcher with additional classpath entries.
     * 
     * @param additionalClasspathEntries The addtional classpath.
     */
    public DefaultJvmLauncherFactory(List<String> additionalClasspathEntries)
    {
        this.additionalClasspathEntries = additionalClasspathEntries;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JvmLauncher createJvmLauncher(JvmLauncherRequest request)
    {
        DefaultJvmLauncher launcher = new DefaultJvmLauncher();

        if (additionalClasspathEntries != null && additionalClasspathEntries.size() != 0)
        {
            launcher.addClasspathEntries(additionalClasspathEntries);
        }

        return launcher;
    }

}
