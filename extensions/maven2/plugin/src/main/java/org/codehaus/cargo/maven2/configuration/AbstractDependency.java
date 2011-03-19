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

import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

/**
 * Common field and method to {@link Dependency} and {@link Deployable}.
 * 
 * @version $Id$
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
                    if (artifact.getType() == getType()
                        || artifact.getType() != null
                        && artifact.getType().equals(getType()))
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

        return resolvedArtifact.getFile().getPath();
    }

}
