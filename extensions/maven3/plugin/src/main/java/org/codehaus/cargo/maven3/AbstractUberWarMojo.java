/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2024 Ali Tokmen.
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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * Common mojo for Uberjar.
 */
public abstract class AbstractUberWarMojo extends AbstractMojo
{
    /**
     * The Maven project.
     */
    @Parameter(property = "project", readonly = true, required = true)
    private MavenProject project;

    /**
     * @throws MojoExecutionException on error
     */
    @Override
    public abstract void execute() throws MojoExecutionException;

    /**
     * Gets the configured project.
     * 
     * @return the maven project
     */
    public MavenProject getProject()
    {
        return this.project;
    }

    /**
     * Sets the configured project.
     * 
     * @param project the project to use
     */
    public void setProject(MavenProject project)
    {
        this.project = project;
    }

}
