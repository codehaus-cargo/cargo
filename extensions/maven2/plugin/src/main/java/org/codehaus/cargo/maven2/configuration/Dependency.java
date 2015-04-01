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
package org.codehaus.cargo.maven2.configuration;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.cargo.maven2.util.CargoProject;

/**
 * Allow users to add classpath entries to the classpath used to start the container. This is the
 * configuration class for passing <code>LocalContainer.addExtraClasspath()</code> information.
 * 
 */
public class Dependency extends AbstractDependency
{
    /**
     * Represents the "extra" classpath type.
     */
    public static final String EXTRA_CLASSPATH = "extra";

    /**
     * Represents the "shared" classpath type.
     */
    public static final String SHARED_CLASSPATH = "shared";

    /**
     * The Dependency target classpath.
     */
    private String classpath = EXTRA_CLASSPATH;

    /**
     * @return The Dependency target classpath.
     */
    public String getClasspath()
    {
        return classpath;
    }

    /**
     * @param classpath the Dependency target classpath.
     */
    public void setClasspath(String classpath) 
    {
        this.classpath = classpath;
    }

    /**
     * Evaluates if the Dependency is targeted for a classpath type.
     * @param classpath a classpath type to compare with
     * @return <code>true</code> if this Dependency is targeted for the classpath type.
     */
    public boolean isOnClasspath(String classpath)
    {
        return this.classpath.equals(classpath);
    }

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
