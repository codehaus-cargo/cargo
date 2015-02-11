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

import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.cargo.container.internal.util.JdkUtils;

/**
 * Dynamically load embedded container dependencies.
 * 
 * @version $Id$
 */
public class EmbeddedContainerArtifactResolver
{
    /**
     * Artifact resolver.
     */
    private ArtifactResolver artifactResolver;

    /**
     * Local repository.
     */
    private ArtifactRepository localRepository;

    /**
     * List of repositories to look in.
     */
    private List<ArtifactRepository> repositories;

    /**
     * Artifact factory.
     */
    private ArtifactFactory artifactFactory;

    /**
     * Map of embedded container dependencies.
     */
    private Map<String, List<Dependency>> containerDependencies =
        new HashMap<String, List<Dependency>>();

    /**
     * JDK utilities.
     */
    private JdkUtils jdkUtils = new JdkUtils();

    /**
     * Class that represents a dependency.
     */
    private class Dependency
    {
        /**
         * Group id.
         */
        public String groupId;

        /**
         * Artifact id.
         */
        public String artifactId;

        /**
         * Version.
         */
        public String version;

        /**
         * Save all attributes.
         * @param groupId Group id.
         * @param artifactId Artifact id.
         * @param version Version.
         */
        public Dependency(String groupId, String artifactId, String version)
        {
            this.groupId = groupId;
            this.artifactId = artifactId;
            this.version = version;
        }
    }

    /**
     * Save all attributes.
     * @param artifactResolver Artifact resolver.
     * @param localRepository Local repository.
     * @param repositories List of repositories to look in.
     * @param artifactFactory Artifact factory.
     */
    public EmbeddedContainerArtifactResolver(ArtifactResolver artifactResolver,
        ArtifactRepository localRepository, List<ArtifactRepository> repositories,
        ArtifactFactory artifactFactory)
    {
        this.artifactResolver = artifactResolver;
        this.localRepository = localRepository;
        this.repositories = repositories;
        this.artifactFactory = artifactFactory;

        List<Dependency> jetty4xDependencies = new ArrayList<Dependency>();
        jetty4xDependencies.add(new Dependency("ant", "ant", "1.6.4"));
        jetty4xDependencies.add(new Dependency("jetty", "org.mortbay.jetty", "4.2.27"));
        jetty4xDependencies.add(new Dependency("javax.servlet", "servlet-api", "2.4"));
        jetty4xDependencies.add(new Dependency("javax.servlet", "jsp-api", "2.0"));
        jetty4xDependencies.add(new Dependency("tomcat", "jasper-compiler", "4.1.30"));
        jetty4xDependencies.add(new Dependency("tomcat", "jasper-runtime", "4.1.30"));
        this.containerDependencies.put("jetty4x", jetty4xDependencies);

        List<Dependency> jetty5xDependencies = new ArrayList<Dependency>();
        jetty5xDependencies.add(new Dependency("jetty", "org.mortbay.jetty", "5.1.12"));
        jetty5xDependencies.add(new Dependency("javax.servlet", "servlet-api", "2.4"));
        jetty5xDependencies.add(new Dependency("javax.servlet", "jsp-api", "2.0"));
        jetty5xDependencies.add(new Dependency("ant", "ant", "1.6.4"));
        jetty5xDependencies.add(new Dependency("xerces", "xercesImpl", "2.6.2"));
        jetty5xDependencies.add(new Dependency("xerces", "xmlParserAPIs", "2.6.2"));
        jetty5xDependencies.add(new Dependency("tomcat", "jasper-compiler", "5.5.12"));
        jetty5xDependencies.add(new Dependency("tomcat", "jasper-runtime", "5.5.12"));
        jetty5xDependencies.add(new Dependency("commons-el", "commons-el", "1.0"));
        jetty5xDependencies.add(new Dependency("commons-logging", "commons-logging", "1.0.4"));
        this.containerDependencies.put("jetty5x", jetty5xDependencies);

        List<Dependency> jetty6xDependencies = new ArrayList<Dependency>();
        jetty6xDependencies.add(new Dependency("org.mortbay.jetty", "jsp-api-2.0", "6.1.26"));
        jetty6xDependencies.add(new Dependency("org.mortbay.jetty", "servlet-api-2.5", "6.1.14"));
        jetty6xDependencies.add(new Dependency("org.mortbay.jetty", "jetty", "6.1.26"));
        jetty6xDependencies.add(new Dependency("org.mortbay.jetty", "jetty-util", "6.1.26"));
        jetty6xDependencies.add(new Dependency("org.mortbay.jetty", "jetty-naming", "6.1.26"));
        jetty6xDependencies.add(new Dependency("org.mortbay.jetty", "jetty-plus", "6.1.26"));
        jetty6xDependencies.add(new Dependency("ant", "ant", "1.6.5"));
        jetty6xDependencies.add(new Dependency("commons-el", "commons-el", "1.0"));
        jetty6xDependencies.add(new Dependency("tomcat", "jasper-compiler", "5.5.15"));
        jetty6xDependencies.add(new Dependency("tomcat", "jasper-runtime", "5.5.15"));
        jetty6xDependencies.add(new Dependency("tomcat", "jasper-compiler-jdt", "5.5.15"));
        jetty6xDependencies.add(new Dependency("javax.mail", "mail", "1.4"));
        jetty6xDependencies.add(new Dependency("javax.activation", "activation", "1.1"));
        jetty6xDependencies.add(new Dependency("geronimo-spec", "geronimo-spec-jta", "1.0.1B-rc4"));
        jetty6xDependencies.add(new Dependency("xerces", "xercesImpl", "2.6.2"));
        jetty6xDependencies.add(new Dependency("xerces", "xmlParserAPIs", "2.6.2"));
        jetty6xDependencies.add(new Dependency("commons-logging", "commons-logging", "1.0.4"));
        jetty6xDependencies.add(new Dependency("log4j", "log4j", "1.2.14"));
        this.containerDependencies.put("jetty6x", jetty6xDependencies);

        List<Dependency> jetty7xDependencies = new ArrayList<Dependency>();
        jetty7xDependencies.add(new Dependency("javax.servlet", "servlet-api", "2.5"));
        jetty7xDependencies.add(new Dependency("org.eclipse.jdt.core.compiler", "ecj", "3.5.1"));
        jetty7xDependencies.add(new Dependency("org.eclipse.jetty", "jetty-continuation",
            "7.6.16.v20140903"));
        jetty7xDependencies.add(new Dependency("org.eclipse.jetty", "jetty-http",
            "7.6.16.v20140903"));
        jetty7xDependencies.add(new Dependency("org.eclipse.jetty", "jetty-io",
            "7.6.16.v20140903"));
        jetty7xDependencies.add(new Dependency("org.eclipse.jetty", "jetty-jsp",
            "7.6.16.v20140903"));
        jetty7xDependencies.add(new Dependency("org.eclipse.jetty", "jetty-util",
            "7.6.16.v20140903"));
        jetty7xDependencies.add(new Dependency("org.eclipse.jetty", "jetty-security",
            "7.6.16.v20140903"));
        jetty7xDependencies.add(new Dependency("org.eclipse.jetty", "jetty-server",
            "7.6.16.v20140903"));
        jetty7xDependencies.add(new Dependency("org.eclipse.jetty", "jetty-servlet",
            "7.6.16.v20140903"));
        jetty7xDependencies.add(new Dependency("org.eclipse.jetty", "jetty-webapp",
            "7.6.16.v20140903"));
        jetty7xDependencies.add(new Dependency("org.eclipse.jetty", "jetty-xml",
            "7.6.16.v20140903"));
        jetty7xDependencies.add(new Dependency("org.mortbay.jetty", "jsp-api-2.1-glassfish",
            "2.1.v20100127"));
        jetty7xDependencies.add(new Dependency("org.mortbay.jetty", "jsp-2.1-glassfish",
            "2.1.v20100127"));
        this.containerDependencies.put("jetty7x", jetty7xDependencies);

        List<Dependency> jetty8xDependencies = new ArrayList<Dependency>();
        jetty8xDependencies.add(new Dependency("org.mortbay.jetty", "servlet-api", "3.0.20100224"));
        jetty8xDependencies.add(new Dependency("org.eclipse.jetty", "jetty-annotations",
            "8.1.16.v20140903"));
        jetty8xDependencies.add(new Dependency("org.eclipse.jetty", "jetty-continuation",
            "8.1.16.v20140903"));
        jetty8xDependencies.add(new Dependency("org.eclipse.jetty", "jetty-http",
            "8.1.16.v20140903"));
        jetty8xDependencies.add(new Dependency("org.eclipse.jetty", "jetty-io",
            "8.1.16.v20140903"));
        jetty8xDependencies.add(new Dependency("org.eclipse.jetty", "jetty-jndi",
            "8.1.16.v20140903"));
        jetty8xDependencies.add(new Dependency("org.eclipse.jetty", "jetty-plus",
            "8.1.16.v20140903"));
        jetty8xDependencies.add(new Dependency("org.eclipse.jetty", "jetty-util",
            "8.1.16.v20140903"));
        jetty8xDependencies.add(new Dependency("org.eclipse.jetty", "jetty-security",
            "8.1.16.v20140903"));
        jetty8xDependencies.add(new Dependency("org.eclipse.jetty", "jetty-server",
            "8.1.16.v20140903"));
        jetty8xDependencies.add(new Dependency("org.eclipse.jetty", "jetty-servlet",
            "8.1.16.v20140903"));
        jetty8xDependencies.add(new Dependency("org.eclipse.jetty", "jetty-webapp",
            "8.1.16.v20140903"));
        jetty8xDependencies.add(new Dependency("org.eclipse.jetty", "jetty-xml",
            "8.1.16.v20140903"));
        jetty8xDependencies.add(new Dependency("org.eclipse.jetty.orbit", "org.objectweb.asm",
            "3.1.0.v200803061910"));
        jetty8xDependencies.add(new Dependency("org.eclipse.jetty.orbit", "javax.mail.glassfish",
            "1.4.1.v201005082020"));
        jetty8xDependencies.add(new Dependency("org.eclipse.jetty.orbit", "javax.activation",
            "1.1.0.v201105071233"));
        jetty8xDependencies.add(new Dependency("org.eclipse.jetty.orbit", "javax.annotation",
            "1.1.0.v201108011116"));
        jetty8xDependencies.add(new Dependency("javax.el", "el-api", "2.2"));
        jetty8xDependencies.add(new Dependency("org.apache.tomcat", "tomcat-jsp-api",
            "7.0.55"));
        jetty8xDependencies.add(new Dependency("org.glassfish.web", "jsp-impl",
            "2.2.1"));
        this.containerDependencies.put("jetty8x", jetty8xDependencies);

        List<Dependency> jetty9xDependencies = new ArrayList<Dependency>();
        jetty9xDependencies.add(new Dependency("org.eclipse.jetty", "jetty-annotations",
            "9.2.7.v20150116"));
        jetty9xDependencies.add(new Dependency("org.eclipse.jetty", "jetty-continuation",
            "9.2.7.v20150116"));
        jetty9xDependencies.add(new Dependency("org.eclipse.jetty", "jetty-http",
            "9.2.7.v20150116"));
        jetty9xDependencies.add(new Dependency("org.eclipse.jetty", "jetty-io",
            "9.2.7.v20150116"));
        jetty9xDependencies.add(new Dependency("org.eclipse.jetty", "jetty-jndi",
            "9.2.7.v20150116"));
        jetty9xDependencies.add(new Dependency("org.eclipse.jetty", "jetty-plus",
            "9.2.7.v20150116"));
        jetty9xDependencies.add(new Dependency("org.eclipse.jetty", "jetty-util",
            "9.2.7.v20150116"));
        jetty9xDependencies.add(new Dependency("org.eclipse.jetty", "jetty-security",
            "9.2.7.v20150116"));
        jetty9xDependencies.add(new Dependency("org.eclipse.jetty", "jetty-server",
            "9.2.7.v20150116"));
        jetty9xDependencies.add(new Dependency("org.eclipse.jetty", "jetty-servlet",
            "9.2.7.v20150116"));
        jetty9xDependencies.add(new Dependency("org.eclipse.jetty", "jetty-webapp",
            "9.2.7.v20150116"));
        jetty9xDependencies.add(new Dependency("org.eclipse.jetty", "jetty-xml",
            "9.2.7.v20150116"));
        jetty9xDependencies.add(new Dependency("javax.servlet", "javax.servlet-api", "3.1.0"));
        // annotations
        jetty9xDependencies.add(new Dependency("org.ow2.asm", "asm", "4.1"));
        jetty9xDependencies.add(new Dependency("org.ow2.asm", "asm-commons", "4.1"));
        jetty9xDependencies.add(new Dependency("javax.annotation", "javax.annotation-api", "1.2"));
        // jndi
        jetty9xDependencies.add(new Dependency("org.eclipse.jetty.orbit", "javax.activation",
            "1.1.0.v201105071233"));
        jetty9xDependencies.add(new Dependency("org.eclipse.jetty.orbit", "javax.mail.glassfish",
            "1.4.1.v201005082020"));
        jetty9xDependencies.add(new Dependency("javax.transaction", "javax.transaction-api",
            "1.2"));
        // jsp
        jetty9xDependencies.add(new Dependency("javax.el", "javax.el-api", "3.0.0"));
        jetty9xDependencies.add(new Dependency("org.glassfish.web", "javax.servlet.jsp",
            "2.3.2"));
        jetty9xDependencies.add(new Dependency("javax.servlet.jsp", "javax.servlet.jsp-api",
            "2.3.1"));
        jetty9xDependencies.add(new Dependency("org.eclipse.jetty.orbit", "javax.servlet.jsp.jstl",
            "1.2.0.v201105211821"));
        jetty9xDependencies.add(new Dependency("org.eclipse.jetty.toolchain", "jetty-jsp-jdt",
            "2.3.3"));
        jetty9xDependencies.add(new Dependency("org.eclipse.jetty.orbit", "org.eclipse.jdt.core",
            "3.8.2.v20130121"));
        this.containerDependencies.put("jetty9x", jetty9xDependencies);

        List<Dependency> tomcat6xDependencies = new ArrayList<Dependency>();
        tomcat6xDependencies.add(new Dependency("org.apache.tomcat", "servlet-api", "6.0.43"));
        tomcat6xDependencies.add(new Dependency("org.apache.tomcat", "annotations-api", "6.0.43"));
        tomcat6xDependencies.add(new Dependency("org.apache.tomcat", "el-api", "6.0.43"));
        tomcat6xDependencies.add(new Dependency("org.apache.tomcat", "jsp-api", "6.0.43"));
        tomcat6xDependencies.add(new Dependency("org.apache.tomcat", "juli", "6.0.43"));
        tomcat6xDependencies.add(new Dependency("org.apache.tomcat", "catalina", "6.0.43"));
        tomcat6xDependencies.add(new Dependency("org.apache.tomcat", "coyote", "6.0.43"));
        tomcat6xDependencies.add(new Dependency("org.apache.tomcat", "jasper", "6.0.43"));
        tomcat6xDependencies.add(new Dependency("org.apache.tomcat", "jasper-el", "6.0.43"));
        tomcat6xDependencies.add(new Dependency("org.eclipse.jdt.core.compiler", "ecj",
            "4.3.1"));
        this.containerDependencies.put("tomcat6x", tomcat6xDependencies);

        List<Dependency> tomcat7xDependencies = new ArrayList<Dependency>();
        tomcat7xDependencies.add(new Dependency("org.apache.tomcat", "tomcat-servlet-api",
            "7.0.59"));
        tomcat7xDependencies.add(new Dependency("org.apache.tomcat", "tomcat-annotations-api",
            "7.0.59"));
        tomcat7xDependencies.add(new Dependency("org.apache.tomcat", "tomcat-el-api", "7.0.59"));
        tomcat7xDependencies.add(new Dependency("org.apache.tomcat", "tomcat-jsp-api", "7.0.59"));
        tomcat7xDependencies.add(new Dependency("org.apache.tomcat", "tomcat-juli", "7.0.59"));
        tomcat7xDependencies.add(new Dependency("org.apache.tomcat", "tomcat-api", "7.0.59"));
        tomcat7xDependencies.add(new Dependency("org.apache.tomcat", "tomcat-util", "7.0.59"));
        tomcat7xDependencies.add(new Dependency("org.apache.tomcat", "tomcat-catalina", "7.0.59"));
        tomcat7xDependencies.add(new Dependency("org.apache.tomcat", "tomcat-coyote", "7.0.59"));
        tomcat7xDependencies.add(new Dependency("org.apache.tomcat", "tomcat-jasper", "7.0.59"));
        tomcat7xDependencies.add(new Dependency("org.apache.tomcat", "tomcat-jasper-el",
            "7.0.59"));
        tomcat7xDependencies.add(new Dependency("org.eclipse.jdt.core.compiler", "ecj",
            "4.4"));
        this.containerDependencies.put("tomcat7x", tomcat7xDependencies);

        List<Dependency> tomcat8xDependencies = new ArrayList<Dependency>();
        tomcat8xDependencies.add(new Dependency("org.apache.tomcat", "tomcat-servlet-api",
            "8.0.18"));
        tomcat8xDependencies.add(new Dependency("org.apache.tomcat", "tomcat-annotations-api",
            "8.0.18"));
        tomcat8xDependencies.add(new Dependency("org.apache.tomcat", "tomcat-el-api", "8.0.18"));
        tomcat8xDependencies.add(new Dependency("org.apache.tomcat", "tomcat-jsp-api", "8.0.18"));
        tomcat8xDependencies.add(new Dependency("org.apache.tomcat", "tomcat-juli", "8.0.18"));
        tomcat8xDependencies.add(new Dependency("org.apache.tomcat", "tomcat-api", "8.0.18"));
        tomcat8xDependencies.add(new Dependency("org.apache.tomcat", "tomcat-util", "8.0.18"));
        tomcat8xDependencies.add(new Dependency("org.apache.tomcat", "tomcat-util-scan",
            "8.0.18"));
        tomcat8xDependencies.add(new Dependency("org.apache.tomcat", "tomcat-catalina", "8.0.18"));
        tomcat8xDependencies.add(new Dependency("org.apache.tomcat", "tomcat-coyote", "8.0.18"));
        tomcat8xDependencies.add(new Dependency("org.apache.tomcat", "tomcat-jasper", "8.0.18"));
        tomcat8xDependencies.add(new Dependency("org.apache.tomcat", "tomcat-jasper-el",
            "8.0.18"));
        tomcat8xDependencies.add(new Dependency("org.eclipse.jdt.core.compiler", "ecj",
            "4.4"));
        this.containerDependencies.put("tomcat8x", tomcat8xDependencies);
    }

    /**
     * Resolve dependencies.
     * @param containerId Container id.
     * @param parent Parent {@link ClassLoader}.
     * @return {@link ClassLoader} with the resolved dependencies and given <code>parent</code>.
     * @throws MojoExecutionException If dependencies cannot be resolved.
     */
    public ClassLoader resolveDependencies(String containerId, ClassLoader parent)
        throws MojoExecutionException
    {
        URLClassLoader classloader;

        List<Dependency> dependencies = this.containerDependencies.get(containerId);
        if (dependencies == null)
        {
            return null;
        }

        try
        {
            List<URL> urls = new ArrayList<URL>(dependencies.size() + 1);
            for (Dependency dependency : dependencies)
            {
                Artifact artifact = this.artifactFactory.createArtifact(dependency.groupId,
                    dependency.artifactId, dependency.version, "compile", "jar");
                this.artifactResolver.resolve(artifact, this.repositories, this.localRepository);
                urls.add(artifact.getFile().toURI().toURL());
            }

            // On OSX, the tools.jar classes are included in the classes.jar so there is no need to
            // include any tools.jar file to the cp.
            if (!this.jdkUtils.isOSX())
            {
                urls.add(this.jdkUtils.getToolsJar().toURI().toURL());
            }

            URL[] urlArray = (URL[]) urls.toArray(new URL[urls.size()]);
            if (parent == null)
            {
                classloader = new URLClassLoader(urlArray);
            }
            else
            {
                classloader = new URLClassLoader(urlArray, parent);
            }
        }
        catch (Exception e)
        {
            throw new MojoExecutionException("Failed to resolve dependency", e);
        }

        return classloader;
    }
}
