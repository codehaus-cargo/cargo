/*
 * ========================================================================
 *
 * Copyright 2006 Vincent Massol.
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
package org.codehaus.cargo.maven2.jetty;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.cargo.container.internal.util.JdkUtils;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * Dynamically load Jetty dependencies.
 *
 * @version $Id$
 */
public class JettyArtifactResolver
{
    private ArtifactResolver artifactResolver;

    private ArtifactRepository localRepository;

    private List repositories;

    private ArtifactFactory artifactFactory;

    private Map jettyDependencies = new HashMap();

    private JdkUtils jdkUtils = new JdkUtils();

    private class Dependency
    {
        public String groupId;
        public String artifactId;
        public String version;

        public Dependency(String groupId, String artifactId, String version)
        {
            this.groupId = groupId;
            this.artifactId = artifactId;
            this.version = version;
        }
    }

    public JettyArtifactResolver(ArtifactResolver artifactResolver,
        ArtifactRepository localRepository, List repositories, ArtifactFactory artifactFactory)
    {
        this.artifactResolver = artifactResolver;
        this.localRepository = localRepository;
        this.repositories = repositories;
        this.artifactFactory = artifactFactory;

        List jetty4xDependencies = new ArrayList();
        jetty4xDependencies.add(new Dependency("ant", "ant", "1.6.4"));
        jetty4xDependencies.add(new Dependency("jetty", "org.mortbay.jetty", "4.2.27"));
        jetty4xDependencies.add(new Dependency("javax.servlet", "servlet-api", "2.4"));
        jetty4xDependencies.add(new Dependency("javax.servlet", "jsp-api", "2.0"));
        jetty4xDependencies.add(new Dependency("tomcat", "jasper-compiler", "4.1.30"));
        jetty4xDependencies.add(new Dependency("tomcat", "jasper-runtime", "4.1.30"));

        List jetty5xDependencies = new ArrayList();
        jetty5xDependencies.add(new Dependency("jetty", "org.mortbay.jetty", "5.1.14"));
        jetty5xDependencies.add(new Dependency("javax.servlet", "servlet-api", "2.4"));
        jetty5xDependencies.add(new Dependency("javax.servlet", "jsp-api", "2.0"));
        jetty5xDependencies.add(new Dependency("ant", "ant", "1.6.4"));
        jetty5xDependencies.add(new Dependency("xerces", "xercesImpl","2.6.2"));
        jetty5xDependencies.add(new Dependency("xerces", "xmlParserAPIs","2.6.2"));
        jetty5xDependencies.add(new Dependency("tomcat", "jasper-compiler", "5.5.12"));
        jetty5xDependencies.add(new Dependency("tomcat", "jasper-runtime", "5.5.12"));
        jetty5xDependencies.add(new Dependency("commons-el", "commons-el", "1.0"));
        jetty5xDependencies.add(new Dependency("commons-logging", "commons-logging", "1.0.4"));

        List jetty6xDependencies = new ArrayList();
        jetty6xDependencies.add(new Dependency("org.mortbay.jetty", "jsp-api-2.0", "6.1.1rc1"));
        jetty6xDependencies.add(new Dependency("org.mortbay.jetty", "servlet-api-2.5", "6.1.1rc1"));
        jetty6xDependencies.add(new Dependency("org.mortbay.jetty", "jetty", "6.1.1rc1"));
        jetty6xDependencies.add(new Dependency("org.mortbay.jetty", "jetty-util", "6.1.1rc1"));
        jetty6xDependencies.add(new Dependency("org.mortbay.jetty", "jetty-naming", "6.1.1rc1"));
        jetty6xDependencies.add(new Dependency("org.mortbay.jetty", "jetty-plus", "6.1.1rc1"));
        jetty6xDependencies.add(new Dependency("ant", "ant", "1.6.5"));
        jetty6xDependencies.add(new Dependency("commons-el", "commons-el", "1.0"));
        jetty6xDependencies.add(new Dependency("tomcat", "jasper-compiler", "5.5.15"));
        jetty6xDependencies.add(new Dependency("tomcat", "jasper-runtime", "5.5.15"));
        jetty6xDependencies.add(new Dependency("tomcat", "jasper-compiler-jdt","5.5.15"));
        jetty6xDependencies.add(new Dependency("javax.mail", "mail", "1.4"));
        jetty6xDependencies.add(new Dependency("javax.activation", "activation", "1.1"));
        jetty6xDependencies.add(new Dependency("geronimo-spec", "geronimo-spec-jta", "1.0.1B-rc4"));
        jetty6xDependencies.add(new Dependency("xerces", "xercesImpl","2.6.2"));
        jetty6xDependencies.add(new Dependency("xerces", "xmlParserAPIs","2.6.2"));
        jetty6xDependencies.add(new Dependency("commons-logging", "commons-logging","1.0.4"));
        jetty6xDependencies.add(new Dependency("log4j", "log4j", "1.2.14"));

        this.jettyDependencies.put("jetty4x", jetty4xDependencies);
        this.jettyDependencies.put("jetty5x", jetty5xDependencies);
        this.jettyDependencies.put("jetty6x", jetty6xDependencies);
    }

    public ClassLoader resolveDependencies(String jettyContainerId, ClassLoader parent)
        throws MojoExecutionException
    {
        URLClassLoader classloader;

        try
        {
            List dependencies = (List) this.jettyDependencies.get(jettyContainerId);

            URL[] urls = new URL[dependencies.size() + 1];
            int i = 0;
            Iterator it = dependencies.iterator();
            while (it.hasNext())
            {
                Dependency dependency = (Dependency) it.next();
                Artifact artifact = this.artifactFactory.createArtifact(dependency.groupId,
                    dependency.artifactId, dependency.version, "compile", "jar");
                this.artifactResolver.resolve(artifact, this.repositories, this.localRepository );
                urls[i++] = artifact.getFile().toURL();
            }

            // On OSX, the tools.jar classes are included in the classes.jar so there is no need to
            // include any tools.jar file to the cp.
            if (!this.jdkUtils.isOSX())
            {
                urls[i++] = this.jdkUtils.getToolsJar().toURL();
            }

            if (parent == null)
            {
                classloader = new URLClassLoader(urls);
            }
            else
            {
                classloader = new URLClassLoader(urls, parent);
            }
        }
        catch (Exception e)
        {
            throw new MojoExecutionException("Failed to resolve dependency", e);
        }

        return classloader;
    }
}
