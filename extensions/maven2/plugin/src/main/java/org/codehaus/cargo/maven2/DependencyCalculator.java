/* 
 * ========================================================================
 * 
 * Copyright 2007 Vincent Massol.
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
package org.codehaus.cargo.maven2;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.installer.ArtifactInstallationException;
import org.apache.maven.artifact.installer.ArtifactInstaller;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.apache.maven.profiles.DefaultProfileManager;
import org.apache.maven.profiles.ProfileManager;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.artifact.InvalidDependencyVersionException;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

/**
 * This class is effectively an unmitigated hack. Any offers to do it 'properly'
 * are gratefully received from someone who can get the guts of Maven to do what
 * want.
 * 
 * Basically, given our 'uberwar' project, instead of simply merging WEB-INF/lib
 * files, we wish to treat those war files as ordinary dependencies, and to
 * calculate the 'effective' list of jar files that SHOULD be in WEB-INF/lib
 * that way. I.E, if we are including A.WAR and B.WAR, both of which use
 * different versions of X.JAR, then we should calculate what the 'right'
 * version of the X.JAR that we ought to be using.
 * 
 * This seems very hard to do given the tools provided by maven. There are
 * alternate solutions, such as WAR files producing their code in a JAR as well,
 * but this relies on including BOTH the WAR and the JAR, which is not pretty.
 * 
 * This class does it a hacky way. For each of the war files in the dependency
 * tree (including the caller), it generates an alternate pom file, which
 * 'pretends' that the WAR file is a POM, and replaces any dependent WARS with
 * equivalent dependencies of type POM with a different classifier. It then
 * invokes maven to resolve that project, which appears to resolve the versions
 * (as would have been done in an all-jar universe).
 * 
 * A better way would probably be to be able to customise the dependency
 * calculation system, but this seems very bound up in all the project/artifact
 * gubbins.
 * 
 */
public class DependencyCalculator {

	/** @component */
	private ArtifactFactory artifactFactory;

	/** @component */
	private ArtifactResolver resolver;

	/** @parameter expression="${localRepository}" */
	private ArtifactRepository localRepository;

	/** @parameter expression="${project.remoteArtifactRepositories}" */
	private List remoteRepositories;

	/** @parameter expression="${project}" */
	private MavenProject mavenProject;

	/** @component */
	private MavenProjectBuilder mavenProjectBuilder;

	/** @component */
	private ArtifactInstaller installer;

	private PlexusContainer container;

	public DependencyCalculator(ArtifactFactory artifactFactory,
			ArtifactResolver resolver, ArtifactRepository localRepository,
			List remoteRepositories, MavenProject mavenProject,
			MavenProjectBuilder mavenProjectBuilder,
			ArtifactInstaller installer, PlexusContainer container) {
		this.artifactFactory = artifactFactory;
		this.resolver = resolver;
		this.localRepository = localRepository;
		this.remoteRepositories = remoteRepositories;
		this.mavenProject = mavenProject;
		this.mavenProjectBuilder = mavenProjectBuilder;
		this.installer = installer;
		this.container = container;

	}

	public Set execute() throws ArtifactResolutionException,
			ArtifactNotFoundException, ProjectBuildingException,
			FileNotFoundException, IOException, XmlPullParserException,
			InvalidDependencyVersionException, ArtifactInstallationException {
		ProfileManager profileManager = new DefaultProfileManager(container);

		fixupProjectArtifact();

		// Calculate the new deps
		Artifact art = mavenProject.getArtifact();
		Artifact art2 = artifactFactory.createArtifactWithClassifier(art
				.getGroupId() + ".cargodeps", art.getArtifactId(), art.getVersion(), "pom",
				null);
		resolver.resolve(art2, remoteRepositories, localRepository);

		MavenProject mavenProject = mavenProjectBuilder.buildWithDependencies(
				art2.getFile(), localRepository, profileManager);

		Set filesToAdd = new HashSet();

		for (Iterator i = mavenProject.getArtifacts().iterator(); i.hasNext();) {
			Artifact artdep = (Artifact) i.next();
			if (artdep.getType().equals("jar")) {
				resolver.resolve(artdep, remoteRepositories, localRepository);
				filesToAdd.add(artdep.getFile());

			}
		}

		return filesToAdd;
	}

	void fixupProjectArtifact() throws FileNotFoundException, IOException,
			XmlPullParserException, ArtifactResolutionException,
			ArtifactNotFoundException, InvalidDependencyVersionException,
			ProjectBuildingException, ArtifactInstallationException {
		MavenProject mp2 = new MavenProject(mavenProject);
		// For each of our dependencies..
		for (Iterator i = mp2.createArtifacts(artifactFactory, null, null)
				.iterator(); i.hasNext();) {
			Artifact art = (Artifact) i.next();
			if (art.getType().equals("war")) {
				// Sigh...
				Artifact art2 = artifactFactory.createArtifactWithClassifier(
						art.getGroupId(), art.getArtifactId(),
						art.getVersion(), "pom", null);
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

	void fixupRepositoryArtifact(Artifact artifact)
			throws InvalidDependencyVersionException, FileNotFoundException,
			IOException, XmlPullParserException, ProjectBuildingException,
			ArtifactResolutionException, ArtifactNotFoundException,
			ArtifactInstallationException {

		// Resolve it
		resolver.resolve(artifact, remoteRepositories, localRepository);
		File artifactFile = artifact.getFile();

		// Also, create a project for it
		MavenProject mavenProject = mavenProjectBuilder.buildFromRepository(
				artifact, remoteRepositories, localRepository);
		for (Iterator i = mavenProject.createArtifacts(artifactFactory, null,
				null).iterator(); i.hasNext();) {
			Artifact art = (Artifact) i.next();

			if (art.getType().equals("war")) {
				// Sigh...
				Artifact art2 = artifactFactory.createArtifactWithClassifier(
						art.getGroupId(), art.getArtifactId(),
						art.getVersion(), "pom", null);
				fixupRepositoryArtifact(art2);
			}
		}

		MavenXpp3Reader pomReader = new MavenXpp3Reader();
		Model pomFile = pomReader.read(new FileReader(artifactFile));

		fixModelAndSaveInRepository(artifact, pomFile);

	}

	void fixModelAndSaveInRepository(Artifact artifact, Model pomFile)
			throws IOException, ArtifactInstallationException {
		for (Iterator i = pomFile.getDependencies().iterator(); i.hasNext();) {
			Dependency art = (Dependency) i.next();
			if (art.getType().equals("war")) {
				art.setGroupId( art.getGroupId() + ".cargodeps");
				art.setType("pom");
			}
		}

		pomFile.setPackaging("pom");
		
		String version = pomFile.getVersion();

		if (version == null)
			version = pomFile.getParent().getVersion();

		File outFile = File.createTempFile("pom", ".xml");
		MavenXpp3Writer pomWriter = new MavenXpp3Writer();

		pomWriter.write(new FileWriter(outFile), pomFile);

		Artifact art2 = artifactFactory.createArtifactWithClassifier(artifact
				.getGroupId() + ".cargodeps", artifact.getArtifactId(), artifact.getVersion(),
				"pom", null);

		installer.install(outFile, art2, localRepository);
		outFile.delete();
	}

}
