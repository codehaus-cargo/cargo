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

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.artifact.Artifact;

import java.util.Set;
import java.util.Iterator;

/**
 * Common field and method to {@link Dependency} and {@link Deployable}.
 *
 * @version $Id$
 */
public abstract class AbstractDependency
{
    private String groupId;

    private String artifactId;

    private String type;

    private String location;
    
    private String classifier;

    public void setType(String type)
    {
        this.type = type;
    }

    public String getType()
    {
        return this.type;
    }

    public void setLocation(String location)
    {
        this.location = location;
    }

    public String getLocation()
    {
        return this.location;
    }

    public void setGroupId(String groupId)
    {
        this.groupId = groupId;
    }

    public String getGroupId()
    {
        return this.groupId;
    }

    public void setArtifactId(String artifactId)
    {
        this.artifactId = artifactId;
    }

    public String getArtifactId()
    {
        return this.artifactId;
    }

    public void setClassifier(String classifier)
    {
    	this.classifier = classifier;
    }
    
    public String getClassifier()
    {
    	return this.classifier;
    }
    
    protected String findArtifactLocation(Set artifacts, Log log) throws MojoExecutionException
    {
        Artifact resolvedArtifact = null;
        
        log.debug("Searching for an artifact that matches [" + getGroupId() + ":"
            + getArtifactId() + ":" + getType() +  ":" + getClassifier() + "]...");

        Iterator it = artifacts.iterator();
        while (it.hasNext())
        {
            Artifact artifact = (Artifact) it.next();

            log.debug("Checking artifact [" + artifact.getGroupId() + ":"
                + artifact.getArtifactId() + ":" + artifact.getType() + ":" + artifact.getClassifier() + "]...");

            // TODO: Find a better to handle match between m2 types and cargo types...
            if (artifact.getGroupId().equals(getGroupId())
                && artifact.getArtifactId().equals(getArtifactId())
                && (artifact.getClassifier() == getClassifier() 
                || (artifact.getClassifier() != null && artifact.getClassifier().equals(getClassifier())))
                && (artifact.getType() == getType()
                || (artifact.getType() != null && artifact.getType().equals(getType()))))
            {
                resolvedArtifact = artifact;
                break;
            }
        }

        if (resolvedArtifact == null)
        {
            throw new MojoExecutionException( "Artifact [" + getGroupId() + ":" + getArtifactId()
                + ":" + getType() + "] is not a dependency of the project.");
        }

        return resolvedArtifact.getFile().getPath();
    }

}
