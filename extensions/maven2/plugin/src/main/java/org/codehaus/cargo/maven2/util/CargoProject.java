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

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

/**
 * Holder class to transport all required information to the configuration classes.
 * 
 * @version $Id$
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
        this.log = log;
        this.packaging = packaging;
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.buildDirectory = buildDirectory;
        this.finalName = finalName;
        this.artifacts = artifacts;
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
            project.getArtifacts(),
            log);
    }

    /**
     * Saves all attributes and merge project module artifacts with the project artifacts.
     * 
     * @param project Maven2 project.
     * @param resolver Maven2 artifact resolver.
     * @param localRepository Maven2 local artifact repository.
     * @param log Logger.
     */
    public CargoProject(MavenProject project, ArtifactResolver resolver,
        ArtifactRepository localRepository, Log log)
    {
        this(
            project.getPackaging(),
            project.getGroupId(),
            project.getArtifactId(),
            project.getBuild().getDirectory(),
            project.getBuild().getFinalName(),
            aggregateArtifacts(new HashSet<Artifact>(project.getArtifacts()), project, resolver, 
                localRepository),
            log);
    }

    /**
     * Merge resolved Maven project module artifacts with provided set of artifacts.
     * 
     * @param artifacts provided set of artifacts.
     * @param project Maven2 project.
     * @param resolver Maven2 artifact resolver.
     * @param localRepository Maven2 local artifact repository.
     * @return merged set of artifacts.
     */
    protected static Set<Artifact> aggregateArtifacts(Set<Artifact> artifacts, 
        MavenProject project, ArtifactResolver resolver, ArtifactRepository localRepository)
    {
        if (project.getCollectedProjects() != null)
        {
            Iterator<?> modulesIterator = project.getCollectedProjects().iterator();
            while (modulesIterator.hasNext())
            {
                MavenProject module = (MavenProject) modulesIterator.next();
                if ("jar".equals(module.getPackaging()) || isJ2EEPackaging(module.getPackaging()))
                {
                    Artifact artifact = (Artifact) module.getArtifact();
                    File file = artifact.getFile();
                    if (file == null)
                    {
                        // first try to resolve a build target
                        file = new File(module.getBuild().getDirectory(), 
                            module.getBuild().getFinalName() + "." + module.getPackaging());
                    }
                    if (file != null && (!file.exists() || !file.isFile()))
                    {
                        // else try to resolve from local repository only
                        // (as a child module artifact you should have build it first)
                        file = null;
                        try
                        {
                            resolver.resolve(artifact, null, localRepository);
                            file = artifact.getFile();
                        }
                        catch (Exception e)
                        {
                            // ignore resolver failure:
                            // if the artifact actually is referenced an error about it
                            // will be given at a later time
                        }
                    }
                    if (file != null)
                    {
                        artifact.setFile(file);
                        artifact.setScope("provide");
                        artifacts.add(artifact);
                    }
                }
                aggregateArtifacts(artifacts, module, resolver, localRepository);
            }
        }
        return artifacts;
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
     * Evaluate if a project packaging is a Java EE packaging.
     * @param packaging a project packaging
     * @return <code>true</code> if a project packaging is a Java EE packaging.
     */
    protected static boolean isJ2EEPackaging(String packaging)
    {
        boolean result = false;

        if ("war".equalsIgnoreCase(packaging))
        {
            result = true;
        }
        else if ("ear".equalsIgnoreCase(packaging))
        {
            result = true;
        }
        else if ("ejb".equalsIgnoreCase(packaging))
        {
            result = true;
        }
        else if ("uberwar".equalsIgnoreCase(packaging))
        {
            result = true;
        }
        else if ("rar".equalsIgnoreCase(packaging))
        {
            result = true;
        }
        else if ("bundle".equalsIgnoreCase(packaging))
        {
            result = true;
        }

        return result;
    }

    /**
     * @return <code>true</code> if the project has a Java EE packaging.
     */
    public boolean isJ2EEPackaging()
    {
        return isJ2EEPackaging(getPackaging());
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
