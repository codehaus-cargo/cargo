/* 
 * ========================================================================
 * 
 * Copyright 2005-2006 Vincent Massol.
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

import java.util.Set;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

/**
 * Holder class to transport all required information to the configuration classes.
 *
 * @version $Id$
 */
public class CargoProject
{
    private Log log;
    
    private String packaging;
    private String groupId;
    private String artifactId;
    private String buildDirectory;
    private String finalName;
    private Set artifacts;
    private ClassLoader embeddedClassLoader;

    public CargoProject(String packaging, String groupId, String artifactId, String buildDirectory,
        String finalName, Set artifacts, Log log)
    {
        this.log = log;
        this.packaging = packaging;
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.buildDirectory = buildDirectory;
        this.finalName = finalName;
        this.artifacts = artifacts;
    }
    
    public CargoProject(MavenProject project, Log log)
    {
        this(
            project.getPackaging(), 
            project.getGroupId(), 
            project.getArtifactId(),
            project.getBuild().getDirectory(),
            project.getBuild().getFinalName(),
            project.getArtifacts(),
            log);
    }

    public String getPackaging()
    {
        return this.packaging;
    }

    public String getGroupId()
    {
        return this.groupId;
    }
    
    public String getArtifactId()
    {
        return this.artifactId;
    }

    public String getBuildDirectory()
    {
        return this.buildDirectory;
    }
    
    public String getFinalName()
    {
        return this.finalName;
    }

    public Set getArtifacts()
    {
        return this.artifacts;
    }
    
    public Log getLog()
    {
        return this.log;
    }

    public boolean isJ2EEPackaging()
    {
        return (getPackaging().equalsIgnoreCase("war") 
            || getPackaging().equalsIgnoreCase("ear")
            || getPackaging().equalsIgnoreCase("ejb")
            || getPackaging().equalsIgnoreCase("uberwar")
            || getPackaging().equalsIgnoreCase("rar"));
    }

    public void setEmbeddedClassLoader(ClassLoader classLoader)
    {
        this.embeddedClassLoader = classLoader;
    }

    public ClassLoader getEmbeddedClassLoader()
    {
        return this.embeddedClassLoader;
    }
}
