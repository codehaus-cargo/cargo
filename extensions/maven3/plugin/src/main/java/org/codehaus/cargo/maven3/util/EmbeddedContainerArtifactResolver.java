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
package org.codehaus.cargo.maven3.util;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.shared.transfer.artifact.DefaultArtifactCoordinate;
import org.apache.maven.shared.transfer.artifact.resolve.ArtifactResolver;
import org.codehaus.cargo.container.internal.util.JdkUtils;

/**
 * Dynamically load embedded container dependencies.
 */
public class EmbeddedContainerArtifactResolver
{
    /**
     * Artifact resolver.
     */
    private ArtifactResolver artifactResolver;

    /**
     * Maven project building request.
     */
    private ProjectBuildingRequest projectBuildingRequest;

    /**
     * Map of embedded container dependencies.
     */
    private Map<String, List<Dependency>> containerDependencies =
        new HashMap<String, List<Dependency>>();

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
         * Classifier (optional).
         */
        public String classifier;

        /**
         * Save all attributes.
         * @param groupId Group id.
         * @param artifactId Artifact id.
         * @param version Version.
         */
        public Dependency(String groupId, String artifactId, String version)
        {
            this(groupId, artifactId, null, version);
        }

        /**
         * Save all attributes.
         * @param groupId Group id.
         * @param artifactId Artifact id.
         * @param classifier Classifier (optional).
         * @param version Version.
         */
        public Dependency(String groupId, String artifactId, String classifier, String version)
        {
            this.groupId = groupId;
            this.artifactId = artifactId;
            this.version = version;
            this.classifier = classifier;
        }
    }

    /**
     * Save all attributes.
     * @param artifactResolver Artifact resolver.
     * @param projectBuildingRequest Maven project building request.
     */
    public EmbeddedContainerArtifactResolver(ArtifactResolver artifactResolver,
        ProjectBuildingRequest projectBuildingRequest)
    {
        this.artifactResolver = artifactResolver;
        this.projectBuildingRequest = projectBuildingRequest;

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
        jetty7xDependencies.add(
            new Dependency("org.eclipse.jdt", "org.eclipse.jdt.core", "3.7.1"));
        jetty7xDependencies.add(
            new Dependency("org.eclipse.jetty", "jetty-continuation", "7.6.21.v20160908"));
        jetty7xDependencies.add(
            new Dependency("org.eclipse.jetty", "jetty-http", "7.6.21.v20160908"));
        jetty7xDependencies.add(
            new Dependency("org.eclipse.jetty", "jetty-io", "7.6.21.v20160908"));
        jetty7xDependencies.add(
            new Dependency("org.eclipse.jetty", "jetty-jsp", "7.6.21.v20160908"));
        jetty7xDependencies.add(
            new Dependency("org.eclipse.jetty", "jetty-util", "7.6.21.v20160908"));
        jetty7xDependencies.add(
            new Dependency("org.eclipse.jetty", "jetty-security", "7.6.21.v20160908"));
        jetty7xDependencies.add(
            new Dependency("org.eclipse.jetty", "jetty-server", "7.6.21.v20160908"));
        jetty7xDependencies.add(
            new Dependency("org.eclipse.jetty", "jetty-servlet", "7.6.21.v20160908"));
        jetty7xDependencies.add(
            new Dependency("org.eclipse.jetty", "jetty-webapp", "7.6.21.v20160908"));
        jetty7xDependencies.add(
            new Dependency("org.eclipse.jetty", "jetty-xml", "7.6.21.v20160908"));
        jetty7xDependencies.add(
            new Dependency("org.mortbay.jetty", "jsp-api-2.1-glassfish", "2.1.v20100127"));
        jetty7xDependencies.add(
            new Dependency("org.mortbay.jetty", "jsp-2.1-glassfish", "2.1.v20100127"));
        this.containerDependencies.put("jetty7x", jetty7xDependencies);

        List<Dependency> jetty8xDependencies = new ArrayList<Dependency>();
        jetty8xDependencies.add(new Dependency("org.mortbay.jetty", "servlet-api", "3.0.20100224"));
        jetty8xDependencies.add(
            new Dependency("org.eclipse.jetty", "jetty-annotations", "8.1.22.v20160922"));
        jetty8xDependencies.add(
            new Dependency("org.eclipse.jetty", "jetty-continuation", "8.1.22.v20160922"));
        jetty8xDependencies.add(
            new Dependency("org.eclipse.jetty", "jetty-http", "8.1.22.v20160922"));
        jetty8xDependencies.add(
            new Dependency("org.eclipse.jetty", "jetty-io", "8.1.22.v20160922"));
        jetty8xDependencies.add(
            new Dependency("org.eclipse.jetty", "jetty-jndi", "8.1.22.v20160922"));
        jetty8xDependencies.add(
            new Dependency("org.eclipse.jetty", "jetty-plus", "8.1.22.v20160922"));
        jetty8xDependencies.add(
            new Dependency("org.eclipse.jetty", "jetty-util", "8.1.22.v20160922"));
        jetty8xDependencies.add(
            new Dependency("org.eclipse.jetty", "jetty-security", "8.1.22.v20160922"));
        jetty8xDependencies.add(
            new Dependency("org.eclipse.jetty", "jetty-server", "8.1.22.v20160922"));
        jetty8xDependencies.add(
            new Dependency("org.eclipse.jetty", "jetty-servlet", "8.1.22.v20160922"));
        jetty8xDependencies.add(
            new Dependency("org.eclipse.jetty", "jetty-webapp", "8.1.22.v20160922"));
        jetty8xDependencies.add(
            new Dependency("org.eclipse.jetty", "jetty-xml", "8.1.22.v20160922"));
        jetty8xDependencies.add(
            new Dependency("org.eclipse.jetty.orbit", "org.objectweb.asm", "3.1.0.v200803061910"));
        jetty8xDependencies.add(new Dependency(
            "org.eclipse.jetty.orbit", "javax.mail.glassfish", "1.4.1.v201005082020"));
        jetty8xDependencies.add(
            new Dependency("org.eclipse.jetty.orbit", "javax.activation", "1.1.0.v201105071233"));
        jetty8xDependencies.add(
            new Dependency("org.eclipse.jetty.orbit", "javax.annotation", "1.1.0.v201108011116"));
        jetty8xDependencies.add(
            new Dependency("org.eclipse.jetty.orbit", "javax.el", "2.2.0.v201108011116"));
        jetty8xDependencies.add(
            new Dependency("org.eclipse.jetty.orbit", "javax.servlet.jsp", "2.2.0.v201112011158"));
        jetty8xDependencies.add(new Dependency(
            "org.eclipse.jetty.orbit", "org.apache.jasper.glassfish", "2.2.2.v201112011158"));
        this.containerDependencies.put("jetty8x", jetty8xDependencies);

        List<Dependency> jetty9xDependencies = new ArrayList<Dependency>();
        jetty9xDependencies.add(
            new Dependency("org.eclipse.jetty", "jetty-annotations", "9.4.53.v20231009"));
        jetty9xDependencies.add(
            new Dependency("org.eclipse.jetty", "jetty-continuation", "9.4.53.v20231009"));
        jetty9xDependencies.add(
            new Dependency("org.eclipse.jetty", "jetty-http", "9.4.53.v20231009"));
        jetty9xDependencies.add(
            new Dependency("org.eclipse.jetty", "jetty-io", "9.4.53.v20231009"));
        jetty9xDependencies.add(
            new Dependency("org.eclipse.jetty", "jetty-jndi", "9.4.53.v20231009"));
        jetty9xDependencies.add(
            new Dependency("org.eclipse.jetty", "jetty-plus", "9.4.53.v20231009"));
        jetty9xDependencies.add(
            new Dependency("org.eclipse.jetty", "jetty-util", "9.4.53.v20231009"));
        jetty9xDependencies.add(
            new Dependency("org.eclipse.jetty", "jetty-security", "9.4.53.v20231009"));
        jetty9xDependencies.add(
            new Dependency("org.eclipse.jetty", "jetty-server", "9.4.53.v20231009"));
        jetty9xDependencies.add(
            new Dependency("org.eclipse.jetty", "jetty-servlet", "9.4.53.v20231009"));
        jetty9xDependencies.add(
            new Dependency("org.eclipse.jetty", "jetty-webapp", "9.4.53.v20231009"));
        jetty9xDependencies.add(
            new Dependency("org.eclipse.jetty", "jetty-xml", "9.4.53.v20231009"));
        jetty9xDependencies.add(new Dependency("javax.servlet", "javax.servlet-api", "3.1.0"));
        jetty9xDependencies.add(
            new Dependency("org.eclipse.jetty.toolchain", "jetty-schemas", "3.1"));
        // annotations
        jetty9xDependencies.add(new Dependency("org.ow2.asm", "asm", "9.6"));
        jetty9xDependencies.add(new Dependency("org.ow2.asm", "asm-commons", "9.6"));
        jetty9xDependencies.add(
            new Dependency("javax.annotation", "javax.annotation-api", "1.3.2"));
        // jndi
        jetty9xDependencies.add(new Dependency(
            "org.eclipse.jetty.orbit", "javax.mail.glassfish", "1.4.1.v201005082020"));
        jetty9xDependencies.add(
            new Dependency("javax.transaction", "javax.transaction-api", "1.3"));
        // jsp
        jetty9xDependencies.add(new Dependency("org.eclipse.jdt", "ecj", "3.19.0"));
        jetty9xDependencies.add(
            new Dependency("org.eclipse.jetty", "apache-jsp", "9.4.53.v20231009"));
        jetty9xDependencies.add(new Dependency("org.mortbay.jasper", "apache-el", "8.5.70"));
        jetty9xDependencies.add(new Dependency("org.mortbay.jasper", "apache-jsp", "8.5.70"));
        this.containerDependencies.put("jetty9x", jetty9xDependencies);

        List<Dependency> jetty10xDependencies = new ArrayList<Dependency>();
        jetty10xDependencies.add(
            new Dependency("org.eclipse.jetty.toolchain", "jetty-servlet-api", "4.0.6"));
        jetty10xDependencies.add(new Dependency("org.slf4j", "slf4j-api", "2.0.9"));
        jetty10xDependencies.add(
            new Dependency("org.eclipse.jetty", "jetty-slf4j-impl", "10.0.20"));
        jetty10xDependencies.add(
            new Dependency("org.eclipse.jetty", "jetty-annotations", "10.0.20"));
        jetty10xDependencies.add(new Dependency("org.eclipse.jetty", "jetty-http", "10.0.20"));
        jetty10xDependencies.add(new Dependency("org.eclipse.jetty", "jetty-io", "10.0.20"));
        jetty10xDependencies.add(new Dependency("org.eclipse.jetty", "jetty-jndi", "10.0.20"));
        jetty10xDependencies.add(new Dependency("org.eclipse.jetty", "jetty-plus", "10.0.20"));
        jetty10xDependencies.add(new Dependency("org.eclipse.jetty", "jetty-util", "10.0.20"));
        jetty10xDependencies.add(new Dependency("org.eclipse.jetty", "jetty-security", "10.0.20"));
        jetty10xDependencies.add(new Dependency("org.eclipse.jetty", "jetty-server", "10.0.20"));
        jetty10xDependencies.add(new Dependency("org.eclipse.jetty", "jetty-servlet", "10.0.20"));
        jetty10xDependencies.add(new Dependency("org.eclipse.jetty", "jetty-webapp", "10.0.20"));
        jetty10xDependencies.add(new Dependency("org.eclipse.jetty", "jetty-xml", "10.0.20"));
        // jsp
        jetty10xDependencies.add(new Dependency("org.eclipse.jdt", "ecj", "3.33.0"));
        jetty10xDependencies.add(new Dependency("org.eclipse.jetty", "apache-jsp", "10.0.20"));
        jetty10xDependencies.add(new Dependency("org.mortbay.jasper", "apache-el", "9.0.83.1"));
        jetty10xDependencies.add(new Dependency("org.mortbay.jasper", "apache-jsp", "9.0.83.1"));
        this.containerDependencies.put("jetty10x", jetty10xDependencies);

        List<Dependency> jetty11xDependencies = new ArrayList<Dependency>();
        jetty11xDependencies.add(
            new Dependency("org.eclipse.jetty.toolchain", "jetty-jakarta-servlet-api", "5.0.2"));
        jetty11xDependencies.add(new Dependency("org.slf4j", "slf4j-api", "2.0.9"));
        jetty11xDependencies.add(
            new Dependency("org.eclipse.jetty", "jetty-slf4j-impl", "11.0.20"));
        jetty11xDependencies.add(
            new Dependency("org.eclipse.jetty", "jetty-annotations", "11.0.20"));
        jetty11xDependencies.add(new Dependency("org.eclipse.jetty", "jetty-http", "11.0.20"));
        jetty11xDependencies.add(new Dependency("org.eclipse.jetty", "jetty-io", "11.0.20"));
        jetty11xDependencies.add(new Dependency("org.eclipse.jetty", "jetty-jndi", "11.0.20"));
        jetty11xDependencies.add(new Dependency("org.eclipse.jetty", "jetty-plus", "11.0.20"));
        jetty11xDependencies.add(new Dependency("org.eclipse.jetty", "jetty-util", "11.0.20"));
        jetty11xDependencies.add(new Dependency("org.eclipse.jetty", "jetty-security", "11.0.20"));
        jetty11xDependencies.add(new Dependency("org.eclipse.jetty", "jetty-server", "11.0.20"));
        jetty11xDependencies.add(new Dependency("org.eclipse.jetty", "jetty-servlet", "11.0.20"));
        jetty11xDependencies.add(new Dependency("org.eclipse.jetty", "jetty-webapp", "11.0.20"));
        jetty11xDependencies.add(new Dependency("org.eclipse.jetty", "jetty-xml", "11.0.20"));
        // jsp
        jetty11xDependencies.add(new Dependency("org.eclipse.jdt", "ecj", "3.33.0"));
        jetty11xDependencies.add(new Dependency("org.eclipse.jetty", "apache-jsp", "11.0.20"));
        jetty11xDependencies.add(new Dependency("org.mortbay.jasper", "apache-el", "10.0.27"));
        jetty11xDependencies.add(new Dependency("org.mortbay.jasper", "apache-jsp", "10.0.27"));
        this.containerDependencies.put("jetty11x", jetty11xDependencies);

        List<Dependency> tomcat6xDependencies = new ArrayList<Dependency>();
        tomcat6xDependencies.add(new Dependency("org.apache.tomcat", "servlet-api", "6.0.44"));
        tomcat6xDependencies.add(new Dependency("org.apache.tomcat", "annotations-api", "6.0.44"));
        tomcat6xDependencies.add(new Dependency("org.apache.tomcat", "el-api", "6.0.44"));
        tomcat6xDependencies.add(new Dependency("org.apache.tomcat", "jsp-api", "6.0.44"));
        tomcat6xDependencies.add(new Dependency("org.apache.tomcat", "juli", "6.0.44"));
        tomcat6xDependencies.add(new Dependency("org.apache.tomcat", "catalina", "6.0.44"));
        tomcat6xDependencies.add(new Dependency("org.apache.tomcat", "coyote", "6.0.44"));
        tomcat6xDependencies.add(new Dependency("org.apache.tomcat", "jasper", "6.0.44"));
        tomcat6xDependencies.add(new Dependency("org.apache.tomcat", "jasper-el", "6.0.44"));
        tomcat6xDependencies.add(new Dependency("org.eclipse.jdt.core.compiler", "ecj", "4.3.1"));
        this.containerDependencies.put("tomcat6x", tomcat6xDependencies);

        List<Dependency> tomcat7xDependencies = new ArrayList<Dependency>();
        tomcat7xDependencies.add(
            new Dependency("org.apache.tomcat", "tomcat-servlet-api", "7.0.109"));
        tomcat7xDependencies.add(
            new Dependency("org.apache.tomcat", "tomcat-annotations-api", "7.0.109"));
        tomcat7xDependencies.add(new Dependency("org.apache.tomcat", "tomcat-dbcp", "7.0.109"));
        tomcat7xDependencies.add(new Dependency("org.apache.tomcat", "tomcat-el-api", "7.0.109"));
        tomcat7xDependencies.add(new Dependency("org.apache.tomcat", "tomcat-jsp-api", "7.0.109"));
        tomcat7xDependencies.add(new Dependency("org.apache.tomcat", "tomcat-juli", "7.0.109"));
        tomcat7xDependencies.add(new Dependency("org.apache.tomcat", "tomcat-api", "7.0.109"));
        tomcat7xDependencies.add(new Dependency("org.apache.tomcat", "tomcat-util", "7.0.109"));
        tomcat7xDependencies.add(new Dependency("org.apache.tomcat", "tomcat-catalina", "7.0.109"));
        tomcat7xDependencies.add(new Dependency("org.apache.tomcat", "tomcat-coyote", "7.0.109"));
        tomcat7xDependencies.add(new Dependency("org.apache.tomcat", "tomcat-jasper", "7.0.109"));
        tomcat7xDependencies.add(
            new Dependency("org.apache.tomcat", "tomcat-jasper-el", "7.0.109"));
        tomcat7xDependencies.add(new Dependency("org.eclipse.jdt.core.compiler", "ecj", "4.4.2"));
        this.containerDependencies.put("tomcat7x", tomcat7xDependencies);

        List<Dependency> tomcat8xDependencies = new ArrayList<Dependency>();
        tomcat8xDependencies.add(
            new Dependency("org.apache.tomcat", "tomcat-jaspic-api", "8.5.100"));
        tomcat8xDependencies.add(
            new Dependency("org.apache.tomcat", "tomcat-servlet-api", "8.5.100"));
        tomcat8xDependencies.add(
            new Dependency("org.apache.tomcat", "tomcat-annotations-api", "8.5.100"));
        tomcat8xDependencies.add(new Dependency("org.apache.tomcat", "tomcat-dbcp", "8.5.100"));
        tomcat8xDependencies.add(new Dependency("org.apache.tomcat", "tomcat-el-api", "8.5.100"));
        tomcat8xDependencies.add(new Dependency("org.apache.tomcat", "tomcat-jsp-api", "8.5.100"));
        tomcat8xDependencies.add(new Dependency("org.apache.tomcat", "tomcat-juli", "8.5.100"));
        tomcat8xDependencies.add(new Dependency("org.apache.tomcat", "tomcat-api", "8.5.100"));
        tomcat8xDependencies.add(new Dependency("org.apache.tomcat", "tomcat-jni", "8.5.100"));
        tomcat8xDependencies.add(new Dependency("org.apache.tomcat", "tomcat-util", "8.5.100"));
        tomcat8xDependencies.add(
            new Dependency("org.apache.tomcat", "tomcat-util-scan", "8.5.100"));
        tomcat8xDependencies.add(new Dependency("org.apache.tomcat", "tomcat-catalina", "8.5.100"));
        tomcat8xDependencies.add(new Dependency("org.apache.tomcat", "tomcat-coyote", "8.5.100"));
        tomcat8xDependencies.add(
            new Dependency("org.apache.tomcat", "tomcat-websocket-api", "8.5.100"));
        tomcat8xDependencies.add(
            new Dependency("org.apache.tomcat", "tomcat-websocket", "8.5.100"));
        tomcat8xDependencies.add(new Dependency("org.apache.tomcat", "tomcat-jasper", "8.5.100"));
        tomcat8xDependencies.add(
            new Dependency("org.apache.tomcat", "tomcat-jasper-el", "8.5.100"));
        // To get to the actual ECJ version, open the MANIFEST.MF file in Tomcat's lib/ecj-xxx.jar
        tomcat8xDependencies.add(new Dependency("org.eclipse.jdt", "ecj", "3.12.3"));
        this.containerDependencies.put("tomcat8x", tomcat8xDependencies);

        List<Dependency> tomcat9xDependencies = new ArrayList<Dependency>();
        tomcat9xDependencies.add(
            new Dependency("org.apache.tomcat", "tomcat-jaspic-api", "9.0.96"));
        tomcat9xDependencies.add(
            new Dependency("org.apache.tomcat", "tomcat-servlet-api", "9.0.96"));
        tomcat9xDependencies.add(
            new Dependency("org.apache.tomcat", "tomcat-annotations-api", "9.0.96"));
        tomcat9xDependencies.add(new Dependency("org.apache.tomcat", "tomcat-dbcp", "9.0.96"));
        tomcat9xDependencies.add(new Dependency("org.apache.tomcat", "tomcat-el-api", "9.0.96"));
        tomcat9xDependencies.add(new Dependency("org.apache.tomcat", "tomcat-jsp-api", "9.0.96"));
        tomcat9xDependencies.add(new Dependency("org.apache.tomcat", "tomcat-juli", "9.0.96"));
        tomcat9xDependencies.add(new Dependency("org.apache.tomcat", "tomcat-api", "9.0.96"));
        tomcat9xDependencies.add(new Dependency("org.apache.tomcat", "tomcat-jni", "9.0.96"));
        tomcat9xDependencies.add(new Dependency("org.apache.tomcat", "tomcat-util", "9.0.96"));
        tomcat9xDependencies.add(
            new Dependency("org.apache.tomcat", "tomcat-util-scan", "9.0.96"));
        tomcat9xDependencies.add(new Dependency("org.apache.tomcat", "tomcat-catalina", "9.0.96"));
        tomcat9xDependencies.add(new Dependency("org.apache.tomcat", "tomcat-coyote", "9.0.96"));
        tomcat9xDependencies.add(
            new Dependency("org.apache.tomcat", "tomcat-websocket-api", "9.0.96"));
        tomcat9xDependencies.add(
            new Dependency("org.apache.tomcat", "tomcat-websocket", "9.0.96"));
        tomcat9xDependencies.add(new Dependency("org.apache.tomcat", "tomcat-jasper", "9.0.96"));
        tomcat9xDependencies.add(
            new Dependency("org.apache.tomcat", "tomcat-jasper-el", "9.0.96"));
        // To get to the actual ECJ version, open the MANIFEST.MF file in Tomcat's lib/ecj-xxx.jar
        tomcat9xDependencies.add(new Dependency("org.eclipse.jdt", "ecj", "3.26.0"));
        this.containerDependencies.put("tomcat9x", tomcat9xDependencies);

        List<Dependency> tomcat10xDependencies = new ArrayList<Dependency>();
        tomcat10xDependencies.add(
            new Dependency("org.apache.tomcat", "jakartaee-migration", "shaded", "1.0.8"));
        tomcat10xDependencies.add(
            new Dependency("org.apache.tomcat", "tomcat-jaspic-api", "10.1.30"));
        tomcat10xDependencies.add(
            new Dependency("org.apache.tomcat", "tomcat-servlet-api", "10.1.30"));
        tomcat10xDependencies.add(
            new Dependency("org.apache.tomcat", "tomcat-annotations-api", "10.1.30"));
        tomcat10xDependencies.add(new Dependency("org.apache.tomcat", "tomcat-dbcp", "10.1.30"));
        tomcat10xDependencies.add(new Dependency("org.apache.tomcat", "tomcat-el-api", "10.1.30"));
        tomcat10xDependencies.add(
            new Dependency("org.apache.tomcat", "tomcat-jaspic-api", "10.1.30"));
        tomcat10xDependencies.add(new Dependency("org.apache.tomcat", "tomcat-jsp-api", "10.1.30"));
        tomcat10xDependencies.add(new Dependency("org.apache.tomcat", "tomcat-juli", "10.1.30"));
        tomcat10xDependencies.add(new Dependency("org.apache.tomcat", "tomcat-api", "10.1.30"));
        tomcat10xDependencies.add(new Dependency("org.apache.tomcat", "tomcat-jni", "10.1.30"));
        tomcat10xDependencies.add(new Dependency("org.apache.tomcat", "tomcat-util", "10.1.30"));
        tomcat10xDependencies.add(
            new Dependency("org.apache.tomcat", "tomcat-util-scan", "10.1.30"));
        tomcat10xDependencies.add(
            new Dependency("org.apache.tomcat", "tomcat-catalina", "10.1.30"));
        tomcat10xDependencies.add(new Dependency("org.apache.tomcat", "tomcat-coyote", "10.1.30"));
        tomcat10xDependencies.add(
            new Dependency("org.apache.tomcat", "tomcat-websocket-api", "10.1.30"));
        tomcat10xDependencies.add(
            new Dependency("org.apache.tomcat", "tomcat-websocket-client-api", "10.1.30"));
        tomcat10xDependencies.add(
            new Dependency("org.apache.tomcat", "tomcat-websocket", "10.1.30"));
        tomcat10xDependencies.add(new Dependency("org.apache.tomcat", "tomcat-jasper", "10.1.30"));
        tomcat10xDependencies.add(
            new Dependency("org.apache.tomcat", "tomcat-jasper-el", "10.1.30"));
        // To get to the actual ECJ version, open the MANIFEST.MF file in Tomcat's lib/ecj-xxx.jar
        tomcat10xDependencies.add(new Dependency("org.eclipse.jdt", "ecj", "3.33.0"));
        this.containerDependencies.put("tomcat10x", tomcat10xDependencies);

        List<Dependency> tomcat11xDependencies = new ArrayList<Dependency>();
        tomcat11xDependencies.add(
            new Dependency("org.apache.tomcat", "jakartaee-migration", "shaded", "1.0.8"));
        tomcat11xDependencies.add(
            new Dependency("org.apache.tomcat", "tomcat-jaspic-api", "11.0.0-M26"));
        tomcat11xDependencies.add(
            new Dependency("org.apache.tomcat", "tomcat-servlet-api", "11.0.0-M26"));
        tomcat11xDependencies.add(
            new Dependency("org.apache.tomcat", "tomcat-annotations-api", "11.0.0-M26"));
        tomcat11xDependencies.add(new Dependency("org.apache.tomcat", "tomcat-dbcp", "11.0.0-M26"));
        tomcat11xDependencies.add(
            new Dependency("org.apache.tomcat", "tomcat-el-api", "11.0.0-M26"));
        tomcat11xDependencies.add(
            new Dependency("org.apache.tomcat", "tomcat-jaspic-api", "11.0.0-M26"));
        tomcat11xDependencies.add(
            new Dependency("org.apache.tomcat", "tomcat-jsp-api", "11.0.0-M26"));
        tomcat11xDependencies.add(new Dependency("org.apache.tomcat", "tomcat-juli", "11.0.0-M26"));
        tomcat11xDependencies.add(new Dependency("org.apache.tomcat", "tomcat-api", "11.0.0-M26"));
        tomcat11xDependencies.add(new Dependency("org.apache.tomcat", "tomcat-jni", "11.0.0-M26"));
        tomcat11xDependencies.add(new Dependency("org.apache.tomcat", "tomcat-util", "11.0.0-M26"));
        tomcat11xDependencies.add(
            new Dependency("org.apache.tomcat", "tomcat-util-scan", "11.0.0-M26"));
        tomcat11xDependencies.add(
            new Dependency("org.apache.tomcat", "tomcat-catalina", "11.0.0-M26"));
        tomcat11xDependencies.add(
            new Dependency("org.apache.tomcat", "tomcat-coyote", "11.0.0-M26"));
        tomcat11xDependencies.add(
            new Dependency("org.apache.tomcat", "tomcat-websocket-api", "11.0.0-M26"));
        tomcat11xDependencies.add(
            new Dependency("org.apache.tomcat", "tomcat-websocket-client-api", "11.0.0-M26"));
        tomcat11xDependencies.add(
            new Dependency("org.apache.tomcat", "tomcat-websocket", "11.0.0-M26"));
        tomcat11xDependencies.add(
            new Dependency("org.apache.tomcat", "tomcat-jasper", "11.0.0-M26"));
        tomcat11xDependencies.add(
            new Dependency("org.apache.tomcat", "tomcat-jasper-el", "11.0.0-M26"));
        // To get to the actual ECJ version, open the MANIFEST.MF file in Tomcat's lib/ecj-xxx.jar
        tomcat11xDependencies.add(new Dependency("org.eclipse.jdt", "ecj", "3.38.0"));
        this.containerDependencies.put("tomcat11x", tomcat11xDependencies);
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
                DefaultArtifactCoordinate coordinate = new DefaultArtifactCoordinate();
                coordinate.setGroupId(dependency.groupId);
                coordinate.setArtifactId(dependency.artifactId);
                coordinate.setVersion(dependency.version);
                if (dependency.classifier != null)
                {
                    coordinate.setClassifier(dependency.classifier);
                }
                coordinate.setExtension("jar");

                Artifact artifact = artifactResolver.resolveArtifact(
                    this.projectBuildingRequest, coordinate).getArtifact();
                urls.add(artifact.getFile().toURI().toURL());
            }

            // On OSX, the tools.jar classes are included in the classes.jar so there is no need to
            // include any tools.jar file to the cp. On Java 9, there is no more tools.jar.
            if (!JdkUtils.isOSX() && JdkUtils.getMajorJavaVersion() < 9)
            {
                urls.add(JdkUtils.getToolsJar().toURI().toURL());
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
