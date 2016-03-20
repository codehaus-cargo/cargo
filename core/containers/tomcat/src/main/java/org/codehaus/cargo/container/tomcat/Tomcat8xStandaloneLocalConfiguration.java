/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2011 Vincent Massol, 2012-2015 Ali Tokmen.
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
package org.codehaus.cargo.container.tomcat;

import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.tomcat.internal.Tomcat8xConfigurationBuilder;
import org.codehaus.cargo.container.tomcat.internal.TomcatUtils;
import org.dom4j.Element;

/**
 * Catalina standalone {@link org.codehaus.cargo.container.spi.configuration.ContainerConfiguration}
 * implementation.
 */
public class Tomcat8xStandaloneLocalConfiguration extends Tomcat7xStandaloneLocalConfiguration
{
    /**
     * For further details, see
     * <a href="http://tomcat.apache.org/tomcat-8.0-doc/config/resources.html">Apache Tomcat 8
     * Configuration Reference</a>, in particular the <pre>DirResourceSet</pre> section.
     */
    protected static final String DIR_RESOURCE_SET =
        "org.apache.catalina.webresources.DirResourceSet";

    /**
     * For further details, see
     * <a href="http://tomcat.apache.org/tomcat-8.0-doc/config/resources.html">Apache Tomcat 8
     * Configuration Reference</a>, in particular the <pre>JarResourceSet</pre> section.
     */
    protected static final String JAR_RESOURCE_SET =
        "org.apache.catalina.webresources.JarResourceSet";

    /**
     * {@inheritDoc}
     * 
     * @see Tomcat7xStandaloneLocalConfiguration#Tomcat7xStandaloneLocalConfiguration(String)
     */
    public Tomcat8xStandaloneLocalConfiguration(String dir)
    {
        super(dir);

        configurationBuilder = new Tomcat8xConfigurationBuilder();
    }

    /**
     * {@inheritDoc}. For further details, see
     * <a href="http://tomcat.apache.org/tomcat-8.0-doc/config/resources.html">Apache Tomcat 8
     * Configuration Reference</a>, in particular the <pre>PostResources</pre> section.
     */
    @Override
    protected String getExtraClasspathToken(WAR deployable)
    {
        String[] extraClasspath = TomcatUtils.getExtraClasspath(deployable);
        StringBuilder sb = new StringBuilder();
        sb.append("<Resources>");
        for (String path : extraClasspath)
        {
            sb.append("<PostResources ");
            writePostResource(path, sb);
            sb.append("\" webAppMount=\"/WEB-INF/classes");
            sb.append("\" />");
        }
        sb.append("</Resources>");
        return sb.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void configureExtraClasspathToken(WAR deployable, Element context)
    {
        String[] extraClasspath = TomcatUtils.getExtraClasspath(deployable);
        if (extraClasspath != null)
        {
            Element resources = context.element("Resources");
            if (resources == null)
            {
                resources = context.addElement("Resources");
            }

            for (String path : extraClasspath)
            {
                Element postResource = resources.addElement("PostResources");
                writePostResource(path, postResource);
                postResource.addAttribute("webAppMount", "/WEB-INF/classes");
            }
        }
    }

    /**
     * Write post Resources using with a StringBuilder
     * 
     * @param path will be in the post resource
     * @param sb the StringBuilder we fill
     */
    private void writePostResource(String path, StringBuilder sb)
    {
        if (getFileHandler().isDirectory(path))
        {
            writeDirectoryPostResource(sb, path);
        }
        else if (path.toLowerCase().endsWith(".jar"))
        {
            writeJarPostResource(sb, path);
        }
        else
        {
            writeFilePostResource(sb, path);
        }
    }

    /**
     * Write post Resources using with a PostResources xml element
     * 
     * @param path will be in the post resource
     * @param postResourceEl the xml element we fill
     */
    private void writePostResource(String path, Element postResourceEl)
    {
        if (getFileHandler().isDirectory(path))
        {
            writeDirectoryPostResource(postResourceEl, path);
        }
        else if (path.toLowerCase().endsWith(".jar"))
        {
            writeJarPostResource(postResourceEl, path);
        }
        else
        {
            writeFilePostResource(postResourceEl, path);
        }
    }

    /**
     * Write directory post resource
     * 
     * @param path will be in the post resource
     * @param sb the StringBuilder we fill
     */
    private void writeDirectoryPostResource(StringBuilder sb, String path)
    {
        sb.append("className=\"" + DIR_RESOURCE_SET + "\" base=\"");
        sb.append(path.replace("&", "&amp;"));
    }

    /**
     * Write directory post resource
     * 
     * @param path will be in the post resource
     * @param postResourceEl the xml element we fill
     */
    private void writeDirectoryPostResource(Element postResourceEl, String path)
    {
        postResourceEl.addAttribute("className", DIR_RESOURCE_SET);
        postResourceEl.addAttribute("base", path.replace("&", "&amp;"));
    }

    /**
     * Write jar post resource
     * 
     * @param path will be in the post resource
     * @param sb the StringBuilder we fill
     */
    private void writeJarPostResource(StringBuilder sb, String path)
    {
        sb.append("className=\"" + JAR_RESOURCE_SET + "\" base=\"");
        sb.append(path.replace("&", "&amp;"));
    }

    /**
     * Write jar post resource
     * 
     * @param path will be in the post resource
     * @param postResourceEl the xml element we fill
     */
    private void writeJarPostResource(Element postResourceEl, String path)
    {
        postResourceEl.addAttribute("className", JAR_RESOURCE_SET);
        postResourceEl.addAttribute("base", path.replace("&", "&amp;"));
    }

    /**
     * Write file post resource
     * 
     * @param path will be in the post resource
     * @param sb the StringBuilder we fill
     */
    private void writeFilePostResource(StringBuilder sb, String path)
    {
        sb.append("className=\"" + DIR_RESOURCE_SET + "\" base=\"");
        sb.append(getFileHandler().getParent(path).replace("&", "&amp;"));
        sb.append("\" internalPath=\"");
        sb.append(getFileHandler().getName(path).replace("&", "&amp;"));
    }

    /**
     * Write file post resource
     * 
     * @param path will be in the post resource
     * @param postResourceEl the xml element we fill
     */
    private void writeFilePostResource(Element postResourceEl, String path)
    {
        postResourceEl.addAttribute("className", JAR_RESOURCE_SET);
        postResourceEl.addAttribute("base",
            getFileHandler().getParent(path).replace("&", "&amp;"));
        postResourceEl.addAttribute("internalPath",
            getFileHandler().getName(path).replace("&", "&amp;"));
    }

    /**
     * {@inheritDoc}
     * 
     * @see Object#toString()
     */
    @Override
    public String toString()
    {
        return "Tomcat 8.x Standalone Configuration";
    }
}
