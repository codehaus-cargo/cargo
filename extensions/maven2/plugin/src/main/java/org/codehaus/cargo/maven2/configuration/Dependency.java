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
package org.codehaus.cargo.maven2.configuration;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.cargo.maven2.util.CargoProject;

/**
 * Allow users to add classpath entries to the classpath used to start the container. This is the
 * configuration class for passing <code>LocalContainer.addExtraClasspath()</code> information.
 * 
 * @version $Id$
 */
public class Dependency extends AbstractDependency
{
    /**
     * Get the path of this dependency.
     * @param project Cargo project.
     * @return Path of this dependency.
     * @throws MojoExecutionException If the artifact is incorrect.
     */
    public String getDependencyPath(CargoProject project) throws MojoExecutionException
    {
        String path = getLocation();

        if (path == null)
        {
            if (getGroupId() == null || getArtifactId() == null)
            {
                throw new MojoExecutionException("You must specify a groupId/artifactId or "
                    + "a location that points to a directory or JAR");
            }

            // Default to jar if not type is specified
            if (getType() == null)
            {
                setType("jar");
            }

            path = findArtifactLocation(project.getArtifacts(), project.getLog());
        }

        project.getLog().debug("Classpath location = [" + new File(path).getPath() + "]");

        return new File(path).getPath();
    }
}
