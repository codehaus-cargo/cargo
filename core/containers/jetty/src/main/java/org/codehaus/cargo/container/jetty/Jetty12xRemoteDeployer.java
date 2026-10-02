/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2026 Ali Tokmen.
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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codehaus.cargo.container.RemoteContainer;

/**
 * {@inheritDoc}
 */
public class Jetty12xRemoteDeployer extends JettyRemoteDeployer
{
    /**
     * Pattern for matching Jetty EE version numbers.
     */
    private static final Pattern EE_VERSION_PATTERN = Pattern.compile("ee(\\d+)");

    /**
     * Jetty EE version used.
     */
    private String eeVersion;

    /**
     * {@inheritDoc}
     * @see Jetty12xRemoteDeployer#Jetty12xRemoteDeployer(RemoteContainer)
     */
    public Jetty12xRemoteDeployer(RemoteContainer container)
    {
        super(container);

        this.eeVersion = getContainer().getConfiguration().getPropertyValue(
            JettyPropertySet.DEPLOYER_EE_VERSION);

        Matcher matcher = EE_VERSION_PATTERN.matcher(this.eeVersion);
        if (!matcher.matches())
        {
            throw new IllegalArgumentException(
                "EE version [" + this.eeVersion + "] doesn't match " + EE_VERSION_PATTERN);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String createDefaultDeployerUrl()
    {
        String defaultDeployerUrl = super.createDefaultDeployerUrl();

        return defaultDeployerUrl + "-" + this.eeVersion;
    }
}
