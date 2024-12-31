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
package org.codehaus.cargo.maven3;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.codehaus.cargo.tools.daemon.DaemonStart;

/**
 * Start a container via the daemon.
 */
@Mojo(
    name = "daemon-start", requiresDependencyResolution = ResolutionScope.TEST, threadSafe = true)
public class DaemonStartMojo extends AbstractDaemonMojo
{
    @Override
    protected void performAction() throws MojoExecutionException
    {
        DaemonStart request = new DaemonStart();

        request.setContainer(daemonContainer);
        request.setDeployables(daemonDeployables);
        request.setHandleId(daemonHandleId);
        request.setAutostart(daemonAutostartContainer);
        request.setAdditionalClasspathEntries(daemonClasspaths);

        try
        {
            if (getContainerElement() != null)
            {
                request.setInstallerZipFile(getContainerElement().getInstallerZipFile());

                File logFile = getContainerElement().getLog();

                if (logFile != null)
                {
                    request.setLogFile(logFile.getName());
                }
            }

            daemonClient.start(request);
        }
        catch (Exception e)
        {
            throw new MojoExecutionException(e.toString());
        }

        waitDeployableMonitor(daemonContainer, true);
    }
}
