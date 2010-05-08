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
package org.codehaus.cargo.sample.java;

import org.codehaus.cargo.util.CargoException;
import org.codehaus.cargo.container.internal.util.JdkUtils;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.MalformedURLException;
import java.io.File;
import java.io.FileNotFoundException;

/**
 * Create a classloader with Jetty dependencies.
 *
 * @version $Id$
 */
public class JettyClasspathResolver
{
    private Map jettyDependencies = new HashMap();

    private JdkUtils jdkUtils = new JdkUtils();

    public JettyClasspathResolver()
    {
        List jetty4xDependencies = new ArrayList();
        jetty4xDependencies.add("lib/javax.servlet.jar");
        jetty4xDependencies.add("lib/org.mortbay.jmx.jar");
        jetty4xDependencies.add("lib/org.mortbay.jetty.jar");
        jetty4xDependencies.add("ext/jasper-compiler.jar");
        jetty4xDependencies.add("ext/jasper-runtime.jar");
        jetty4xDependencies.add("ext/ant.jar");
        jetty4xDependencies.add("ext/ant-launcher.jar");
        jetty4xDependencies.add("ext/jcert.jar");
        jetty4xDependencies.add("ext/jmxri.jar");
        jetty4xDependencies.add("ext/jmxtools.jar");
        jetty4xDependencies.add("ext/jnet.jar");
        jetty4xDependencies.add("ext/jsse.jar");
        jetty4xDependencies.add("ext/xercesImpl.jar");
        jetty4xDependencies.add("ext/xml-apis.jar");

        List jetty5xDependencies = new ArrayList();
        jetty5xDependencies.add("lib/javax.servlet.jar");
        jetty5xDependencies.add("lib/org.mortbay.jetty.jar");
        jetty5xDependencies.add("lib/org.mortbay.jmx.jar");
        jetty5xDependencies.add("ext/ant.jar");
        jetty5xDependencies.add("ext/commons-el.jar");
        jetty5xDependencies.add("ext/jasper-compiler.jar");
        jetty5xDependencies.add("ext/jasper-runtime.jar");
        jetty5xDependencies.add("ext/commons-logging.jar");
        jetty5xDependencies.add("ext/mx4j.jar");
        jetty5xDependencies.add("ext/mx4j-remote.jar");
        jetty5xDependencies.add("ext/mx4j-tools.jar");
        jetty5xDependencies.add("ext/xercesImpl.jar");
        jetty5xDependencies.add("ext/xml-apis.jar");
        jetty5xDependencies.add("ext/xmlParserAPIs.jar");

        List jetty6xDependencies = new ArrayList();
        jetty6xDependencies.add("lib/jetty.jar");
        jetty6xDependencies.add("lib/jetty-util.jar");
        jetty6xDependencies.add("lib/servlet-api-2.5.jar");
        jetty6xDependencies.add("lib/jsp-2.0/jsp-api-2.0.jar");
        jetty6xDependencies.add("lib/jsp-2.0/jasper-compiler-jdt-5.5.15.jar");
        jetty6xDependencies.add("lib/jsp-2.0/jasper-compiler-5.5.15.jar");
        jetty6xDependencies.add("lib/jsp-2.0/jasper-runtime-5.5.15.jar");
        jetty6xDependencies.add("lib/jsp-2.0/xercesImpl-2.6.2.jar");
        jetty6xDependencies.add("lib/jsp-2.0/xmlParserAPIs-2.6.2.jar");
        jetty6xDependencies.add("lib/jsp-2.0/commons-el-1.0.jar");
        jetty6xDependencies.add("lib/jsp-2.0/jcl104-over-slf4j-1.0-rc5.jar");
        jetty6xDependencies.add("lib/jsp-2.0/slf4j-simple-1.0-rc5.jar");
        jetty6xDependencies.add("lib/management/jetty-management.jar");
        jetty6xDependencies.add("lib/management/mx4j-3.0.1.jar");
        jetty6xDependencies.add("lib/management/mx4j-tools-2.1.1.jar");
        jetty6xDependencies.add("lib/naming/jetty-naming.jar");
        jetty6xDependencies.add("lib/plus/jetty-plus.jar");
        jetty6xDependencies.add("lib/xbean/jetty-xbean.jar");

        List jetty7xDependencies = new ArrayList();
        jetty7xDependencies.add("lib/jetty-ajp-7.0.1.v20091125.jar");
        jetty7xDependencies.add("lib/jetty-annotations-7.0.1.v20091125.jar");
        jetty7xDependencies.add("lib/jetty-client-7.0.1.v20091125.jar");
        jetty7xDependencies.add("lib/jetty-continuation-7.0.1.v20091125.jar");
        jetty7xDependencies.add("lib/jetty-deploy-7.0.1.v20091125.jar");
        jetty7xDependencies.add("lib/jetty-http-7.0.1.v20091125.jar");
        jetty7xDependencies.add("lib/jetty-io-7.0.1.v20091125.jar");
        jetty7xDependencies.add("lib/jetty-jmx-7.0.1.v20091125.jar");
        jetty7xDependencies.add("lib/jetty-jndi-7.0.1.v20091125.jar");
        jetty7xDependencies.add("lib/jetty-plus-7.0.1.v20091125.jar");
        jetty7xDependencies.add("lib/jetty-policy-7.0.1.v20091125.jar");
        jetty7xDependencies.add("lib/jetty-rewrite-7.0.1.v20091125.jar");
        jetty7xDependencies.add("lib/jetty-security-7.0.1.v20091125.jar");
        jetty7xDependencies.add("lib/jetty-server-7.0.1.v20091125.jar");
        jetty7xDependencies.add("lib/jetty-servlet-7.0.1.v20091125.jar");
        jetty7xDependencies.add("lib/jetty-servlets-7.0.1.v20091125.jar");
        jetty7xDependencies.add("lib/jetty-util-7.0.1.v20091125.jar");
        jetty7xDependencies.add("lib/jetty-webapp-7.0.1.v20091125.jar");
        jetty7xDependencies.add("lib/jetty-websocket-7.0.1.v20091125.jar");
        jetty7xDependencies.add("lib/jetty-xml-7.0.1.v20091125.jar");
        jetty7xDependencies.add("lib/jndi/activation-1.1.jar");
        jetty7xDependencies.add("lib/jndi/mail-1.4.jar");
        jetty7xDependencies.add("lib/servlet-api-2.5.jar");
        // JSP support is not packaged with Jetty7x by default
//        jetty7xDependencies.add("lib/jsp-2.1/ant-1.6.5.jar");
//        jetty7xDependencies.add("lib/jsp-2.1/core-3.1.1.jar");
//        jetty7xDependencies.add("lib/jsp-2.1/jsp-2.1-glassfish-9.1.1.B60.25.p2.jar");
//        jetty7xDependencies.add("lib/jsp-2.1/jsp-api-2.1-glassfish-9.1.1.B60.25.p2.jar");
//        jetty7xDependencies.add("lib/policy/jetty.policy");
        // setuid support is OS specific due to native library
//        jetty7xDependencies.add("lib/ext/jetty-setuid-java-7.0.2.5824.jar");
//        jetty7xDependencies.add("libsetuid.so");
        
        this.jettyDependencies.put("jetty4x", jetty4xDependencies);
        this.jettyDependencies.put("jetty5x", jetty5xDependencies);
        this.jettyDependencies.put("jetty6x", jetty6xDependencies);
        this.jettyDependencies.put("jetty7x", jetty7xDependencies);
    }

    public ClassLoader resolveDependencies(String jettyContainerId, File containerHome)
        throws FileNotFoundException
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
                String dependencyRelativePath = (String) it.next();
                File dependencyPath = new File(containerHome, dependencyRelativePath);
                urls[i++] = dependencyPath.toURL();
            }

            // On OSX, the tools.jar classes are included in the classes.jar so there is no need to
            // include any tools.jar file to the cp.
            if (!this.jdkUtils.isOSX())
            {
                urls[i++] = this.jdkUtils.getToolsJar().toURI().toURL();
            }

            // We pass null as the parent to ensure no jars outside of the ones we've added are
            // added to the classpath.
            classloader = new URLClassLoader(urls, null);
        }
        catch (MalformedURLException e)
        {
            throw new CargoException("Failed to resolve dependency", e);
        }

        return classloader;
    }
}
