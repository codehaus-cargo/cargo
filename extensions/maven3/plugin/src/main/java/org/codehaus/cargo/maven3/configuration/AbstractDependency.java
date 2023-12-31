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
package org.codehaus.cargo.maven3.configuration;

import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

import org.codehaus.cargo.container.deployable.DeployableType;

/**
 * Common field and method to {@link Dependency} and {@link Deployable}.
 */
public abstract class AbstractDependency
{
    /**
     * Group id.
     */
    private String groupId;

    /**
     * Artifact id.
     */
    private String artifactId;

    /**
     * Type.
     */
    private String type;

    /**
     * Location.
     */
    private String location;

    /**
     * Classifier.
     */
    private String classifier;

    /**
     * @param type Type.
     */
    public void setType(String type)
    {
        this.type = type;
    }

    /**
     * @return Type.
     */
    public String getType()
    {
        return this.type;
    }

    /**
     * @param location Location.
     */
    public void setLocation(String location)
    {
        this.location = location;
    }

    /**
     * @return Location.
     */
    public String getLocation()
    {
        return this.location;
    }

    /**
     * @param groupId Group id.
     */
    public void setGroupId(String groupId)
    {
        this.groupId = groupId;
    }

    /**
     * @return Group id.
     */
    public String getGroupId()
    {
        return this.groupId;
    }

    /**
     * @param artifactId Artifact id.
     */
    public void setArtifactId(String artifactId)
    {
        this.artifactId = artifactId;
    }

    /**
     * @return Artifact id.
     */
    public String getArtifactId()
    {
        return this.artifactId;
    }

    /**
     * @param classifier Classifier.
     */
    public void setClassifier(String classifier)
    {
        this.classifier = classifier;
    }

    /**
     * @return Classifier.
     */
    public String getClassifier()
    {
        return this.classifier;
    }

    /**
     * Find artifact location for the artifact of this dependency.
     * @param artifacts All artifacts' list.
     * @param log Logger.
     * @return Artifact location.
     * @throws MojoExecutionException If resolve fails.
     */
    protected String findArtifactLocation(Set<Artifact> artifacts, Log log)
        throws MojoExecutionException
    {
        Artifact resolvedArtifact = null;

        log.debug("Searching for an artifact that matches [" + getGroupId() + ":"
            + getArtifactId() + ":" + getType() + ":" + getClassifier() + "]...");

        for (Artifact artifact : artifacts)
        {
            log.debug("Checking artifact [" + artifact.getGroupId() + ":"
                + artifact.getArtifactId() + ":" + artifact.getType() + ":"
                + artifact.getClassifier() + "]...");

            // TODO: Find a better to handle match between m2 types and cargo types...
            if (artifact.getGroupId().equals(getGroupId())
                && artifact.getArtifactId().equals(getArtifactId()))
            {
                if (artifact.getClassifier() == getClassifier()
                    || artifact.getClassifier() != null
                    && artifact.getClassifier().equals(getClassifier()))
                {
                    String artifactType = artifact.getType();
                    if (artifactType == null || DeployableType.EJB.getType().equals(artifactType))
                    {
                        artifactType = "jar";
                    }
                    String type = getType();
                    if (type == null || DeployableType.BUNDLE.getType().equals(type)
                        || DeployableType.EJB.getType().equals(type))
                    {
                        type = "jar";
                    }
                    if (type.equals(artifactType))
                    {
                        resolvedArtifact = artifact;
                        break;
                    }
                }
            }
        }

        if (resolvedArtifact == null)
        {
            throw new MojoExecutionException("Artifact [" + getGroupId() + ":" + getArtifactId()
                + ":" + getType() + "] is not a dependency of the project.");
        }

        if (resolvedArtifact.getFile() == null)
        {
            throw new MojoExecutionException("The file for artifact [" + resolvedArtifact
                + " is null (probably does not exist).");
        }

        return resolvedArtifact.getFile().getPath();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return "AbstractDependency"
            + "{ groupId=" + groupId
            + ", artifactId=" + artifactId
            + ", type=" + type
            + ", classifier=" + classifier
            + " }";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object other)
    {
        if (other == null)
        {
            return false;
        }
        if (!(other instanceof AbstractDependency))
        {
            return false;
        }

        return this.toString().equals(((AbstractDependency) other).toString());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode()
    {
        return this.toString().hashCode();
    }

}
