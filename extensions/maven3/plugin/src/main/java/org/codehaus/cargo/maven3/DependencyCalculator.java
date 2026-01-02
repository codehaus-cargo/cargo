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
package org.codehaus.cargo.maven3;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.project.ProjectBuildingResult;
import org.apache.maven.shared.transfer.artifact.DefaultArtifactCoordinate;
import org.apache.maven.shared.transfer.artifact.install.ArtifactInstaller;
import org.apache.maven.shared.transfer.artifact.resolve.ArtifactResolver;
import org.apache.maven.shared.transfer.dependencies.resolve.DependencyResolver;

/**
 * This class is effectively an unmitigated hack. Any offers to do it 'properly' are gratefully
 * received from someone who can get the guts of Maven to do what want.<br><br>
 * Basically, given our 'uberwar' project, instead of simply merging WEB-INF/lib files, we wish to
 * treat those war files as ordinary dependencies, and to calculate the 'effective' list of jar
 * files that SHOULD be in WEB-INF/lib that way. I.E, if we are including A.WAR and B.WAR, both of
 * which use different versions of X.JAR, then we should calculate what the 'right' version of the
 * X.JAR that we ought to be using.<br><br>
 * This seems very hard to do given the tools provided by maven. There are alternate solutions, such
 * as WAR files producing their code in a JAR as well, but this relies on including BOTH the WAR and
 * the JAR, which is not pretty.<br><br>
 * This class does it a hacky way. For each of the war files in the dependency tree (including the
 * caller), it generates an alternate pom file, which 'pretends' that the WAR file is a POM, and
 * replaces any dependent WARS with equivalent dependencies of type POM with a different classifier.
 * It then invokes maven to resolve that project, which appears to resolve the versions (as would
 * have been done in an all-jar universe).<br><br>
 * A better way would probably be to be able to customise the dependency calculation system, but
 * this seems very bound up in all the project/artifact gubbins.
 */
public class DependencyCalculator
{

    /**
     * Maven artifact resolver, used to dynamically resolve JARs for the containers and also to
     * resolve the JARs for the embedded container's classpaths.
     */
    @Component
    private ArtifactResolver artifactResolver;

    /**
     * Maven dependency resolver, used to dynamically resolve dependencies of artifacts.
     */
    @Component
    private DependencyResolver dependencyResolver;

    /**
     * Maven project building request.
     */
    @Parameter(
        defaultValue = "${session.projectBuildingRequest}", readonly = true, required = true)
    private ProjectBuildingRequest projectBuildingRequest;

    /**
     * The Maven project.
     */
    @Parameter(property = "project", readonly = true, required = true)
    private MavenProject mavenProject;

    /**
     * Maven project builder, used to calculate dependencies.
     */
    @Component
    private ProjectBuilder mavenProjectBuilder;

    /**
     * Maven artifact installer, used to calculate dependencies.
     */
    @Component
    private ArtifactInstaller installer;

    /**
     * Saves all attributes.
     * @param dependencyResolver Dependency resolver.
     * @param artifactResolver Artifact resolver.
     * @param projectBuildingRequest Maven 3 project building request.
     * @param mavenProject Maven 3 project.
     * @param mavenProjectBuilder Maven 3 project builder.
     * @param installer Artifact installer.
     */
    public DependencyCalculator(DependencyResolver dependencyResolver,
        ArtifactResolver artifactResolver, ProjectBuildingRequest projectBuildingRequest,
        MavenProject mavenProject, ProjectBuilder mavenProjectBuilder, ArtifactInstaller installer)
    {
        this.dependencyResolver = dependencyResolver;
        this.artifactResolver = artifactResolver;
        this.projectBuildingRequest = projectBuildingRequest;
        this.mavenProject = mavenProject;
        this.mavenProjectBuilder = mavenProjectBuilder;
        this.installer = installer;
    }

    /**
     * Execute the dependency calculator.
     * @return List of dependency files.
     * @throws Exception If anything goes wrong.
     */
    public Set<File> execute() throws Exception
    {
        fixupProjectArtifact();

        // Calculate the new deps
        Artifact art = mavenProject.getArtifact();

        DefaultArtifactCoordinate containerArtifactCoordinate = new DefaultArtifactCoordinate();
        containerArtifactCoordinate.setGroupId(art.getGroupId() + ".cargodeps");
        containerArtifactCoordinate.setArtifactId(art.getArtifactId());
        containerArtifactCoordinate.setVersion(art.getVersion());
        containerArtifactCoordinate.setExtension("pom");

        Artifact art2 = artifactResolver.resolveArtifact(
            projectBuildingRequest, containerArtifactCoordinate).getArtifact();

        ProjectBuildingResult projectBuildingResult =
            mavenProjectBuilder.build(art2, projectBuildingRequest);

        Set<File> filesToAdd = new HashSet<File>();

        for (Artifact artifact : projectBuildingResult.getProject().getArtifacts())
        {
            if (artifact.getType().equals("jar"))
            {
                filesToAdd.add(artifact.getFile());
            }
        }

        return filesToAdd;
    }

    /**
     * Fixup the project artifact.
     * @throws Exception If anything goes wrong.
     */
    protected void fixupProjectArtifact() throws Exception
    {
        MavenProject mp2 = mavenProject.clone();

        // For each of our dependencies..
        for (Artifact artifact : mp2.getArtifacts())
        {
            if (artifact.getType().equals("war"))
            {
                DefaultArtifactCoordinate containerArtifactCoordinate =
                    new DefaultArtifactCoordinate();
                containerArtifactCoordinate.setGroupId(artifact.getGroupId());
                containerArtifactCoordinate.setArtifactId(artifact.getArtifactId());
                containerArtifactCoordinate.setVersion(artifact.getVersion());
                containerArtifactCoordinate.setExtension("pom");

                Artifact art2 = artifactResolver.resolveArtifact(
                    projectBuildingRequest, containerArtifactCoordinate).getArtifact();
                fixupRepositoryArtifact(art2);
            }
        }

        // If we mess with this model, it's the 'REAL' model. So lets copy it
        Model pomFile = mp2.getModel();

        File outFile = File.createTempFile("pom", ".xml");
        MavenXpp3Writer pomWriter = new MavenXpp3Writer();

        pomWriter.write(new FileWriter(outFile), pomFile);

        MavenXpp3Reader pomReader = new MavenXpp3Reader();
        pomFile = pomReader.read(new FileReader(outFile));

        Artifact art = mp2.getArtifact();
        fixModelAndSaveInRepository(art, pomFile);
        outFile.delete();
    }

    /**
     * Fixup an artifact.
     * @param artifact Artifact to fixup.
     * @throws Exception If anything goes wrong.
     */
    protected void fixupRepositoryArtifact(Artifact artifact) throws Exception
    {
        File artifactFile = artifact.getFile();

        ProjectBuildingResult projectBuildingResult =
            mavenProjectBuilder.build(artifact, projectBuildingRequest);

        for (Artifact createdArtifact : projectBuildingResult.getProject().getArtifacts())
        {
            if (createdArtifact.getType().equals("war"))
            {
                DefaultArtifactCoordinate containerArtifactCoordinate =
                    new DefaultArtifactCoordinate();
                containerArtifactCoordinate.setGroupId(createdArtifact.getGroupId());
                containerArtifactCoordinate.setArtifactId(createdArtifact.getArtifactId());
                containerArtifactCoordinate.setVersion(createdArtifact.getVersion());
                containerArtifactCoordinate.setExtension("pom");

                Artifact art2 = artifactResolver.resolveArtifact(
                    projectBuildingRequest, containerArtifactCoordinate).getArtifact();
                fixupRepositoryArtifact(art2);
            }
        }

        MavenXpp3Reader pomReader = new MavenXpp3Reader();
        Model pomFile = pomReader.read(new FileReader(artifactFile));

        fixModelAndSaveInRepository(artifact, pomFile);
    }

    /**
     * Fix model and save in repository.
     * @param artifact Artifact.
     * @param pomFile Maven 3 model file.
     * @throws Exception If anything goes wrong.
     */
    protected void fixModelAndSaveInRepository(Artifact artifact, Model pomFile) throws Exception
    {
        for (Object dependency : pomFile.getDependencies())
        {
            Dependency art = (Dependency) dependency;
            if (art.getType().equals("war"))
            {
                art.setGroupId(art.getGroupId() + ".cargodeps");
                art.setType("pom");
            }
        }

        pomFile.setPackaging("pom");

        String version = pomFile.getVersion();

        if (version == null)
        {
            version = pomFile.getParent().getVersion();
        }

        File outFile = File.createTempFile("pom", ".xml");
        MavenXpp3Writer pomWriter = new MavenXpp3Writer();

        pomWriter.write(new FileWriter(outFile), pomFile);

        DefaultArtifactCoordinate containerArtifactCoordinate = new DefaultArtifactCoordinate();
        containerArtifactCoordinate.setGroupId(artifact.getGroupId() + ".cargodeps");
        containerArtifactCoordinate.setArtifactId(artifact.getArtifactId());
        containerArtifactCoordinate.setVersion(artifact.getVersion());
        containerArtifactCoordinate.setExtension("pom");

        Artifact art2 = artifactResolver.resolveArtifact(
            projectBuildingRequest, containerArtifactCoordinate).getArtifact();

        List<Artifact> artifactList = new ArrayList<Artifact>(1);
        artifactList.add(art2);

        installer.install(projectBuildingRequest, artifactList);
        outFile.delete();
    }

}
