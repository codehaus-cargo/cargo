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
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolver;

/**
 * An ArtifactInstaller encapsulates the resolving of an artifact from a local
 * or remote Maven repository.
 *
 * @version $Id$
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
     * Resolves the dependency and return the artifact file.
     * @param artifactFactory The artifact factory is used to create valid Maven {@link Artifact}
     * objects.
     * @param artifactResolver The artifact resolver is used to dynamically resolve
     * {@link Artifact} objects. It will automatically download whatever needed.
     * @param localRepository The local Maven repository. This is used by the artifact resolver to
     * download resolved artifacts and put them in the local repository so that they won't have to
     * be fetched again next time the plugin is executed.
     * @param repositories The remote Maven repositories used by the artifact resolver to look for
     * artifacts.
     * @return Resolved dependency.
     * @throws ArtifactResolutionException If artifact resolution fails.
     * @throws ArtifactNotFoundException If artifact not found.
     */
    public File resolve(ArtifactFactory artifactFactory, ArtifactResolver artifactResolver,
        ArtifactRepository localRepository, List<ArtifactRepository> repositories)
        throws ArtifactResolutionException, ArtifactNotFoundException
    {
        Artifact artifact = artifactFactory.createArtifactWithClassifier(groupId, artifactId,
            version, type, classifier);
        artifactResolver.resolve(artifact, repositories, localRepository);
        return artifact.getFile();
    }
}
