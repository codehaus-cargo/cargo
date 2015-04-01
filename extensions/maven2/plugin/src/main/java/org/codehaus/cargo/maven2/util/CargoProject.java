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
package org.codehaus.cargo.maven2.util;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

/**
 * Holder class to transport all required information to the configuration classes.
 * 
 */
public class CargoProject
{
    /**
     * Logger.
     */
    private Log log;

    /**
     * Packaging.
     */
    private String packaging;

    /**
     * Group id.
     */
    private String groupId;

    /**
     * Artifact id.
     */
    private String artifactId;

    /**
     * Build directory.
     */
    private String buildDirectory;

    /**
     * Final name.
     */
    private String finalName;

    /**
     * Skip installation of containers.
     */
    private boolean daemonRun = false;


    /**
     * Project artifacts.
     */
    private Set<Artifact> artifacts;

    /**
     * {@link ClassLoader} that's embedded with dependencies.
     */
    private ClassLoader embeddedClassLoader;

    /**
     * Saves all attributes.
     * @param packaging Packaging.
     * @param groupId Group id.
     * @param artifactId Artifact id.
     * @param buildDirectory Build directory.
     * @param finalName Final name.
     * @param artifacts Project artifacts.
     * @param log Logger.
     */
    public CargoProject(String packaging, String groupId, String artifactId, String buildDirectory,
        String finalName, Set<Artifact> artifacts, Log log)
    {
        this(
            packaging,
            groupId,
            artifactId,
            buildDirectory,
            finalName,
            null,
            Collections.<Artifact>emptyList(),
            artifacts,
            log);
    }

    /**
     * Saves all attributes.
     * @param project Maven2 project.
     * @param log Logger.
     */
    public CargoProject(MavenProject project, Log log)
    {
        this(
            project.getPackaging(),
            project.getGroupId(),
            project.getArtifactId(),
            project.getBuild().getDirectory(),
            project.getBuild().getFinalName(),
            project.getArtifact(),
            project.getAttachedArtifacts(), 
            project.getArtifacts(),
            log);
    }

    /**
     * Saves all attributes.
     * @param packaging Packaging.
     * @param groupId Group id.
     * @param artifactId Artifact id.
     * @param buildDirectory Build directory.
     * @param finalName Final name.
     * @param artifact Artifact.
     * @param attachedArtifacts Attach artifacts.
     * @param artifacts Project artifacts.
     * @param log Logger.
     */
    private CargoProject(String packaging, String groupId, String artifactId,
        String buildDirectory, String finalName, Artifact artifact,
        List<Artifact> attachedArtifacts, Set<Artifact> artifacts, Log log)
    {
        this.log = log;
        this.packaging = packaging;
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.buildDirectory = buildDirectory;
        this.finalName = finalName;
        this.artifacts =
            new LinkedHashSet<Artifact>(1 + attachedArtifacts.size() + artifacts.size());
        if (artifact != null)
        {
            this.artifacts.add(artifact);
        }
        this.artifacts.addAll(attachedArtifacts);
        this.artifacts.addAll(artifacts);
    }

    /**
     * @return Packaging.
     */
    public String getPackaging()
    {
        return this.packaging;
    }

    /**
     * @return Group id.
     */
    public String getGroupId()
    {
        return this.groupId;
    }

    /**
     * @return Artifact id.
     */
    public String getArtifactId()
    {
        return this.artifactId;
    }

    /**
     * @return Build directory.
     */
    public String getBuildDirectory()
    {
        return this.buildDirectory;
    }

    /**
     * @return Final name.
     */
    public String getFinalName()
    {
        return this.finalName;
    }

    /**
     * @return Project artifacts.
     */
    public Set<Artifact> getArtifacts()
    {
        return this.artifacts;
    }

    /**
     * @return Logger.
     */
    public Log getLog()
    {
        return this.log;
    }

    /**
     * @return if project is part of a daemon run.
     */
    public boolean isDaemonRun()
    {
        return this.daemonRun;
    }

    /**
     * @param enable If project is part of a daemon run.
     */
    public void setDaemonRun(boolean enable)
    {
        this.daemonRun = enable;
    }


    /**
     * @return <code>true</code> if the project has a Java EE packaging.
     */
    public boolean isJ2EEPackaging()
    {
        boolean result = false;

        if (getPackaging().equalsIgnoreCase("war"))
        {
            result = true;
        }
        else if (getPackaging().equalsIgnoreCase("ear"))
        {
            result = true;
        }
        else if (getPackaging().equalsIgnoreCase("ejb"))
        {
            result = true;
        }
        else if (getPackaging().equalsIgnoreCase("uberwar"))
        {
            result = true;
        }
        else if (getPackaging().equalsIgnoreCase("rar"))
        {
            result = true;
        }
        else if (getPackaging().equalsIgnoreCase("bundle"))
        {
            result = true;
        }

        return result;
    }

    /**
     * @param classLoader {@link ClassLoader} that's embedded with dependencies.
     */
    public void setEmbeddedClassLoader(ClassLoader classLoader)
    {
        this.embeddedClassLoader = classLoader;
    }

    /**
     * @return {@link ClassLoader} that's embedded with dependencies.
     */
    public ClassLoader getEmbeddedClassLoader()
    {
        return this.embeddedClassLoader;
    }
}
