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
package org.codehaus.cargo.maven3.configuration;

import java.io.File;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.shared.transfer.artifact.DefaultArtifactCoordinate;
import org.apache.maven.shared.transfer.artifact.resolve.ArtifactResolver;
import org.apache.maven.shared.transfer.artifact.resolve.ArtifactResolverException;

/**
 * An ArtifactInstaller encapsulates the resolving of an artifact from a local
 * or remote Maven repository.
 */
public class ArtifactInstaller
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
     * Version.
     */
    private String version;

    /**
     * Artifact type.
     */
    private String type = "zip";

    /**
     * Classifier.
     */
    private String classifier;

    /**
     * Destination directory where the zipped container install will be extracted.
     */
    private String extractDir;

    /**
     * @return Group id.
     */
    public String getGroupId()
    {
        return groupId;
    }

    /**
     * @param groupId Group id.
     */
    public void setGroupId(String groupId)
    {
        this.groupId = groupId;
    }

    /**
     * @return Artifact id.
     */
    public String getArtifactId()
    {
        return artifactId;
    }

    /**
     * @param artifactId Artifact id.
     */
    public void setArtifactId(String artifactId)
    {
        this.artifactId = artifactId;
    }

    /**
     * @return Version.
     */
    public String getVersion()
    {
        return version;
    }

    /**
     * @param version Version.
     */
    public void setVersion(String version)
    {
        this.version = version;
    }

    /**
     * @return Artifact type.
     */
    public String getType()
    {
        return type;
    }

    /**
     * @param type Artifact type.
     */
    public void setType(String type)
    {
        this.type = type;
    }

    /**
     * @return Classifier.
     */
    public String getClassifier()
    {
        return classifier;
    }

    /**
     * @param classifier Classifier.
     */
    public void setClassifier(String classifier)
    {
        this.classifier = classifier;
    }

    /**
     * @return the destination directory where the zipped container install will be installed.
     */
    public String getExtractDir()
    {
        return this.extractDir;
    }

    /**
     * @param extractDir the destination directory where the zipped container install will be
     * installed.
     */
    public void setExtractDir(String extractDir)
    {
        this.extractDir = extractDir;
    }

    /**
     * Resolves the dependency and return the artifact file.
     * @param artifactResolver The artifact resolver is used to dynamically resolve
     * {@link Artifact} objects. It will automatically download whatever needed.
     * @param projectBuildingRequest Maven project building request.
     * @return Resolved dependency.
     * @throws ArtifactResolverException If artifact resolution fails or artifact not found.
     */
    public File resolve(ArtifactResolver artifactResolver,
        ProjectBuildingRequest projectBuildingRequest) throws ArtifactResolverException
    {
        DefaultArtifactCoordinate coordinate = new DefaultArtifactCoordinate();
        coordinate.setGroupId(groupId);
        coordinate.setArtifactId(artifactId);
        coordinate.setVersion(version);
        coordinate.setExtension(type);
        coordinate.setClassifier(classifier);

        Artifact artifact =
            artifactResolver.resolveArtifact(projectBuildingRequest, coordinate).getArtifact();
        return artifact.getFile();
    }
}
