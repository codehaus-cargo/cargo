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
package org.codehaus.cargo.container.jetty;

import java.util.Arrays;
import java.util.List;

import org.codehaus.cargo.container.spi.packager.AbstractDirectoryPackager;

/**
 * Packager for a Jetty distribution.
 */
public class JettyDirectoryPackager extends AbstractDirectoryPackager
{
    /**
     * The portion of Jetty's distribution to exclude from packaging.
     */
    private static final List<String> EXCLUDED_FROM_DISTRIBUTION =
        Arrays.asList(
            "contexts/**", "etc/**", "logs/**", "resources/**", "start.d/**", "start.ini",
                "webapps/**");

    /**
     * The portion of Jetty's configuration to exclude from packaging.
     */
    private static final List<String> EXCLUDED_FROM_CONFIGURATION =
        Arrays.asList("logs/**");

    /**
     * {@inheritDoc}
     * @see AbstractDirectoryPackager#AbstractDirectoryPackager(String)
     */
    public JettyDirectoryPackager(String targetDirectory)
    {
        super(targetDirectory);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<String> getConfigurationExclusions()
    {
        return JettyDirectoryPackager.EXCLUDED_FROM_CONFIGURATION;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<String> getDistributionExclusions()
    {
        return JettyDirectoryPackager.EXCLUDED_FROM_DISTRIBUTION;
    }
}
